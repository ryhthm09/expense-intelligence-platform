package com.expenseintelligence.controller;

import com.expenseintelligence.annotations.CurrentUser;
import com.expenseintelligence.security.UserPrincipal;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only probe endpoint for verifying {@link com.expenseintelligence.annotations.CurrentUser}
 * resolution. Active only under the {@code test} profile.
 */
@RestController
@Profile("test")
public class CurrentUserProbeController {

    @GetMapping("/api/v1/_test/current-user-id")
    public String currentUserId(@CurrentUser UserPrincipal user) {
        return user.getId().toString();
    }
}
