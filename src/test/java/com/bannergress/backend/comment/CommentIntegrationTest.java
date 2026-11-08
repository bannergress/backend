package com.bannergress.backend.comment;

import com.bannergress.backend.user.UserMappingService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.bannergress.backend.comment.CommentModels.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Runs against a disposable PostGIS database: all historical migrations, real locks and transactional rollback. */
@EnabledIfEnvironmentVariable(named = "COMMENTS_TEST_JDBC_URL", matches = ".+")
class CommentIntegrationTest {
    private static DriverManagerDataSource dataSource;
    private static JdbcTemplate jdbc;
    private AnnotationConfigApplicationContext context;
    private CommentService service;
    private CommentOutbox outbox;
    private UserMappingService mappings;
    private UUID banner;
    private final JwtAuthenticationToken bot = bot("moderation-test", true);

    @Configuration
    @EnableTransactionManagement
    static class Transactions {
    }

    @BeforeAll
    static void migrate() {
        String url = System.getenv("COMMENTS_TEST_JDBC_URL");
        if (!url.matches("jdbc:postgresql://(127\\.0\\.0\\.1|localhost):[0-9]+/comments_test")) {
            throw new IllegalArgumentException("Integration tests require a disposable loopback comments_test database");
        }
        dataSource = new DriverManagerDataSource(url, "postgres", "");
        jdbc = new JdbcTemplate(dataSource);
        Flyway.configure().dataSource(dataSource).load().migrate();
        Flyway.configure().dataSource(dataSource).load().validate();
    }

    @BeforeEach
    void setup() {
        jdbc.execute("TRUNCATE comment_outbox, comment_audit, banner_comment, comment_author");
        banner = UUID.randomUUID();
        new TransactionTemplate(new DataSourceTransactionManager(dataSource)).executeWithoutResult(status -> {
            jdbc.update("""
                INSERT INTO banner(uuid, canonical_slug, created, width, number_of_missions, online, title, type,
                    number_of_submitted_missions, number_of_disabled_missions)
                VALUES (?, ?, now(), 1, 1, true, 'Test banner', 'sequential', 0, 0)
                """, banner, banner.toString());
            jdbc.update("INSERT INTO banner_slug(slug, banner) VALUES (?, ?)", banner.toString(), banner);
        });
        mappings = mock(UserMappingService.class);
        when(mappings.getAgentName(anyString())).thenReturn(Optional.of("VerifiedAgent"));
        open(settings("TEST", true, true, true, 3, 10));
    }

