# Tours Manager - .NET 8 Web API

This is a .NET 8 implementation of the Tours Manager backend, providing the same REST API as the Spring Boot version. This allows the React frontend to work interchangeably with either backend.

## 🏗️ Project Structure

```
ToursManager.Api/
├── Controllers/          # REST API endpoints (like Spring @RestController)
│   ├── AuthController.cs      # POST /api/auth/register, /api/auth/login
│   └── GuidesController.cs    # CRUD operations for guides
│
├── Services/             # Business logic layer (like Spring @Service)
│   ├── GuideService.cs        # Guide CRUD operations
│   └── JwtService.cs          # JWT token generation/validation
│
├── Data/                 # Database context (like Spring JPA)
│   └── ApplicationDbContext.cs # Entity Framework DbContext
│
├── Models/               # Entity classes (like Spring @Entity)
│   ├── Guide.cs               # Guide entity with many-to-many relationship
│   └── Language.cs            # Language entity
│
├── DTOs/                 # Data Transfer Objects (API contracts)
│   ├── GuideDto.cs            # Main DTO for Guide
│   ├── GuideUpdateDto.cs      # Partial update DTO (PATCH)
│   ├── LoginRequest.cs        # Login credentials
│   ├── RegisterRequest.cs     # Registration data
│   └── JwtResponse.cs         # JWT token response
│
├── Program.cs            # Application entry point & configuration
├── appsettings.json      # Production configuration (empty secrets)
├── appsettings.Development.json  # Dev configuration with defaults
└── ToursManager.Api.csproj      # Project file (like pom.xml)
```

## 🔄 Architecture Comparison: Spring Boot vs .NET

### Dependency Injection

**Spring Boot:**
```java
@RestController
public class GuideController {
    private final GuideService guideService;
    
    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }
}
```

**.NET (Same pattern!):**
```csharp
[ApiController]
public class GuidesController : ControllerBase {
    private readonly GuideService _guideService;
    
    public GuidesController(GuideService guideService) {
        _guideService = guideService;
    }
}
```

### Entity Definition

**Spring Boot (JPA):**
```java
@Entity
@Table(name = "guides")
public class Guide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @ManyToMany
    @JoinTable(name = "guide_languages",
        joinColumns = @JoinColumn(name = "guide_id"),
        inverseJoinColumns = @JoinColumn(name = "language_id"))
    private Set<Language> languages;
}
```

**.NET (Entity Framework Core):**
```csharp
[Table("guides")]
public class Guide {
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }
    
    [Required]
    public string FirstName { get; set; }
    
    public ICollection<Language> Languages { get; set; }
}

// Relationship configured in DbContext:
modelBuilder.Entity<Guide>()
    .HasMany(g => g.Languages)
    .WithMany(l => l.Guides)
    .UsingEntity("guide_languages");
```

### REST Controllers

**Spring Boot:**
```java
@RestController
@RequestMapping("/api/guides")
public class GuideController {
    @GetMapping
    public ResponseEntity<List<GuideDTO>> getAllGuides() { }
    
    @GetMapping("/{id}")
    public ResponseEntity<GuideDTO> getGuideById(@PathVariable Long id) { }
    
    @PatchMapping("/profile")
    public ResponseEntity<GuideDTO> partialUpdate(@RequestBody GuideUpdateDTO dto) { }
}
```

**.NET:**
```csharp
[ApiController]
[Route("api/[controller]")]
public class GuidesController : ControllerBase {
    [HttpGet]
    public async Task<ActionResult<IEnumerable<GuideDto>>> GetAllGuides() { }
    
    [HttpGet("{id}")]
    public async Task<ActionResult<GuideDto>> GetGuideById(long id) { }
    
    [HttpPatch("profile")]
    public async Task<ActionResult<GuideDto>> PartialUpdate([FromBody] GuideUpdateDto dto) { }
}
```

### Database Access

**Spring Boot (Repository Pattern):**
```java
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    Optional<Guide> findByEmail(String email);
}

@Service
public class GuideService {
    private final GuideRepository guideRepository;
    
    public Guide getByEmail(String email) {
        return guideRepository.findByEmail(email).orElse(null);
    }
}
```

**.NET (DbContext Direct - Idiomatic .NET):**
```csharp
public class GuideService {
    private readonly ApplicationDbContext _context;
    
    public async Task<Guide?> GetByEmailAsync(string email) {
        return await _context.Guides
            .Include(g => g.Languages)  // Eager loading
            .FirstOrDefaultAsync(g => g.Email == email);
    }
}
```

> **Key Difference:** .NET typically injects `DbContext` directly into services instead of creating repository interfaces. Entity Framework Core already implements the Repository and Unit of Work patterns.

