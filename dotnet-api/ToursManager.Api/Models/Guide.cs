using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ToursManager.Api.Models;

/// <summary>
/// Represents a tour guide with their contact information and language skills
/// </summary>
[Table("guides")]
public class Guide
{
    [Key]
    [Column("id")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }
    
    [Required(ErrorMessage = "First name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "First name must be between 2 and 50 characters")]
    [Column("first_name", TypeName = "varchar(50)")]
    public string FirstName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Last name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "Last name must be between 2 and 50 characters")]
    [Column("last_name", TypeName = "varchar(50)")]
    public string LastName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Email is required")]
    [EmailAddress(ErrorMessage = "Email must be valid")]
    [StringLength(100)]
    [Column("email", TypeName = "varchar(100)")]
    public string Email { get; set; } = string.Empty;
    
    /// <summary>
    /// BCrypt hashed password. Nullable for existing guides (before authentication was added).
    /// New registrations via /api/auth/register will require password.
    /// </summary>
    [Column("password")]
    public string? Password { get; set; }
    
    [StringLength(20, ErrorMessage = "Phone number cannot exceed 20 characters")]
    [Column("phone_number", TypeName = "varchar(20)")]
    public string? PhoneNumber { get; set; }
    
    [StringLength(500, ErrorMessage = "Profile cannot exceed 500 characters")]
    [Column("profile", TypeName = "varchar(500)")]
    public string? Profile { get; set; }
    
    [Required]
    [Column("active")]
    public bool Active { get; set; } = true;
    
    [Required]
    [Column("created_at")]
    public DateTime CreatedAt { get; set; }
    
    [Required]
    [Column("updated_at")]
    public DateTime UpdatedAt { get; set; }
    
    // Navigation property - many-to-many with languages
    public ICollection<Language> Languages { get; set; } = new List<Language>();
    
    /// <summary>
    /// Called before entity is inserted (similar to @PrePersist in JPA)
    /// </summary>
    public void OnCreate()
    {
        CreatedAt = DateTime.UtcNow;
        UpdatedAt = DateTime.UtcNow;
    }
    
    /// <summary>
    /// Called before entity is updated (similar to @PreUpdate in JPA)
    /// </summary>
    public void OnUpdate()
    {
        UpdatedAt = DateTime.UtcNow;
    }
    
    public override string ToString()
    {
        return $"Guide{{Id={Id}, FirstName='{FirstName}', LastName='{LastName}', " +
               $"Email='{Email}', PhoneNumber='{PhoneNumber}', Profile='{Profile}', " +
               $"LanguagesCount={Languages?.Count ?? 0}, Active={Active}, " +
               $"CreatedAt={CreatedAt}, UpdatedAt={UpdatedAt}}}";
    }
}
