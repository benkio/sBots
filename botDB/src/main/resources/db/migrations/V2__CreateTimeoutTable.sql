CREATE TABLE IF NOT EXISTS timeout(
  chat_id INTEGER PRIMARY KEY,
  timeout_value TEXT NOT NULL,
  last_interaction TEXT NOT NULL
);

CREATE UNIQUE INDEX timeout_key
ON timeout(chat_id);
