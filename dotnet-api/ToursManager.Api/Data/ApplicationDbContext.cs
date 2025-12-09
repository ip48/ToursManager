using Microsoft.EntityFrameworkCore;
using ToursManager.Api.Models;

namespace ToursManager.Api.Data;

/// <summary>
/// Database context for the ToursManager application.
/// Equivalent to JPA's EntityManager - manages entity instances and database operations.
/// </summary>
public class ApplicationDbContext : DbContext
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options)
    {
    }
    
    // DbSet properties (equivalent to JPA repositories)
    public DbSet<Guide> Guides { get; set; } = null!;
    public DbSet<Language> Languages { get; set; } = null!;
    
    /// <summary>
    /// Configure entity relationships and constraints.
    /// Equivalent to JPA annotations and @JoinTable configuration.
    /// </summary>
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        
        // Configure Guide entity
        modelBuilder.Entity<Guide>(entity =>
        {
            // Primary key
            entity.HasKey(g => g.Id);
            
            // Unique constraint on email
            entity.HasIndex(g => g.Email).IsUnique();
            
            // Configure snake_case column names to match PostgreSQL convention
            entity.Property(g => g.FirstName).HasColumnName("first_name");
            entity.Property(g => g.LastName).HasColumnName("last_name");
            entity.Property(g => g.Email).HasColumnName("email");
            entity.Property(g => g.Password).HasColumnName("password");
            entity.Property(g => g.PhoneNumber).HasColumnName("phone_number");
            entity.Property(g => g.Profile).HasColumnName("profile");
            entity.Property(g => g.Active).HasColumnName("active");
            entity.Property(g => g.CreatedAt).HasColumnName("created_at");
            entity.Property(g => g.UpdatedAt).HasColumnName("updated_at");
            
            // Configure many-to-many relationship with Language
            entity.HasMany(g => g.Languages)
                  .WithMany(l => l.Guides)
                  .UsingEntity<Dictionary<string, object>>(
                      "guide_languages",  // Join table name
                      j => j.HasOne<Language>().WithMany().HasForeignKey("language_id"),
                      j => j.HasOne<Guide>().WithMany().HasForeignKey("guide_id")
                  );
        });
        
        // Configure Language entity
        modelBuilder.Entity<Language>(entity =>
        {
            // Primary key
            entity.HasKey(l => l.Id);
            
            // Unique constraint on code
            entity.HasIndex(l => l.Code).IsUnique();
            
            // Configure snake_case column names
            entity.Property(l => l.Code).HasColumnName("code");
            entity.Property(l => l.Name).HasColumnName("name");
        });
    }
    
    /// <summary>
    /// Override SaveChanges to automatically update timestamps.
    /// Equivalent to JPA's @PrePersist and @PreUpdate lifecycle callbacks.
    /// </summary>
    public override int SaveChanges()
    {
        UpdateTimestamps();
        return base.SaveChanges();
    }
    
    /// <summary>
    /// Override SaveChangesAsync to automatically update timestamps.
    /// </summary>
    public override Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        UpdateTimestamps();
        return base.SaveChangesAsync(cancellationToken);
    }
    
    /// <summary>
    /// Automatically set CreatedAt and UpdatedAt timestamps.
    /// </summary>
    private void UpdateTimestamps()
    {
        var entries = ChangeTracker.Entries()
            .Where(e => e.Entity is Guide && (e.State == EntityState.Added || e.State == EntityState.Modified));
        
        foreach (var entry in entries)
        {
            var guide = (Guide)entry.Entity;
            
            if (entry.State == EntityState.Added)
            {
                guide.OnCreate();  // Set both CreatedAt and UpdatedAt
            }
            else if (entry.State == EntityState.Modified)
            {
                guide.OnUpdate();  // Update only UpdatedAt
            }
        }
    }
}
