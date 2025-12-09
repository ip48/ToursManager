using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ToursManager.Api.Models;

/// <summary>
/// Represents a language that guides can speak
/// </summary>
[Table("languages")]
public class Language
{
    [Key]
    [Column("id")]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }
    
    [Required(ErrorMessage = "Language code is required")]
    [StringLength(3, MinimumLength = 2, ErrorMessage = "Language code must be 2-3 characters (ISO 639-1)")]
    [Column("code", TypeName = "varchar(3)")]
    public string Code { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Language name is required")]
    [StringLength(50, ErrorMessage = "Language name cannot exceed 50 characters")]
    [Column("name", TypeName = "varchar(50)")]
    public string Name { get; set; } = string.Empty;
    
    // Navigation property - many-to-many with guides
    public ICollection<Guide> Guides { get; set; } = new List<Guide>();
    
    public override string ToString()
    {
        return $"Language{{Id={Id}, Code='{Code}', Name='{Name}'}}";
    }
}
