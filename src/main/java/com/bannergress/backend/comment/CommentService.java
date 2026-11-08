package com.bannergress.backend.comment;

import com.bannergress.backend.user.UserMappingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        if (text == null || text.isBlank() || text.length() > 2000 || request.requestId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid comment");
        }
        var banner = banner(slug);
        var previous = store.jdbc.query("SELECT * FROM banner_comment WHERE author_id = ? AND submission_key = ?",
            store::mapComment, author.id(), request.requestId());
        if (!previous.isEmpty()) {
            var comment = previous.getFirst();
            if (!comment.body().equals(text) || !comment.bannerReference().equals(banner.id())) {
                throw new ResponseStatusException(CONFLICT, "Submission key already used for another comment");
            }
            return view(comment);
        }
        int minute = store.jdbc.queryForObject("""
            SELECT count(*) FROM banner_comment WHERE author_id = ?
            AND created_at > clock_timestamp() - interval '1 minute'
            """, Integer.class, author.id());
        int day = store.jdbc.queryForObject("""
            SELECT count(*) FROM banner_comment WHERE author_id = ?
            AND created_at > clock_timestamp() - interval '1 day'
            """, Integer.class, author.id());
        if (minute >= settings.maxPerMinute() || day >= settings.maxPerDay()) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS, "Comment rate limit reached");
        }
        UUID id = UUID.randomUUID();
        State state = author.mode() == Mode.PREMODERATION ? State.PENDING : State.PUBLISHED;
        store.jdbc.update("""
            INSERT INTO banner_comment(id, banner_uuid, banner_reference, banner_title, banner_slug,
                author_id, agent_name, body, state, submission_key)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, id, banner.id(), banner.id(), banner.title(), banner.slug(), author.id(), agent, text,
            state.name(), request.requestId());
        store.event(id, author.id(), "CREATED", settings.environment());
        return view(store.comment(id));
    }

    record BannerInfo(UUID id, String title, String slug) {
    }

    private BannerInfo banner(String slug) {
        return CommentStore.one(store.jdbc.query("""
            SELECT b.uuid, b.title, b.canonical_slug FROM banner b
            JOIN banner_slug s ON s.banner = b.uuid WHERE s.slug = ?
            """, (r, n) -> new BannerInfo(r.getObject("uuid", UUID.class), r.getString("title"),
                r.getString("canonical_slug")), slug));
    }

    @Transactional(readOnly = true)
    public Page published(String slug, int offset, int limit) {
        pageBounds(offset, limit);
        UUID banner = banner(slug).id();
        var comments = store.jdbc.query("""
            SELECT * FROM banner_comment WHERE banner_uuid = ? AND state = 'PUBLISHED'
            ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
            """, store::mapComment, banner, limit, offset).stream().map(c -> new View(c.id(), c.bannerReference(),
                c.body(), c.agent(), c.createdAt(), c.state(), c.version(), null)).toList();
        long count = store.jdbc.queryForObject("SELECT count(*) FROM banner_comment WHERE banner_uuid = ? AND state = 'PUBLISHED'",
            Long.class, banner);
        return new Page(comments, count, offset, limit);
    }

    @Transactional(readOnly = true)
    public Page mine(String subject, int offset, int limit) {
        pageBounds(offset, limit);
        var comments = store.jdbc.query("""
            SELECT c.* FROM banner_comment c JOIN comment_author a ON a.id = c.author_id
            WHERE a.subject = ? ORDER BY c.created_at DESC, c.id DESC LIMIT ? OFFSET ?
            """, store::mapComment, subject, limit, offset).stream().map(this::view).toList();
        long count = store.jdbc.queryForObject("""
            SELECT count(*) FROM banner_comment c JOIN comment_author a ON a.id = c.author_id WHERE a.subject = ?
            """, Long.class, subject);
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
        store.jdbc.update("UPDATE banner_comment SET state = 'WITHDRAWN', version = version + 1 WHERE id = ?", id);
        audit(id, author.id(), "author:" + subject, null, UUID.randomUUID(), "WITHDRAW", expectedVersion,
            "Author withdrawal", comment.state().name(), State.WITHDRAWN.name());
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
        var initial = store.comment(id);
        var author = store.lockAuthor(initial.author());
        var comment = store.comment(id);
        String actor = "bot:" + authentication.getName();
        if (repeated(actor, request.requestId(), id, author.id(), request.action().name(),
            request.expectedVersion(), request.reason(), request.moderatorId())) {
            return moderationView(comment);
        }
        version(comment.version(), request.expectedVersion());
        State target = target(comment.state(), request.action());
        boolean strike = request.action() == Action.REMOVE || request.action() == Action.REJECT;
        store.jdbc.update("""
            UPDATE banner_comment SET state = ?, reviewed = true, strike = ?, reason = ?, version = version + 1
            WHERE id = ?
            """, target.name(), strike, request.reason(), id);
        audit(id, author.id(), actor, request.moderatorId(), request.requestId(), request.action().name(),
            request.expectedVersion(), request.reason(), comment.state().name(), target.name());
        if (strike && store.strikes(author.id()) >= settings.strikeThreshold()
            && author.mode() != Mode.PREMODERATION) {
            store.jdbc.update("UPDATE comment_author SET mode = 'PREMODERATION', version = version + 1 WHERE id = ?",
                author.id());
            audit(null, author.id(), actor, request.moderatorId(), UUID.randomUUID(), "AUTO_PREMODERATION",
                author.version(), "Negative decision threshold reached", author.mode().name(), Mode.PREMODERATION.name());
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
        var author = store.lockAuthor(id);
        String actor = "bot:" + authentication.getName();
        String action = request.mode() == Mode.POSTMODERATION ? "RESET_POSTMODERATION" : "REQUIRE_PREMODERATION";
        if (repeated(actor, request.requestId(), null, id, action, request.expectedVersion(),
            request.reason(), request.moderatorId())) {
            return;
        }
        version(author.version(), request.expectedVersion());
        if (request.mode() == Mode.POSTMODERATION) {
            store.jdbc.update("UPDATE banner_comment SET forgiven = true WHERE author_id = ? AND strike", id);
        }
        store.jdbc.update("UPDATE comment_author SET mode = ?, version = version + 1 WHERE id = ?",
            request.mode().name(), id);
        audit(null, id, actor, request.moderatorId(), request.requestId(), action, request.expectedVersion(),
            request.reason(), author.mode().name(), request.mode().name());
        store.event(null, id, "AUTHOR_MODE_CHANGED", settings.environment());
    }

    private boolean repeated(String actor, UUID key, UUID comment, UUID author, String action, long version,
        String reason, long moderator) {
        var rows = store.jdbc.queryForList("SELECT * FROM comment_audit WHERE actor = ? AND request_key = ?", actor, key);
        if (rows.isEmpty()) {
            return false;
        }
        var row = rows.getFirst();
        if (!Objects.equals(row.get("comment_id"), comment) || !row.get("author_id").equals(author)
            || !row.get("action").equals(action) || !row.get("reason").equals(reason)
            || ((Number) row.get("expected_version")).longValue() != version
            || ((Number) row.get("moderator_id")).longValue() != moderator) {
            throw new ResponseStatusException(CONFLICT, "Decision key already used for another request");
        }
        return true;
    }

    private void audit(UUID comment, UUID author, String actor, Long moderator, UUID key, String action,
        long version, String reason, String previous, String next) {
        int inserted = store.jdbc.update("""
            INSERT INTO comment_audit(id, comment_id, author_id, actor, moderator_id, request_key,
                action, expected_version, reason, previous_state, next_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(actor, request_key) DO NOTHING
            """, UUID.randomUUID(), comment, author, actor, moderator, key, action, version, reason, previous, next);
        if (inserted != 1) {
            throw new ResponseStatusException(CONFLICT, "Concurrent duplicate moderation request; retry");
        }
    }

    @Transactional(readOnly = true)
    public ModerationView moderation(UUID id) {
        return moderationView(store.comment(id));
    }

    @Transactional(readOnly = true)
    public List<ModerationView> moderationQueue(UUID author, State state, int offset, int limit) {
        pageBounds(offset, limit);
        return store.jdbc.query("""
            SELECT * FROM banner_comment
            WHERE (?::uuid IS NULL OR author_id = ?) AND (?::text IS NULL OR state = ?)
            ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
            """, store::mapComment, author, author, state == null ? null : state.name(),
            state == null ? null : state.name(), limit, offset).stream().map(this::moderationView).toList();
    }

    @Transactional(readOnly = true)
    public List<Audit> history(UUID author, int offset, int limit) {
        pageBounds(offset, limit);
        return store.jdbc.query("""
            SELECT * FROM comment_audit WHERE author_id = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
            """, (r, n) -> new Audit(r.getObject("id", UUID.class), r.getObject("comment_id", UUID.class),
                author, r.getObject("moderator_id", Long.class), r.getString("action"), r.getString("reason"),
                r.getString("previous_state"), r.getString("next_state"), r.getTimestamp("created_at").toInstant()),
            author, limit, offset);
    }

    private ModerationView moderationView(CommentStore.Comment comment) {
        var author = store.author(comment.author());
        var banners = store.jdbc.query("SELECT canonical_slug, title FROM banner WHERE uuid = ?",
            (r, n) -> new BannerInfo(comment.bannerId(), r.getString("title"), r.getString("canonical_slug")),
            comment.bannerId());
        var banner = banners.isEmpty() ? new BannerInfo(comment.bannerReference(), comment.title(), comment.slug())
            : banners.getFirst();
        String url = settings.website() + "/banner/" + URLEncoder.encode(banner.slug(), StandardCharsets.UTF_8);
        String label = comment.agent() == null ? "Ingress not linked — test exemption" : comment.agent();
        return new ModerationView(comment.id(), author.id(), comment.bannerReference(), banner.title(), url,
            !banners.isEmpty(), comment.body(), label, comment.agent() != null, comment.createdAt(), comment.state(),
            comment.reviewed(), comment.reason(), comment.version(), author.mode(), author.version(),
            store.strikes(author.id()), settings.environment());
    }

    private View view(CommentStore.Comment comment) {
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
