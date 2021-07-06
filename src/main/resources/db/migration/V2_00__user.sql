CREATE TABLE "user" (
  "id" text NOT NULL,
  "verification_agent" text,
  "verification_token" uuid,
  PRIMARY KEY ("id")
);
