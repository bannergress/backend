CREATE TABLE place_parenthood (
  parent text NOT NULL,
  child text NOT NULL,
  PRIMARY KEY (parent, child),
  FOREIGN KEY (parent) REFERENCES place (id),
  FOREIGN KEY (child) REFERENCES place (id)
);

INSERT INTO place_parenthood (
  parent,
  child
)
SELECT DISTINCT
  parent,
  child
FROM (
  SELECT
    p1.id AS parent,
    p2.id AS child,
    rank() OVER (PARTITION BY p2.id ORDER BY p1.type DESC) AS rnk
  FROM place p1
  JOIN banner_start_place bsp1 ON p1.id = bsp1.place 
  JOIN banner_start_place bsp2 ON bsp1.banner = bsp2.banner 
  JOIN place p2 ON bsp2.place = p2.id
  WHERE p1.type < p2.type
) x
WHERE rnk = 1;

ALTER TABLE place DROP COLUMN parent_place;

ALTER TABLE place ADD collapsed boolean;

UPDATE place p SET collapsed = false;

ALTER TABLE place ALTER COLUMN collapsed SET NOT NULL;
