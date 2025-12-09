using System.ComponentModel.DataAnnotations;

namespace ToursManager.Api.DTOs;

/// <summary>
/// Register Request DTO - used when a new guide registers.
/// Similar to GuideDto but includes password.
/// </summary>
public class RegisterRequest
{
    [Required(ErrorMessage = "First name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "First name must be between 2 and 50 characters")]
    public string FirstName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Last name is required")]
    [StringLength(50, MinimumLength = 2, ErrorMessage = "Last name must be between 2 and 50 characters")]
    public string LastName { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Email is required")]
    [EmailAddress(ErrorMessage = "Email must be valid")]
    public string Email { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Password is required")]
    [StringLength(100, MinimumLength = 6, ErrorMessage = "Password must be at least 6 characters")]
    public string Password { get; set; } = string.Empty;
    
    [StringLength(20, ErrorMessage = "Phone number cannot exceed 20 characters")]
    public string? PhoneNumber { get; set; }
    
    [StringLength(500, ErrorMessage = "Profile cannot exceed 500 characters")]
    public string? Profile { get; set; }
    
    /// <summary>
    /// Comma-separated language codes
    /// </summary>
    public string? Languages { get; set; }
    
    public bool Active { get; set; } = true;
}
