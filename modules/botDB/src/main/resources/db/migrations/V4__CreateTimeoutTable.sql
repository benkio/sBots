CREATE TABLE IF NOT EXISTS timeout(
  platform TEXT NOT NULL,
  chat_id TEXT NOT NULL,
  bot_id TEXT NOT NULL,
  timeout_value TEXT NOT NULL,
  last_interaction TEXT NOT NULL,
  PRIMARY KEY(platform, chat_id, bot_id),
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);
