package com.bannergress.backend.comment;

import com.bannergress.backend.user.UserMappingService;
import com.bannergress.backend.banner.Banner;
import jakarta.persistence.PersistenceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.bannergress.backend.comment.CommentModels.*;
import static org.springframework.http.HttpStatus.*;

/** Owns eligibility, publication and moderation transactions; Telegram is never the source of truth. */
@Service
@Transactional
public class CommentService {
    private final CommentStore store;
    private final CommentSettings settings;
    private final UserMappingService mappings;

    public CommentService(CommentStore store, CommentSettings settings, UserMappingService mappings) {
        this.store = store;
        this.settings = settings;
        this.mappings = mappings;
    }

    private Optional<String> verifiedAgent(String subject) {
        if (!settings.verifiedAttributeTrusted()) {
            return Optional.empty();
        }
        try {
            return mappings.getAgentName(subject).filter(s -> !s.isBlank());
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Verification temporarily unavailable");
        }
    }

    public Capabilities capabilities(String subject) {
        var author = store.lockAuthor(subject);
        if (!settings.enabled()) {
            return new Capabilities(false, "COMMENTS_DISABLED", false, author.mode(),
                store.strikes(author.id()), settings.environment());
        }
        boolean verified = verifiedAgent(subject).isPresent();
        boolean exempt = settings.allowUnlinkedTestUsers();
        String denial = verified || exempt ? null : "INGRESS_VERIFICATION_REQUIRED";
        return new Capabilities(denial == null, denial, exempt && !verified, author.mode(),
            store.strikes(author.id()), settings.environment());
    }

