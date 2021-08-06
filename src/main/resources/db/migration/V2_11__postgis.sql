CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE banner ADD COLUMN start_point geometry(point, 4326);
UPDATE banner SET start_point = CASE WHEN start_latitude IS NOT NULL AND start_longitude IS NOT NULL THEN st_makepoint(start_longitude, start_latitude) END;
ALTER TABLE banner DROP COLUMN start_longitude;
ALTER TABLE banner DROP COLUMN start_latitude;
CREATE INDEX ON banner USING GIST (start_point);

ALTER TABLE poi ADD COLUMN point geometry(point, 4326);
UPDATE poi SET point = CASE WHEN latitude IS NOT NULL AND longitude IS NOT NULL THEN st_makepoint(longitude, latitude) END;
ALTER TABLE poi DROP COLUMN longitude;
ALTER TABLE poi DROP COLUMN latitude;
CREATE INDEX ON poi USING GIST (point);

ALTER TABLE poi_audit ADD COLUMN point geometry(point, 4326);
UPDATE poi_audit SET point = CASE WHEN latitude IS NOT NULL AND longitude IS NOT NULL THEN st_makepoint(longitude, latitude) END;
ALTER TABLE poi_audit DROP COLUMN longitude;
ALTER TABLE poi_audit DROP COLUMN latitude;

ALTER TABLE place_coordinate ADD COLUMN point geometry(point, 4326);
UPDATE place_coordinate SET point = st_makepoint(longitude, latitude);
ALTER TABLE place_coordinate DROP COLUMN longitude;
ALTER TABLE place_coordinate DROP COLUMN latitude;
CREATE INDEX ON place_coordinate USING GIST (point);
