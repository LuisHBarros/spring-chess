package com.chess.game.infrastructure.persistence.dynamodb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Component
public class DynamoDbTableInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbTableInitializer.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbTableInitializer(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.game-history-table-name:game-history}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void run(String... args) {
        try {
            ListTablesResponse listResponse = dynamoDbClient.listTables();
            if (listResponse.tableNames().contains(tableName)) {
                log.info("DynamoDB table '{}' already exists.", tableName);
                return;
            }

            log.info("Creating DynamoDB table '{}'...", tableName);
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("gameId").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("moveNumber").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("gameId").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("moveNumber").attributeType(ScalarAttributeType.N).build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(createTableRequest);
            log.info("DynamoDB table '{}' successfully created.", tableName);
        } catch (Exception e) {
            log.warn("Unable to auto-create DynamoDB table '{}': {}", tableName, e.getMessage());
        }
    }
}
