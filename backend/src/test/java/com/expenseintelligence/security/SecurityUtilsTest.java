package com.expenseintelligence.security;

import com.expenseintelligence.domain.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_whenAuthenticated_returnsUserPrincipal() {
        UserPrincipal principal = testPrincipal();
        setAuthentication(principal);

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();

        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo(principal.getId());
        assertThat(currentUser.getEmail()).isEqualTo(principal.getEmail());
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_returnsNull() {
        assertThat(SecurityUtils.getCurrentUser()).isNull();
    }

    @Test
    void getCurrentUserId_whenAuthenticated_returnsUuid() {
        UserPrincipal principal = testPrincipal();
        setAuthentication(principal);

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(principal.getId());
    }

    @Test
    void getCurrentUserId_whenNotAuthenticated_returnsNull() {
        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }

    @Test
    void getCurrentUser_whenPrincipalIsNotUserPrincipal_returnsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null));

        assertThat(SecurityUtils.getCurrentUser()).isNull();
        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }

    private static UserPrincipal testPrincipal() {
        User user = User.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .email("security-utils@example.com")
                .password("secret")
                .firstName("Security")
                .lastName("Utils")
                .enabled(true)
                .build();
        return new UserPrincipal(user);
    }

    private static void setAuthentication(UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
