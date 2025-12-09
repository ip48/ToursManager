# .NET Backend Implementation Progress

## ✅ Completed

### 1. Environment Setup
- ✅ Installed .NET 8.0.415 SDK in dev container
- ✅ Created ASP.NET Core Web API project (ToursManager.Api)
- ✅ Installed NuGet packages:
  - Npgsql.EntityFrameworkCore.PostgreSQL 8.0.10 (PostgreSQL provider)
  - Microsoft.EntityFrameworkCore.Design 8.0.11 (migrations tooling)
  - Microsoft.AspNetCore.Authentication.JwtBearer 8.0.11 (JWT authentication)
  - BCrypt.Net-Next 4.0.3 (password hashing)
  - Swashbuckle.AspNetCore 6.9.0 (Swagger/OpenAPI documentation)

### 2. Project Structure
Created directories:
- `Models/` - Entity classes (Guide, Language)
- `DTOs/` - Data Transfer Objects for API
- `Services/` - Business logic and JWT service
- `Data/` - Database context

### 3. Entity Models (equivalent to JPA @Entity)
- ✅ **Language.cs** - Language entity with Code, Name properties
- ✅ **Guide.cs** - Guide entity with all fields matching Spring Boot:
  - FirstName, LastName, Email, Password (BCrypt)
  - PhoneNumber, Profile, Active, CreatedAt, UpdatedAt
  - Many-to-many relationship with Languages
  - OnCreate() and OnUpdate() methods for timestamps

### 4. DTOs (Data Transfer Objects)
- ✅ **GuideDto.cs** - Main DTO for Guide API responses
- ✅ **GuideUpdateDto.cs** - DTO for PATCH partial updates (all fields optional)
- ✅ **LoginRequest.cs** - Login credentials
- ✅ **RegisterRequest.cs** - New guide registration with password
- ✅ **JwtResponse.cs** - JWT token response after login

### 5. Database Context
- ✅ **ApplicationDbContext.cs** - Entity Framework DbContext
  - Configured Guide and Language entities
  - Many-to-many relationship with join table "guide_languages"
  - Snake_case column names (first_name, last_name, etc.)
  - Unique constraints on email and language code
  - Automatic timestamp updates on SaveChanges()

### 6. Services
- ✅ **JwtService.cs** - JWT token generation and validation
  - GenerateToken(email) - Creates JWT with 24-hour expiration
  - ValidateToken(token) - Verifies signature and expiration
  - GetEmailFromToken(token) - Extracts email from claims
  - Uses HS256 algorithm (same as Spring Boot)

### 7. Configuration
- ✅ **appsettings.Development.json** - Development configuration:
  ```json
  {
    "ConnectionStrings": {
      "DefaultConnection": "Host=localhost;Port=5433;Database=toursmanager;..."
    },
    "Jwt": {
      "Secret": "dev_insecure_secret_key_minimum_32_characters_long_12345",
      "ExpirationHours": 24
    },
    "Cors": {
      "AllowedOrigins": "http://localhost:5173,http://localhost:3000"
    }
  }
  ```
- ✅ **appsettings.json** - Production template (empty values for environment variables)

## 🔄 Next Steps (To Complete)

### 8. Controllers (REST API Endpoints)
Need to create:
- **AuthController** - POST /api/auth/register, POST /api/auth/login
- **GuidesController** - GET /api/guides, POST /api/guides, PATCH /api/guides/profile
- Match exact endpoints from Spring Boot for interchangeability

### 9. Service Layer (Business Logic)
- **GuideService** - CRUD operations, partial updates
- **LanguageService** - Language lookup by codes

### 10. Program.cs Configuration
Configure services:
- Entity Framework with PostgreSQL
- JWT Authentication middleware
- CORS policy
- Swagger/OpenAPI
- Dependency Injection for services

### 11. Database Migrations
Create initial migration:
```bash
dotnet ef migrations add InitialCreate
dotnet ef database update
```

### 12. Testing
- Build the project: `dotnet build`
- Run the project: `dotnet run`
- Test endpoints with React frontend (change API_BASE_URL to http://localhost:5000)

## 📚 Key Comparisons: .NET vs Spring Boot

| Feature | Spring Boot (Java) | ASP.NET Core (.NET) |
|---------|-------------------|---------------------|
| **Dependency Injection** | `@Autowired`, Constructor injection | Constructor injection (built-in) |
| **ORM** | JPA/Hibernate | Entity Framework Core |
| **Entities** | `@Entity`, `@Table`, `@Column` | `[Table]`, `[Column]` attributes |
| **Timestamps** | `@PrePersist`, `@PreUpdate` | Override SaveChanges() |
| **Configuration** | `application.properties` | `appsettings.json` |
| **Controllers** | `@RestController`, `@GetMapping` | `[ApiController]`, `[HttpGet]` |
| **Validation** | `@NotBlank`, `@Email`, `@Size` | `[Required]`, `[EmailAddress]`, `[StringLength]` |
| **JWT** | JJWT library | System.IdentityModel.Tokens.Jwt |
| **Password Hashing** | BCryptPasswordEncoder | BCrypt.Net-Next |
| **Many-to-Many** | `@ManyToMany`, `@JoinTable` | HasMany().WithMany().UsingEntity() |
| **Async** | CompletableFuture | async/await with Task<T> |

## 🎯 Interview Talking Points

1. **Framework Philosophy**: Both use dependency injection as core principle
2. **ORM Differences**: JPA uses annotations on entities, EF Core uses Fluent API in OnModelCreating
3. **Middleware vs Filters**: ASP.NET middleware pipeline vs Spring Security filter chain
4. **Async Patterns**: C# async/await is more intuitive than Java CompletableFuture
5. **Configuration**: JSON (hierarchical) vs properties files (flat key-value)
6. **Null Safety**: C# nullable reference types vs Java Optional<T>
7. **Language Features**: C# properties vs Java getters/setters, C# LINQ vs Java Streams

## 📝 Next Session Plan

1. Configure Program.cs with all middleware
2. Create AuthController and GuidesController
3. Implement GuideService with partial update logic
4. Test with Swagger UI
5. Test interchangeability with React frontend
6. Create documentation comparing implementations

## 🚀 Running the .NET API

```bash
cd /workspaces/ToursManager/dotnet-api/ToursManager.Api

# Restore packages (if needed)
dotnet restore

# Build
dotnet build

# Run migrations
dotnet ef database update

# Run the API
dotnet run

# API will be available at:
# - HTTP: http://localhost:5000
# - HTTPS: https://localhost:5001
# - Swagger: http://localhost:5000/swagger
```

## 🔗 Resources for Interview

- Entity Framework Core: https://learn.microsoft.com/en-us/ef/core/
- ASP.NET Core: https://learn.microsoft.com/en-us/aspnet/core/
- C# Language: https://learn.microsoft.com/en-us/dotnet/csharp/
- JWT in .NET: https://learn.microsoft.com/en-us/aspnet/core/security/authentication/
