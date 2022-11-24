CREATE TABLE IF NOT EXISTS timeout(
  chat_id BIGINT PRIMARY KEY NOT NULL,
  timeout_value TEXT NOT NULL,
  last_interaction TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS timeout_key
ON timeout(chat_id);
