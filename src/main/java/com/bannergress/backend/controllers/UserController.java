package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.UserDto;
import com.bannergress.backend.entities.User;
import com.bannergress.backend.exceptions.VerificationFailedException;
import com.bannergress.backend.exceptions.VerificationStateException;
import com.bannergress.backend.services.AgentService;
import com.bannergress.backend.services.UserMappingService;
import com.bannergress.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AgentService agentService;

    @GetMapping("/user")
    public UserDto get(Principal principal) {
        String userId = principal.getName();
        User user = userService.getOrCreate(userId);
        UserDto result = new UserDto();
        result.verificationAgent = user.getVerificationAgent();
        result.verificationToken = user.getVerificationToken();
        result.agent = userMappingService.getAgentName(userId)
            .map(agentName -> MissionController.toAgentSummary(agentService.importAgent(agentName, null))).orElse(null);
        return result;
    }

    @PostMapping("/user/claim")
    public UserDto claim(Principal principal, @RequestParam String agent) {
        String userId = principal.getName();
        userService.claim(userId, agent);
        return get(principal);
    }

    @DeleteMapping("/user/claim")
    public UserDto clearClaim(Principal principal) {
        String userId = principal.getName();
        userService.clearClaim(userId);
        return get(principal);
    }

    @PostMapping("/user/verify")
    public UserDto verify(Principal principal) throws VerificationStateException, VerificationFailedException {
        String userId = principal.getName();
        userService.verify(userId);
        return get(principal);
    }

    @PostMapping("/user/unlink")
    public UserDto unlink(Principal principal) {
        String userId = principal.getName();
        userService.unlink(userId);
        return get(principal);
    }
}
