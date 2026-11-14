package com.bannergress.backend.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** API contracts deliberately separate author/public views from privileged moderation data. */
public final class CommentModels {
    private CommentModels() {
    }

    public enum State { PUBLISHED, PENDING, REMOVED, REJECTED, WITHDRAWN }
    public enum Mode { POSTMODERATION, PREMODERATION }
    public enum Action { KEEP, APPROVE, REMOVE, REJECT, RESTORE }

    public record Create(@NotNull UUID requestId, @NotBlank @Size(max = 2000) String text) {
    }

    public record Decision(@NotNull UUID requestId, @Min(0) long expectedVersion,
        @NotNull Action action, @NotBlank @Size(max = 200) String reason, @Min(1) long moderatorId) {
    }

    public record ChangeMode(@NotNull UUID requestId, @Min(0) long expectedVersion,
        @NotNull Mode mode, @NotBlank @Size(max = 200) String reason, @Min(1) long moderatorId) {
    }

    public record Capabilities(boolean canComment, String denialReason, boolean testExemption,
        Mode mode, int effectiveStrikes, String environment) {
    }

    public record View(UUID id, UUID bannerId, String text, String agentName,
        Instant createdAt, State state, long version, String reason) {
    }

    public record Page(List<View> items, long total, int offset, int limit) {
    }

    public record ModerationView(UUID id, UUID authorReference, UUID bannerId, String bannerTitle,
        String bannerUrl, boolean bannerExists, String text, String authorLabel, boolean ingressVerified,
        Instant createdAt, State state, boolean reviewed, String reason, long version,
        Mode authorMode, long authorVersion, int effectiveStrikes, String environment) {
    }

    public record Audit(UUID id, UUID commentId, UUID authorReference, Long moderatorId,
        String action, String reason, String previousState, String nextState, Instant createdAt) {
    }

    public record Delivery(UUID id, UUID commentId, UUID authorReference, String eventType,
        String environment, UUID leaseToken, Instant leaseUntil, int attempts) {
    }

    public record Claim(@Min(1) @Max(50) int limit) {
    }

    public record Acknowledge(@NotNull UUID leaseToken,
        @NotNull @Size(min = 1, max = 100) List<@NotBlank @Size(max = 80) String> telegramMessageIds) {
    }

    public record Retry(@NotNull UUID leaseToken) {
    }

    public record QueueStats(long queued, long dead, Instant oldestQueuedAt) {
    }
}