    public View create(String subject, String slug, Create request) {
        if (!settings.enabled()) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "New comments are disabled");
        }
        var author = store.lockAuthor(subject);
        String agent = verifiedAgent(subject).orElse(null);
        if (agent == null && !settings.allowUnlinkedTestUsers()) {
            throw new ResponseStatusException(FORBIDDEN, "Verified Ingress link required");
        }
        String text = request.text();
        if (text == null || text.isBlank() || text.length() > 2000 || text.indexOf(0) >= 0 || request.requestId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid comment");
        }
        var previous = store.entities().createQuery("from BannerComment where author = :author and submissionKey = :key", BannerComment.class)
            .setParameter("author", author.id()).setParameter("key", request.requestId()).getResultList();
        if (!previous.isEmpty()) {
            var comment = previous.getFirst();
            if (!comment.body().equals(text) || !sameSubmissionBanner(comment, slug)) {
                throw new ResponseStatusException(CONFLICT, "Submission key already used for another comment");
            }
            return view(comment);
        }
        var banner = banner(slug);
        Instant now = store.now();
        long minute = recentCount(author.id(), now.minus(1, ChronoUnit.MINUTES));
        long day = recentCount(author.id(), now.minus(1, ChronoUnit.DAYS));
        if (minute >= settings.maxPerMinute() || day >= settings.maxPerDay()) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS, "Comment rate limit reached");
        }
        var comment = new BannerComment();
        comment.createdAt = now;
        comment.bannerId = banner.id();
        comment.bannerReference = banner.id();
        comment.title = banner.title();
        comment.slug = banner.slug();
        comment.author = author.id();
        comment.agent = agent;
        comment.body = text;
        comment.state = author.mode() == Mode.PREMODERATION ? State.PENDING : State.PUBLISHED;
        comment.submissionKey = request.requestId();
        comment.submissionSlug = slug;
        store.entities().persist(comment);
        store.event(comment.id(), author.id(), "CREATED", settings.environment());
        return view(comment);
    }

    private boolean sameSubmissionBanner(BannerComment comment, String slug) {
        if (Objects.equals(comment.submissionSlug, slug)) {
            return true;
        }
        return store.entities().createQuery("select count(b) from Banner b join b.slugs s where s = :slug and b.uuid = :id", Long.class)
            .setParameter("slug", slug).setParameter("id", comment.bannerReference()).getSingleResult() > 0;
    }

    private long recentCount(UUID author, Instant since) {
        return store.entities().createQuery("select count(c) from BannerComment c where c.author = :author and c.createdAt > :since", Long.class)
            .setParameter("author", author).setParameter("since", since).getSingleResult();
    }

    record BannerInfo(UUID id, String title, String slug) {
    }

    private BannerInfo banner(String slug) {
        Banner banner = CommentStore.one(store.entities().createQuery("select b from Banner b join b.slugs s where s = :slug", Banner.class)
            .setParameter("slug", slug).getResultList());
        return new BannerInfo(banner.getUuid(), banner.getTitle(), banner.getCanonicalSlug());
    }

    @Transactional(readOnly = true)
    public Page published(String slug, int offset, int limit) {
        pageBounds(offset, limit);
        UUID banner = banner(slug).id();
        var comments = store.entities().createQuery("from BannerComment where bannerId = :banner and state = :state order by createdAt desc, id desc", BannerComment.class)
            .setParameter("banner", banner).setParameter("state", State.PUBLISHED)
            .setFirstResult(offset).setMaxResults(limit).getResultList().stream()
            .map(c -> new View(c.id(), c.bannerReference(), c.body(), c.agent(), c.createdAt(), c.state(), c.version(), null)).toList();
        long count = store.entities().createQuery("select count(c) from BannerComment c where c.bannerId = :banner and c.state = :state", Long.class)
            .setParameter("banner", banner).setParameter("state", State.PUBLISHED).getSingleResult();
        return new Page(comments, count, offset, limit);
    }

    @Transactional(readOnly = true)
    public Page mine(String subject, int offset, int limit) {
        pageBounds(offset, limit);
        var comments = store.entities().createQuery("from BannerComment c where c.author in (select a.id from CommentAuthor a where a.subject = :subject) order by c.createdAt desc, c.id desc", BannerComment.class)
            .setParameter("subject", subject).setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::view).toList();
        long count = store.entities().createQuery("select count(c) from BannerComment c where c.author in (select a.id from CommentAuthor a where a.subject = :subject)", Long.class)
            .setParameter("subject", subject).getSingleResult();
        return new Page(comments, count, offset, limit);
    }

    public View withdraw(String subject, UUID id, long expectedVersion) {
        var author = store.lockAuthor(subject);
        var comment = store.comment(id);
        if (!comment.author().equals(author.id())) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        if (comment.state() == State.WITHDRAWN) {
            return view(comment);
        }
        version(comment.version(), expectedVersion);
        State previous = comment.state();
        comment.state = State.WITHDRAWN;
        comment.version++;
        audit(id, author.id(), "author:" + subject, null, UUID.randomUUID(), "WITHDRAW", expectedVersion,
            "Author withdrawal", previous.name(), State.WITHDRAWN.name());
        store.event(id, author.id(), "WITHDRAWN", settings.environment());
        return view(store.comment(id));
    }

    /** Bind the service to a configured OAuth client and moderator allowlist, not just a role. */
    public void authorizeBot(Authentication authentication, Long moderator) {
        if (!(authentication instanceof JwtAuthenticationToken token)
            || settings.moderationClientId().isBlank()
            || !settings.moderationClientId().equals(token.getToken().getClaimAsString("azp"))
            || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_moderate-comments"))
            || (moderator != null && !settings.moderatorIds().contains(moderator))) {
            throw new ResponseStatusException(FORBIDDEN);
        }
    }

    public ModerationView decide(Authentication authentication, UUID id, Decision request) {
        authorizeBot(authentication, request.moderatorId());
        validateReason(request.reason());
        var initial = store.comment(id);
        var author = store.lockAuthor(initial.author());
        var comment = store.refreshComment(id);
        String actor = "bot:" + authentication.getName();
        if (repeated(request.requestId(), id, author.id(), request.action().name(),
            request.expectedVersion(), request.reason(), request.moderatorId())) {
            return moderationView(comment);
        }
        version(comment.version(), request.expectedVersion());
        State target = target(comment.state(), request.action());
        boolean strike = request.action() == Action.REMOVE || request.action() == Action.REJECT;
        State previous = comment.state();
        comment.state = target;
        comment.reviewed = true;
        comment.strike = strike;
        comment.forgiven = false;
        comment.strikeGeneration = author.strikeGeneration;
        comment.reason = request.reason();
        comment.version++;
        audit(id, author.id(), actor, request.moderatorId(), request.requestId(), request.action().name(),
            request.expectedVersion(), request.reason(), previous.name(), target.name());
        if (strike && store.strikes(author.id()) >= settings.strikeThreshold()
            && author.mode() != Mode.PREMODERATION) {
            Mode previousMode = author.mode();
            long previousVersion = author.version();
            author.mode = Mode.PREMODERATION;
            author.version++;
            audit(null, author.id(), actor, request.moderatorId(), UUID.randomUUID(), "AUTO_PREMODERATION",
                previousVersion, "Negative decision threshold reached", previousMode.name(), Mode.PREMODERATION.name());
            store.event(null, author.id(), "AUTHOR_MODE_CHANGED", settings.environment());
        }
        store.event(id, author.id(), "DECIDED", settings.environment());
        return moderationView(store.comment(id));
    }

    static State target(State current, Action action) {
        boolean valid = switch (action) {
            case KEEP, REMOVE -> current == State.PUBLISHED;
            case APPROVE, REJECT -> current == State.PENDING;
            case RESTORE -> current == State.REMOVED || current == State.REJECTED;
        };
        if (!valid) {
            throw new ResponseStatusException(CONFLICT, "Action is not valid for the current comment state");
        }
        return switch (action) {
            case KEEP, APPROVE, RESTORE -> State.PUBLISHED;
            case REMOVE -> State.REMOVED;
            case REJECT -> State.REJECTED;
        };
    }

    public void changeMode(Authentication authentication, UUID id, ChangeMode request) {
        authorizeBot(authentication, request.moderatorId());
        validateReason(request.reason());
        var author = store.lockAuthor(id);
        String actor = "bot:" + authentication.getName();
        String action = request.mode() == Mode.POSTMODERATION ? "RESET_POSTMODERATION" : "REQUIRE_PREMODERATION";
        if (repeated(request.requestId(), null, id, action, request.expectedVersion(),
            request.reason(), request.moderatorId())) {
            return;
        }
        version(author.version(), request.expectedVersion());
        if (request.mode() == Mode.POSTMODERATION) {
            author.strikeGeneration++;
        }
        Mode previousMode = author.mode();
        author.mode = request.mode();
        author.version++;
        audit(null, id, actor, request.moderatorId(), request.requestId(), action, request.expectedVersion(),
            request.reason(), previousMode.name(), request.mode().name());
        store.event(null, id, "AUTHOR_MODE_CHANGED", settings.environment());
    }

    private static void validateReason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 200 || reason.indexOf(0) >= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid moderation reason");
        }
    }

    private boolean repeated(UUID key, UUID comment, UUID author, String action, long version,
        String reason, long moderator) {
        var rows = store.entities().createQuery("from CommentDecision where requestKey = :key", CommentDecision.class)
            .setParameter("key", key).getResultList();
        if (rows.isEmpty()) {
            return false;
        }
        var row = rows.getFirst();
        if (!Objects.equals(row.comment, comment) || !row.author.equals(author)
            || !row.action.equals(action) || !row.reason.equals(reason)
            || row.expectedVersion != version || !Objects.equals(row.moderator, moderator)) {
            throw new ResponseStatusException(CONFLICT, "Decision key already used for another request");
        }
        return true;
    }

    private void audit(UUID comment, UUID author, String actor, Long moderator, UUID key, String action,
        long version, String reason, String previous, String next) {
        var decision = new CommentDecision();
        decision.createdAt = store.now();
        decision.comment = comment;
        decision.author = author;
        decision.actor = actor;
        decision.moderator = moderator;
        decision.requestKey = key;
        decision.action = action;
        decision.expectedVersion = version;
        decision.reason = reason;
        decision.previousState = previous;
        decision.nextState = next;
        store.entities().persist(decision);
        try {
            store.entities().flush();
        } catch (PersistenceException exception) {
            if (CommentStore.constraint(exception, "comment_audit_request_key_key")) {
                throw new ResponseStatusException(CONFLICT, "Concurrent duplicate moderation request; retry");
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public ModerationView moderation(UUID id) {
        return moderationView(store.comment(id));
    }

    @Transactional(readOnly = true)
    public List<ModerationView> moderationQueue(UUID author, State state, int offset, int limit) {
        pageBounds(offset, limit);
        String filter = (author == null ? "" : " and author = :author") + (state == null ? "" : " and state = :state");
        var query = store.entities().createQuery("from BannerComment where 1 = 1" + filter + " order by createdAt desc, id desc", BannerComment.class);
        if (author != null) {
            query.setParameter("author", author);
        }
        if (state != null) {
            query.setParameter("state", state);
        }
        return query.setFirstResult(offset).setMaxResults(limit).getResultList().stream().map(this::moderationView).toList();
    }

    @Transactional(readOnly = true)
    public List<Audit> history(UUID author, int offset, int limit) {
        pageBounds(offset, limit);
        return store.entities().createQuery("from CommentDecision where author = :author order by createdAt desc, id desc", CommentDecision.class)
            .setParameter("author", author).setFirstResult(offset).setMaxResults(limit).getResultList().stream()
            .map(d -> new Audit(d.id, d.comment, d.author, d.moderator, d.action, d.reason, d.previousState, d.nextState, d.createdAt)).toList();
    }

    private ModerationView moderationView(BannerComment comment) {
        var author = store.author(comment.author());
        Banner current = comment.bannerId() == null ? null : store.entities().find(Banner.class, comment.bannerId());
        var banner = current == null ? new BannerInfo(comment.bannerReference(), comment.title(), comment.slug())
            : new BannerInfo(current.getUuid(), current.getTitle(), current.getCanonicalSlug());
        String url = settings.website() + "/banner/" + URLEncoder.encode(banner.slug(), StandardCharsets.UTF_8);
        String label = comment.agent() == null ? "Ingress not linked — test exemption" : comment.agent();
        return new ModerationView(comment.id(), author.id(), comment.bannerReference(), banner.title(), url,
            current != null, comment.body(), label, comment.agent() != null, comment.createdAt(), comment.state(),
            comment.reviewed(), comment.reason(), comment.version(), author.mode(), author.version(),
            store.strikes(author.id()), settings.environment());
    }

    private View view(BannerComment comment) {
        return new View(comment.id(), comment.bannerReference(), comment.body(), comment.agent(),
            comment.createdAt(), comment.state(), comment.version(), comment.reason());
    }

    static void pageBounds(int offset, int limit) {
        if (offset < 0 || offset > 100000 || limit < 1 || limit > 100) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid pagination bounds");
        }
    }

    static void version(long actual, long expected) {
        if (actual != expected) {
            throw new ResponseStatusException(CONFLICT, "Stale version; refresh before acting");
        }
    }
}
