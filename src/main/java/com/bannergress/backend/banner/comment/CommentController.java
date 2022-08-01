package com.bannergress.backend.banner.comment;

import com.bannergress.backend.agent.AgentService;
import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerService;
import com.bannergress.backend.dto.CommentDto;
import com.bannergress.backend.mission.MissionController;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.user.UserMappingService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** REST endpoint for comments. */
@RestController
@Validated
@PreAuthorize("isAuthenticated()")
public class CommentController {
    @Autowired
    private UserMappingService userMappingService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private CommentService commentService;

    /**
     * Lists all comments of a banner.
     *
     * @param bannerId Banner ID.
     * @return Comments of the banner.
     */
    @GetMapping(value = "/bnrs/{bannerId}/comments")
    public List<CommentDto> list(@PathVariable final String bannerId) {
        Banner banner = getBanner(bannerId);
        return banner.getComments().stream() //
            .sorted(Comparator.comparing(Comment::getCreated).reversed()) //
            .map(this::toDetails) //
            .collect(Collectors.toList());
    }

    /**
     * Create / updates a comment for a banner.
     *
     * @param bannerId Banner ID.
     * @param comment  Comment to create / update.
     * @return Updated comment.
     */
    @RolesAllowed(Roles.CREATE_COMMENT)
    @PutMapping("/bnrs/{bannerId}/comments/{uuid}")
    public CommentDto put(@PathVariable String bannerId, @PathVariable UUID uuid,
                          @Valid @RequestBody CommentDto comment) {
        Banner banner = getBanner(bannerId);
        return toDetails(commentService.createOrUpdate(uuid, banner, comment));
    }

    /**
     * Deletes a comment for a banner.
     *
     * @param bannerId Banner ID.
     * @param id       Comment ID to delete.
     */
    @RolesAllowed(Roles.MANAGE_COMMENTS)
    @DeleteMapping("/bnrs/{bannerId}/comments/{uuid}")
    public void delete(@PathVariable String bannerId, @PathVariable UUID uuid) {
        commentService.delete(uuid);
    }

    private Banner getBanner(String bannerId) {
        return bannerService.findBySlug(bannerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private CommentDto toDetails(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.ratingAccessibility = comment.getRatingAccessibility();
        dto.ratingRoundTheClock = comment.getRatingRoundTheClock();
        dto.comment = comment.getComment();
        dto.created = comment.getCreated();
        dto.ratingOverall = comment.getRatingOverall();
        dto.ratingPassphrases = comment.getRatingPassphrases();
        dto.type = comment.getType();
        dto.author = userMappingService.getAgentName(comment.getUser().getId()) //
            .map(agentName -> agentService.importAgent(agentName, null)) //
            .map(MissionController::toAgentSummary) //
            .orElse(null);
        dto.uuid = comment.getUuid();
        return dto;
    }
}
