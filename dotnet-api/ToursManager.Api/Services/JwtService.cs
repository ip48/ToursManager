using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace ToursManager.Api.Services;

/// <summary>
/// JWT Service - Generates and validates JWT tokens.
/// Equivalent to Spring Boot's JwtTokenProvider.
/// 
/// JWT Structure: header.payload.signature
/// - Header: algorithm + token type
/// - Payload: claims (user data)
/// - Signature: ensures token hasn't been tampered with
/// </summary>
public class JwtService
{
    private readonly IConfiguration _configuration;
    private readonly SymmetricSecurityKey _signingKey;
    
    public JwtService(IConfiguration configuration)
    {
        _configuration = configuration;
        
        // Get secret from configuration
        var secret = _configuration["Jwt:Secret"] 
            ?? throw new InvalidOperationException("JWT secret is not configured");
        
        // Create signing key from secret
        _signingKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
    }
    
    /// <summary>
    /// Generate JWT token from email.
    /// Token contains: email, issued time, expiration time.
    /// </summary>
    public string GenerateToken(string email)
    {
        var now = DateTime.UtcNow;
        var expirationHours = int.Parse(_configuration["Jwt:ExpirationHours"] ?? "24");
        var expiry = now.AddHours(expirationHours);
        
        var claims = new[]
        {
            new Claim(ClaimTypes.Email, email),
            new Claim(ClaimTypes.NameIdentifier, email),  // Use email as user identifier
            new Claim(JwtRegisteredClaimNames.Sub, email),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),  // Unique token ID
            new Claim(JwtRegisteredClaimNames.Iat, new DateTimeOffset(now).ToUnixTimeSeconds().ToString())
        };
        
        var credentials = new SigningCredentials(_signingKey, SecurityAlgorithms.HmacSha256);
        
        var token = new JwtSecurityToken(
            issuer: _configuration["Jwt:Issuer"],
            audience: _configuration["Jwt:Audience"],
            claims: claims,
            notBefore: now,
            expires: expiry,
            signingCredentials: credentials
        );
        
        return new JwtSecurityTokenHandler().WriteToken(token);
    }
    
    /// <summary>
    /// Extract email from JWT token.
    /// </summary>
    public string? GetEmailFromToken(string token)
    {
        try
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var jwtToken = tokenHandler.ReadJwtToken(token);
            
            // Try to get email from different claim types
            return jwtToken.Claims.FirstOrDefault(c => c.Type == ClaimTypes.Email)?.Value
                ?? jwtToken.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub)?.Value;
        }
        catch
        {
            return null;
        }
    }
    
    /// <summary>
    /// Validate JWT token.
    /// Returns true if token is valid, false otherwise.
    /// </summary>
    public bool ValidateToken(string token)
    {
        try
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var validationParameters = GetValidationParameters();
            
            tokenHandler.ValidateToken(token, validationParameters, out _);
            return true;
        }
        catch
        {
            // Token is invalid (expired, malformed, wrong signature, etc.)
            return false;
        }
    }
    
    /// <summary>
    /// Get token validation parameters.
    /// Used by both manual validation and ASP.NET Core authentication middleware.
    /// </summary>
    public TokenValidationParameters GetValidationParameters()
    {
        return new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = _signingKey,
            ValidateIssuer = true,
            ValidIssuer = _configuration["Jwt:Issuer"],
            ValidateAudience = true,
            ValidAudience = _configuration["Jwt:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero  // No tolerance for expiration time
        };
    }
}
