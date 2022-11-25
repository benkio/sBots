PRAGMA foreign_keys=off;

CREATE TABLE IF NOT EXISTS temp_media(
  media_name TEXT PRIMARY KEY NOT NULL,
  kind TEXT NULL,
  media_url TEXT NOT NULL,
  media_count INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS media_key
ON temp_media(media_name);

INSERT INTO temp_media(media_name, kind, media_url, media_count, created_at)
SELECT media_name, kind, media_url, 0, created_at
FROM media;

DROP TABLE media;

ALTER TABLE temp_media RENAME TO media; 

PRAGMA foreign_keys=on;
