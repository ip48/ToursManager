# AWS Deployment Guide - Secret Management

## Overview

This guide shows how to securely manage secrets (JWT_SECRET, DB passwords) in AWS for production deployment.

---

## Local Development vs Production

### Local Development (Current Setup)

**How secrets are stored:**
```bash
# .env file (gitignored)
JWT_SECRET=dev_change_this_to_random_secret_minimum_32_characters
DB_PASSWORD=postgres_dev_password
```

**How Spring Boot reads them:**
```properties
# application-dev.properties has fallback
jwt.secret=${JWT_SECRET:dev_insecure_secret_key...}
```

**Flow:**
1. Copy `.env.example` to `.env`
2. Fill in your dev secrets
3. Docker Compose reads `.env` automatically
4. Spring Boot gets env vars from container

---

### Production (AWS)

**How secrets are stored:**
- AWS Secrets Manager (encrypted, rotatable, audited)
- Never in source code
- Never in container images
- No fallback values

**How Spring Boot reads them:**
```properties
# application.properties requires env var (no fallback)
jwt.secret=${JWT_SECRET}

# Fails fast if not set - forces production to provide it
```

**Flow:**
1. Store secret in AWS Secrets Manager
2. IAM role grants access to secret
3. ECS/Beanstalk injects secret as environment variable
4. Spring Boot reads from env var

---

## AWS Secrets Manager Setup

### 1. Create Secrets in AWS Secrets Manager

```bash
# JWT Secret
aws secretsmanager create-secret \
    --name toursmanager/prod/JWT_SECRET \
    --description "JWT signing secret for ToursManager production" \
    --secret-string "$(openssl rand -base64 48)"

# Database Password (if using RDS)
aws secretsmanager create-secret \
    --name toursmanager/prod/DB_PASSWORD \
    --description "PostgreSQL password for ToursManager production" \
    --secret-string "your_strong_database_password"
```

**Note:** Use `openssl rand -base64 48` to generate a strong random secret (64 characters).

### 2. Retrieve Secrets (for verification)

```bash
# Get JWT secret
aws secretsmanager get-secret-value \
    --secret-id toursmanager/prod/JWT_SECRET \
    --query SecretString \
    --output text

# Get DB password
aws secretsmanager get-secret-value \
    --secret-id toursmanager/prod/DB_PASSWORD \
    --query SecretString \
    --output text
```

---

## Deployment Options

### Option A: AWS ECS Fargate (Recommended for Containers)

#### 1. Create IAM Task Role

