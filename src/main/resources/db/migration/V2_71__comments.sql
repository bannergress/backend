-- Author locks serialize submissions, strikes and mode changes across all backend instances.
CREATE TABLE comment_author (
    id uuid PRIMARY KEY,
    subject text NOT NULL UNIQUE,
    mode varchar(20) NOT NULL DEFAULT 'POSTMODERATION'
        CHECK (mode IN ('POSTMODERATION', 'PREMODERATION')),
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE banner_comment (
    id uuid PRIMARY KEY,
    banner_uuid uuid REFERENCES banner(uuid) ON DELETE SET NULL,
    banner_reference uuid NOT NULL,
    banner_title text NOT NULL,
    banner_slug text NOT NULL,
    author_id uuid NOT NULL REFERENCES comment_author(id),
    agent_name text,
    body text NOT NULL CHECK (char_length(body) BETWEEN 1 AND 2000),
    created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    state varchar(16) NOT NULL CHECK (state IN ('PUBLISHED', 'PENDING', 'REMOVED', 'REJECTED', 'WITHDRAWN')),
    reviewed boolean NOT NULL DEFAULT false,
    strike boolean NOT NULL DEFAULT false,
    forgiven boolean NOT NULL DEFAULT false,
    reason varchar(200),
    version bigint NOT NULL DEFAULT 0,
    submission_key uuid NOT NULL,
    UNIQUE (author_id, submission_key)
);
CREATE INDEX banner_comment_public ON banner_comment(banner_uuid, created_at DESC, id DESC)
    WHERE state = 'PUBLISHED';
CREATE INDEX banner_comment_author ON banner_comment(author_id, created_at DESC, id DESC);
CREATE INDEX banner_comment_pending ON banner_comment(created_at, id) WHERE state = 'PENDING';

CREATE TABLE comment_audit (
    id uuid PRIMARY KEY,
    comment_id uuid REFERENCES banner_comment(id),
    author_id uuid NOT NULL REFERENCES comment_author(id),
    actor text NOT NULL,
    moderator_id bigint,
    request_key uuid NOT NULL,
    action varchar(32) NOT NULL,
    expected_version bigint NOT NULL,
    reason varchar(200) NOT NULL,
    previous_state varchar(20) NOT NULL,
    next_state varchar(20) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    UNIQUE (request_key)
);
CREATE INDEX comment_audit_author ON comment_audit(author_id, created_at, id);

CREATE TABLE comment_outbox (
    id uuid PRIMARY KEY,
    comment_id uuid REFERENCES banner_comment(id),
    author_id uuid NOT NULL REFERENCES comment_author(id),
    event_type varchar(32) NOT NULL,
    environment varchar(4) NOT NULL CHECK (environment IN ('TEST', 'PROD')),
    created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    available_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    lease_token uuid,
    lease_until timestamptz,
    attempts integer NOT NULL DEFAULT 0,
    state varchar(12) NOT NULL DEFAULT 'QUEUED' CHECK (state IN ('QUEUED', 'DELIVERED', 'DEAD')),
    telegram_message_ids text[] NOT NULL DEFAULT '{}'
);
CREATE INDEX comment_outbox_pending ON comment_outbox(environment, available_at, created_at)
    WHERE state = 'QUEUED';
