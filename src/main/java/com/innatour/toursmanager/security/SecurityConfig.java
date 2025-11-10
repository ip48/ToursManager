package com.innatour.toursmanager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration
 * 
 * This configures Spring Security for our application:
 * 1. Which endpoints require authentication
 * 2. How passwords are encrypted
 * 3. JWT filter integration
 * 4. CORS and CSRF settings
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Password encoder - BCrypt hashing
     * BCrypt automatically handles salting and is slow by design (protects against brute force)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager - used for login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Security filter chain - main security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(csrf -> csrf.disable())
                
                // Enable CORS (allow frontend to access API)
                .cors(cors -> cors.configure(http))  // Use default CORS configuration
                
                // Configure which endpoints require authentication
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers(
                                "/api/auth/**",           // Login and register
                                "/api/hello",             // Test endpoint
                                "/api/guides",            // View all guides (public)
                                "/api/guides/{id}",       // View specific guide (public)
                                "/api/guides/by-email/**", // View by email (public for now)
                                "/swagger-ui/**",         // Swagger UI (new path)
                                "/swagger-ui.html",       // Swagger UI (old path)
                                "/v3/api-docs/**",        // OpenAPI docs
                                "/swagger-resources/**",  // Swagger resources
                                "/webjars/**",            // Swagger webjars
                                "/error"                  // Error handling
                        ).permitAll()
                        
                        // Protected endpoints (require JWT authentication)
                        .requestMatchers("/api/guides/profile").authenticated()
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                
                // Stateless session (no server-side sessions - JWT is stateless)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * CORS configuration - allow frontend to access API
     * 
     * Development: Uses cors.allowed-origins from application-dev.properties (defaults to localhost:5173,3000)
     * Production: Set ALLOWED_ORIGINS environment variable with your domain(s)
     * Example: ALLOWED_ORIGINS=https://myapp.com,https://www.myapp.com
     * 
     * The property supports comma-separated list of origins.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Split comma-separated origins from Spring property (which reads from env var or default)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
