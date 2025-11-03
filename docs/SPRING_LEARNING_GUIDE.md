# Spring Boot Learning Guide - Understanding Your ToursManager Project

This guide explains every Spring concept, annotation, and architectural decision in your project. Study this at your own pace!

---

## Table of Contents
1. [Project Structure Overview](#project-structure-overview)
2. [Spring Boot Basics](#spring-boot-basics)
3. [Annotations Explained](#annotations-explained)
4. [Architecture: Layers Explained](#architecture-layers-explained)
5. [How Spring Works (Dependency Injection)](#how-spring-works)
6. [JPA Relationships (Many-to-Many)](#jpa-relationships)
7. [Your Guide Entity - Line by Line](#guide-entity-explained)
8. [Repository Layer](#repository-layer)
9. [Service Layer](#service-layer)
10. [Controller Layer & DTOs](#controller-layer)
11. [Configuration Classes](#configuration-classes)
12. [Exception Handling](#exception-handling)
13. [What's Required vs Optional](#whats-required-vs-optional)

---

## 1. Project Structure Overview

```
src/main/java/com/innatour/toursmanager/
├── ToursManagerApplication.java    # Main entry point (REQUIRED)
├── config/                          # Configuration classes (OPTIONAL but recommended)
│   ├── OpenApiConfig.java
│   └── DataInitializer.java        # Seeds reference data on startup
├── controller/                      # REST API endpoints (REQUIRED for API)
│   ├── GuideController.java
│   └── HelloController.java
├── dto/                            # Data Transfer Objects (OPTIONAL but recommended)
│   ├── ErrorResponse.java
│   └── GuideDTO.java               # API compatibility layer
├── exception/                      # Error handling (OPTIONAL but recommended)
│   └── GlobalExceptionHandler.java
├── model/                          # Database entities (REQUIRED for JPA)
│   ├── Guide.java                  # With @ManyToMany to Language
│   └── Language.java               # Reference data (ISO 639-1 codes)
├── repository/                     # Database access (REQUIRED for JPA)
│   ├── GuideRepository.java        # With custom queries
│   └── LanguageRepository.java     # Language lookup methods
└── service/                        # Business logic (OPTIONAL but best practice)
    └── GuideService.java

src/main/resources/
└── application.properties          # Configuration (REQUIRED)
```

**Why this structure?**
- **Separation of concerns**: Each layer has one job
- **Testability**: Easy to test each layer independently
- **Maintainability**: Easy to find and modify code
- **Standard**: Industry-standard Spring Boot pattern

---

## 2. Spring Boot Basics

### What is Spring Boot?

Spring Boot is a framework that makes it easy to create production-ready applications. It handles the "plumbing" so you can focus on business logic.

**Without Spring Boot, you'd need to:**
- Configure a web server (Tomcat) manually
- Set up database connections manually
- Write lots of boilerplate code
- Configure dependency injection yourself

**With Spring Boot:**
- Embedded web server (Tomcat) included
- Auto-configuration for common tasks
- Minimal configuration needed
- Just focus on your business logic

### The Main Application Class

**File:** `ToursManagerApplication.java`

```java
@SpringBootApplication
public class ToursManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToursManagerApplication.class, args);
    }
}
```

**What happens when you run this?**
1. Spring Boot scans your packages for classes with Spring annotations
2. Creates and manages objects (beans) automatically
3. Starts the embedded Tomcat server on port 8080
4. Connects to the database using your `application.properties`
5. Makes your REST API endpoints available

**@SpringBootApplication** is actually 3 annotations combined:
- `@Configuration` - This class provides Spring configuration
- `@EnableAutoConfiguration` - Spring Boot guesses what you need and sets it up
- `@ComponentScan` - Scans for Spring components in this package and subpackages

**REQUIRED**: Yes, every Spring Boot app needs exactly one of these.

---

## 3. Annotations Explained

### Core Spring Annotations

#### @Component, @Service, @Repository, @Controller, @RestController

These tell Spring: "Create and manage an instance of this class for me"

```java
@Component    // Generic - Spring manages this class
@Service      // Business logic layer (specialization of @Component)
@Repository   // Database access layer (specialization of @Component)
@Controller   // Web MVC controller (specialization of @Component)
@RestController // REST API controller (@Controller + @ResponseBody)
```

**Why different names if they do the same thing?**
- **Readability**: Makes your architecture clear
- **Future features**: Spring can add layer-specific features
- **Best practice**: Use the most specific one

**Example:**
```java
@Service  // Tells Spring: "This is a service, manage it for me"
public class GuideService {
    // Spring creates ONE instance and reuses it everywhere
}
```

**REQUIRED**: Yes, at least one of these on classes you want Spring to manage.

---

#### @Autowired (Dependency Injection)

Tells Spring: "I need this dependency, inject it for me"

**Old way (manual):**
```java
public class GuideService {
    private GuideRepository repository = new GuideRepository(); // BAD!
}
```

**Spring way (dependency injection):**
```java
@Service
public class GuideService {
    private final GuideRepository repository;
    
    @Autowired  // Spring injects the repository here
    public GuideService(GuideRepository repository) {
        this.repository = repository;
    }
}
```

**Why is this better?**
- Loose coupling - easy to swap implementations
- Testability - easy to inject mock objects
- Spring manages the lifecycle

**Modern style (no @Autowired needed):**
```java
@Service
public class GuideService {
    private final GuideRepository repository;
    
    // If there's only ONE constructor, @Autowired is optional
    public GuideService(GuideRepository repository) {
        this.repository = repository;
    }
}
```

**REQUIRED**: Only if you have multiple constructors. Otherwise optional (Spring assumes the constructor).

---

### JPA/Database Annotations

#### @Entity
Tells JPA: "This class represents a database table"

```java
@Entity
@Table(name = "guides")  // Optional: specify table name
public class Guide {
    // This becomes a table with columns for each field
}
```

**What happens?**
- JPA creates a table named "guides" (or class name if @Table not specified)
- Each field becomes a column
- Spring Boot can auto-create the table on startup (if configured)

**REQUIRED**: Yes, for JPA entities.

---

#### @ManyToMany, @OneToMany, @ManyToOne, @OneToOne
Define relationships between entities

```java
// Guide can speak many Languages, Language spoken by many Guides
@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "guide_languages",           // Join table name
    joinColumns = @JoinColumn(name = "guide_id"),
    inverseJoinColumns = @JoinColumn(name = "language_id")
)
private Set<Language> languages = new HashSet<>();
```

**Relationship types:**
- `@OneToOne` - 1:1 (e.g., User ↔ UserProfile)
- `@OneToMany` / `@ManyToOne` - 1:N (e.g., Guide → Tours, Tour → Guide)
- `@ManyToMany` - N:M (e.g., Guides ↔ Languages)

**FetchType:**
- `EAGER` - Load related entities immediately (use for always-needed data)
- `LAZY` - Load only when accessed (better performance, but can cause LazyInitializationException)

**CascadeType:**
- `PERSIST` - Save related entities when parent is saved
- `MERGE` - Update related entities when parent is updated
- `REMOVE` - Delete related entities when parent is deleted (⚠️ be careful!)
- `ALL` - All cascade operations (⚠️ use sparingly!)

**When to use @ManyToMany:**
- Both entities can exist independently
- Many-to-many relationship in business logic
- Need a join table to link them

**Example in ToursManager:**
- A Guide can speak many Languages
- A Language can be spoken by many Guides
- Join table `guide_languages` links them
- Languages are reference data (shouldn't be deleted with Guide)

**REQUIRED**: Yes, to define entity relationships.

---

#### @Id
Marks the primary key field

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**@GeneratedValue strategies:**
- `IDENTITY` - Database auto-increments (PostgreSQL SERIAL, MySQL AUTO_INCREMENT)
- `AUTO` - Let JPA choose
- `SEQUENCE` - Use database sequence
- `TABLE` - Use a separate table for IDs

**REQUIRED**: Yes, every entity needs exactly one @Id.

---

#### @Column
Configures how a field maps to a database column

```java
@Column(nullable = false, unique = true, length = 100)
private String email;
```

**Options:**
- `nullable = false` - Database NOT NULL constraint
- `unique = true` - Database UNIQUE constraint
- `length = 100` - VARCHAR(100)
- `name = "email_address"` - Custom column name

**REQUIRED**: No, JPA uses sensible defaults. Use it for:
- Constraints (nullable, unique)
- Custom column names
- Size limits

---

#### @PrePersist, @PreUpdate
Lifecycle callbacks - run before saving to database

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**When they run:**
- `@PrePersist` - Before INSERT (creating new record)
- `@PreUpdate` - Before UPDATE (modifying existing record)

**REQUIRED**: No, but useful for automatic timestamps.

---

### Validation Annotations (Jakarta Bean Validation)

```java
@NotBlank(message = "First name is required")
@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
@Email(message = "Email must be valid")
```

**Common annotations:**
- `@NotNull` - Field cannot be null
- `@NotBlank` - String cannot be null or empty (trims whitespace)
- `@Size(min, max)` - Length constraints
- `@Email` - Valid email format
- `@Min(value)` - Minimum numeric value
- `@Max(value)` - Maximum numeric value
- `@Pattern(regexp)` - Must match regex

**When validation happens:**
When you use `@Valid` in a controller:
```java
@PostMapping
public ResponseEntity<Guide> createGuide(@Valid @RequestBody Guide guide) {
    // If validation fails, Spring throws MethodArgumentNotValidException
    // GlobalExceptionHandler catches it and returns error response
}
```

**REQUIRED**: No, but highly recommended for data integrity.

---

### REST Controller Annotations

#### @RestController
Marks a class as a REST API controller

```java
@RestController
@RequestMapping("/api/guides")
public class GuideController {
    // Methods handle HTTP requests
}
```

**@RestController = @Controller + @ResponseBody**
- Returns data (JSON) instead of view names (HTML pages)

**REQUIRED**: Yes, for REST APIs. Use @Controller for traditional MVC (returning HTML views).

---

#### @RequestMapping, @GetMapping, @PostMapping, etc.

Map HTTP requests to methods

```java
@RequestMapping("/api/guides")  // Base path for all methods in this controller
public class GuideController {
    
    @GetMapping  // GET /api/guides
    public List<Guide> getAllGuides() { }
    
    @GetMapping("/{id}")  // GET /api/guides/123
    public Guide getById(@PathVariable Long id) { }
    
    @PostMapping  // POST /api/guides
    public Guide create(@RequestBody Guide guide) { }
    
    @PutMapping("/{id}")  // PUT /api/guides/123
    public Guide update(@PathVariable Long id, @RequestBody Guide guide) { }
    
    @DeleteMapping("/{id}")  // DELETE /api/guides/123
    public void delete(@PathVariable Long id) { }
    
    @PatchMapping("/{id}/activate")  // PATCH /api/guides/123/activate
    public void activate(@PathVariable Long id) { }
}
```

**HTTP Method conventions:**
- `GET` - Read/retrieve data (safe, idempotent)
- `POST` - Create new resource
- `PUT` - Update entire resource (replace)
- `PATCH` - Partial update
- `DELETE` - Remove resource

**REQUIRED**: Yes, to map URLs to methods.

---

#### @PathVariable, @RequestParam, @RequestBody

Extract data from HTTP requests

```java
// URL: /api/guides/123
@GetMapping("/{id}")
public Guide getById(@PathVariable Long id) {
    // id = 123
}

// URL: /api/guides?active=true&search=John
@GetMapping
public List<Guide> search(
    @RequestParam(required = false) Boolean active,  // active = true
    @RequestParam(required = false) String search    // search = "John"
) { }

// Request body: { "firstName": "John", "lastName": "Doe" }
@PostMapping
public Guide create(@RequestBody Guide guide) {
    // guide object populated from JSON
}
```

**@PathVariable** - from URL path (`/guides/{id}`)
**@RequestParam** - from query string (`?active=true`)
**@RequestBody** - from request body (JSON → Java object)

**REQUIRED**: Yes, to access request data.

---

### OpenAPI/Swagger Annotations (Documentation)

```java
@Tag(name = "Guides", description = "Guide management APIs")
public class GuideController {
    
    @Operation(summary = "Get all guides", description = "Retrieve guides with filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public List<Guide> getAllGuides(
        @Parameter(description = "Filter by active status") 
        @RequestParam(required = false) Boolean active
    ) { }
}
```

**Purpose:**
- Generate interactive API documentation (Swagger UI)
- Describe what endpoints do
- Document parameters and responses

**REQUIRED**: No, purely for documentation. Your API works without them.

---

## 4. Architecture: Layers Explained

### The "Spring Layered Architecture"

```
Request → Controller → Service → Repository → Database
         ↓           ↓          ↓
       Response   Business   Data Access
                   Logic
```

### Why Layers?

#### **Controller Layer** (REST API endpoints)
**Job:** Handle HTTP requests/responses
**Does:**
- Map URLs to methods
- Extract data from requests
- Call service layer
- Return responses

**Does NOT:**
- Business logic
- Database access
- Complex validation

**Example:**
```java
@RestController
@RequestMapping("/api/guides")
public class GuideController {
    private final GuideService service;
    
    @PostMapping
    public ResponseEntity<Guide> create(@Valid @RequestBody Guide guide) {
        Guide created = service.createGuide(guide);  // Delegate to service
        return ResponseEntity.status(201).body(created);
    }
}
```

---

#### **Service Layer** (Business logic)
**Job:** Implement business rules
**Does:**
- Coordinate multiple repository calls
- Implement business logic (e.g., "email must be unique")
- Transaction management
- Data transformation

**Does NOT:**
- Handle HTTP requests
- Direct database queries (uses repository)

**Example:**
```java
@Service
public class GuideService {
    private final GuideRepository repository;
    
    public Guide createGuide(Guide guide) {
        // Business rule: email must be unique
        if (repository.findByEmail(guide.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        return repository.save(guide);
    }
}
```

---

#### **Repository Layer** (Database access)
**Job:** Abstract database operations
**Does:**
- CRUD operations (Create, Read, Update, Delete)
- Custom queries
- Data persistence

**Does NOT:**
- Business logic
- HTTP handling

**Example:**
```java
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    // Spring generates implementation automatically!
    Optional<Guide> findByEmail(String email);
    List<Guide> findByActive(Boolean active);
}
```

---

#### **Why not just put everything in the controller?**

**Bad (all in controller):**
```java
@RestController
public class GuideController {
    @Autowired
    private EntityManager em;  // Direct database access
    
    @PostMapping("/api/guides")
    public Guide create(@RequestBody Guide guide) {
        // Business logic in controller - BAD!
        List<Guide> existing = em.createQuery("SELECT g FROM Guide g WHERE g.email = :email")
            .setParameter("email", guide.getEmail())
            .getResultList();
        
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Email exists");
        }
        
        em.persist(guide);
        return guide;
    }
}
```

**Problems:**
- Can't reuse business logic elsewhere
- Hard to test (need full HTTP stack)
- Mixed responsibilities
- Hard to maintain

**Good (layered):**
```java
// Controller - handles HTTP
@RestController
public class GuideController {
    private final GuideService service;
    
    @PostMapping("/api/guides")
    public Guide create(@RequestBody Guide guide) {
        return service.createGuide(guide);
    }
}

// Service - business logic
@Service
public class GuideService {
    private final GuideRepository repository;
    
    public Guide createGuide(Guide guide) {
        if (repository.findByEmail(guide.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email exists");
        }
        return repository.save(guide);
    }
}

// Repository - database
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    Optional<Guide> findByEmail(String email);
}
```

**Benefits:**
- Easy to test (mock each layer)
- Reusable (service can be called from different controllers)
- Clear responsibilities
- Easy to maintain

---

## 5. How Spring Works (Dependency Injection & IoC Container)

### The Spring Container (Application Context)

Think of Spring as a "smart factory" that creates and manages your objects.

**Without Spring:**
```java
public class Main {
    public static void main(String[] args) {
        GuideRepository repository = new GuideRepository();
        GuideService service = new GuideService(repository);
        GuideController controller = new GuideController(service);
        
        // You manually create everything!
    }
}
```

**With Spring:**
```java
@SpringBootApplication
public class ToursManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToursManagerApplication.class, args);
        // Spring creates everything automatically!
    }
}
```

### How does Spring know what to create?

**Step 1:** Spring scans for annotations
```java
@Service
public class GuideService { }  // Spring finds this

@Repository
public interface GuideRepository { }  // Spring finds this

@RestController
public class GuideController { }  // Spring finds this
```

**Step 2:** Spring creates beans (managed objects)
```
Spring Container:
├── guideRepository (instance of GuideRepository)
├── guideService (instance of GuideService)
└── guideController (instance of GuideController)
```

**Step 3:** Spring injects dependencies
```java
@Service
public class GuideService {
    private final GuideRepository repository;
    
    public GuideService(GuideRepository repository) {
        // Spring looks in container, finds guideRepository bean, injects it
        this.repository = repository;
    }
}
```

### Benefits of Dependency Injection

1. **Loose Coupling**
   ```java
   // Interface
   public interface NotificationService {
       void send(String message);
   }
   
   // Implementation 1
   @Service
   public class EmailNotificationService implements NotificationService {
       public void send(String message) { /* send email */ }
   }
   
   // Implementation 2
   @Service
   public class SmsNotificationService implements NotificationService {
       public void send(String message) { /* send SMS */ }
   }
   
   // Usage - easy to swap implementations!
   @Service
   public class GuideService {
       private final NotificationService notificationService;
       
       public GuideService(NotificationService notificationService) {
           this.notificationService = notificationService;  // Spring injects one
       }
   }
   ```

2. **Testability**
   ```java
   // Easy to test with mocks
   @Test
   public void testCreateGuide() {
       GuideRepository mockRepo = mock(GuideRepository.class);
       GuideService service = new GuideService(mockRepo);
       
       // Test with mock, no database needed!
   }
   ```

3. **Lifecycle Management**
   - Spring creates beans when needed
   - Reuses same instance (singleton by default)
   - Cleans up when application shuts down

---

## 6. Your Guide Entity - Line by Line

Let's examine every line of `Guide.java`:

```java
package com.innatour.toursmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity  // JPA: This is a database entity
@Table(name = "guides")  // JPA: Table name (optional, defaults to class name)
public class Guide {
    
    @Id  // JPA: Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;
    
    @NotBlank(message = "First name is required")  // Validation: cannot be empty
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)  // JPA: NOT NULL, VARCHAR(50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")  // Validation: email format
    @Column(nullable = false, unique = true, length = 100)  // UNIQUE constraint
    private String email;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(length = 20)  // Optional field (nullable = true by default)
    private String phoneNumber;
    
    @Size(max = 500, message = "Profile cannot exceed 500 characters")
    @Column(length = 500)
    private String profile;
    
    @Size(max = 200, message = "Languages cannot exceed 200 characters")
    @Column(length = 200)
    private String languages;
    
    @Column(nullable = false)
    private Boolean active = true;  // Default value
    
    @Column(nullable = false, updatable = false)  // Cannot be updated after creation
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist  // JPA lifecycle: runs before INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate  // JPA lifecycle: runs before UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Guide() {  // JPA requires no-arg constructor
    }
    
    public Guide(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and Setters
    // JPA needs these to access fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    // ... (rest of getters/setters)
    
    @Override
    public String toString() {
        return "Guide{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profile='" + profile + '\'' +
                ", languages='" + languages + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
```

**Required elements:**
- `@Entity` - Yes
- `@Id` - Yes
- No-arg constructor - Yes (JPA requirement)
- Getters/setters - Yes (JPA uses them)

**Optional elements:**
- `@Table` - No (defaults to class name)
- `@Column` - No (sensible defaults)
- Validation annotations - No (but recommended)
- `@PrePersist/@PreUpdate` - No (but useful)
- Constructor with parameters - No (but convenient)
- `toString()` - No (but helpful for debugging)

---

## 7. Repository Layer

```java
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    Optional<Guide> findByEmail(String email);
    
    List<Guide> findByActive(Boolean active);
    
    List<Guide> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    List<Guide> findByLanguagesContainingIgnoreCase(String language);
}
```

### Spring Data JPA Magic

**You write:**
- An interface (no implementation!)
- Method names following conventions

**Spring generates:**
- Full implementation at runtime
- SQL queries based on method names

### Method Naming Conventions

**Pattern:** `findBy[Field][Operation][Keyword]`

**Examples:**
- `findByEmail(String email)` → `WHERE email = ?`
- `findByActive(Boolean active)` → `WHERE active = ?`
- `findByEmailAndActive(String email, Boolean active)` → `WHERE email = ? AND active = ?`
- `findByFirstNameContaining(String name)` → `WHERE first_name LIKE %?%`
- `findByFirstNameContainingIgnoreCase(String name)` → `WHERE LOWER(first_name) LIKE LOWER(%?%)`

**Keywords:**
- `And`, `Or` - Combine conditions
- `Containing` - LIKE %value%
- `StartingWith` - LIKE value%
- `EndingWith` - LIKE %value
- `IgnoreCase` - Case-insensitive
- `GreaterThan`, `LessThan` - Comparisons
- `Between` - Range
- `OrderBy[Field][Asc|Desc]` - Sorting

### JpaRepository Methods (Inherited)

You get these for free:
```java
// CRUD operations
save(Guide guide) - INSERT or UPDATE
findById(Long id) - SELECT by ID
findAll() - SELECT all
delete(Guide guide) - DELETE
deleteById(Long id) - DELETE by ID
count() - COUNT

// Batch operations
saveAll(List<Guide> guides) - Bulk INSERT/UPDATE
deleteAll() - DELETE all
```

### Custom Queries (Advanced)

If method names get too complex, use `@Query`:
```java
@Query("SELECT g FROM Guide g WHERE g.email = :email AND g.active = true")
Optional<Guide> findActiveByEmail(@Param("email") String email);

// Native SQL
@Query(value = "SELECT * FROM guides WHERE email = ?1", nativeQuery = true)
Optional<Guide> findByEmailNative(String email);
```

**Required:**
- Extend `JpaRepository<Entity, IdType>` - Yes
- `@Repository` annotation - No (but recommended for clarity)

---

## 8. Service Layer

```java
@Service
public class GuideService {
    
    private final GuideRepository guideRepository;
    
    // Constructor injection
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }
    
    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }
    
    public List<Guide> getActiveGuides() {
        return guideRepository.findByActive(true);
    }
    
    public Optional<Guide> getGuideById(Long id) {
        return guideRepository.findById(id);
    }
    
    public Optional<Guide> getGuideByEmail(String email) {
        return guideRepository.findByEmail(email);
    }
    
    public List<Guide> searchGuides(String searchTerm) {
        return guideRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }
    
    public List<Guide> searchGuidesByLanguage(String language) {
        return guideRepository.findByLanguagesContainingIgnoreCase(language);
    }
    
    public Guide createGuide(Guide guide) {
        // Business rule: email must be unique
        if (guideRepository.findByEmail(guide.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + guide.getEmail());
        }
        return guideRepository.save(guide);
    }
    
    public Guide updateGuide(Long id, Guide guideDetails) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        
        // Business rule: if email changed, new email must be unique
        if (!guide.getEmail().equals(guideDetails.getEmail())) {
            if (guideRepository.findByEmail(guideDetails.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + guideDetails.getEmail());
            }
        }
        
        guide.setFirstName(guideDetails.getFirstName());
        guide.setLastName(guideDetails.getLastName());
        guide.setEmail(guideDetails.getEmail());
        guide.setPhoneNumber(guideDetails.getPhoneNumber());
        guide.setProfile(guideDetails.getProfile());
        guide.setLanguages(guideDetails.getLanguages());
        guide.setActive(guideDetails.getActive());
        
        return guideRepository.save(guide);
    }
    
    public void deleteGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guideRepository.delete(guide);
    }
    
    public void deactivateGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guide.setActive(false);
        guideRepository.save(guide);
    }
    
    public void activateGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with id: " + id));
        guide.setActive(true);
        guideRepository.save(guide);
    }
}
```

**Why Service Layer?**
- Business logic in one place
- Reusable (can be called from multiple controllers, scheduled tasks, etc.)
- Transactional (wrap multiple operations in a transaction)
- Testable (easy to unit test without HTTP)

**@Transactional (Advanced):**
```java
@Transactional
public void transferGuide(Long fromId, Long toId) {
    // Multiple database operations in one transaction
    // If any fails, all roll back
    Guide from = guideRepository.findById(fromId).orElseThrow();
    Guide to = guideRepository.findById(toId).orElseThrow();
    
    from.setActive(false);
    guideRepository.save(from);
    
    to.setActive(true);
    guideRepository.save(to);
    
    // Either both succeed or both fail
}
```

**Required:**
- `@Service` - Yes (so Spring manages it)
- Service layer itself - No, but best practice

---

## 9. Controller Layer

See the GuideController in your project. Key points:

**Job:**
- Map HTTP requests to methods
- Extract request data
- Call service layer
- Return HTTP responses

**Does NOT contain:**
- Database queries
- Business logic
- Complex validation

**ResponseEntity:**
```java
// Different status codes
return ResponseEntity.ok(guide);                    // 200 OK
return ResponseEntity.status(HttpStatus.CREATED)    // 201 Created
    .body(guide);
return ResponseEntity.noContent().build();          // 204 No Content
return ResponseEntity.notFound().build();           // 404 Not Found
return ResponseEntity.badRequest().body(error);     // 400 Bad Request
```

---

## 10. Configuration Classes

### OpenApiConfig.java

```java
@Configuration  // Spring: This class provides configuration
public class OpenApiConfig {
    
    @Bean  // Spring: Create and manage this object
    public OpenAPI toursManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tours Manager API")
                        .description("REST API for Tours Manager")
                        .version("1.0.0"));
    }
}
```

**@Configuration** - Provides Spring beans
**@Bean** - Method that returns an object for Spring to manage

**Required:** No, but useful for customizing Spring Boot behavior.

---

## 11. Exception Handling

### GlobalExceptionHandler.java

```java
@RestControllerAdvice  // Global exception handler for all @RestController
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)  // Catch this exception type
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        // Extract field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        // Handle business logic errors
        // ...
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        // Catch-all for unexpected errors
        // ...
    }
}
```

**How it works:**
1. Exception thrown in controller or service
2. Spring catches it
3. Looks for @ExceptionHandler method that matches exception type
4. Calls that method
5. Returns the ResponseEntity

**Required:** No, but highly recommended for consistent error handling.

---

## 12. What's Required vs Optional

### Must Have (App won't work without these)

**Application:**
- `@SpringBootApplication` on main class
- `main()` method calling `SpringApplication.run()`

**Entity:**
- `@Entity`
- `@Id`
- No-arg constructor
- Getters/setters

**Repository:**
- Extend `JpaRepository<Entity, IdType>`

**Controller:**
- `@RestController` or `@Controller`
- `@RequestMapping` or `@GetMapping` etc. on methods

**Configuration:**
- `application.properties` with database settings

### Good to Have (Best practices, cleaner code)

**Architecture:**
- Service layer (`@Service`)
- Repository interface (`@Repository`)
- Separate DTO classes

**Validation:**
- `@Valid` in controllers
- Validation annotations in entities

**Error Handling:**
- `@RestControllerAdvice`
- Custom error responses

**Documentation:**
- OpenAPI annotations
- JavaDoc comments

### Nice to Have (Optional enhancements)

- `@PrePersist/@PreUpdate` for timestamps
- `@Transactional` for complex operations
- Custom queries with `@Query`
- Pagination and sorting
- Caching
- Security (Spring Security)

---

## Summary: Learning Path

**Level 1 - Understand:**
1. What each layer does (Controller, Service, Repository)
2. Basic annotations (@RestController, @Service, @Repository, @Entity)
3. How dependency injection works (constructor injection)

**Level 2 - Master:**
1. JPA annotations (@Id, @Column, @PrePersist)
2. Validation annotations (@NotBlank, @Email, @Size)
3. Repository method naming conventions
4. Request mapping (@GetMapping, @PostMapping, @PathVariable, @RequestBody)

**Level 3 - Advanced:**
1. @Transactional for complex operations
2. Custom queries with @Query
3. Exception handling with @RestControllerAdvice
4. Configuration with @Configuration and @Bean

---

## Next Steps

1. **Read this guide** thoroughly
2. **Examine your code** - match each annotation to this guide
3. **Experiment** - change things, see what breaks, understand why
4. **Add features** - try creating a Tour entity using the same patterns
5. **Ask questions** - focus on one concept at a time

Remember: You don't need to memorize everything. Understanding the concepts and knowing where to look is more important!

---

**Spring Boot Official Docs:** https://docs.spring.io/spring-boot/index.html
**Spring Data JPA:** https://docs.spring.io/spring-data/jpa/reference/
**Jakarta Bean Validation:** https://beanvalidation.org/
