package com.innatour.toursmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for partial updates to Guide entities.
 * All fields are optional - only non-null fields will be updated.
 */
@Schema(description = "Guide partial update request - only send fields you want to change")
public class GuideUpdateDTO {
    
    @Schema(description = "First name", example = "John")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "Phone number with country code", example = "+1234567890")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid (E.164 format)")
    private String phoneNumber;
    
    @Schema(description = "Guide biography/profile description", example = "Experienced guide with 10 years in wildlife tours")
    @Size(max = 1000, message = "Profile must not exceed 1000 characters")
    private String profile;
    
    @Schema(description = "Comma-separated language codes (ISO 639-1)", example = "en,es,fr")
    private String languages;
    
    @Schema(description = "Active status", example = "true")
    private Boolean active;
    
    // Constructors
    public GuideUpdateDTO() {
    }
    
    // Getters and Setters
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
}
