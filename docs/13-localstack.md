# LocalStack Profile

LocalStack is included in Docker Compose for future AWS-style local development.

The current CRUD services do not require AWS services, but the `localstack` profile provides:

- endpoint: `http://localhost:4566`
- region: `us-east-1`

This gives the project a ready place to add S3, SQS, SNS, or other cloud integration examples later without changing the profile strategy.
