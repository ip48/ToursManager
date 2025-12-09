using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using ToursManager.Api.DTOs;
using ToursManager.Api.Services;

namespace ToursManager.Api.Controllers;

/// <summary>
/// Guides Controller - CRUD operations for guides.
/// Equivalent to Spring Boot's GuideController.
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class GuidesController : ControllerBase
{
    private readonly GuideService _guideService;
    private readonly ILogger<GuidesController> _logger;
    
    public GuidesController(GuideService guideService, ILogger<GuidesController> logger)
    {
        _guideService = guideService;
        _logger = logger;
    }
    
    /// <summary>
    /// Get all guides
    /// GET /api/guides
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(IEnumerable<GuideDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<GuideDto>>> GetAllGuides()
    {
        var guides = await _guideService.GetAllAsync();
        var guideDtos = guides.Select(GuideService.ToDto);
        return Ok(guideDtos);
    }
    
    /// <summary>
    /// Get guide by ID
    /// GET /api/guides/{id}
    /// </summary>
    [HttpGet("{id}")]
    [ProducesResponseType(typeof(GuideDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<GuideDto>> GetGuideById(long id)
    {
        var guide = await _guideService.GetByIdAsync(id);
        if (guide == null)
        {
            return NotFound(new { message = $"Guide with ID {id} not found" });
        }
        
        return Ok(GuideService.ToDto(guide));
    }
    
    /// <summary>
    /// Get authenticated user's profile
    /// GET /api/guides/profile
    /// Requires authentication (JWT token)
    /// </summary>
    [HttpGet("profile")]
    [Authorize]  // Equivalent to Spring Security's @PreAuthorize
    [ProducesResponseType(typeof(GuideDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<GuideDto>> GetMyProfile()
    {
        // Get email from JWT token claims
        var email = User.FindFirst(ClaimTypes.Email)?.Value;
        if (string.IsNullOrEmpty(email))
        {
            return Unauthorized(new { message = "Invalid token" });
        }
        
        var guide = await _guideService.GetByEmailAsync(email);
        if (guide == null)
        {
            return NotFound(new { message = "Guide not found" });
        }
        
        return Ok(GuideService.ToDto(guide));
    }
    
    /// <summary>
    /// Partial update authenticated user's profile (PATCH)
    /// PATCH /api/guides/profile
    /// Only updates fields that are provided (non-null)
    /// </summary>
    [HttpPatch("profile")]
    [Authorize]
    [ProducesResponseType(typeof(GuideDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<GuideDto>> PartialUpdateMyProfile([FromBody] GuideUpdateDto updateDto)
    {
        // Get email from JWT token
        var email = User.FindFirst(ClaimTypes.Email)?.Value;
        if (string.IsNullOrEmpty(email))
        {
            return Unauthorized(new { message = "Invalid token" });
        }
        
        var updatedGuide = await _guideService.PartialUpdateByEmailAsync(email, updateDto);
        if (updatedGuide == null)
        {
            return NotFound(new { message = "Guide not found" });
        }
        
        return Ok(GuideService.ToDto(updatedGuide));
    }
    
    /// <summary>
    /// Full update authenticated user's profile (PUT)
    /// PUT /api/guides/profile
    /// Requires all fields to be provided
    /// </summary>
    [HttpPut("profile")]
    [Authorize]
    [ProducesResponseType(typeof(GuideDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<GuideDto>> UpdateMyProfile([FromBody] GuideDto guideDto)
    {
        // Get email from JWT token
        var email = User.FindFirst(ClaimTypes.Email)?.Value;
        if (string.IsNullOrEmpty(email))
        {
            return Unauthorized(new { message = "Invalid token" });
        }
        
        var guide = await _guideService.GetByEmailAsync(email);
        if (guide == null)
        {
            return NotFound(new { message = "Guide not found" });
        }
        
        // Update all fields
        guide.FirstName = guideDto.FirstName;
        guide.LastName = guideDto.LastName;
        guide.Email = guideDto.Email;
        guide.PhoneNumber = guideDto.PhoneNumber;
        guide.Profile = guideDto.Profile;
        guide.Active = guideDto.Active;
        
        // Update languages if provided
        if (!string.IsNullOrWhiteSpace(guideDto.Languages))
        {
            var codes = guideDto.Languages.Split(',', StringSplitOptions.RemoveEmptyEntries)
                .Select(c => c.Trim().ToLower())
                .ToList();
            
            var languages = await _guideService.GetLanguagesByCodesAsync(codes);
            guide.Languages = languages;
        }
        
        var updatedGuide = await _guideService.UpdateAsync(guide);
        return Ok(GuideService.ToDto(updatedGuide));
    }
    
    /// <summary>
    /// Delete guide by ID
    /// DELETE /api/guides/{id}
    /// </summary>
    [HttpDelete("{id}")]
    [Authorize]  // In production, add admin role check
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteGuide(long id)
    {
        var deleted = await _guideService.DeleteAsync(id);
        if (!deleted)
        {
            return NotFound(new { message = $"Guide with ID {id} not found" });
        }
        
        return NoContent();
    }
}
