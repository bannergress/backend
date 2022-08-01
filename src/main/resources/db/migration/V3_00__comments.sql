CREATE TYPE comment_type AS ENUM ('comment', 'review');
CREATE CAST (varchar AS comment_type) WITH INOUT AS IMPLICIT;

CREATE TYPE round_the_clock_type AS ENUM ('unrestricted', 'restricted');
CREATE CAST (varchar AS round_the_clock_type) WITH INOUT AS IMPLICIT;

CREATE TABLE "comment" (
  "uuid" uuid NOT NULL,
  "banner" uuid NOT NULL,
  "user" text NOT NULL,
  "type" comment_type NOT NULL,
  "created" timestamp with time zone NOT NULL,
  "comment" text,
  "rating_round_the_clock" round_the_clock_type,
  "rating_overall" integer,
  "rating_accessibility" integer,
  "rating_passphrases" integer,
  PRIMARY KEY ("uuid"),
  FOREIGN KEY ("banner") REFERENCES "banner"("uuid"),
  FOREIGN KEY ("user") REFERENCES "user"("id")
);
CREATE INDEX ON "comment" ("banner");

CREATE TABLE "comment_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "banner" uuid,
  "user" text,
  "type" comment_type,
  "comment" text,
  "rating_round_the_clock" round_the_clock_type,
  "rating_overall" integer,
  "rating_accessibility" integer,
  "rating_passphrases" integer,
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);

ALTER TABLE "banner"
  ADD COLUMN "round_the_clock" boolean,
  ADD COLUMN "average_rating_round_the_clock" real,
  ADD COLUMN "average_rating_overall" real,
  ADD COLUMN "average_rating_accessibility" real,
  ADD COLUMN "average_rating_passphrases" real;
ALTER TABLE "banner_audit" ADD COLUMN "round_the_clock" boolean;
