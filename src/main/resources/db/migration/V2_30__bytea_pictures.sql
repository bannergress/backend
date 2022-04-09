ALTER TABLE banner_picture RENAME COLUMN picture TO picture_oid;
ALTER TABLE banner_picture ADD COLUMN picture bytea;

UPDATE banner_picture SET picture = lo_get(picture_oid);

ALTER TABLE banner_picture ALTER COLUMN picture SET NOT NULL;
ALTER TABLE banner_picture DROP COLUMN picture_oid;
