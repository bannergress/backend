CREATE EXTENSION IF NOT EXISTS pg_trgm;


CREATE SEQUENCE "hibernate_sequence";


CREATE TYPE banner_type AS ENUM ('sequential', 'anyOrder');
CREATE CAST (varchar AS banner_type) WITH INOUT AS IMPLICIT;

CREATE TYPE faction AS ENUM ('enlightened', 'resistance');
CREATE CAST (varchar AS faction) WITH INOUT AS IMPLICIT;

CREATE TYPE mission_type AS ENUM ('sequential', 'anyOrder', 'hidden');
CREATE CAST (varchar AS mission_type) WITH INOUT AS IMPLICIT;

CREATE TYPE objective AS ENUM ('hack', 'captureOrUpgrade', 'createLink', 'createField', 'installMod', 'takePhoto', 'viewWaypoint', 'enterPassphrase');
CREATE CAST (varchar AS objective) WITH INOUT AS IMPLICIT;

CREATE TYPE place_type AS ENUM ('country', 'administrative_area_level_1', 'administrative_area_level_2', 'administrative_area_level_3', 'administrative_area_level_4', 'administrative_area_level_5', 'locality');
CREATE CAST (varchar AS place_type) WITH INOUT AS IMPLICIT;

CREATE TYPE poi_type AS ENUM ('portal', 'fieldTripWaypoint', 'unavailable');
CREATE CAST (varchar AS poi_type) WITH INOUT AS IMPLICIT;


CREATE TABLE "banner_picture" (
  "hash" text NOT NULL,
  "picture" "oid" NOT NULL,
  "expiration"  timestamp with time zone,
  PRIMARY KEY ("hash")
);


CREATE TABLE "banner" (
  "uuid" uuid NOT NULL,
  "complete" boolean NOT NULL,
  "created" timestamp with time zone NOT NULL,
  "description" text,
  "length_meters" integer,
  "width" integer NOT NULL,
  "number_of_missions" integer NOT NULL,
  "online" boolean NOT NULL,
  "start_latitude" numeric(8,6),
  "start_longitude" numeric(9,6),
  "title" text NOT NULL,
  "picture" text,
  "type" banner_type NOT NULL,
  PRIMARY KEY ("uuid"),
  FOREIGN KEY ("picture") REFERENCES "banner_picture"("hash")
);
CREATE INDEX ON "banner" ("created" DESC);
CREATE INDEX ON "banner" ("start_latitude", "start_longitude");
CREATE INDEX ON "banner" USING gin ((lower("title")) gin_trgm_ops);


CREATE TABLE "place" (
  "id" text NOT NULL,
  "type" place_type NOT NULL,
  "number_of_banners" integer NOT NULL,
  "boundary_min_latitude" numeric(8,6) NOT NULL,
  "boundary_min_longitude" numeric(9,6) NOT NULL,
  "boundary_max_latitude" numeric(8,6) NOT NULL,
  "boundary_max_longitude" numeric(9,6) NOT NULL,
  "parent_place" text,
  PRIMARY KEY ("id"),
  FOREIGN KEY ("parent_place") REFERENCES "place"("id")
);
CREATE INDEX ON "place" ("type");
CREATE INDEX ON "place" ("parent_place");


CREATE TABLE "place_information" (
  "uuid" uuid NOT NULL,
  "formatted_address" text NOT NULL,
  "language_code" text NOT NULL,
  "long_name" text NOT NULL,
  "short_name" text NOT NULL,
  "place" text NOT NULL,
  PRIMARY KEY ("uuid"),
  UNIQUE ("place", "language_code"),
  FOREIGN KEY ("place") REFERENCES "place"("id")
);
CREATE INDEX ON "place_information" USING gin ((lower("long_name")) gin_trgm_ops);
CREATE INDEX ON "place_information" USING gin ((lower("formatted_address")) gin_trgm_ops);


CREATE TABLE "place_coordinate" (
  "uuid" uuid NOT NULL,
  "place" text NOT NULL,
  "latitude" numeric(8,6) NOT NULL,
  "longitude" numeric(9,6) NOT NULL,
  PRIMARY KEY ("uuid"),
  UNIQUE ("latitude", "longitude", "place"),
  FOREIGN KEY ("place") REFERENCES "place"("id")
);


