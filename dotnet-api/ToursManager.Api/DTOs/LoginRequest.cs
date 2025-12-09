using System.ComponentModel.DataAnnotations;

namespace ToursManager.Api.DTOs;

/// <summary>
/// Login Request DTO - used when a guide wants to log in
/// </summary>
public class LoginRequest
{
    [Required(ErrorMessage = "Email is required")]
    [EmailAddress(ErrorMessage = "Email must be valid")]
    public string Email { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Password is required")]
    public string Password { get; set; } = string.Empty;
}