**Policy (JSON):**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-1:YOUR_ACCOUNT_ID:secret:toursmanager/prod/*"
      ]
    }
  ]
}
```

**Create policy:**
```bash
aws iam create-policy \
    --policy-name ToursManagerSecretsAccess \
    --policy-document file://secrets-policy.json
```

**Attach to task role:**
```bash
aws iam attach-role-policy \
    --role-name ecsTaskRole \
    --policy-arn arn:aws:iam::YOUR_ACCOUNT_ID:policy/ToursManagerSecretsAccess
```

#### 2. ECS Task Definition

```json
{
  "family": "toursmanager",
  "taskRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/ecsTaskRole",
  "executionRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/ecsTaskExecutionRole",
  "networkMode": "awsvpc",
  "containerDefinitions": [
    {
      "name": "toursmanager-backend",
      "image": "YOUR_ECR_REPO:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://your-rds-endpoint:5432/toursmanager"
        },
        {
          "name": "DB_USERNAME",
          "value": "postgres"
        }
      ],
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:YOUR_ACCOUNT_ID:secret:toursmanager/prod/JWT_SECRET"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:YOUR_ACCOUNT_ID:secret:toursmanager/prod/DB_PASSWORD"
        }
      ]
    }
  ],
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512"
}
```

**Key points:**
- `environment` - Plain text env vars (non-sensitive)
- `secrets` - References to Secrets Manager (sensitive)
- ECS automatically injects secrets as environment variables
- Spring Boot reads from `${JWT_SECRET}` just like local dev

---

### Option B: AWS Elastic Beanstalk

#### 1. Set Environment Variables in Beanstalk

**Via Console:**
1. Go to Elastic Beanstalk console
2. Select your environment
3. Configuration → Software → Environment properties
4. Add:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `JWT_SECRET` = (retrieve from Secrets Manager and paste)
   - `DB_PASSWORD` = (retrieve from Secrets Manager and paste)

**Via CLI:**
```bash
# Retrieve secret
JWT_SECRET=$(aws secretsmanager get-secret-value \
    --secret-id toursmanager/prod/JWT_SECRET \
    --query SecretString --output text)

# Set in Beanstalk
eb setenv JWT_SECRET="$JWT_SECRET" SPRING_PROFILES_ACTIVE=prod
```

**Better approach (use .ebextensions to read from Secrets Manager):**

Create `.ebextensions/secrets.config`:
```yaml
commands:
  01_get_secrets:
    command: |
      export JWT_SECRET=$(aws secretsmanager get-secret-value --secret-id toursmanager/prod/JWT_SECRET --query SecretString --output text)
      export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id toursmanager/prod/DB_PASSWORD --query SecretString --output text)
```

---

### Option C: AWS Lambda (if you containerize for Lambda)

**Environment variables with KMS encryption:**
```bash
aws lambda update-function-configuration \
    --function-name toursmanager-api \
    --environment Variables={
        JWT_SECRET=$(aws secretsmanager get-secret-value --secret-id toursmanager/prod/JWT_SECRET --query SecretString --output text),
        SPRING_PROFILES_ACTIVE=prod
    } \
    --kms-key-id arn:aws:kms:us-east-1:YOUR_ACCOUNT_ID:key/YOUR_KEY_ID
```

---

## Environment Variables Summary

### Required for All Environments

| Variable | Dev Value | Prod Value | Where Stored |
|----------|-----------|------------|--------------|
| `JWT_SECRET` | `.env` file (local) | AWS Secrets Manager | Minimum 32 chars |
| `DB_PASSWORD` | `.env` file (local) | AWS Secrets Manager | Strong password |
| `SPRING_DATASOURCE_URL` | `.env` or hardcoded | Environment var | RDS endpoint |
| `DB_USERNAME` | `.env` or `postgres` | Environment var | Usually `postgres` |
| `SPRING_PROFILES_ACTIVE` | `dev` (default) | `prod` | Environment var |

### How Spring Boot Reads Them

```properties
# application.properties (no fallback - fails if not set)
jwt.secret=${JWT_SECRET}

# application-dev.properties (has fallback for convenience)
jwt.secret=${JWT_SECRET:dev_insecure_secret_key_minimum_32_characters_long_12345}
```

---

## Security Best Practices

### ✅ DO:
- Use AWS Secrets Manager for all production secrets
- Use IAM roles (not IAM users with access keys)
- Use least privilege (only grant access to specific secrets)
- Rotate secrets regularly (Secrets Manager supports automatic rotation)
- Use strong random secrets: `openssl rand -base64 48`
- Use HTTPS/TLS for all connections
- Monitor secret access with CloudTrail
- Use different secrets for dev/staging/prod

### ❌ DON'T:
- Commit secrets to git (even in private repos)
- Use the same secret across environments
- Store secrets in container images
- Log secrets in application logs
- Use default/fallback values in production
- Share secrets via email/Slack
- Hardcode secrets in source code

---

## Testing Secret Injection

### Local (Docker Compose)
```bash
# Check if env vars are set
docker compose exec app env | grep JWT_SECRET

# Check Spring Boot property
docker compose exec app sh -c 'echo ${JWT_SECRET}'
```

### ECS
```bash
# Get task ARN
TASK_ARN=$(aws ecs list-tasks --cluster your-cluster --query 'taskArns[0]' --output text)

# Execute command in container
aws ecs execute-command \
    --cluster your-cluster \
    --task $TASK_ARN \
    --container toursmanager-backend \
    --interactive \
    --command "env | grep JWT_SECRET"
```

### Verify Application Startup
```bash
# Check logs for JWT provider initialization
docker logs toursmanager-backend | grep -i jwt

# Should NOT see the actual secret value in logs!
```

---

## Rotating JWT_SECRET

**Important:** Rotating JWT_SECRET invalidates all existing tokens.

### Rotation Strategy:

1. **Planned rotation (graceful):**
   ```bash
   # 1. Generate new secret
   NEW_SECRET=$(openssl rand -base64 48)
   
   # 2. Update Secrets Manager
   aws secretsmanager update-secret \
       --secret-id toursmanager/prod/JWT_SECRET \
       --secret-string "$NEW_SECRET"
   
   # 3. Deploy new version (ECS/Beanstalk restarts with new secret)
   # 4. All users will need to re-login (tokens invalidated)
   ```

2. **Emergency rotation (breach detected):**
   - Same as above but immediate
   - Consider implementing token blacklist for extra security

3. **Smooth rotation (advanced - dual key support):**
   - Support multiple JWT secrets (old + new)
   - Sign new tokens with new secret
   - Accept both old and new for validation
   - Requires code changes (maintain key array)

---

## Troubleshooting

### Problem: Application fails to start with "jwt.secret not set"

**Solution:**
```bash
# Check if secret exists in Secrets Manager
aws secretsmanager describe-secret --secret-id toursmanager/prod/JWT_SECRET

# Check IAM role has permission
aws iam simulate-principal-policy \
    --policy-source-arn arn:aws:iam::ACCOUNT:role/ecsTaskRole \
    --action-names secretsmanager:GetSecretValue \
    --resource-arns arn:aws:secretsmanager:REGION:ACCOUNT:secret:toursmanager/prod/JWT_SECRET

# Check ECS task definition has correct secret reference
aws ecs describe-task-definition --task-definition toursmanager
```

### Problem: "Access Denied" when reading secret

**Solution:**
- Check IAM role attached to task/instance
- Check IAM policy includes `secretsmanager:GetSecretValue`
- Check resource ARN matches (wildcards, region, account ID)

### Problem: Secret value visible in logs

**Solution:**
- Never log environment variables
- Review application logs
- Use CloudWatch Logs Insights to search for leaked secrets
- Rotate secret immediately if exposed

---

## AWS Free Tier Considerations

**Secrets Manager pricing:**
- $0.40 per secret per month
- $0.05 per 10,000 API calls
- **Estimate:** 2 secrets × $0.40 = $0.80/month

**Alternative: AWS Systems Manager Parameter Store (SSM)**
- Free tier: 10,000 API calls/month
- Standard parameters: Free
- Advanced parameters: $0.05 per parameter per month

**For learning/portfolio:** Secrets Manager is better (purpose-built, supports rotation), but SSM is free alternative.

---

## Next Steps

1. ✅ Update `application.properties` (remove fallback)
2. ✅ Add `.env.example` with instructions
3. ✅ Ensure `.env` is in `.gitignore`
4. ⬜ Create AWS Secrets Manager secrets
5. ⬜ Set up IAM roles and policies
6. ⬜ Configure ECS task definition or Beanstalk environment
7. ⬜ Test deployment
8. ⬜ Add to LinkedIn: "Implemented secure secret management with AWS Secrets Manager, IAM least privilege, and JWT authentication"

---

## Resources

- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [ECS Secrets Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/specifying-sensitive-data-secrets.html)
- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
