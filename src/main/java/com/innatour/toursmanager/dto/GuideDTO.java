package com.innatour.toursmanager.dto;

import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.model.Language;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Guide entity
 * Converts between entity (with Language objects) and API format (with language code strings)
 */
public class GuideDTO {
    
    private Long id;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 500, message = "Profile cannot exceed 500 characters")
    private String profile;
    
    private String languages; // Comma-separated language codes (e.g., "en,es,fr")
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public GuideDTO() {
    }
    
    /**
     * Convert Guide entity to DTO (for API responses)
     */
    public static GuideDTO fromEntity(Guide guide) {
        GuideDTO dto = new GuideDTO();
        dto.setId(guide.getId());
        dto.setFirstName(guide.getFirstName());
        dto.setLastName(guide.getLastName());
        dto.setEmail(guide.getEmail());
        dto.setPhoneNumber(guide.getPhoneNumber());
        dto.setProfile(guide.getProfile());
        dto.setActive(guide.getActive());
        dto.setCreatedAt(guide.getCreatedAt());
        dto.setUpdatedAt(guide.getUpdatedAt());
        
        // Convert Set<Language> to comma-separated string of codes
        if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
            String languageCodes = guide.getLanguages().stream()
                    .map(Language::getCode)
                    .collect(Collectors.joining(","));
            dto.setLanguages(languageCodes);
        }
        
        return dto;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public void setProfile(String profile) {
        this.profile = profile;
    }
    
    public String getLanguages() {
        return languages;
    }
    
    public void setLanguages(String languages) {
        this.languages = languages;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