    private void open(CommentSettings settings) {
        if (context != null) {
            context.close();
        }
        context = new AnnotationConfigApplicationContext();
        context.register(Transactions.class);
        context.registerBean("dataSource", DriverManagerDataSource.class, () -> dataSource);
        context.registerBean("transactionManager", DataSourceTransactionManager.class,
            () -> new DataSourceTransactionManager(dataSource));
        context.registerBean(JdbcTemplate.class, () -> jdbc);
        context.registerBean(CommentSettings.class, () -> settings);
        context.registerBean(UserMappingService.class, () -> mappings);
        context.register(CommentStore.class, CommentService.class, CommentOutbox.class);
        context.refresh();
        service = context.getBean(CommentService.class);
        outbox = context.getBean(CommentOutbox.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    static CommentSettings settings(String environment, boolean enabled, boolean exempt, boolean trusted,
        int threshold, int minuteLimit) {
        String realm = environment.equals("TEST") ? "bannergress-test" : "bannergress";
        return new CommentSettings(enabled, environment, exempt, trusted, threshold, minuteLimit, 100,
            "moderation-test", Set.of(123L), "https://login.bannergress.com/auth/realms/" + realm);
    }

    static JwtAuthenticationToken bot(String client, boolean role) {
        Jwt jwt = Jwt.withTokenValue("synthetic-test-token").header("alg", "none").subject("service-account")
            .claim("azp", client).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        return new JwtAuthenticationToken(jwt, role ? List.of(new SimpleGrantedAuthority("ROLE_moderate-comments")) : List.of());
    }

    private View post(String author) {
        return service.create(author, banner.toString(), new Create(UUID.randomUUID(), "Comment 🌍\n<script>untrusted</script>"));
    }

    private ModerationView decide(View comment, Action action) {
        return service.decide(bot, comment.id(), new Decision(UUID.randomUUID(), comment.version(), action, "Policy reason", 123));
    }

    @Test
    void productionRequiresVerifiedLinkAndFailsClosedOnMappingFailure() {
        open(settings("PROD", true, false, true, 3, 10));
        when(mappings.getAgentName("unlinked")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> post("unlinked")).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403");
        when(mappings.getAgentName("broken")).thenThrow(new IllegalStateException("private upstream details"));
        assertThatThrownBy(() -> post("broken")).hasMessageContaining("503").hasMessageNotContaining("private upstream");
        assertThat(post("linked").state()).isEqualTo(State.PUBLISHED);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM comment_outbox", Integer.class)).isEqualTo(1);
    }

    @Test
    void testUnlinkedUserIsNotMisrepresentedAsVerified() {
        when(mappings.getAgentName("unlinked")).thenReturn(Optional.empty());
        var capabilities = service.capabilities("unlinked");
        assertThat(capabilities.canComment()).isTrue();
        assertThat(capabilities.testExemption()).isTrue();
        var comment = post("unlinked");
        var moderation = service.moderation(comment.id());
        assertThat(moderation.ingressVerified()).isFalse();
        assertThat(moderation.authorLabel()).contains("Ingress not linked");
        assertThat(moderation.environment()).isEqualTo("TEST");
        assertThat(moderation.bannerUrl()).startsWith("https://test.bannergress.com/banner/");
    }

    @Test
    void untrustedAttributeCannotGrantProductionWritesAndFeatureDefaultsCanStopNewPosts() {
        open(settings("PROD", true, false, false, 3, 10));
        assertThat(service.capabilities("a").canComment()).isFalse();
        assertThatThrownBy(() -> post("a")).hasMessageContaining("403");
        open(settings("TEST", false, true, true, 3, 10));
        assertThat(service.capabilities("a").denialReason()).isEqualTo("COMMENTS_DISABLED");
        assertThatThrownBy(() -> post("a")).hasMessageContaining("503");
    }

    @Test
    void submissionRetriesAreIdempotentAndRejectReusedKeys() {
        var request = new Create(UUID.randomUUID(), "same");
        var first = service.create("a", banner.toString(), request);
        assertThat(service.create("a", banner.toString(), request)).isEqualTo(first);
        assertThatThrownBy(() -> service.create("a", banner.toString(), new Create(request.requestId(), "different")))
            .hasMessageContaining("409");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM banner_comment", Integer.class)).isEqualTo(1);
        assertThat(outbox.stats().queued()).isEqualTo(1);
    }

    @Test
    void thresholdPendingVisibilityApprovalReversalAndAuditedReset() {
        for (int i = 0; i < 3; i++) {
            decide(post("a"), Action.REMOVE);
        }
        assertThat(service.capabilities("a").mode()).isEqualTo(Mode.PREMODERATION);
        var pending = post("a");
        assertThat(pending.state()).isEqualTo(State.PENDING);
        assertThat(service.published(banner.toString(), 0, 30).total()).isZero();
        assertThat(service.mine("a", 0, 30).total()).isEqualTo(4);
        assertThat(service.mine("other", 0, 30).items()).isEmpty();
        var rejected = decide(pending, Action.REJECT);
        assertThat(rejected.effectiveStrikes()).isEqualTo(4);
        var restored = service.decide(bot, rejected.id(), new Decision(UUID.randomUUID(), rejected.version(),
            Action.RESTORE, "Mistaken decision", 123));
        assertThat(restored.effectiveStrikes()).isEqualTo(3);
        assertThat(restored.authorMode()).isEqualTo(Mode.PREMODERATION);
        assertThat(service.published(banner.toString(), 0, 30).items().getFirst().reason()).isNull();
        var reset = new ChangeMode(UUID.randomUUID(), restored.authorVersion(), Mode.POSTMODERATION, "Forgiven", 123);
        service.changeMode(bot, restored.authorReference(), reset);
        service.changeMode(bot, restored.authorReference(), reset);
        assertThat(service.capabilities("a").effectiveStrikes()).isZero();
        assertThat(post("a").state()).isEqualTo(State.PUBLISHED);
        assertThat(service.history(restored.authorReference(), 0, 100)).anyMatch(a -> a.action().equals("RESET_POSTMODERATION"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM comment_audit WHERE action = 'RESET_POSTMODERATION'", Integer.class)).isEqualTo(1);
    }

    @Test
    void keepAndApproveDoNotAddStrikesAndDisabledSubmissionsLeaveModerationAvailable() {
        var comment = post("a");
        var kept = decide(comment, Action.KEEP);
        assertThat(kept.effectiveStrikes()).isZero();
        assertThat(kept.reviewed()).isTrue();
        service.changeMode(bot, kept.authorReference(), new ChangeMode(UUID.randomUUID(),
            kept.authorVersion(), Mode.PREMODERATION, "Manual review required", 123));
        var pending = post("a");
        open(settings("TEST", false, true, true, 3, 10));
        var approved = decide(pending, Action.APPROVE);
        assertThat(approved.state()).isEqualTo(State.PUBLISHED);
        assertThat(approved.effectiveStrikes()).isZero();
        assertThat(service.published(banner.toString(), 0, 30).total()).isEqualTo(2);
        assertThat(outbox.claim(50)).isNotEmpty();
        assertThatThrownBy(() -> post("a")).hasMessageContaining("503");
    }

    @Test
    void concurrentWorkersCannotClaimTheSameOutboxEvent() throws Exception {
        post("a");
        CountDownLatch ready = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> {
                ready.await(10, TimeUnit.SECONDS);
                return outbox.claim(10);
            });
            var second = workers.submit(() -> {
                ready.countDown();
                return outbox.claim(10);
            });
            var a = first.get(10, TimeUnit.SECONDS);
            var b = second.get(10, TimeUnit.SECONDS);
            assertThat(a.size() + b.size()).isEqualTo(1);
        }
    }

    @Test
    void moderationRetriesAndStaleActionsDoNotDuplicateStrikes() {
        var comment = post("a");
        var decision = new Decision(UUID.randomUUID(), 0, Action.REMOVE, "Spam", 123);
        var first = service.decide(bot, comment.id(), decision);
        assertThat(service.decide(bot, comment.id(), decision)).isEqualTo(first);
        assertThat(first.effectiveStrikes()).isEqualTo(1);
        assertThatThrownBy(() -> service.decide(bot, comment.id(), new Decision(UUID.randomUUID(), 0,
            Action.KEEP, "Stale", 123))).hasMessageContaining("409");
        assertThatThrownBy(() -> service.decide(bot, comment.id(), new Decision(decision.requestId(), 0,
            Action.REMOVE, "Different", 123))).hasMessageContaining("409");
    }

    @Test
    void withdrawalIsOwnerOnlyDoesNotStrikeAndCannotBeRestored() {
        var comment = post("a");
        assertThatThrownBy(() -> service.withdraw("b", comment.id(), 0)).hasMessageContaining("404");
        var withdrawn = service.withdraw("a", comment.id(), 0);
        assertThat(service.withdraw("a", comment.id(), 0)).isEqualTo(withdrawn);
        assertThat(service.capabilities("a").effectiveStrikes()).isZero();
        assertThatThrownBy(() -> decide(withdrawn, Action.RESTORE)).hasMessageContaining("409");
        assertThat(service.published(banner.toString(), 0, 30).items()).isEmpty();
        assertThat(outbox.stats().queued()).isEqualTo(2);
    }

    @Test
    void rateLimitsAndInputValidationApplyToUnlinkedTestUsers() {
        open(settings("TEST", true, true, false, 3, 1));
        post("a");
        assertThatThrownBy(() -> post("a")).hasMessageContaining("429");
        assertThatThrownBy(() -> service.create("b", banner.toString(), new Create(UUID.randomUUID(), " \n")))
            .hasMessageContaining("400");
        assertThatThrownBy(() -> service.create("b", banner.toString(), new Create(UUID.randomUUID(), "x".repeat(2001))))
            .hasMessageContaining("400");
        assertThatThrownBy(() -> service.published(banner.toString(), -1, 30)).hasMessageContaining("400");
    }

    @Test
    void unlinkDoesNotResetAbuseHistory() {
        open(settings("PROD", true, false, true, 1, 10));
        decide(post("a"), Action.REMOVE);
        when(mappings.getAgentName("a")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> post("a")).hasMessageContaining("403");
        when(mappings.getAgentName("a")).thenReturn(Optional.of("RenamedAgent"));
        assertThat(post("a").state()).isEqualTo(State.PENDING);
        assertThat(service.capabilities("a").effectiveStrikes()).isEqualTo(1);
    }

    @Test
    void botRoleClientAndModeratorAreAllRequired() {
        assertThatThrownBy(() -> service.authorizeBot(bot("website", true), 123L)).hasMessageContaining("403");
        assertThatThrownBy(() -> service.authorizeBot(bot("moderation-test", false), 123L)).hasMessageContaining("403");
        assertThatThrownBy(() -> service.authorizeBot(bot, 999L)).hasMessageContaining("403");
        assertThatCode(() -> service.authorizeBot(bot, 123L)).doesNotThrowAnyException();
    }

    @Test
    void aliasRenameAndDeletionPreservePrivateAuditWithoutOrphanedPublicComments() {
        var comment = post("a");
        new TransactionTemplate(new DataSourceTransactionManager(dataSource)).executeWithoutResult(status -> {
            jdbc.update("INSERT INTO banner_slug(slug, banner) VALUES (?, ?)", "renamed-" + banner, banner);
            jdbc.update("UPDATE banner SET canonical_slug = ? WHERE uuid = ?", "renamed-" + banner, banner);
        });
        assertThat(service.published(banner.toString(), 0, 30).total()).isEqualTo(1);
        assertThat(service.moderation(comment.id()).bannerUrl()).contains("renamed-");
        new TransactionTemplate(new DataSourceTransactionManager(dataSource)).executeWithoutResult(status -> {
            jdbc.update("DELETE FROM banner_slug WHERE banner = ?", banner);
            jdbc.update("DELETE FROM banner WHERE uuid = ?", banner);
        });
        assertThat(service.moderation(comment.id()).bannerExists()).isFalse();
        assertThat(service.mine("a", 0, 30).total()).isEqualTo(1);
        assertThatThrownBy(() -> service.published(banner.toString(), 0, 30)).hasMessageContaining("404");
    }

    @Test
    void outboxLeasesAreFencedRetryableAndRecoverable() {
        post("a");
        var first = outbox.claim(10).getFirst();
        assertThat(outbox.claim(10)).isEmpty();
        assertThatThrownBy(() -> outbox.acknowledge(first.id(), new Acknowledge(UUID.randomUUID(), List.of("-100:1"))))
            .hasMessageContaining("409");
        jdbc.update("UPDATE comment_outbox SET lease_until = now() - interval '1 second'");
        var second = outbox.claim(10).getFirst();
        assertThat(second.leaseToken()).isNotEqualTo(first.leaseToken());
        assertThatThrownBy(() -> outbox.retry(first.id(), first.leaseToken())).hasMessageContaining("409");
        outbox.retry(second.id(), second.leaseToken());
        assertThat(outbox.claim(10)).isEmpty();
        jdbc.update("UPDATE comment_outbox SET available_at = now() - interval '1 second', attempts = 9");
        var last = outbox.claim(10).getFirst();
        outbox.retry(last.id(), last.leaseToken());
        assertThat(outbox.stats().dead()).isEqualTo(1);
        assertThat(outbox.dead(0, 30)).containsExactly(last.id());
        outbox.requeue(last.id());
        var fresh = outbox.claim(10).getFirst();
        var ack = new Acknowledge(fresh.leaseToken(), List.of("-100:123"));
        outbox.acknowledge(fresh.id(), ack);
        outbox.acknowledge(fresh.id(), ack);
        assertThat(outbox.stats().queued()).isZero();
        assertThat(outbox.claim(10)).isEmpty();
    }

    @Test
    void outboxAndCommentRollbackTogether() {
        var transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        transaction.executeWithoutResult(status -> {
            post("a");
            status.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM banner_comment", Integer.class)).isZero();
        assertThat(outbox.stats().queued()).isZero();
    }

    @Test
    void concurrentThresholdDecisionSerializesSubsequentSubmission() throws Exception {
        open(settings("TEST", true, true, true, 1, 10));
        var comment = post("a");
        UUID author = service.moderation(comment.id()).authorReference();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch submitStarted = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var decision = workers.submit(() -> new TransactionTemplate(new DataSourceTransactionManager(dataSource))
                .executeWithoutResult(status -> {
                    jdbc.queryForObject("SELECT version FROM comment_author WHERE id = ? FOR UPDATE", Long.class, author);
                    locked.countDown();
                    try {
                        assertThat(submitStarted.await(10, TimeUnit.SECONDS)).isTrue();
                    } catch (InterruptedException exception) {
                        throw new IllegalStateException(exception);
                    }
                    decide(comment, Action.REMOVE);
                }));
            var submission = workers.submit(() -> {
                assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
                submitStarted.countDown();
                return post("a");
            });
            decision.get(15, TimeUnit.SECONDS);
            assertThat(submission.get(15, TimeUnit.SECONDS).state()).isEqualTo(State.PENDING);
        }
    }
}
