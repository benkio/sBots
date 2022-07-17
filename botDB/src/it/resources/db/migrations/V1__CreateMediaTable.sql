CREATE TABLE media(
  media_name VARCHAR(255) PRIMARY KEY,
  kind VARCHAR(255) NULL,
  media_content LONGBLOB NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX media_key
ON media(media_name);