## 🔐 JWT Authentication Flow

### 1. Registration (POST /api/auth/register)
```
Client → RegisterRequest (email, password, firstName, lastName)
  ↓
AuthController.Register()
  ↓
- Check if email exists
- Hash password with BCrypt
- Create Guide entity
- Save to database
  ↓
JwtService.GenerateToken(email)
  ↓
Return JwtResponse (token, email, firstName, lastName)
```

### 2. Login (POST /api/auth/login)
```
Client → LoginRequest (email, password)
  ↓
AuthController.Login()
  ↓
- Find guide by email
- Verify password with BCrypt
  ↓
JwtService.GenerateToken(email)
  ↓
Return JwtResponse (token, email, firstName, lastName)
```

### 3. Protected Endpoints (e.g., GET /api/guides/profile)
```
Client → Request with Authorization: Bearer <token>
  ↓
JWT Middleware (configured in Program.cs)
  ↓
- Extract token from header
- Validate signature, expiration, issuer, audience
- Extract claims (email) and add to User.Claims
  ↓
Controller method with [Authorize] attribute
  ↓
- Access email via User.FindFirst(ClaimTypes.Email)
- Execute business logic
  ↓
Return response
```

## ⚙️ Configuration (appsettings.json)

**.NET uses hierarchical JSON** instead of flat properties files:

**Spring Boot:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/toursmanager
jwt.secret=dev_insecure_secret
cors.allowed-origins=http://localhost:5173
```

**.NET:**
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Port=5433;Database=toursmanager"
  },
  "Jwt": {
    "Secret": "dev_insecure_secret",
    "ExpirationHours": 24
  },
  "Cors": {
    "AllowedOrigins": "http://localhost:5173"
  }
}
```

Accessed in code:
```csharp
var secret = _configuration["Jwt:Secret"];
var connectionString = _configuration.GetConnectionString("DefaultConnection");
```

## 🚀 Program.cs - The Heart of .NET

