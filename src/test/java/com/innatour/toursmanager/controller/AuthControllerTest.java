package com.innatour.toursmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innatour.toursmanager.dto.RegisterRequest;
import com.innatour.toursmanager.model.Guide;
import com.innatour.toursmanager.repository.GuideRepository;
import com.innatour.toursmanager.security.JwtTokenProvider;
import com.innatour.toursmanager.service.GuideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;
    private GuideRepository guideRepository;
    private GuideService guideService;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        guideRepository = Mockito.mock(GuideRepository.class);
        guideService = Mockito.mock(GuideService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

        AuthController controller = new AuthController(guideService, guideRepository, passwordEncoder, jwtTokenProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @SuppressWarnings("null")  // Suppress null-safety warnings for test code
    public void registerReturnsJwtToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Alice");
        req.setLastName("Tester");
        req.setEmail("alice@example.com");
        req.setPassword("password123");

        when(guideRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        Guide created = new Guide();
        created.setId(1L);
        created.setEmail("alice@example.com");
        created.setFirstName("Alice");
        created.setLastName("Tester");

        when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
        when(guideService.createGuide(any(), any())).thenReturn(created);
        when(jwtTokenProvider.generateToken("alice@example.com")).thenReturn("test-jwt-token");

        String requestBody = mapper.writeValueAsString(req);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(guideService, times(1)).createGuide(any(), any());
    }
}
