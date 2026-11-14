-- Immutable moderation command receipts retain API history and globally unique request keys.
-- Entity change history is owned by Envers and the application's existing revision entity.
ALTER TABLE comment_audit RENAME TO comment_decision;

CREATE TABLE comment_author_audit (
    id uuid NOT NULL,
    rev integer NOT NULL REFERENCES revision(id),
    revtype smallint,
    subject text,
    mode varchar(20),
    version bigint,
    PRIMARY KEY (id, rev)
);
CREATE TABLE banner_comment_audit (
    id uuid NOT NULL,
    rev integer NOT NULL REFERENCES revision(id),
    revtype smallint,
    banner_uuid uuid,
    banner_reference uuid,
    banner_title text,
    banner_slug text,
    author_id uuid,
    agent_name text,
    body text,
    created_at timestamptz,
    state varchar(16),
    reviewed boolean,
    strike boolean,
    forgiven boolean,
    reason varchar(200),
    version bigint,
    submission_key uuid,
    PRIMARY KEY (id, rev)
);
