ALTER TABLE banner DROP COLUMN complete;
ALTER TABLE banner ADD COLUMN number_of_submitted_missions integer;
ALTER TABLE banner ADD COLUMN number_of_disabled_missions integer;
UPDATE banner b SET
  number_of_submitted_missions = 0,
  number_of_disabled_missions = (
    SELECT COUNT(*) FROM banner_mission bm
    JOIN mission m ON bm.mission = m.id
    WHERE bm.banner = b.uuid AND m.status = 'disabled'
  );
ALTER TABLE BANNER ALTER COLUMN number_of_submitted_missions SET NOT NULL;
ALTER TABLE BANNER ALTER COLUMN number_of_disabled_missions SET NOT NULL;

CREATE TABLE banner_placeholder (
  banner uuid NOT NULL,
  position integer NOT NULL,
  PRIMARY KEY (banner, position),
  FOREIGN KEY (banner) REFERENCES banner (uuid)
);

CREATE TABLE banner_placeholder_audit (
  rev integer NOT NULL,
  banner uuid NOT NULL,
  position integer NOT NULL,
  revtype smallint,
  PRIMARY KEY (rev, banner, position),
  FOREIGN KEY (rev) REFERENCES revision (id)
);
