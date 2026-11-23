-- Per-chat LLM settings and context (#1487). The issue asked for V11, but V11 and V12 are already applied in
-- production and Flyway runs with outOfOrder(false): V13 is the first free version.
--
-- One settings row per (platform, chat, bot). `enabled` defaults to off: the /llm command turns it on (#1488) and
-- `forget` drops the chat's context rows but keeps the switch. `updated_at` is epoch seconds.
CREATE TABLE IF NOT EXISTS llm_chat_settings (
  platform   TEXT    NOT NULL,
  chat_id    TEXT    NOT NULL,
  bot_id     TEXT    NOT NULL,
  enabled    BOOLEAN NOT NULL DEFAULT 0,
  updated_at INTEGER NOT NULL,
  PRIMARY KEY (platform, chat_id, bot_id),
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

-- The retained context of a chat (#1487). The rolling window, TTL cleanup and anonymous sender labels come with #1489;
-- this migration only stores the messages. There is no artificial id: rows are ordered and removed by `created_at`
-- (epoch seconds), and a whole chat is dropped by `forget`.
CREATE TABLE IF NOT EXISTS llm_context (
  platform   TEXT    NOT NULL,
  chat_id    TEXT    NOT NULL,
  bot_id     TEXT    NOT NULL,
  role       TEXT    NOT NULL,
  content    TEXT    NOT NULL,
  created_at INTEGER NOT NULL,
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE INDEX IF NOT EXISTS llm_context_chat_created
  ON llm_context (platform, chat_id, bot_id, created_at);

CREATE INDEX IF NOT EXISTS llm_context_created_at
  ON llm_context (created_at);
