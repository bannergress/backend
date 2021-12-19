ALTER TABLE "mission" ADD COLUMN "status_update_queued_since" timestamp with time zone;
ALTER TABLE "mission" ADD COLUMN "author_update_queued_since" timestamp with time zone;

CREATE INDEX ON "mission" ("status_update_queued_since") WHERE "status_update_queued_since" IS NOT NULL;
CREATE INDEX ON "mission" ("author_update_queued_since") WHERE "author_update_queued_since" IS NOT NULL;
