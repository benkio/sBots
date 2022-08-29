CREATE TABLE IF NOT EXISTS media(
  media_name TEXT PRIMARY KEY,
  kind TEXT NULL,
  media_url TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX media_key
ON media(media_name);
