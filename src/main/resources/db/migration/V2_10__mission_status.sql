CREATE TYPE mission_status AS ENUM ('submitted', 'published', 'disabled');
CREATE CAST (varchar AS mission_status) WITH INOUT AS IMPLICIT;

ALTER TABLE mission ADD COLUMN status mission_status;
UPDATE mission SET status = (CASE WHEN online THEN 'published' ELSE 'disabled' END)::mission_status;
ALTER TABLE mission ALTER COLUMN status SET NOT NULL;
ALTER TABLE mission DROP COLUMN online;

ALTER TABLE mission_audit ADD COLUMN status mission_status;
UPDATE mission_audit SET status = (CASE WHEN online THEN 'published' ELSE 'disabled' END)::mission_status;
ALTER TABLE mission_audit DROP COLUMN online;
