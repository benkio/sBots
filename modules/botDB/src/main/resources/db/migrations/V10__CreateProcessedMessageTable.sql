-- Incoming messages already handled by a bot: a redelivery of the same message must not be answered twice.
-- Rows are short lived, they are deleted after a retention period (see DBProcessedMessage).
CREATE TABLE IF NOT EXISTS processed_message(
  platform TEXT NOT NULL,
  bot_id TEXT NOT NULL,
  chat_id TEXT NOT NULL,
  message_id TEXT NOT NULL,
  processed_at INTEGER NOT NULL,
  PRIMARY KEY(platform, bot_id, chat_id, message_id)
);

CREATE INDEX IF NOT EXISTS processed_message_processed_at
ON processed_message(processed_at);
