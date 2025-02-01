CREATE TABLE IF NOT EXISTS media(
  media_name TEXT PRIMARY KEY NOT NULL,
  kinds TEXT NULL,
  mime_type TEXT NOT NULL,
  media_sources TEXT NOT NULL,
  media_count INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS media_key
ON media(media_name);
