ALTER TABLE banner ADD COLUMN event_start_date date;
ALTER TABLE banner ADD COLUMN event_start_timestamp timestamp with time zone;
ALTER TABLE banner ADD COLUMN event_end_date date;
ALTER TABLE banner ADD COLUMN event_end_timestamp timestamp with time zone;

ALTER TABLE banner_audit ADD COLUMN event_start_date date;
ALTER TABLE banner_audit ADD COLUMN event_end_date date;