CREATE TABLE "banner_start_place" (
  "banner" uuid NOT NULL,
  "place" text NOT NULL,
  PRIMARY KEY ("banner", "place"),
  FOREIGN KEY ("banner") REFERENCES "banner"("uuid"),
  FOREIGN KEY ("place") REFERENCES "place"("id")
);
CREATE INDEX ON "banner_start_place" ("place") INCLUDE ("banner");


CREATE TABLE "poi" (
  "id" text NOT NULL,
  "latitude" numeric(8,6),
  "longitude" numeric(9,6),
  "picture_url" text,
  "title" text,
  "type" poi_type NOT NULL,
  PRIMARY KEY ("id")
);


CREATE TABLE "named_agent" (
  "name" text NOT NULL,
  "faction" faction NOT NULL,
  PRIMARY KEY ("name") INCLUDE ("faction")
);


CREATE TABLE "mission" (
  "id" text NOT NULL,
  "average_duration_milliseconds" bigint NOT NULL,
  "description" text,
  "latest_update_details" timestamp with time zone,
  "latest_update_summary" timestamp with time zone,
  "number_completed" integer,
  "online" boolean NOT NULL,
  "picture_url" text NOT NULL,
  "rating" numeric(7,6) NOT NULL,
  "title" text NOT NULL,
  "type" mission_type,
  "author" text,
  PRIMARY KEY ("id"),
  FOREIGN KEY ("author") REFERENCES "named_agent"("name")
);
CREATE INDEX ON "mission" USING gin ((lower("title")) gin_trgm_ops);
CREATE INDEX ON "mission" ("author");
CREATE INDEX ON "mission" ((lower("author")));


CREATE TABLE "mission_step" (
  "uuid" uuid NOT NULL,
  "objective" objective,
  "mission" text,
  "poi" text,
  "position" integer,
  PRIMARY KEY ("uuid"),
  FOREIGN KEY ("mission") REFERENCES "mission"("id"),
  FOREIGN KEY ("poi") REFERENCES "poi"("id")
);
CREATE INDEX ON "mission_step" ("mission") INCLUDE ("poi", "uuid", "objective", "position");
CREATE INDEX ON "mission_step" ("poi");


CREATE TABLE "banner_mission" (
  "banner" uuid NOT NULL,
  "mission" text NOT NULL,
  "position" integer NOT NULL,
  PRIMARY KEY ("banner", "position") INCLUDE ("mission"),
  FOREIGN KEY ("banner") REFERENCES "banner"("uuid"),
  FOREIGN KEY ("mission") REFERENCES "mission"("id")
);
CREATE INDEX ON "banner_mission" ("mission") INCLUDE ("banner", "position");


CREATE TABLE "news" (
  "uuid" uuid NOT NULL,
  "content" text NOT NULL,
  "created" timestamp with time zone NOT NULL,
  PRIMARY KEY ("uuid")
);


CREATE TABLE "revision" (
  "id" integer NOT NULL,
  "created" timestamp with time zone NOT NULL,
  "userid" text,
  PRIMARY KEY ("id")
);


CREATE TABLE "banner_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "description" text,
  "title" text,
  "type" banner_type,
  "width" integer NOT NULL,
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "banner_mission_audit" (
  "rev" integer NOT NULL,
  "banner" uuid NOT NULL,
  "mission" text NOT NULL,
  "position" integer NOT NULL,
  "revtype" smallint,
  PRIMARY KEY ("rev", "banner", "mission", "position"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "mission_audit" (
  "id" text NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "description" text,
  "online" boolean,
  "picture_url" text,
  "title" text,
  "type" mission_type,
  "author" text,
  PRIMARY KEY ("id", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "mission_step_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "objective" objective,
  "position" integer,
  "mission" text,
  "poi" text,
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "named_agent_audit" (
  "name" text NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "faction" faction,
  PRIMARY KEY ("name", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "poi_audit" (
  "id" text NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "latitude" numeric(8,6),
  "longitude" numeric(9,6),
  "picture_url" text,
  "title" text,
  "type" poi_type,
  PRIMARY KEY ("id", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);


CREATE TABLE "news_audit" (
  "uuid" uuid NOT NULL,
  "rev" integer NOT NULL,
  "revtype" smallint,
  "content" text,
  PRIMARY KEY ("uuid", "rev"),
  FOREIGN KEY ("rev") REFERENCES "revision"("id")
);
