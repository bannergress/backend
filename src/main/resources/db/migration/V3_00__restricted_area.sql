CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE "poi" ADD COLUMN "point" geography(POINT);
UPDATE "poi" SET "point" = CASE WHEN "latitude" IS NOT NULL AND "longitude" IS NOT NULL THEN st_makepoint("longitude", "latitude") END;
ALTER TABLE "poi" DROP COLUMN "latitude";
ALTER TABLE "poi" DROP COLUMN "longitude";

ALTER TABLE "poi_audit" ADD COLUMN "point" geography(POINT);
UPDATE "poi_audit" SET "point" = CASE WHEN "latitude" IS NOT NULL AND "longitude" IS NOT NULL THEN st_makepoint("longitude", "latitude") END;
ALTER TABLE "poi_audit" DROP COLUMN "latitude";
ALTER TABLE "poi_audit" DROP COLUMN "longitude";

CREATE TABLE "restricted_area" (
  "uuid" uuid NOT NULL,
  "title" text NOT NULL,
  "area" geography(MULTIPOLYGON) NOT NULL,
  "general_restriction" boolean NOT NULL,
  "general_restriction_description" text,
  "general_restriction_url" text,
  "timed_restriction" boolean NOT NULL,
  "timed_restriction_description" text,
  "timed_restriction_url" text,
  "monetary_restriction" boolean NOT NULL,
  "monetary_restriction_description" text,
  "monetary_restriction_url" text,
  "start_date" timestamp with time zone,
  "end_date" timestamp with time zone,
  PRIMARY KEY ("uuid")
);

CREATE TABLE "restricted_area_poi" (
  "restricted_area" uuid NOT NULL,
  "poi" text NOT NULL,
  PRIMARY KEY ("restricted_area", "poi"),
  FOREIGN KEY ("restricted_area") REFERENCES "restricted_area"("uuid"),
  FOREIGN KEY ("poi") REFERENCES "poi"("id")
);
CREATE INDEX ON "banner_start_place" ("place") INCLUDE ("banner");

CREATE TABLE "restricted_area_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "title" text NOT NULL,
  "area" geography(MULTIPOLYGON) NOT NULL,
  "general_restriction" boolean NOT NULL,
  "general_restriction_description" text,
  "general_restriction_url" text,
  "timed_restriction" boolean NOT NULL,
  "timed_restriction_description" text,
  "timed_restriction_url" text,
  "monetary_restriction" boolean NOT NULL,
  "monetary_restriction_description" text,
  "monetary_restriction_url" text,
  "start_date" timestamp with time zone,
  "end_date" timestamp with time zone,
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);
