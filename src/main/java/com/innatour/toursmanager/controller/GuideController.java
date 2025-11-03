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
    
    // CREATE new guide
    @Operation(summary = "Register a new guide", description = "Create a new tour guide profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Guide successfully registered",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GuideDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GuideDTO> createGuide(
            @Parameter(description = "Guide details with comma-separated language codes") @Valid @RequestBody GuideDTO guideDTO) {
        // Create Guide entity from DTO
        Guide guide = new Guide();
        guide.setFirstName(guideDTO.getFirstName());
        guide.setLastName(guideDTO.getLastName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setProfile(guideDTO.getProfile());
        guide.setActive(guideDTO.getActive() != null ? guideDTO.getActive() : true);
        
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
}
