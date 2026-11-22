-- Retain the submitted path independently of canonical banner aliases and deletion.
ALTER TABLE banner_comment ADD COLUMN submission_slug text;
UPDATE banner_comment SET submission_slug = banner_slug;
ALTER TABLE banner_comment ALTER COLUMN submission_slug SET NOT NULL;
ALTER TABLE banner_comment_audit ADD COLUMN submission_slug text;

-- Forgiveness is an audited O(1) author baseline change. Historical comment snapshots stay immutable.
ALTER TABLE comment_author ADD COLUMN strike_generation bigint NOT NULL DEFAULT 0;
ALTER TABLE comment_author_audit ADD COLUMN strike_generation bigint;
ALTER TABLE banner_comment ADD COLUMN strike_generation bigint NOT NULL DEFAULT 0;
ALTER TABLE banner_comment_audit ADD COLUMN strike_generation bigint;
CREATE INDEX banner_comment_strikes ON banner_comment(author_id, strike_generation)
    WHERE strike AND NOT forgiven;
