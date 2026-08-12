package com.chess.game.infrastructure.persistence.dynamodb.adapter;

import com.chess.game.domain.model.*;
import com.chess.game.domain.repository.GameHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DynamoDbGameHistoryRepositoryAdapter implements GameHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbGameHistoryRepositoryAdapter.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbGameHistoryRepositoryAdapter(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.game-history-table-name:game-history}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public void saveMoveRecord(GameHistoryRecord record) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("gameId", AttributeValue.builder().s(record.getGameId().getValue().toString()).build());
            item.put("moveNumber", AttributeValue.builder().n(String.valueOf(record.getMoveNumber())).build());
            item.put("playerId", AttributeValue.builder().s(record.getPlayerId().getValue().toString()).build());
            item.put("from", AttributeValue.builder().s(record.getFrom().toAlgebraic()).build());
            item.put("to", AttributeValue.builder().s(record.getTo().toAlgebraic()).build());
            item.put("pieceType", AttributeValue.builder().s(record.getPieceType().name()).build());
            item.put("moveType", AttributeValue.builder().s(record.getMoveType().name()).build());
            item.put("algebraic", AttributeValue.builder().s(record.getAlgebraic()).build());
            item.put("boardState", AttributeValue.builder().s(record.getBoardState()).build());
            item.put("timestamp", AttributeValue.builder().s(record.getTimestamp().toString()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            log.info("Saved move history record #{} for game {} to DynamoDB table {}",
                    record.getMoveNumber(), record.getGameId(), tableName);
        } catch (Exception e) {
            log.error("Failed to save move record to DynamoDB: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<GameHistoryRecord> findByGameId(GameId gameId) {
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":gId", AttributeValue.builder().s(gameId.getValue().toString()).build());

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .keyConditionExpression("gameId = :gId")
                    .expressionAttributeValues(expressionAttributeValues)
                    .scanIndexForward(true) // ascending by moveNumber sort key
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);

            return response.items().stream()
                    .map(this::toRecord)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to query game history from DynamoDB for gameId {}: {}", gameId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private GameHistoryRecord toRecord(Map<String, AttributeValue> item) {
        GameId gId = GameId.fromString(item.get("gameId").s());
        int moveNum = Integer.parseInt(item.get("moveNumber").n());
        PlayerId pId = PlayerId.fromString(item.get("playerId").s());
        Position from = Position.fromAlgebraic(item.get("from").s());
        Position to = Position.fromAlgebraic(item.get("to").s());
        PieceType pieceType = PieceType.valueOf(item.get("pieceType").s());
        MoveType moveType = MoveType.valueOf(item.get("moveType").s());
        String algebraic = item.containsKey("algebraic") ? item.get("algebraic").s() : "";
        String boardState = item.containsKey("boardState") ? item.get("boardState").s() : "";
        Instant timestamp = item.containsKey("timestamp") ? Instant.parse(item.get("timestamp").s()) : Instant.now();

        return GameHistoryRecord.reconstitute(gId, moveNum, pId, from, to, pieceType, moveType, algebraic, boardState, timestamp);
    }
}
