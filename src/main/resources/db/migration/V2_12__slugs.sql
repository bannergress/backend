CREATE TABLE "banner_slug" (
  "slug" text NOT NULL,
  "banner" "uuid" NOT NULL,
  PRIMARY KEY ("slug"),
  UNIQUE ("banner", "slug"),
  FOREIGN KEY ("banner") REFERENCES "banner"("uuid")
);
INSERT INTO "banner_slug" ("slug", "banner") SELECT "slug", "uuid" FROM "banner";

ALTER TABLE "banner" RENAME COLUMN "slug" TO "canonical_slug";
ALTER TABLE "banner" ADD FOREIGN KEY ("uuid", "canonical_slug") REFERENCES "banner_slug" ("banner", "slug") DEFERRABLE INITIALLY DEFERRED;
