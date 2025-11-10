# Production CORS Configuration

## How CORS Works in This Project

### Configuration Source
- **Development:** `application-dev.properties` contains `cors.allowed-origins=${ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}`
- **Production:** Set `ALLOWED_ORIGINS` environment variable to override default
- **SecurityConfig:** Injects `cors.allowed-origins` property via `@Value` annotation

### Development (Default)
- **No environment variable set**
- Allows: `http://localhost:5173`, `http://localhost:3000` (from `application-dev.properties` default)
- Used when running locally

### Production
- **Set `ALLOWED_ORIGINS` environment variable**
- Allows: Your actual domain(s)
- Example: `https://toursmanager.com`, `https://www.toursmanager.com`
- Overrides the `application-dev.properties` default via Spring's property resolution

## Setting Up for Production

### Option 1: Single Domain
```bash
# AWS ECS/Beanstalk/EC2
ALLOWED_ORIGINS=https://toursmanager.com
```

### Option 2: Multiple Domains
```bash
# Multiple domains (comma-separated, no spaces)
ALLOWED_ORIGINS=https://toursmanager.com,https://www.toursmanager.com
```

### Option 3: Different Environments
```bash
# Staging
ALLOWED_ORIGINS=https://staging.toursmanager.com

# Production
ALLOWED_ORIGINS=https://toursmanager.com,https://www.toursmanager.com
```

## AWS Deployment Examples

### ECS Task Definition (Fargate)
```json
{
  "containerDefinitions": [
    {
      "name": "tours-manager",
      "environment": [
        {
          "name": "ALLOWED_ORIGINS",
          "value": "https://toursmanager.com,https://www.toursmanager.com"
        }
      ]
    }
  ]
}
```

### Elastic Beanstalk
```bash
# .ebextensions/environment.config
option_settings:
  - namespace: aws:elasticbeanstalk:application:environment
    option_name: ALLOWED_ORIGINS
    value: https://toursmanager.com,https://www.toursmanager.com
```

### Docker Run (EC2)
```bash
docker run -e ALLOWED_ORIGINS=https://toursmanager.com \
  -p 8080:8080 \
  tours-manager:latest
```

### Docker Compose (Production)
```yaml
# docker-compose.prod.yml
services:
  app:
    environment:
      - ALLOWED_ORIGINS=https://toursmanager.com,https://www.toursmanager.com
      - SPRING_PROFILES_ACTIVE=prod
```

## Security Best Practices

### ✅ Good - Specific Domains
```bash
ALLOWED_ORIGINS=https://toursmanager.com,https://www.toursmanager.com
```

### ❌ Bad - Wildcard (allows ANY website!)
```bash
ALLOWED_ORIGINS=*  # DON'T DO THIS IN PRODUCTION!
```

### ⚠️ Be Careful - HTTP vs HTTPS
```bash
# Development (HTTP)
http://localhost:5173

# Production (HTTPS - always use HTTPS!)
https://toursmanager.com
```

## Common Deployment Scenarios

### Scenario 1: Frontend and Backend on Same Domain
```
Frontend: https://toursmanager.com
Backend:  https://toursmanager.com/api

ALLOWED_ORIGINS=https://toursmanager.com
```

### Scenario 2: Frontend and Backend on Different Domains
```
Frontend: https://app.toursmanager.com
Backend:  https://api.toursmanager.com

ALLOWED_ORIGINS=https://app.toursmanager.com
```

### Scenario 3: Multiple Frontend Apps
```
Web App:    https://toursmanager.com
Admin App:  https://admin.toursmanager.com
Mobile Web: https://m.toursmanager.com

ALLOWED_ORIGINS=https://toursmanager.com,https://admin.toursmanager.com,https://m.toursmanager.com
```

## Testing Production CORS

### 1. Check Environment Variable
```bash
# SSH into your server
echo $ALLOWED_ORIGINS
# Should print: https://toursmanager.com
```

### 2. Test CORS Preflight
```bash
curl -X OPTIONS https://api.toursmanager.com/api/auth/login \
  -H "Origin: https://toursmanager.com" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

**Expected response headers:**
```
Access-Control-Allow-Origin: https://toursmanager.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

### 3. Test from Browser Console
```javascript
// Open https://toursmanager.com
// Open browser DevTools console
fetch('https://api.toursmanager.com/api/hello')
  .then(r => r.text())
  .then(console.log)
  .catch(console.error);
```

## Troubleshooting

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Cause:** Environment variable not set or wrong domain

**Fix:**
```bash
# Check environment variable
echo $ALLOWED_ORIGINS

# Restart application after setting
export ALLOWED_ORIGINS=https://toursmanager.com
java -jar tours-manager.jar
```

### Error: "CORS policy: The 'Access-Control-Allow-Origin' header contains multiple values"

**Cause:** Multiple CORS configurations (e.g., both in `SecurityConfig` and `@CrossOrigin`)

**Fix:** Use only `SecurityConfig` CORS bean (already done in this project)

### Error: "Access-Control-Allow-Credentials"

**Cause:** Frontend sending cookies but backend doesn't allow credentials

**Fix:** Already configured with `setAllowCredentials(true)`

## Migration Checklist

When moving to production:

- [ ] Buy domain name (e.g., `toursmanager.com`)
- [ ] Set up SSL certificate (use AWS Certificate Manager or Let's Encrypt)
- [ ] Deploy frontend to domain (e.g., `https://toursmanager.com`)
- [ ] Deploy backend to API domain (e.g., `https://api.toursmanager.com`)
- [ ] Set `ALLOWED_ORIGINS` environment variable in AWS
- [ ] Update frontend `API_BASE_URL` to production API
- [ ] Test CORS from production frontend
- [ ] Monitor CloudWatch logs for CORS errors

## Configuration Pattern

This project uses Spring Boot's externalized configuration pattern consistently:

```properties
# application-dev.properties
cors.allowed-origins=${ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}
```

```java
// SecurityConfig.java
@Value("${cors.allowed-origins}")
private String allowedOrigins;
```

This pattern:
- Reads from `ALLOWED_ORIGINS` environment variable if set
- Falls back to default value (`http://localhost:5173,http://localhost:3000`)
- Follows same pattern as `JWT_SECRET`, database credentials, etc.
- No hardcoded values in Java code

## Related Files

- Backend CORS: `src/main/java/.../security/SecurityConfig.java` - Injects `cors.allowed-origins`
- Dev Config: `src/main/resources/application-dev.properties` - Defines CORS property with defaults
- Frontend API URL: `frontend/src/constants/api.ts`
- Prod Config: `src/main/resources/application-prod.properties`

## Summary

**Development:** No configuration needed (uses localhost)

**Production:** Set one environment variable:
```bash
ALLOWED_ORIGINS=https://your-actual-domain.com
```

That's it! 🚀
