namespace ToursManager.Api.DTOs;

/// <summary>
/// JWT Response DTO - returned after successful login.
/// Contains the JWT token that the frontend will use for authenticated requests.
/// </summary>
public class JwtResponse
{
    public string Token { get; set; } = string.Empty;
    
    /// <summary>
    /// Token type (standard for JWT)
    /// </summary>
    public string Type { get; set; } = "Bearer";
    
    public string Email { get; set; } = string.Empty;
    
    public string FirstName { get; set; } = string.Empty;
    
    public string LastName { get; set; } = string.Empty;
    
    public JwtResponse(string token, string email, string firstName, string lastName)
    {
        Token = token;
        Email = email;
        FirstName = firstName;
        LastName = lastName;
    }
}
