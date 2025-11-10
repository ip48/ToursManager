package com.innatour.toursmanager.controller;

import com.innatour.toursmanager.dto.GuideDTO;
import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.service.GuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guides")
@CrossOrigin(origins = "*") // Configure properly for production
@Tag(name = "Guides", description = "Guide management APIs - Register and manage tour guides")
public class GuideController {
    
    private final GuideService guideService;
    
    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }
    
    // GET all guides
    @Operation(summary = "Get all guides", description = "Retrieve all guides with optional filtering by active status, name search, or language")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved guides",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<GuideDTO>> getAllGuides(
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Search by first or last name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by language code (e.g., 'en', 'es')") @RequestParam(required = false) String language) {
        
        List<Guide> guides;
        
        if (language != null && !language.trim().isEmpty()) {
            guides = guideService.searchGuidesByLanguage(language);
        } else if (search != null && !search.trim().isEmpty()) {
            guides = guideService.searchGuides(search);
        } else if (active != null && active) {
            guides = guideService.getActiveGuides();
        } else {
            guides = guideService.getAllGuides();
        }
        
        // Convert to DTOs for API response
        List<GuideDTO> guideDTOs = guides.stream()
                .map(GuideDTO::fromEntity)
                .toList();
        
        return ResponseEntity.ok(guideDTOs);
    }
    
    // GET guide by ID
    @Operation(summary = "Get guide by ID", description = "Retrieve a specific guide by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<GuideDTO> getGuideById(
            @Parameter(description = "Guide ID") @PathVariable Long id) {
        return guideService.getGuideById(id)
                .map(GuideDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // CREATE new guide (DEPRECATED - use /api/auth/register for new guides with authentication)
    @Deprecated
    @Operation(summary = "Register a new guide (deprecated)", 
               description = "Create a new tour guide profile WITHOUT password. Use /api/auth/register for new guides with authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Guide successfully registered",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GuideDTO> createGuide(
            @Parameter(description = "Guide details with comma-separated language codes") @Valid @RequestBody GuideDTO guideDTO) {
        // NOTE: This endpoint creates guides without passwords (for backward compatibility)
        // New guides should use /api/auth/register which includes password
        
        // Create Guide entity from DTO
        Guide guide = new Guide();
        guide.setFirstName(guideDTO.getFirstName());
        guide.setLastName(guideDTO.getLastName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setProfile(guideDTO.getProfile());
        guide.setActive(guideDTO.getActive() != null ? guideDTO.getActive() : true);
        // Password is null - existing guides without passwords can't login
        
        // Create guide with language codes
        Guide createdGuide = guideService.createGuide(guide, guideDTO.getLanguages());
        
        // Convert back to DTO for response
        return ResponseEntity.status(HttpStatus.CREATED).body(GuideDTO.fromEntity(createdGuide));
    }
    
    // UPDATE existing guide
    @Operation(summary = "Update guide", description = "Update an existing guide's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide successfully updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<GuideDTO> updateGuide(
            @Parameter(description = "Guide ID") @PathVariable Long id, 
            @Valid @RequestBody GuideDTO guideDTO) {
        // Create Guide entity from DTO
        Guide guide = new Guide();
        guide.setFirstName(guideDTO.getFirstName());
        guide.setLastName(guideDTO.getLastName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setProfile(guideDTO.getProfile());
        guide.setActive(guideDTO.getActive() != null ? guideDTO.getActive() : true);
        
        // Update guide with language codes
        Guide updatedGuide = guideService.updateGuide(id, guide, guideDTO.getLanguages());
        
        // Convert back to DTO for response
        return ResponseEntity.ok(GuideDTO.fromEntity(updatedGuide));
    }
    
    // DELETE guide
    @Operation(summary = "Delete guide", description = "Permanently delete a guide")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Guide successfully deleted", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuide(
            @Parameter(description = "Guide ID") @PathVariable Long id) {
        // Errors handled automatically by GlobalExceptionHandler
        guideService.deleteGuide(id);
        return ResponseEntity.noContent().build();
    }
    
    // DEACTIVATE guide (soft delete)
    @Operation(summary = "Deactivate guide", description = "Mark a guide as inactive (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Guide deactivated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateGuide(
            @Parameter(description = "Guide ID") @PathVariable Long id) {
        guideService.deactivateGuide(id);
        return ResponseEntity.noContent().build();
    }
    
    // ACTIVATE guide
    @Operation(summary = "Activate guide", description = "Mark a guide as active")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Guide activated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateGuide(
            @Parameter(description = "Guide ID") @PathVariable Long id) {
        guideService.activateGuide(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET guide by EMAIL (for guide self-service)
    @Operation(summary = "Get guide by email", description = "Retrieve a guide by their email address (for profile editing)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @GetMapping("/by-email/{email}")
    public ResponseEntity<GuideDTO> getGuideByEmail(
            @Parameter(description = "Guide email address") @PathVariable String email) {
        return guideService.getGuideByEmail(email)
                .map(GuideDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // UPDATE guide by EMAIL (for guide self-service)
    @Operation(summary = "Update guide by email", description = "Update a guide's profile using their email (for self-service editing)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide successfully updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PutMapping("/by-email/{email}")
    public ResponseEntity<GuideDTO> updateGuideByEmail(
            @Parameter(description = "Current email address") @PathVariable String email,
            @Valid @RequestBody GuideDTO guideDTO) {
        // Create Guide entity from DTO
        Guide guide = new Guide();
        guide.setFirstName(guideDTO.getFirstName());
        guide.setLastName(guideDTO.getLastName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setProfile(guideDTO.getProfile());
        guide.setActive(guideDTO.getActive() != null ? guideDTO.getActive() : true);
        
        // Update guide with language codes
        Guide updatedGuide = guideService.updateGuideByEmail(email, guide, guideDTO.getLanguages());
        
        // Convert back to DTO for response
        return ResponseEntity.ok(GuideDTO.fromEntity(updatedGuide));
    }
    
    // GET current user's profile (AUTHENTICATED)
    @Operation(summary = "Get my profile", description = "Get the currently authenticated guide's profile (requires JWT token)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @GetMapping("/profile")
    public ResponseEntity<GuideDTO> getMyProfile() {
        // Get email from Spring Security context (set by JWT filter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return guideService.getGuideByEmail(email)
                .map(GuideDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // UPDATE current user's profile (AUTHENTICATED)
    @Operation(summary = "Update my profile", description = "Update the currently authenticated guide's profile (requires JWT token)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PutMapping("/profile")
    public ResponseEntity<GuideDTO> updateMyProfile(@Valid @RequestBody GuideDTO guideDTO) {
        // Get email from Spring Security context (set by JWT filter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Create Guide entity from DTO
        Guide guide = new Guide();
        guide.setFirstName(guideDTO.getFirstName());
        guide.setLastName(guideDTO.getLastName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setProfile(guideDTO.getProfile());
        guide.setActive(guideDTO.getActive() != null ? guideDTO.getActive() : true);
        
        // Update guide with language codes (using current email from JWT)
        Guide updatedGuide = guideService.updateGuideByEmail(email, guide, guideDTO.getLanguages());
        
        // Convert back to DTO for response
        return ResponseEntity.ok(GuideDTO.fromEntity(updatedGuide));
    }
    
    // PATCH - Partial update current user's profile (AUTHENTICATED)
    @Operation(summary = "Partially update my profile", 
               description = "Update only specific fields of your profile. Send only the fields you want to change. (requires JWT token)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile partially updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PatchMapping("/profile")
    public ResponseEntity<GuideDTO> partialUpdateMyProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Only include fields you want to update. Example: {\"languages\": \"en,es,fr\"}",
                content = @Content(schema = @Schema(implementation = com.innatour.toursmanager.dto.GuideUpdateDTO.class))
            )
            @Valid @RequestBody com.innatour.toursmanager.dto.GuideUpdateDTO updateDTO) {
        // Get email from Spring Security context (set by JWT filter)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Partial update - only non-null fields are updated
        Guide updatedGuide = guideService.partialUpdateGuideByEmail(
            email,
            updateDTO.getFirstName(),
            updateDTO.getLastName(),
            updateDTO.getEmail(),
            updateDTO.getPhoneNumber(),
            updateDTO.getProfile(),
            updateDTO.getLanguages(),
            updateDTO.getActive()
        );
        
        // Convert back to DTO for response
        return ResponseEntity.ok(GuideDTO.fromEntity(updatedGuide));
    }
}
