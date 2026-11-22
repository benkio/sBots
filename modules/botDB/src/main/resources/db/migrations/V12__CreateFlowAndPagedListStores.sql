CREATE TABLE IF NOT EXISTS paged_list_text (
  platform         TEXT    NOT NULL,
  chat_id          TEXT    NOT NULL,
  bot_id           TEXT    NOT NULL,
  command_key      TEXT    NOT NULL,
  page             INTEGER NOT NULL,
  page_items_json  TEXT    NOT NULL,
  request_json     TEXT    NOT NULL,
  saved_at_millis  INTEGER NOT NULL,
  PRIMARY KEY (platform, chat_id, bot_id),
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE TABLE IF NOT EXISTS subscription_draft (
  platform          TEXT    NOT NULL,
  chat_id           TEXT    NOT NULL,
  bot_id            TEXT    NOT NULL,
  requester         TEXT    NOT NULL,
  step              TEXT    NOT NULL,
  subscription_type TEXT    NULL,
  schedule          TEXT    NULL,
  target            TEXT    NULL,
  saved_at_millis   INTEGER NOT NULL,
  PRIMARY KEY (platform, chat_id, bot_id, requester),
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE TABLE IF NOT EXISTS subscription_flow_prompt (
  platform         TEXT    NOT NULL,
  chat_id          TEXT    NOT NULL,
  bot_id           TEXT    NOT NULL,
  requester        TEXT    NOT NULL,
  message_id       TEXT    NOT NULL,
  owner            TEXT    NOT NULL,
  saved_at_millis  INTEGER NOT NULL,
  PRIMARY KEY (platform, chat_id, bot_id, requester),
  FOREIGN KEY (platform) REFERENCES platform(id),
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS subscription_flow_prompt_message_idx
  ON subscription_flow_prompt (platform, chat_id, message_id);
