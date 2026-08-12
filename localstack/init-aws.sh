#!/bin/sh
set -e

echo "=========================================="
echo "Initializing LocalStack AWS Resources..."
echo "=========================================="

export AWS_DEFAULT_REGION=us-east-1

# 1. Create S3 Buckets
echo "Creating S3 bucket: tempo-traces"
awslocal s3 mb s3://tempo-traces

# 2. Create SNS Topics
echo "Creating SNS Topics..."
USER_EVENTS_TOPIC_ARN=$(awslocal sns create-topic --name user-events --query "TopicArn" --output text)
MATCH_EVENTS_TOPIC_ARN=$(awslocal sns create-topic --name match-events.fifo --attributes FifoTopic=true,ContentBasedDeduplication=true --query "TopicArn" --output text)
SOCIAL_EVENTS_TOPIC_ARN=$(awslocal sns create-topic --name social-events --query "TopicArn" --output text)
CHAT_EVENTS_TOPIC_ARN=$(awslocal sns create-topic --name chat-events --query "TopicArn" --output text)

echo "SNS Topics Created:"
echo " - user-events: ${USER_EVENTS_TOPIC_ARN}"
echo " - match-events.fifo: ${MATCH_EVENTS_TOPIC_ARN}"
echo " - social-events: ${SOCIAL_EVENTS_TOPIC_ARN}"
echo " - chat-events: ${CHAT_EVENTS_TOPIC_ARN}"

# Helper function to create queue and return ARN
create_queue() {
  QUEUE_NAME=$1
  ATTRIBUTES=$2
  if [ -n "$ATTRIBUTES" ]; then
    awslocal sqs create-queue --queue-name "$QUEUE_NAME" --attributes "$ATTRIBUTES" --query "QueueUrl" --output text > /dev/null
  else
    awslocal sqs create-queue --queue-name "$QUEUE_NAME" --query "QueueUrl" --output text > /dev/null
  fi
  awslocal sqs get-queue-attributes --queue-url "http://localhost:4566/000000000000/$QUEUE_NAME" --attribute-names QueueArn --query "Attributes.QueueArn" --output text
}

# 3. Create Dead Letter Queues (DLQs)
echo "Creating Dead Letter Queues (DLQs)..."
SOCIAL_USER_DLQ_ARN=$(create_queue "social-user-events-dlq" "")
CHAT_USER_DLQ_ARN=$(create_queue "chat-user-events-dlq" "")
ANALYTICS_USER_DLQ_ARN=$(create_queue "analytics-user-events-dlq" "")
ANALYTICS_SOCIAL_DLQ_ARN=$(create_queue "analytics-social-events-dlq" "")

SOCIAL_MATCH_DLQ_ARN=$(create_queue "social-match-events-dlq.fifo" "FifoQueue=true")
ANALYTICS_MATCH_DLQ_ARN=$(create_queue "analytics-match-events-dlq.fifo" "FifoQueue=true")
AUTH_MATCH_DLQ_ARN=$(create_queue "auth-match-events-dlq.fifo" "FifoQueue=true")

# 4. Create Main SQS Queues configured with RedrivePolicy pointing to DLQs
echo "Creating Main SQS Queues..."
SOCIAL_USER_QUEUE_ARN=$(create_queue "social-user-events-queue" "RedrivePolicy={\"deadLetterTargetArn\":\"${SOCIAL_USER_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")
CHAT_USER_QUEUE_ARN=$(create_queue "chat-user-events-queue" "RedrivePolicy={\"deadLetterTargetArn\":\"${CHAT_USER_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")
ANALYTICS_USER_QUEUE_ARN=$(create_queue "analytics-user-events-queue" "RedrivePolicy={\"deadLetterTargetArn\":\"${ANALYTICS_USER_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")
ANALYTICS_SOCIAL_QUEUE_ARN=$(create_queue "analytics-social-events-queue" "RedrivePolicy={\"deadLetterTargetArn\":\"${ANALYTICS_SOCIAL_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")

SOCIAL_MATCH_QUEUE_ARN=$(create_queue "social-match-events-queue.fifo" "FifoQueue=true,ContentBasedDeduplication=true,RedrivePolicy={\"deadLetterTargetArn\":\"${SOCIAL_MATCH_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")
ANALYTICS_MATCH_QUEUE_ARN=$(create_queue "analytics-match-events-queue.fifo" "FifoQueue=true,ContentBasedDeduplication=true,RedrivePolicy={\"deadLetterTargetArn\":\"${ANALYTICS_MATCH_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")
AUTH_MATCH_QUEUE_ARN=$(create_queue "auth-match-events-queue.fifo" "FifoQueue=true,ContentBasedDeduplication=true,RedrivePolicy={\"deadLetterTargetArn\":\"${AUTH_MATCH_DLQ_ARN}\",\"maxReceiveCount\":\"5\"}")

# 5. Subscribe Queues to Topics
echo "Subscribing SQS Queues to SNS Topics..."

# user-events -> social-user-events-queue, chat-user-events-queue, analytics-user-events-queue
awslocal sns subscribe --topic-arn "$USER_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$SOCIAL_USER_QUEUE_ARN"
awslocal sns subscribe --topic-arn "$USER_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$CHAT_USER_QUEUE_ARN"
awslocal sns subscribe --topic-arn "$USER_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$ANALYTICS_USER_QUEUE_ARN"

# match-events.fifo -> social-match-events-queue.fifo, analytics-match-events-queue.fifo, auth-match-events-queue.fifo
awslocal sns subscribe --topic-arn "$MATCH_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$SOCIAL_MATCH_QUEUE_ARN"
awslocal sns subscribe --topic-arn "$MATCH_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$ANALYTICS_MATCH_QUEUE_ARN"
awslocal sns subscribe --topic-arn "$MATCH_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$AUTH_MATCH_QUEUE_ARN"

# social-events -> analytics-social-events-queue
awslocal sns subscribe --topic-arn "$SOCIAL_EVENTS_TOPIC_ARN" --protocol sqs --notification-endpoint "$ANALYTICS_SOCIAL_QUEUE_ARN"

# 6. Initialize RDS PostgreSQL Instances
echo "Creating RDS PostgreSQL instances: auth-db-instance, social-db-instance, and chat-db-instance..."
awslocal rds create-db-instance \
  --db-instance-identifier auth-db-instance \
  --db-name auth_db \
  --engine postgres \
  --master-username auth_user \
  --master-user-password auth_pass \
  --allocated-storage 20 || true

awslocal rds create-db-instance \
  --db-instance-identifier social-db-instance \
  --db-name social_db \
  --engine postgres \
  --master-username social_user \
  --master-user-password social_pass \
  --allocated-storage 20 || true

awslocal rds create-db-instance \
  --db-instance-identifier chat-db-instance \
  --db-name chat_db \
  --engine postgres \
  --master-username chat_user \
  --master-user-password chat_pass \
  --allocated-storage 20 || true

# 7. Create DynamoDB Tables
echo "Creating DynamoDB table: game-history..."
awslocal dynamodb create-table \
  --table-name game-history \
  --attribute-definitions AttributeName=gameId,AttributeType=S AttributeName=moveNumber,AttributeType=N \
  --key-schema AttributeName=gameId,KeyType=HASH AttributeName=moveNumber,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST || true

echo "=========================================="
echo "LocalStack AWS Initialization Complete!"
echo "=========================================="
