package com.soen345.meow.security;

import com.soen345.meow.Application;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(JwtAuthFilterTest.ProtectedTestController.class)
class JwtAuthFilterTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturn401WhenProtectedEndpointIsAccessedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateValidBearerTokenAndExposeAuthorities() throws Exception {
        User user = new User("admin@example.com", null, passwordEncoder.encode("password123"), "ADMIN");
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken("admin@example.com");

        mockMvc.perform(get("/api/admin/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.name").value("admin@example.com"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"));
    }

    @Test
    void shouldIgnoreMalformedBearerTokenAndStillProtectRoute() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/admin/test")
        public Map<String, Object> test(Authentication authentication) {
            List<String> authorities = authentication == null
                    ? List.of()
                    : authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

            Long userId = authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser
                    ? authenticatedUser.id()
                    : null;

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", authentication != null && authentication.isAuthenticated());
            response.put("name", authentication == null ? null : authentication.getName());
            response.put("userId", userId);
            response.put("authorities", authorities);
            return response;
        }
    }
}