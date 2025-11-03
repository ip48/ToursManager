package com.innatour.toursmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "languages")
public class Language {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Language code is required")
    @Size(min = 2, max = 3, message = "Language code must be 2-3 characters (ISO 639-1)")
    @Column(nullable = false, unique = true, length = 3)
    private String code; // ISO 639-1 code (e.g., "en", "es", "fr")
    
    @NotBlank(message = "Language name is required")
    @Size(max = 50, message = "Language name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String name; // Display name (e.g., "English", "Spanish")
    
    // Constructors
    public Language() {
    }
    
    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "Language{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
