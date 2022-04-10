ALTER TABLE banner ADD COLUMN warning text;
ALTER TABLE banner ADD COLUMN planned_offline_date date;

ALTER TABLE banner_audit ADD COLUMN warning text;
ALTER TABLE banner_audit ADD COLUMN planned_offline_date date;
