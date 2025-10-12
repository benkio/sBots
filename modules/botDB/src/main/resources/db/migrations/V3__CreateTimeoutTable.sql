CREATE TABLE IF NOT EXISTS timeout(
  chat_id BIGINT NOT NULL,
  bot_id TEXT NOT NULL,
  timeout_value TEXT NOT NULL,
  last_interaction TEXT NOT NULL,
  PRIMARY KEY(chat_id, bot_id)
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS timeout_key
ON timeout(chat_id, bot_id);
