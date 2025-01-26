package com.bannergress.backend.user;

import com.bannergress.backend.agent.AgentService;
import com.bannergress.backend.mission.MissionController;
import com.bannergress.backend.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

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

    @Value("${verification.template:%s}")
    private String verificationTemplate;

    @GetMapping("/user")
    public UserDto get(Principal principal) {
        String userId = principal.getName();
        User user = userService.getOrCreate(userId);
        UserDto result = new UserDto();
        result.verificationAgent = user.getVerificationAgent();
        result.verificationToken = user.getVerificationToken();
        result.verificationMessage = user.getVerificationToken() == null ? null
            : String.format(verificationTemplate, user.getVerificationToken());
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
    @RolesAllowed(Roles.VERIFY_USERS)
    public void verify(@RequestParam String agent, @RequestParam UUID token) {
        userService.attemptVerification(agent, token);
    }

    @PostMapping("/user/unlink")
    public UserDto unlink(Principal principal) {
        String userId = principal.getName();
        userService.unlink(userId);
        return get(principal);
    }
}
