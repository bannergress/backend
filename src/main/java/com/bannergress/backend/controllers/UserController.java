package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.UserDto;
import com.bannergress.backend.entities.User;
import com.bannergress.backend.exceptions.VerificationStateException;
import com.bannergress.backend.services.UserMappingService;
import com.bannergress.backend.services.UserService;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

/**
 * REST endpoint for users.
 */
@RestController
@Validated
@PreAuthorize("isAuthenticated()")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMappingService userMappingService;

    @GetMapping("/user")
    public UserDto get(Principal principal) {
        String userId = checkPrincipal(principal);
        User user = userService.getOrCreate(userId);
        UserDto result = new UserDto();
        result.verificationAgent = user.getVerificationAgent();
        result.verificationToken = user.getVerificationToken();
        result.agent = userMappingService.getAgentName(userId).orElse(null);
        return result;
    }

    @PostMapping("/user/claim")
    public UserDto claim(Principal principal, @RequestParam String agent) {
        String userId = checkPrincipal(principal);
        userService.claim(userId, agent);
        return get(principal);
    }

    @PostMapping("/user/verify")
    public UserDto verify(Principal principal) throws VerificationStateException {
        String userId = checkPrincipal(principal);
        userService.verify(userId);
        return get(principal);
    }

    private String checkPrincipal(Principal principal) {
        if (principal instanceof KeycloakPrincipal) {
            return principal.getName();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
