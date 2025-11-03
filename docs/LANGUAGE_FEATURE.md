# Language Feature - Technical Documentation

## Overview

The Guide entity supports multiple languages through a proper relational database model. Languages are stored as ISO 639-1 codes (2-letter standard codes like "en", "es", "fr") in a separate table with a many-to-many relationship.

## Database Schema

```sql
-- Reference table with ISO 639-1 codes
CREATE TABLE languages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) UNIQUE NOT NULL,  -- ISO 639-1 code
    name VARCHAR(50) NOT NULL         -- Display name
);

-- Many-to-many join table
CREATE TABLE guide_languages (
    guide_id BIGINT REFERENCES guides(id),
    language_id BIGINT REFERENCES languages(id),
    PRIMARY KEY (guide_id, language_id)
);
```

## Backend Implementation

### Entity Relationship
```java
// Guide.java
@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "guide_languages",
    joinColumns = @JoinColumn(name = "guide_id"),
    inverseJoinColumns = @JoinColumn(name = "language_id")
)
private Set<Language> languages = new HashSet<>();
```

**Key Points:**
- `FetchType.EAGER` - Languages loaded immediately with Guide (always needed for display)
- `CascadeType.PERSIST, MERGE` - Only these cascades (NOT REMOVE - languages are shared)
- `Set<Language>` - No duplicate languages per guide

### API Compatibility Layer

The API still uses comma-separated strings for backward compatibility:

```java
// GuideDTO.java - Converts between entity and API format
public static GuideDTO fromEntity(Guide guide) {
    // Convert Set<Language> → "en,es,fr"
    String languageCodes = guide.getLanguages().stream()
            .map(Language::getCode)
            .collect(Collectors.joining(","));
    dto.setLanguages(languageCodes);
    return dto;
}
```

### Service Layer Conversion

```java
// GuideService.java
private Set<Language> convertLanguageCodesToEntities(String languageCodes) {
    // "en,es,fr" → Set<Language>
    Set<String> codes = Arrays.stream(languageCodes.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
    
    Set<Language> languages = languageRepository.findByCodeIn(codes);
    
    // Validate all codes exist
    if (languages.size() != codes.size()) {
        throw new IllegalArgumentException("Invalid language codes: ...");
    }
    
    return languages;
}
```

## Supported Languages

25 ISO 639-1 standard language codes pre-loaded on application startup:

| Code | Language | Code | Language | Code | Language |
|------|----------|------|----------|------|----------|
| en | English | es | Spanish | fr | French |
| de | German | it | Italian | pt | Portuguese |
| zh | Chinese | ja | Japanese | ko | Korean |
| ar | Arabic | he | Hebrew | ru | Russian |
| hi | Hindi | tr | Turkish | nl | Dutch |
| pl | Polish | sv | Swedish | no | Norwegian |
| da | Danish | fi | Finnish | cs | Czech |
| el | Greek | th | Thai | vi | Vietnamese |
| id | Indonesian |

**Data Initialization:**
```java
// DataInitializer.java - Runs once on startup
@Bean
CommandLineRunner initLanguages(LanguageRepository languageRepository) {
    return args -> {
        if (languageRepository.count() == 0) {
            languageRepository.save(new Language("en", "English"));
            // ... 24 more
        }
    };
}
```

## API Usage

### Create Guide with Languages
```bash
POST /api/guides
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "languages": "en,es,fr"
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "languages": "en,es,fr",
  "active": true,
  "createdAt": "2025-11-03T09:51:13.564090",
  "updatedAt": "2025-11-03T09:51:13.564124"
}
```

### Search Guides by Language
```bash
GET /api/guides?language=es
```

**Backend Query (efficient SQL join):**
```sql
SELECT DISTINCT g.* 
FROM guides g 
JOIN guide_languages gl ON g.id = gl.guide_id
JOIN languages l ON gl.language_id = l.id
WHERE LOWER(l.code) = LOWER('es')
```

### Validation

**Invalid language codes are rejected:**
```bash
POST /api/guides
{
  "firstName": "John",
  "email": "john@example.com",
  "languages": "en,invalid,fr"
}
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Invalid language codes: [invalid]",
  "path": "/api/guides",
  "timestamp": "2025-11-03T09:52:00"
}
```

## Frontend Integration

### TypeScript Types
```typescript
// types/Guide.ts
export interface GuideFormData {
  firstName: string;
  lastName: string;
  email: string;
  languages: string;  // Comma-separated: "en,es,fr"
}
```

### Language Constants
```typescript
// constants/languages.ts
export const LANGUAGES: Language[] = [
  { code: 'en', name: 'English' },
  { code: 'es', name: 'Spanish' },
  // ... 23 more
];

// Helper functions
export const languageCodesToString = (codes: string[]): string => {
  return codes.join(',');
};

export const stringToLanguageCodes = (str: string): string[] => {
  return str ? str.split(',').map(s => s.trim()).filter(Boolean) : [];
};
```

## Performance Considerations

### Before Migration (String field):
```sql
-- Slow LIKE query
SELECT * FROM guides WHERE languages LIKE '%es%';
-- Problems: No index, matches "test", "espresso", etc.
```

### After Migration (Relational):
```sql
-- Fast indexed join
SELECT DISTINCT g.* 
FROM guides g 
JOIN guide_languages gl ON g.id = gl.guide_id
JOIN languages l ON gl.language_id = l.id
WHERE l.code = 'es';
-- Benefits: Indexed, accurate, can count guides per language
```

## Future Extensions

The relational model makes it easy to add:

1. **Proficiency Levels:**
```java
@Entity
public class GuideLanguage {
    @ManyToOne Guide guide;
    @ManyToOne Language language;
    @Enumerated(EnumType.STRING)
    ProficiencyLevel level; // NATIVE, FLUENT, INTERMEDIATE, BASIC
}
```

2. **Certifications:**
```java
@Column
private LocalDate certificationDate;
@Column
private String certificationNumber;
```

3. **Statistics:**
```sql
-- How many guides speak each language?
SELECT l.name, COUNT(DISTINCT gl.guide_id) as guide_count
FROM languages l
LEFT JOIN guide_languages gl ON l.id = gl.language_id
GROUP BY l.id, l.name
ORDER BY guide_count DESC;
```

## Testing

See tests in:
- Integration test results in terminal output
- Swagger UI: http://localhost:8080/swagger-ui.html
- Manual curl tests documented in IMPLEMENTATION_SUMMARY.md

## Migration Notes

**What changed:**
- ✅ Database: String column → Two tables + join table
- ✅ Backend: String field → Set<Language>
- ✅ API: No changes (still uses comma-separated strings)
- ✅ Frontend: No changes required

**Data migration (if needed):**
If you had existing guides with language strings:
```java
// Convert "en,es,fr" → Set<Language> for each guide
guides.forEach(guide -> {
    String oldLangs = guide.getLanguagesOld();
    Set<Language> newLangs = convertLanguageCodesToEntities(oldLangs);
    guide.setLanguages(newLangs);
});
```

## See Also

- `docs/IMPLEMENTATION_SUMMARY.md` - Full feature timeline
- `docs/CODE_ORGANIZATION.md` - Frontend code structure
- `src/main/java/.../model/Language.java` - Entity source
- `frontend/src/constants/languages.ts` - Frontend constants
