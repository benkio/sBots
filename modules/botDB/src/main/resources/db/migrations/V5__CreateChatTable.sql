CREATE TABLE IF NOT EXISTS chat(
  platform TEXT NOT NULL,
  chat_id TEXT NOT NULL,
  chat_name TEXT NOT NULL,
  chat_url TEXT NOT NULL,
  PRIMARY KEY(platform, chat_id),
  FOREIGN KEY (platform) REFERENCES platform(id)
);
