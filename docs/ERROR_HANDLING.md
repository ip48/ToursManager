# Standardized Error Handling

## ✅ What We Just Added

Created a global exception handler that returns consistent, structured error responses for all API errors.

## The Problem We Solved

**Before:**
- Different endpoints returned errors in different formats
- Some returned plain text, others JSON
- Field validation errors were hard to parse
- Mobile apps would need different error handling for each endpoint

**After:**
- All errors return the same JSON structure
- Easy to parse on web and mobile
- Validation errors include field-specific messages
- One error handling pattern for the entire frontend

---

## Error Response Format

All API errors now return this structure:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email already exists",
    "firstName": "First name must be at least 2 characters"
  },
  "path": "/api/guides",
  "timestamp": "2025-10-29T14:30:00"
}
```

### Fields:
- **status**: HTTP status code (400, 404, 500, etc.)
- **message**: Human-readable error summary
- **errors**: (Optional) Field-specific validation errors - key is field name, value is error message
- **path**: The API endpoint that caused the error
- **timestamp**: When the error occurred

---

## How It Works

### 1. GlobalExceptionHandler (`/exception/GlobalExceptionHandler.java`)

A Spring `@RestControllerAdvice` that catches all exceptions from controllers and converts them to `ErrorResponse`.

### 2. Three Types of Errors Handled

#### **Validation Errors** (from `@Valid`)
When request body fails Jakarta validation:
```java
@PostMapping
public ResponseEntity<Guide> createGuide(@Valid @RequestBody Guide guide) {
    // If validation fails, GlobalExceptionHandler catches it
}
```

**Example Error:**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "firstName": "First name is required"
  },
  "path": "/api/guides"
}
```

#### **Business Logic Errors** (IllegalArgumentException)
When service layer throws IllegalArgumentException:
```java
if (guideRepository.findByEmail(email).isPresent()) {
    throw new IllegalArgumentException("Email already exists");
}
```

**Example Error:**
```json
{
  "status": 400,
  "message": "Email already exists",
  "path": "/api/guides"
}
```

#### **Unexpected Errors** (any Exception)
Catches all other exceptions and returns a safe error (doesn't expose internal details):
```json
{
  "status": 500,
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/guides"
}
```

---

## Benefits

### For Development
- ✅ Cleaner controller code (no try-catch blocks needed)
- ✅ Consistent error format across all endpoints
- ✅ Centralized error handling logic
- ✅ Easy to add new error types

### For Frontend (Web & Mobile)
- ✅ Parse errors the same way everywhere
- ✅ Display field-specific validation errors easily
- ✅ Handle all errors with one function

---

## How to Use in Frontend

### Update the Frontend Service

Update `/frontend/src/services/guideService.ts` to parse the new error format:

```typescript
export const registerGuide = async (guideData: GuideFormData): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(API_ENDPOINTS.GUIDES, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(guideData),
    });

    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      // Parse standardized error response
      const errorData = await response.json();
      
      // If there are field-specific errors, return them
      if (errorData.errors) {
        return { 
          error: errorData.message, 
          fieldErrors: errorData.errors,  // NEW: field-specific errors
          status: response.status 
        };
      }
      
      // Otherwise just return the message
      return { error: errorData.message, status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};
```

### Display Errors in Component

```typescript
const result = await registerGuide(formData);

if (result.data) {
  // Success!
  setSubmitStatus('success');
} else {
  // Handle error
  if (result.fieldErrors) {
    // Show field-specific errors next to each input
    setErrors(result.fieldErrors);
  } else {
    // Show general error message
    setSubmitMessage(result.error);
  }
}
```

---

## Example Error Scenarios

### 1. Missing Required Fields
**Request:**
```json
POST /api/guides
{
  "email": "john@example.com"
}
```

**Response (400):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "firstName": "First name is required",
    "lastName": "Last name is required"
  },
  "path": "/api/guides",
  "timestamp": "2025-10-29T14:30:00"
}
```

### 2. Invalid Email Format
**Request:**
```json
POST /api/guides
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "not-an-email"
}
```

**Response (400):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid"
  },
  "path": "/api/guides",
  "timestamp": "2025-10-29T14:30:00"
}
```

### 3. Duplicate Email (Business Logic Error)
**Request:**
```json
POST /api/guides
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "existing@example.com"
}
```

**Response (400):**
```json
{
  "status": 400,
  "message": "Email already exists: existing@example.com",
  "path": "/api/guides",
  "timestamp": "2025-10-29T14:30:00"
}
```

### 4. Guide Not Found
**Request:**
```
GET /api/guides/9999
```

**Response (400):**
```json
{
  "status": 400,
  "message": "Guide not found with id: 9999",
  "path": "/api/guides/9999",
  "timestamp": "2025-10-29T14:30:00"
}
```

---

## Next Steps

### 1. Update Frontend Service (Optional but Recommended)
Add `fieldErrors` to the `ApiResponse` type and handle them in components.

### 2. Test the Error Responses
Start the backend and try invalid requests to see the new error format:
```bash
# Missing required fields
curl -X POST http://localhost:8080/api/guides \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com"}'

# Invalid email
curl -X POST http://localhost:8080/api/guides \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"invalid"}'
```

### 3. When Adding New Entities
The error handling works automatically for all controllers! Just:
- Use `@Valid` for validation
- Throw `IllegalArgumentException` for business logic errors
- Let GlobalExceptionHandler handle the rest

---

## Summary: What Changed in Controllers

**Before:**
```java
@PostMapping
public ResponseEntity<?> createGuide(@Valid @RequestBody Guide guide) {
    try {
        Guide created = service.createGuide(guide);
        return ResponseEntity.status(201).body(created);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

**After:**
```java
@PostMapping
public ResponseEntity<Guide> createGuide(@Valid @RequestBody Guide guide) {
    // No try-catch needed! GlobalExceptionHandler handles errors automatically
    Guide created = service.createGuide(guide);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

Much cleaner! 🎉
