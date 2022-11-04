CREATE TABLE IF NOT EXISTS subscription(
  subscription_id INTEGER PRIMARY KEY,
  chat_id INTEGER NOT NULL,
  cron TEXT NOT NULL,
  subscribed_at TEXT NOT NULL,
  FOREIGN KEY(chat_id) REFERENCES chat(chat_id)
);
