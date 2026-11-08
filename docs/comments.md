# Banner comments backend (draft)

Implements the backend portion of [website issue 555](https://github.com/bannergress/website/issues/555). No frontend, Telegram bot, BotFather identity, realm role or deployment is created by this change. Do not close the cross-repository feature issue when this backend PR merges.

## Deployment policy

New submissions are **off by default**. Reading existing comments, withdrawal, audit and outbox recovery remain available when submissions are disabled. All website-facing reads currently require authentication, preserving the existing signed-in agent-identity boundary; anonymous reading is deliberately not introduced pending the product decision.

| Property | Default | Meaning |
| --- | --- | --- |
| `comments.enabled` | `false` | Allow new submissions |
| `comments.environment` | `PROD` | Trusted `TEST` / `PROD` deployment assertion |
| `comments.allow-unlinked-test-users` | `false` | Test-only authenticated Ingress-link exemption |
| `comments.verified-attribute-trusted` | `false` | Operator has audited the verified-agent mapping permissions |
| `comments.strike-threshold` | `3` | Distinct effective negative decisions triggering premoderation |
| `comments.max-per-minute` | `10` | Per-author submission limit (withdrawals still count) |
| `comments.max-per-day` | `100` | Additional per-author daily limit |
| `comments.moderation-client-id` | empty | Dedicated bot OAuth client (`azp`), never website/search client |
| `comments.moderator-ids` | empty | Private comma-separated numeric Telegram moderator allowlist |

Environment is read only from server configuration. When enabled, TEST must use the `bannergress-test` issuer realm and PROD must not; a production exemption or mismatched realm fails startup. An untouched existing deployment with comments disabled does not need new properties. TEST and PROD require separate databases, issuers, bot identities/credentials and destinations. Do not point a test deployment at a production DB. The test exemption does not grant anonymity, synthetic verification or a moderation/rate-limit bypass.

**Before enabling verified writes**, audit Keycloak's user-profile configuration and all account/profile APIs: the `agent` attribute must be writable only by the existing verification service/administrators, never by an ordinary user. Only then set `comments.verified-attribute-trusted=true`. This is a required deployment attestation, not a claim that this PR changes Keycloak. No token-supplied nickname or pending claim grants eligibility. Mapping outages fail closed with sanitized 503 responses; missing, empty, multiple or blank mappings are unverified. Test can deliberately skip this prerequisite with the explicit unlinked-user exemption.

Moderation history uses the stable realm subject, not a mutable agent name. Unlink/relink and renaming do not reset it. The existing Ingress integration does not provide an immutable Ingress GUID; account transfers must not be automated as a way to reset history. Any cross-account transfer requires an administrator to review and migrate moderation history explicitly before enabling the new account; no automatic transfer/merge API is provided here.

## Website API

All endpoints require a validated bearer token. Comment responses use `Cache-Control: private, no-store` and vary on Authorization. User IDs, email, Google IDs and tokens are never exposed in comment DTOs.

| Method / path | Contract |
| --- | --- |
| `GET /user/comments/capabilities` | `canComment`, denial reason, `testExemption`, mode, effective strikes, environment |
| `GET /bnrs/{slug}/comments?offset=0&limit=30` | Published items only, published total, newest first ordered by timestamp + UUID |
| `POST /bnrs/{slug}/comments` | `{ "requestId": "UUID", "text": "plain text" }`; returns authoritative published/pending item |
| `GET /user/comments?offset=0&limit=30` | Only the caller's items, including pending/rejected/withdrawn and author-visible reason |
| `DELETE /comments/{id}?expectedVersion=N` | Owner-only withdrawal; unknown/other-author ID returns 404; idempotent if already withdrawn |

`requestId` is an idempotency key scoped to the author. Retrying the exact payload returns the same item, not another comment/event; reusing the key for another body/banner returns 409. Every creation rechecks eligibility. Use 1–100 items and bounded nonnegative offsets. Length is 1–2000 UTF-16 code units at the HTTP boundary, excluding whitespace-only text; bodies are retained verbatim, including Unicode/newlines, and must be rendered as **text**, never HTML. There is no editing, arbitrary direct-comment public lookup, attachment, threading or user-set publication state.

Banner aliases resolve to the immutable UUID. A rename preserves the discussion and the moderator permalink uses the current canonical slug. Deletion sets the live banner FK to null, retaining the immutable UUID/title/slug snapshots and private moderation history, but no comments are publicly reachable for that deleted banner. A future banner merge must explicitly reassign comments under reviewed migration semantics; this PR does not silently infer merges.

## Moderation API and state machine

Prefix: `/moderation/comments/{environment}`, where the exact uppercase TEST/PROD path must match the configured environment or returns 403. This is an assertion, **not** a selector. Require realm role `moderate-comments` **and** the configured dedicated bot OAuth `azp`; role possession by the website client does not suffice. Bot decisions additionally require an allowlisted numeric `moderatorId`.

| Method / suffix | Contract |
| --- | --- |
| `GET /` | Filterable reconciliation list: optional `author` UUID and `state`, `offset`, `limit` |
| `GET /{id}` | Current authoritative moderation card data; no subject/email/token |
| `POST /{id}/decisions` | `{requestId, expectedVersion, action, reason, moderatorId}` |
| `POST /authors/{id}/mode` | `{requestId, expectedVersion, mode, reason, moderatorId}`; author profile version |
| `GET /authors/{id}/audit` | Paginated decisions/mode changes, reasons and numeric moderator IDs; no service subject |

`KEEP` / `REMOVE` apply to published comments; `APPROVE` / `REJECT` to pending comments; `RESTORE` reverses a removed/rejected comment. Only explicit REMOVE/REJECT decisions add a strike, at most one active strike per distinct comment. Reasons are required and author-visible; do not put private moderator notes or personal information in this field. Withdrawn comments cannot be restored by moderation.

Each author has a transactional row lock shared by submissions, withdrawals, all decisions and mode changes. The threshold-crossing strike and PREMODERATION transition commit together; a submission serialized after that transition is pending. Existing published comments are not bulk-hidden. Reversals void that comment's strike but never automatically restore normal posting. Explicit `POSTMODERATION` resets forgive the current strike baseline, keep audit and do not publish existing pending comments. A fresh negative decision after restoration clears forgiveness and counts toward the threshold again. All mode actions are reasoned and audited. Role-granting/agent-link endpoints remain unchanged.

Every comment has an optimistic version. Duplicate decision keys return current state without repeating a decision/strike; different payloads reusing a key and stale versions return 409. The transaction rolls back if a conflicting global decision key is detected. Decision and author-mode request keys share one global namespace across bot principals; the original actor remains in the audit. Author-mode changes have an independent optimistic profile version.

## Bot contract and outbox recovery

Every accepted comment (including one withdrawn before delivery), decision, withdrawal and mode transition writes an outbox event **in the same transaction** as the change. The dedicated bot implementation is a separate task. Do not register handlers in, or reuse credentials/pollers from, search or announce.

| Method / suffix | Contract |
| --- | --- |
| `POST /outbox/claim` | `{ "limit": 1..50 }`; atomically claim available events with `FOR UPDATE SKIP LOCKED` |
| `POST /outbox/{id}/ack` | `{ "leaseToken": "UUID", "telegramMessageIds": ["-100123:456"] }` after successful send/update |
| `POST /outbox/{id}/retry` | `{ "leaseToken": "UUID" }`; exponential retry, bounded at one hour |
| `GET /outbox/stats` | Queue/dead counts and oldest queued timestamp |
| `GET /outbox/dead` | Paginated dead event IDs for operator recovery |
| `POST /outbox/{id}/requeue` | Requeue a dead event after the underlying failure is resolved |

Leases last two minutes and are fenced by a random token. Expired claims can be recovered by another worker; stale claimants cannot ACK/retry another worker's lease. After ten attempts, events enter DEAD (including expired last attempts). Replaying the identical successful ACK is safe. The bot should keep claimed batches small and retry on 409 rather than accepting stale data. No in-memory-only queue or exactly-once guarantee: Telegram send success followed by network/ACK failure can produce duplicates.

For a `CREATED`, `DECIDED` or `WITHDRAWN` event, fetch the current moderation DTO before sending/updating; delivery can be out of order. For `AUTHOR_MODE_CHANGED` (no commentId), reconcile all that author's cards through the filtered queue. Keep/update every duplicate card with current state and version. Persist a canonical comment-to-Telegram-card mapping in the bot; outbox message IDs are delivery receipts, not a substitute for this mapping.

Every card, long-text continuation, reason prompt, confirmation and update must start **[TEST]** or **[PROD]** from the trusted DTO environment. Use its matching banner URL; unlinked test authors must show **Ingress not linked — test exemption**, never a made-up verified agent. Never render subject IDs or credentials. Callback references must bind environment, comment/profile UUID and version; the bot must validate the numeric moderator allowlist **and** configured private chat/topic before submitting a backend decision. This backend cannot verify Telegram chat provenance by itself: a compromised authorized bot principal remains a privileged boundary.

Monitor oldest queued items, DEAD count and pending review age. The bot/operator must alert on a growing backlog; claim is pull-based, so a separate consumer is required. After an outage, resume claiming and reconcile cards; premoderated content remains private, normal posting may continue with queued notifications. Disable only `comments.enabled` to stop new writes without disabling recovery. Never acknowledge an event before the Telegram operation succeeds.

## Retention and operation boundaries

This draft retains immutable moderation audit and delivered receipts; it does not add an irreversible automatic purge or silently delete content on author withdrawal. Agree legal/product retention periods before production activation. Use a reviewed, backed-up operator migration to redact/anonymize retained subject/body/agent snapshots, coordinating deletions of Telegram copies using recorded message references; do not claim removing backend content removes Telegram messages. Never purge queued/pending work as a generic cleanup. Existing banner deletion retains private historical evidence deliberately.

No frontend or real Telegram/Google flow has been exercised by these backend tests. Full feature acceptance still needs the dedicated bot and website integration, test environment provisioning and the production profile-permission audit.

## Tests

```sh
# Use a disposable database only; this runs all historical Flyway migrations.
COMMENTS_TEST_JDBC_URL=jdbc:postgresql://127.0.0.1:55436/comments_test ./gradlew test checkstyleMain checkstyleTest bootJar
```

PostGIS-enabled PostgreSQL 16, database `comments_test`, user `postgres`, no password (loopback disposable test fixture only). Integration tests are skipped if the URL is not supplied; CI must supply it. Tests use actual PostgreSQL transactions/locks, not an H2 substitute. Never point this test URL at an existing application database.
