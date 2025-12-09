using Microsoft.AspNetCore.Mvc;
using ToursManager.Api.DTOs;
using ToursManager.Api.Models;
using ToursManager.Api.Services;

namespace ToursManager.Api.Controllers;

/// <summary>
/// Authentication Controller - handles register and login.
/// Equivalent to Spring Boot's @RestController.
/// </summary>
[ApiController]  // Enables automatic model validation and binding
[Route("api/[controller]")]  // Maps to /api/auth
public class AuthController : ControllerBase
{
    private readonly GuideService _guideService;
    private readonly JwtService _jwtService;
    private readonly ILogger<AuthController> _logger;
    
    public AuthController(
        GuideService guideService,
        JwtService jwtService,
        ILogger<AuthController> logger)
    {
        _guideService = guideService;
        _jwtService = jwtService;
        _logger = logger;
    }
    
    /// <summary>
    /// Register a new guide
    /// POST /api/auth/register
    /// </summary>
    /// <response code="201">Guide successfully registered</response>
    /// <response code="400">Email already exists or invalid input</response>
    [HttpPost("register")]
    [ProducesResponseType(typeof(JwtResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<JwtResponse>> Register([FromBody] RegisterRequest request)
    {
        // Check if email already exists
        if (await _guideService.EmailExistsAsync(request.Email))
        {
            return BadRequest(new { message = "Email already in use" });
        }
        
        // Create guide entity
        var guide = new Guide
        {
            FirstName = request.FirstName,
            LastName = request.LastName,
            Email = request.Email,
            PhoneNumber = request.PhoneNumber,
            Profile = request.Profile,
            Active = request.Active,
            // Hash password with BCrypt
            Password = BCrypt.Net.BCrypt.HashPassword(request.Password)
        };
        
        // Handle languages if provided
        if (!string.IsNullOrWhiteSpace(request.Languages))
        {
            var codes = request.Languages.Split(',', StringSplitOptions.RemoveEmptyEntries)
                .Select(c => c.Trim().ToLower())
                .ToList();
            
            var languages = await _guideService.GetLanguagesByCodesAsync(codes);
            guide.Languages = languages;
        }
        
        // Save guide
        var createdGuide = await _guideService.CreateAsync(guide);
        
        // Generate JWT token
        var token = _jwtService.GenerateToken(createdGuide.Email);
        
        // Return token and user info
        var response = new JwtResponse(
            token,
            createdGuide.Email,
            createdGuide.FirstName,
            createdGuide.LastName
        );
        
        return CreatedAtAction(nameof(Register), response);
    }
    
    /// <summary>
    /// Login - authenticate and receive JWT token
    /// POST /api/auth/login
    /// </summary>
    /// <response code="200">Login successful</response>
    /// <response code="401">Invalid credentials</response>
    [HttpPost("login")]
    [ProducesResponseType(typeof(JwtResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public async Task<ActionResult<JwtResponse>> Login([FromBody] LoginRequest request)
    {
        // Find guide by email
        var guide = await _guideService.GetByEmailAsync(request.Email);
        if (guide == null)
        {
            return Unauthorized(new { message = "Invalid email or password" });
        }
        
        // Verify password
        if (!BCrypt.Net.BCrypt.Verify(request.Password, guide.Password))
        {
            return Unauthorized(new { message = "Invalid email or password" });
        }
        
        // Generate JWT token
        var token = _jwtService.GenerateToken(guide.Email);
        
        // Return token and user info
        var response = new JwtResponse(
            token,
            guide.Email,
            guide.FirstName,
            guide.LastName
        );
        
        return Ok(response);
    }
}
