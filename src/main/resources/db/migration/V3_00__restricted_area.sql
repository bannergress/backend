CREATE TYPE restriction_type AS ENUM ('monetary', 'temporal', 'audience');

CREATE TABLE "restricted_area" (
  "uuid" uuid NOT NULL,
  "title" text NOT NULL,
  "description" text,
  "area" geography(MULTIPOLYGON) NOT NULL,
  "ingress_relevant_area" geography(MULTIPOLYGON) NOT NULL,
  PRIMARY KEY ("uuid")
);

CREATE INDEX ON "restricted_area" USING GIST ("ingress_relevant_area");

CREATE TABLE "restricted_area_restriction" (
  "area" uuid NOT NULL,
  "restriction" restriction_type NOT NULL,
  PRIMARY KEY ("area", "restriction"),
  FOREIGN KEY ("area") REFERENCES "restricted_area" ("uuid")
);

CREATE TABLE "restricted_area_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint NOT NULL,
  "title" text,
  "description" text,
  "area" geography(MULTIPOLYGON),
  "ingress_relevant_area" geography(MULTIPOLYGON),
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);

CREATE TABLE "restricted_area_restriction_audit" (
  "area" uuid NOT NULL,
  "restriction" restriction_type NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint NOT NULL,
  PRIMARY KEY ("area", "restriction", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);
