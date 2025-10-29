package com.innatour.toursmanager.controller;

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
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Guide.class)))
    })
    @GetMapping
    public ResponseEntity<List<Guide>> getAllGuides(
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Search by first or last name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by language spoken") @RequestParam(required = false) String language) {
        
        if (language != null && !language.trim().isEmpty()) {
            return ResponseEntity.ok(guideService.searchGuidesByLanguage(language));
        }
        
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(guideService.searchGuides(search));
        }
        
        if (active != null && active) {
            return ResponseEntity.ok(guideService.getActiveGuides());
        }
        
        return ResponseEntity.ok(guideService.getAllGuides());
    }
    
    // GET guide by ID
    @Operation(summary = "Get guide by ID", description = "Retrieve a specific guide by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Guide.class))),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Guide> getGuideById(
            @Parameter(description = "Guide ID") @PathVariable Long id) {
        return guideService.getGuideById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // CREATE new guide
    @Operation(summary = "Register a new guide", description = "Create a new tour guide profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Guide successfully registered",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Guide.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Guide> createGuide(
            @Parameter(description = "Guide details") @Valid @RequestBody Guide guide) {
        // Validation errors are handled automatically by GlobalExceptionHandler
        // IllegalArgumentException (e.g., duplicate email) is also handled automatically
        Guide createdGuide = guideService.createGuide(guide);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGuide);
    }
    
    // UPDATE existing guide
    @Operation(summary = "Update guide", description = "Update an existing guide's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide successfully updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Guide.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(responseCode = "404", description = "Guide not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Guide> updateGuide(
            @Parameter(description = "Guide ID") @PathVariable Long id, 
            @Valid @RequestBody Guide guide) {
        // Errors handled automatically by GlobalExceptionHandler
        Guide updatedGuide = guideService.updateGuide(id, guide);
        return ResponseEntity.ok(updatedGuide);
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
