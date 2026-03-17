package com.soen345.meow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soen345.meow.dto.LoginRequest;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthLoginTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        String rawPassword = "password123";
        User user = new User("test@example.com", null, passwordEncoder.encode(rawPassword), "CUSTOMER");
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", rawPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void shouldFailLoginWithInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void shouldFailLoginWithInvalidPassword() throws Exception {
        User user = new User("test@example.com", null, passwordEncoder.encode("password123"), "CUSTOMER");
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void shouldReturnValidJWTTokenOnLogin() throws Exception {
        String rawPassword = "password123";
        User user = new User("test@example.com", null, passwordEncoder.encode(rawPassword), "CUSTOMER");
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", rawPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    String token = objectMapper.readTree(response).get("token").asText();
                    assertThat(token).contains(".");
                    long dotCount = token.chars().filter(c -> c == '.').count();
                    assertThat(dotCount).isEqualTo(2);
                });
    }

    @Test
    void shouldFailLoginWithoutEmail() throws Exception {
        LoginRequest request = new LoginRequest(null, "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    void shouldFailLoginWithoutPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is required"));
    }

    @Test
    void shouldNotReturnPasswordHashInResponse() throws Exception {
        String rawPassword = "password123";
        User user = new User("test@example.com", null, passwordEncoder.encode(rawPassword), "CUSTOMER");
        userRepository.save(user);

        LoginRequest request = new LoginRequest("test@example.com", rawPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertThat(response).doesNotContain("password");
                    assertThat(response).doesNotContain("passwordHash");
                });
    }

    @Test
    void shouldLoginWithPhoneInsteadOfEmail() throws Exception {
        String rawPassword = "password123";
        User user = new User(null, "5141234567", passwordEncoder.encode(rawPassword), "CUSTOMER");
        userRepository.save(user);

        LoginRequest request = new LoginRequest("5141234567", rawPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
