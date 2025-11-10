package com.innatour.toursmanager.controller;

import com.innatour.toursmanager.dto.JwtResponse;
import com.innatour.toursmanager.dto.LoginRequest;
import com.innatour.toursmanager.dto.RegisterRequest;
import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.repository.GuideRepository;
import com.innatour.toursmanager.security.JwtTokenProvider;
import com.innatour.toursmanager.service.GuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles:
 * - POST /api/auth/register - Register new guide
 * - POST /api/auth/login - Login and get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Guide authentication APIs - Register and login")
public class AuthController {
    
    private final GuideService guideService;
    private final GuideRepository guideRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthController(GuideService guideService, 
                         GuideRepository guideRepository,
                         PasswordEncoder passwordEncoder, 
                         JwtTokenProvider jwtTokenProvider) {
        this.guideService = guideService;
        this.guideRepository = guideRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Register a new guide
     * POST /api/auth/register
     */
    @Operation(summary = "Register new guide", description = "Create a new guide account with email and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Guide successfully registered",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "400", description = "Email already exists or invalid input", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if email already exists
        if (guideRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        
        // Create guide entity
        Guide guide = new Guide();
        guide.setFirstName(registerRequest.getFirstName());
        guide.setLastName(registerRequest.getLastName());
        guide.setEmail(registerRequest.getEmail());
        guide.setPhoneNumber(registerRequest.getPhoneNumber());
        guide.setProfile(registerRequest.getProfile());
        guide.setActive(registerRequest.getActive() != null ? registerRequest.getActive() : true);
        
        // Hash password with BCrypt
        guide.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        // Create guide with languages
        Guide createdGuide = guideService.createGuide(guide, registerRequest.getLanguages());
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(createdGuide.getEmail());
        
        // Return token and user info
        JwtResponse jwtResponse = new JwtResponse(
                token,
                createdGuide.getEmail(),
                createdGuide.getFirstName(),
                createdGuide.getLastName()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtResponse);
    }
    
    /**
     * Login
     * POST /api/auth/login
     */
    @Operation(summary = "Login", description = "Authenticate guide and receive JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Find guide by email
        Guide guide = guideRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), guide.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(guide.getEmail());
        
        // Return token and user info
        JwtResponse jwtResponse = new JwtResponse(
                token,
                guide.getEmail(),
                guide.getFirstName(),
                guide.getLastName()
        );
        
        return ResponseEntity.ok(jwtResponse);
    }
}
