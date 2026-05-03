# LocalStack Profile

## Why LocalStack

LocalStack provides local AWS-compatible services. It is useful when you want to learn or test cloud integrations without creating real AWS resources.

The current CRUD flow does not require AWS. LocalStack is included so the project already has a place to add future examples such as S3 uploads, SQS messaging, SNS notifications, or DynamoDB.

## How It Is Implemented

`docker-compose.yml` includes a `localstack` container on port `4566`.

The `localstack` profile adds:

- endpoint: `http://localhost:4566`
- region: `us-east-1`

These values exist in service profile files so future AWS clients can be pointed to LocalStack instead of real AWS.

## How To Try It Later

Start LocalStack:

```bash
docker compose up -d localstack
```

Then run a service with:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=localstack
```

## Production Notes

LocalStack is for local development and tests. Production should use real cloud services with IAM permissions, managed secrets, monitoring, retries, and clear ownership of cloud resources.
