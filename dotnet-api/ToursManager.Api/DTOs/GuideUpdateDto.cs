using System.ComponentModel.DataAnnotations;

namespace ToursManager.Api.DTOs;

/// <summary>
/// DTO for partial updates to Guide entities.
/// All fields are optional - only non-null fields will be updated.
/// </summary>
public class GuideUpdateDto
{
    /// <summary>
    /// First name
    /// </summary>
    /// <example>John</example>
    [StringLength(50, MinimumLength = 2, ErrorMessage = "First name must be between 2 and 50 characters")]
    public string? FirstName { get; set; }
    
    /// <summary>
    /// Last name
    /// </summary>
    /// <example>Doe</example>
    [StringLength(50, MinimumLength = 2, ErrorMessage = "Last name must be between 2 and 50 characters")]
    public string? LastName { get; set; }
    
    /// <summary>
    /// Email address
    /// </summary>
    /// <example>john.doe@example.com</example>
    [EmailAddress(ErrorMessage = "Email should be valid")]
    public string? Email { get; set; }
    
    /// <summary>
    /// Phone number with country code
    /// </summary>
    /// <example>+1234567890</example>
    [RegularExpression(@"^\+?[1-9]\d{1,14}$", ErrorMessage = "Phone number should be valid (E.164 format)")]
    public string? PhoneNumber { get; set; }
    
    /// <summary>
    /// Guide biography/profile description
    /// </summary>
    /// <example>Experienced guide with 10 years in wildlife tours</example>
    [StringLength(1000, ErrorMessage = "Profile must not exceed 1000 characters")]
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
    public bool? Active { get; set; }
}
