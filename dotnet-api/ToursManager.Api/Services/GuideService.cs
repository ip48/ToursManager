using Microsoft.EntityFrameworkCore;
using ToursManager.Api.Data;
using ToursManager.Api.DTOs;
using ToursManager.Api.Models;

namespace ToursManager.Api.Services;

/// <summary>
/// Service for Guide business logic.
/// Uses DbContext directly - idiomatic .NET approach (no separate Repository layer).
/// </summary>
public class GuideService
{
    private readonly ApplicationDbContext _context;
    private readonly ILogger<GuideService> _logger;
    
    public GuideService(ApplicationDbContext context, ILogger<GuideService> logger)
    {
        _context = context;
        _logger = logger;
    }
    
    /// <summary>
    /// Get guide by email.
    /// </summary>
    public async Task<Guide?> GetByEmailAsync(string email)
    {
        return await _context.Guides
            .Include(g => g.Languages)  // Eager load languages (like FetchType.EAGER in JPA)
            .FirstOrDefaultAsync(g => g.Email == email);
    }
    
    /// <summary>
    /// Get guide by ID.
    /// </summary>
    public async Task<Guide?> GetByIdAsync(long id)
    {
        return await _context.Guides
            .Include(g => g.Languages)
            .FirstOrDefaultAsync(g => g.Id == id);
    }
    
    /// <summary>
    /// Get all guides.
    /// </summary>
    public async Task<List<Guide>> GetAllAsync()
    {
        return await _context.Guides
            .Include(g => g.Languages)
            .ToListAsync();
    }
    
    /// <summary>
    /// Create a new guide.
    /// </summary>
    public async Task<Guide> CreateAsync(Guide guide)
    {
        _context.Guides.Add(guide);
        await _context.SaveChangesAsync();  // Triggers OnCreate() via SaveChanges override
        return guide;
    }
    
    /// <summary>
    /// Update guide with full replacement (PUT).
    /// </summary>
    public async Task<Guide> UpdateAsync(Guide guide)
    {
        _context.Guides.Update(guide);
        await _context.SaveChangesAsync();  // Triggers OnUpdate() via SaveChanges override
        return guide;
    }
    
    /// <summary>
    /// Partial update guide (PATCH) - only updates non-null fields.
    /// Equivalent to Spring Boot's partialUpdateGuideByEmail.
    /// </summary>
    public async Task<Guide?> PartialUpdateByEmailAsync(string email, GuideUpdateDto updateDto)
    {
        var guide = await GetByEmailAsync(email);
        if (guide == null)
        {
            return null;
        }
        
        // Only update fields that are provided (not null)
        if (updateDto.FirstName != null)
        {
            guide.FirstName = updateDto.FirstName;
        }
        
        if (updateDto.LastName != null)
        {
            guide.LastName = updateDto.LastName;
        }
        
        if (updateDto.Email != null)
        {
            guide.Email = updateDto.Email;
        }
        
        if (updateDto.PhoneNumber != null)
        {
            guide.PhoneNumber = updateDto.PhoneNumber;
        }
        
        if (updateDto.Profile != null)
        {
            guide.Profile = updateDto.Profile;
        }
        
        if (updateDto.Active.HasValue)
        {
            guide.Active = updateDto.Active.Value;
        }
        
        // Handle languages update
        if (updateDto.Languages != null)
        {
            await UpdateGuideLanguagesAsync(guide, updateDto.Languages);
        }
        
        await _context.SaveChangesAsync();
        return guide;
    }
    
    /// <summary>
    /// Update guide's languages from comma-separated codes.
    /// </summary>
    private async Task UpdateGuideLanguagesAsync(Guide guide, string languageCodes)
    {
        if (string.IsNullOrWhiteSpace(languageCodes))
        {
            guide.Languages.Clear();
            return;
        }
        
        var codes = languageCodes.Split(',', StringSplitOptions.RemoveEmptyEntries)
            .Select(c => c.Trim().ToLower())
            .ToList();
        
        // Get languages from database
        var languages = await _context.Languages
            .Where(l => codes.Contains(l.Code.ToLower()))
            .ToListAsync();
        
        // Clear existing and add new
        guide.Languages.Clear();
        foreach (var language in languages)
        {
            guide.Languages.Add(language);
        }
    }
    
    /// <summary>
    /// Delete guide.
    /// </summary>
    public async Task<bool> DeleteAsync(long id)
    {
        var guide = await _context.Guides.FindAsync(id);
        if (guide == null)
        {
            return false;
        }
        
        _context.Guides.Remove(guide);
        await _context.SaveChangesAsync();
        return true;
    }
    
    /// <summary>
    /// Check if email exists.
    /// </summary>
    public async Task<bool> EmailExistsAsync(string email)
    {
        return await _context.Guides.AnyAsync(g => g.Email == email);
    }
    
    /// <summary>
    /// Get languages by their codes.
    /// </summary>
    public async Task<List<Language>> GetLanguagesByCodesAsync(List<string> codes)
    {
        return await _context.Languages
            .Where(l => codes.Contains(l.Code.ToLower()))
            .ToListAsync();
    }
    
    /// <summary>
    /// Convert Guide entity to DTO.
    /// </summary>
    public static GuideDto ToDto(Guide guide)
    {
        return new GuideDto
        {
            Id = guide.Id,
            FirstName = guide.FirstName,
            LastName = guide.LastName,
            Email = guide.Email,
            PhoneNumber = guide.PhoneNumber,
            Profile = guide.Profile,
            Languages = guide.Languages.Any() 
                ? string.Join(",", guide.Languages.Select(l => l.Code))
                : null,
            Active = guide.Active,
            CreatedAt = guide.CreatedAt,
            UpdatedAt = guide.UpdatedAt
        };
    }
}
