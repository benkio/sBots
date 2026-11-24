-- Bounded debug trace of the LLM completions (#1493). Rows are written only when llm.trace.enabled is on and dropped
-- by the TTL cleanup (llm.trace.ttl), so the table cannot grow without bound. `chat_hash` is the salted hash used in
-- the interaction log lines: a weird reply seen in Loki is joined to its row by (chat_hash, bot_id, created_at).
-- `prompt` and `response` hold chat content: that is why the trace is opt-in. `created_at` is epoch seconds.
CREATE TABLE IF NOT EXISTS llm_trace (
  trace_id       INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at     INTEGER NOT NULL,
  chat_hash      TEXT    NOT NULL,
  bot_id         TEXT    NOT NULL,
  prompt_version TEXT    NOT NULL,
  model          TEXT,
  status         TEXT    NOT NULL,
  action         TEXT    NOT NULL,
  prompt         TEXT    NOT NULL,
  response       TEXT,
  FOREIGN KEY (bot_id) REFERENCES bot(id)
);

CREATE INDEX IF NOT EXISTS llm_trace_created_at
  ON llm_trace (created_at);
