CREATE TABLE IF NOT EXISTS subscription(
  subscription_id TEXT PRIMARY KEY NOT NULL,
  chat_id BIGINT NOT NULL,
  bot_id TEXT NOT NULL,
  cron TEXT NOT NULL,
  subscribed_at TEXT NOT NULL,
  FOREIGN KEY(chat_id) REFERENCES chat(chat_id)
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);