`Program.cs` is where everything is wired together (equivalent to Spring Boot's configuration classes):

```csharp
var builder = WebApplication.CreateBuilder(args);

// 1. Configure Services (Dependency Injection)
builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseNpgsql(connectionString));

builder.Services.AddScoped<GuideService>();
builder.Services.AddScoped<JwtService>();

// 2. Configure JWT Authentication
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options => { /* token validation */ });

// 3. Configure CORS
builder.Services.AddCors(options => { /* CORS policy */ });

// 4. Add Controllers & Swagger
builder.Services.AddControllers();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// 5. Configure Middleware Pipeline (Order matters!)
app.UseSwagger();           // Swagger UI
app.UseHttpsRedirection();  // Redirect HTTP → HTTPS
app.UseCors("AllowFrontend"); // CORS - BEFORE Authentication
app.UseAuthentication();    // Read JWT token
app.UseAuthorization();     // Check permissions
app.MapControllers();       // Route requests to controllers

app.Run();
```

### Middleware Order is Critical!
```
Request → UseSwagger → UseHttpsRedirection → UseCors 
  → UseAuthentication → UseAuthorization → MapControllers → Response
```

- **CORS before Authentication**: So preflight requests don't fail
- **Authentication before Authorization**: Must identify user before checking permissions

## 🗄️ Entity Framework Core (ORM)

### DbContext (like JPA's EntityManager)

```csharp
public class ApplicationDbContext : DbContext {
    public DbSet<Guide> Guides { get; set; }
    public DbSet<Language> Languages { get; set; }
    
    protected override void OnModelCreating(ModelBuilder modelBuilder) {
        // Configure relationships, constraints, column names
        modelBuilder.Entity<Guide>()
            .HasMany(g => g.Languages)
            .WithMany(l => l.Guides)
            .UsingEntity("guide_languages");
    }
    
    // Automatic timestamps
    public override Task<int> SaveChangesAsync(...) {
        UpdateTimestamps();  // Called before every save
        return base.SaveChangesAsync(...);
    }
}
```

### Querying (LINQ - Language Integrated Query)

```csharp
// Get all guides with languages (eager loading)
var guides = await _context.Guides
    .Include(g => g.Languages)  // JOIN with languages
    .Where(g => g.Active)       // WHERE active = true
    .OrderBy(g => g.LastName)   // ORDER BY last_name
    .ToListAsync();             // Execute query

// Get single guide
var guide = await _context.Guides
    .FirstOrDefaultAsync(g => g.Email == email);
```

**LINQ is like Java Streams + SQL:**
```csharp
// Java Stream
guides.stream()
    .filter(g -> g.getActive())
    .sorted(Comparator.comparing(Guide::getLastName))
    .collect(Collectors.toList());

// C# LINQ (same concept!)
guides
    .Where(g => g.Active)
    .OrderBy(g => g.LastName)
    .ToList();
```

## 🔧 Key C# Concepts for Java Developers

### 1. Properties (Getters/Setters)
**Java:**
```java
private String firstName;

public String getFirstName() { return firstName; }
public void setFirstName(String firstName) { this.firstName = firstName; }
```

**C# (Auto-property):**
```csharp
public string FirstName { get; set; }
```

### 2. Nullable Reference Types
**Java:**
```java
Optional<Guide> guide = guideRepository.findByEmail(email);
```

**C#:**
```csharp
Guide? guide = await _context.Guides.FirstOrDefaultAsync(...);
// The ? means it can be null
```

### 3. String Interpolation
**Java:**
```java
String message = String.format("Hello, %s %s!", firstName, lastName);
```

**C#:**
```csharp
string message = $"Hello, {firstName} {lastName}!";
```

### 4. Async/Await (Better than CompletableFuture)
**Java:**
```java
CompletableFuture<Guide> future = CompletableFuture.supplyAsync(() -> 
    guideRepository.findByEmail(email));
Guide guide = future.get();
```

**C#:**
```csharp
Guide? guide = await _context.Guides.FirstOrDefaultAsync(g => g.Email == email);
// Much cleaner! No callbacks, no .get(), just await
```

### 5. Attributes (like Java Annotations)
**Java:**
```java
@RestController
@RequestMapping("/api/guides")
```

**C#:**
```csharp
[ApiController]
[Route("api/[controller]")]
```

## 📦 NuGet Packages (like Maven Dependencies)

Our project uses:
- `Npgsql.EntityFrameworkCore.PostgreSQL` - PostgreSQL provider for EF Core
- `Microsoft.EntityFrameworkCore.Design` - Migrations tooling
- `Microsoft.AspNetCore.Authentication.JwtBearer` - JWT authentication
- `BCrypt.Net-Next` - Password hashing
- `Swashbuckle.AspNetCore` - Swagger/OpenAPI documentation

## 🛠️ Common Commands

### Build & Run
```bash
# Restore NuGet packages
dotnet restore

# Build project
dotnet build

# Run application (http://localhost:5000)
dotnet run

# Run with hot reload (changes auto-reload)
dotnet watch run
```

### Database Migrations
```bash
# Create a migration
dotnet ef migrations add InitialCreate

# Apply migrations to database
dotnet ef database update

# Revert last migration
dotnet ef migrations remove

# List all migrations
dotnet ef migrations list
```

### Testing
```bash
# Run tests
dotnet test

# With detailed output
dotnet test --verbosity normal
```

## 🌐 API Endpoints (Same as Spring Boot)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Root info | No |
| GET | `/api/health` | Health check | No |
| POST | `/api/auth/register` | Register new guide | No |
| POST | `/api/auth/login` | Login | No |
| GET | `/api/guides` | Get all guides | No |
| GET | `/api/guides/{id}` | Get guide by ID | No |
| GET | `/api/guides/profile` | Get my profile | Yes (JWT) |
| PATCH | `/api/guides/profile` | Partial update profile | Yes (JWT) |
| PUT | `/api/guides/profile` | Full update profile | Yes (JWT) |
| DELETE | `/api/guides/{id}` | Delete guide | Yes (JWT) |

## 🎯 Interview Talking Points

When discussing this project in your interview:

1. **Architecture**: "Both Spring Boot and .NET use MVC/API architecture with dependency injection as the core pattern."

2. **ORM Differences**: "Spring uses JPA annotations on entities, while .NET uses Fluent API in DbContext. The .NET approach keeps entities cleaner."

3. **Repository Pattern**: "Spring requires repository interfaces, but .NET idiomatically uses DbContext directly in services since EF Core already implements the repository pattern."

4. **Async/Await**: ".NET's async/await is more intuitive than Java's CompletableFuture - it reads like synchronous code but executes asynchronously."

5. **Middleware Pipeline**: ".NET's middleware is similar to Spring Security filters but configured in a single place (Program.cs) making the flow clearer."

6. **Language Features**: "C# properties eliminate getter/setter boilerplate, nullable reference types are built-in (Java's Optional), and LINQ is more powerful than Java Streams."

## 📚 Learning Resources

- [ASP.NET Core Fundamentals](https://learn.microsoft.com/en-us/aspnet/core/fundamentals/)
- [Entity Framework Core](https://learn.microsoft.com/en-us/ef/core/)
- [C# Language Guide](https://learn.microsoft.com/en-us/dotnet/csharp/)
- [Dependency Injection in .NET](https://learn.microsoft.com/en-us/dotnet/core/extensions/dependency-injection)
