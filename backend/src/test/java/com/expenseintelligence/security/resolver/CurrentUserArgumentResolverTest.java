package com.expenseintelligence.security.resolver;

import com.expenseintelligence.annotations.CurrentUser;
import com.expenseintelligence.domain.entity.User;
import com.expenseintelligence.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsParameter_withCurrentUserAndUserPrincipal_returnsTrue() throws Exception {
        MethodParameter parameter = methodParameter("withCurrentUser", UserPrincipal.class);

        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void supportsParameter_withoutAnnotation_returnsFalse() throws Exception {
        MethodParameter parameter = methodParameter("withoutAnnotation", UserPrincipal.class);

        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void supportsParameter_withAnnotationButWrongType_returnsFalse() throws Exception {
        MethodParameter parameter = methodParameter("withWrongType", String.class);

        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void resolveArgument_whenAuthenticated_returnsUserPrincipal() throws Exception {
        UserPrincipal principal = testPrincipal();
        setAuthentication(principal);

        MethodParameter parameter = methodParameter("withCurrentUser", UserPrincipal.class);
        Object resolved = resolver.resolveArgument(parameter, null, null, null);

        assertThat(resolved).isInstanceOf(UserPrincipal.class);
        assertThat(((UserPrincipal) resolved).getId()).isEqualTo(principal.getId());
        assertThat(((UserPrincipal) resolved).getEmail()).isEqualTo(principal.getEmail());
    }

    @Test
    void resolveArgument_whenNotAuthenticated_returnsNull() throws Exception {
        MethodParameter parameter = methodParameter("withCurrentUser", UserPrincipal.class);
        Object resolved = resolver.resolveArgument(parameter, null, null, null);

        assertThat(resolved).isNull();
    }

    @Test
    void resolveArgument_whenPrincipalIsNotUserPrincipal_returnsNull() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null));

        MethodParameter parameter = methodParameter("withCurrentUser", UserPrincipal.class);
        Object resolved = resolver.resolveArgument(parameter, null, null, null);

        assertThat(resolved).isNull();
    }

    private static UserPrincipal testPrincipal() {
        User user = User.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .email("resolver-test@example.com")
                .password("secret")
                .firstName("Resolver")
                .lastName("Test")
                .enabled(true)
                .build();
        return new UserPrincipal(user);
    }

    private static void setAuthentication(UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private static MethodParameter methodParameter(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return new MethodParameter(
                ProbeController.class.getDeclaredMethod(methodName, parameterTypes), 0);
    }

    @RestController
    @SuppressWarnings("unused")
    private static class ProbeController {

        @GetMapping
        void withCurrentUser(@CurrentUser UserPrincipal user) {
        }

        @GetMapping
        void withoutAnnotation(UserPrincipal user) {
        }

        @GetMapping
        void withWrongType(@CurrentUser String userId) {
        }
    }
}
