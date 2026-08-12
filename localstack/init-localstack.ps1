# ==============================================================================
# LocalStack Direct Initialization Script (PowerShell)
# Provisions AWS SNS Topics, SQS Queues, S3 Buckets, and RDS PostgreSQL Instances
# directly in LocalStack using awslocal / aws CLI or via Docker container execution.
# ==============================================================================

param (
    [string]$EndpointUrl = "http://localhost:4566",
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Initializing LocalStack AWS Resources..." -ForegroundColor Cyan
Write-Host "Endpoint: $EndpointUrl | Region: $Region" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Determine CLI binary (awslocal vs aws)
$cli = Get-Command awslocal -ErrorAction SilentlyContinue
if ($cli) {
    $cmd = "awslocal"
    $baseArgs = @()
} else {
    $awsCli = Get-Command aws -ErrorAction SilentlyContinue
    if ($awsCli) {
        $cmd = "aws"
        $baseArgs = @("--endpoint-url", $EndpointUrl, "--region", $Region)
    } else {
        # Fallback to docker exec if LocalStack container is running
        $docker = Get-Command docker -ErrorAction SilentlyContinue
        if ($docker) {
            Write-Host "Using LocalStack container execution (docker exec)..." -ForegroundColor Yellow
            $cmd = "docker"
            $baseArgs = @("exec", "-e", "AWS_DEFAULT_REGION=$Region", "chess-localstack", "awslocal")
        } else {
            Write-Error "Neither awslocal, aws CLI, nor docker was found in PATH."
            exit 1
        }
    }
}

function Invoke-AwsCli {
    param (
        [string[]]$CommandArgs
    )
    $allArgs = $baseArgs + $CommandArgs
    & $cmd $allArgs
}

# 1. Create S3 Buckets
Write-Host "`n[1/5] Creating S3 Buckets..." -ForegroundColor Green
Invoke-AwsCli -CommandArgs @("s3", "mb", "s3://tempo-traces") | Out-Null
Write-Host "  ✔ Created S3 bucket: tempo-traces" -ForegroundColor Gray

# 2. Create SNS Topics
Write-Host "`n[2/5] Creating SNS Topics..." -ForegroundColor Green
Invoke-AwsCli -CommandArgs @("sns", "create-topic", "--name", "user-events") | Out-Null
Invoke-AwsCli -CommandArgs @("sns", "create-topic", "--name", "match-events.fifo", "--attributes", "FifoTopic=true,ContentBasedDeduplication=true") | Out-Null
Invoke-AwsCli -CommandArgs @("sns", "create-topic", "--name", "social-events") | Out-Null
Write-Host "  ✔ Created SNS Topic: user-events" -ForegroundColor Gray
Write-Host "  ✔ Created SNS Topic: match-events.fifo" -ForegroundColor Gray
Write-Host "  ✔ Created SNS Topic: social-events" -ForegroundColor Gray

# 3. Create SQS Queues & DLQs
Write-Host "`n[3/5] Creating SQS Queues & Dead Letter Queues (DLQs)..." -ForegroundColor Green

# DLQs
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "social-user-events-dlq") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-user-events-dlq") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-social-events-dlq") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "social-match-events-dlq.fifo", "--attributes", "FifoQueue=true") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-match-events-dlq.fifo", "--attributes", "FifoQueue=true") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "auth-match-events-dlq.fifo", "--attributes", "FifoQueue=true") | Out-Null

# Main Queues
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "social-user-events-queue") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-user-events-queue") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-social-events-queue") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "social-match-events-queue.fifo", "--attributes", "FifoQueue=true,ContentBasedDeduplication=true") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "analytics-match-events-queue.fifo", "--attributes", "FifoQueue=true,ContentBasedDeduplication=true") | Out-Null
Invoke-AwsCli -CommandArgs @("sqs", "create-queue", "--queue-name", "auth-match-events-queue.fifo", "--attributes", "FifoQueue=true,ContentBasedDeduplication=true") | Out-Null

Write-Host "  ✔ SQS Queues and DLQs created successfully." -ForegroundColor Gray

# 4. Provision RDS PostgreSQL Database Instances
Write-Host "`n[4/5] Provisioning RDS PostgreSQL Instances in LocalStack..." -ForegroundColor Green

Write-Host "  Creating RDS instance: social-db-instance (Database: social_db)..." -ForegroundColor Yellow
Invoke-AwsCli -CommandArgs @(
    "rds", "create-db-instance",
    "--db-instance-identifier", "social-db-instance",
    "--db-name", "social_db",
    "--engine", "postgres",
    "--master-username", "social_user",
    "--master-user-password", "social_pass",
    "--allocated-storage", "20"
) | Out-Null
Write-Host "  ✔ RDS PostgreSQL Instance 'social-db-instance' created." -ForegroundColor Gray

Write-Host "  Creating RDS instance: auth-db-instance (Database: auth_db)..." -ForegroundColor Yellow
Invoke-AwsCli -CommandArgs @(
    "rds", "create-db-instance",
    "--db-instance-identifier", "auth-db-instance",
    "--db-name", "auth_db",
    "--engine", "postgres",
    "--master-username", "auth_user",
    "--master-user-password", "auth_pass",
    "--allocated-storage", "20"
) | Out-Null
Write-Host "  ✔ RDS PostgreSQL Instance 'auth-db-instance' created." -ForegroundColor Gray

# 5. Summary
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "LocalStack Direct Provisioning Complete!" -ForegroundColor Cyan
Write-Host "Social DB: jdbc:postgresql://localhost:5432/social_db (social_user / social_pass)" -ForegroundColor Green
Write-Host "Auth DB:   jdbc:postgresql://localhost:5432/auth_db (auth_user / auth_pass)" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
