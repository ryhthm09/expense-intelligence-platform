package com.expenseintelligence.security;

import com.expenseintelligence.controller.CurrentUserProbeController;
import com.expenseintelligence.domain.entity.User;
import com.expenseintelligence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CurrentUserProbeController.class)
class CurrentUserIntegrationTest {

    private static final String PROBE_URL = "/api/v1/_test/current-user-id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savedUser = userRepository.save(User.builder()
                .email("integration@example.com")
                .password("encoded-password")
                .firstName("Integration")
                .lastName("Test")
                .enabled(true)
                .build());
    }

    @Test
    void protectedProbeEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(PROBE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedProbeEndpoint_withInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(PROBE_URL)
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedProbeEndpoint_withValidToken_resolvesCurrentUser() throws Exception {
        String token = jwtTokenProvider.generateToken(new UserPrincipal(savedUser));

        mockMvc.perform(get(PROBE_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(savedUser.getId().toString()));
    }
}
