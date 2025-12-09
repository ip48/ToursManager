using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace ToursManager.Api.DTOs;

/// <summary>
/// Data Transfer Object for Guide entity.
/// Converts between entity (with Language objects) and API format (with language code strings).
/// </summary>
public class GuideDto
{
    /// <summary>
    /// Guide ID (read-only, auto-generated)
    /// </summary>
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingDefault)]
    public long Id { get; set; }
    
    [Required(ErrorMessage = "First name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "First name must be between 2 and 50 characters")]
    public string FirstName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Last name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "Last name must be between 2 and 50 characters")]
    public string LastName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Email is required")]
    [EmailAddress(ErrorMessage = "Email must be valid")]
    public string Email { get; set; } = string.Empty;
    
    [StringLength(20, ErrorMessage = "Phone number cannot exceed 20 characters")]
    public string? PhoneNumber { get; set; }
    
    [StringLength(500, ErrorMessage = "Profile cannot exceed 500 characters")]
    public string? Profile { get; set; }
    
    /// <summary>
    /// Comma-separated language codes (ISO 639-1)
    /// </summary>
    /// <example>en,es,fr</example>
    public string? Languages { get; set; }
    
    /// <summary>
    /// Active status
    /// </summary>
    /// <example>true</example>
    public bool Active { get; set; }
    
    /// <summary>
    /// Creation timestamp (read-only)
    /// </summary>
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingDefault)]
    public DateTime CreatedAt { get; set; }
    
    /// <summary>
    /// Last update timestamp (read-only)
    /// </summary>
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingDefault)]
    public DateTime UpdatedAt { get; set; }
}
