CREATE TABLE "user" (
  "id" text NOT NULL,
  "verification_agent" text,
  "verification_token" uuid,
  PRIMARY KEY ("id")
);

CREATE TYPE banner_list_type AS ENUM ('none', 'todo', 'done', 'blacklist');
CREATE CAST (varchar AS banner_list_type) WITH INOUT AS IMPLICIT;

CREATE TABLE "banner_settings" (
  "uuid" uuid NOT NULL,
  "banner" uuid NOT NULL,
  "user" text NOT NULL,
  "list_type" banner_list_type NOT NULL,
  "list_added" timestamp with time zone,
  PRIMARY KEY ("uuid"),
  UNIQUE ("user", "banner")
);
