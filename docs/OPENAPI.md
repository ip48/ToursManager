# OpenAPI (Swagger) Documentation

## ✅ What We Just Added

Added Springdoc OpenAPI to automatically generate API documentation from your Spring Boot controllers.

## What You Get

### 1. Interactive API Documentation UI
Once the backend is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html (or http://localhost:8080/swagger-ui.html)
  - Interactive web interface to test API endpoints
  - Try requests directly from your browser
  - See request/response examples

- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
  - Machine-readable API specification
  - Can be imported into Postman, Insomnia, etc.
  - Can be used to generate client code for mobile apps

### 2. Benefits for Mobile Development

When you add React Native later:
- Import the OpenAPI spec into code generation tools
- Auto-generate TypeScript/JavaScript API client code
- Type-safe API calls with zero manual typing
- Always in sync with backend (regenerate when API changes)

### 3. What the Annotations Do

#### `@Tag` (on controller class)
Groups related endpoints together in the documentation.

```java
@Tag(name = "Guides", description = "Guide management APIs")
```

#### `@Operation` (on methods)
Describes what the endpoint does:
```java
@Operation(summary = "Get all guides", 
           description = "Retrieve all guides with optional filtering")
```

#### `@ApiResponses` (on methods)
Documents possible HTTP responses:
```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
```

#### `@Parameter` (on method parameters)
Describes query params, path variables, etc:
```java
@Parameter(description = "Filter by active status") 
@RequestParam(required = false) Boolean active
```

## How to Use

### Start the Backend
```bash
# Use VS Code task "Run Spring Boot" or:
mvn spring-boot:run
```

### Visit Swagger UI
Open in browser: http://localhost:8080/swagger-ui/index.html

You'll see:
- All your API endpoints organized by tags
- Each endpoint with description, parameters, response examples
- "Try it out" buttons to test endpoints directly
- Request/response schemas

### Test an Endpoint
1. Click "Guides" to expand
2. Click "POST /api/guides" (Register a new guide)
3. Click "Try it out"
4. Edit the JSON request body
5. Click "Execute"
6. See the response (success or error)

### Export for Mobile Development
1. Visit http://localhost:8080/v3/api-docs
2. Copy the JSON
3. Use with code generators:
   - OpenAPI Generator: https://openapi-generator.tech/
   - Swagger Codegen
   - orval (for TypeScript/React): https://orval.dev/

Example with orval (future):
```bash
npm install -g orval
orval --input http://localhost:8080/v3/api-docs --output ./src/api
# Generates TypeScript API client automatically!
```

## Next Steps

When you add more entities (Tours, Bookings):
- Add `@Tag` to each controller
- Add `@Operation` to document each endpoint
- Add `@ApiResponses` for different status codes
- The documentation updates automatically!

## Example: What You'll See in Swagger UI

**Guides Section:**
- GET /api/guides - "Get all guides"
  - Parameters: active (boolean), search (string), language (string)
  - Response: Array of Guide objects
- POST /api/guides - "Register a new guide"
  - Request body: Guide object with validation
  - Responses: 201 (success), 400 (validation error)
- GET /api/guides/{id} - "Get guide by ID"
  - Parameter: id (integer)
  - Responses: 200 (guide found), 404 (not found)
- And more...

---

**Key Benefit**: Your API documentation is always up-to-date because it's generated from the code!
