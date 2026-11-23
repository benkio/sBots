ATTACH DATABASE 'botDBProdOld.sqlite3' AS prodDB;

-- Main database is botDBBase.sqlite3 (latest generated schema).
-- Keep generated media/show datasets, but preserve media_count from old prod when key matches.
UPDATE media
SET media_count = (
    SELECT p.media_count
    FROM prodDB.media p
    WHERE p.media_name = media.media_name
)
WHERE EXISTS (
    SELECT 1
    FROM prodDB.media p
    WHERE p.media_name = media.media_name
);

-- Preserve runtime/operational tables from old prod.
INSERT OR REPLACE INTO timeout(platform, chat_id, bot_id, timeout_value, last_interaction)
SELECT platform, chat_id, bot_id, timeout_value, last_interaction
FROM prodDB.timeout;

INSERT OR REPLACE INTO chat(platform, chat_id, chat_name, chat_url)
SELECT platform, chat_id, chat_name, chat_url
FROM prodDB.chat;

-- subscription_type and target must be copied: without them every subscription falls back to the default type `show`.
INSERT OR REPLACE INTO subscription(subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at)
SELECT subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at
FROM prodDB.subscription;

-- The LLM tables arrive with V13: a production DB from the previous deploy has no such tables. Create them in the
-- attached old DB when missing (that file is replaced by the .bak at the end of updateProdDB.sh) so the copy below
-- works on both an upgraded and a first-time deployment.
CREATE TABLE IF NOT EXISTS prodDB.llm_chat_settings(
  platform TEXT NOT NULL,
  chat_id TEXT NOT NULL,
  bot_id TEXT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT 0,
  updated_at INTEGER NOT NULL,
  PRIMARY KEY (platform, chat_id, bot_id)
);

CREATE TABLE IF NOT EXISTS prodDB.llm_context(
  platform TEXT NOT NULL,
  chat_id TEXT NOT NULL,
  bot_id TEXT NOT NULL,
  role TEXT NOT NULL,
  content TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

INSERT OR REPLACE INTO llm_chat_settings(platform, chat_id, bot_id, enabled, updated_at)
SELECT platform, chat_id, bot_id, enabled, updated_at
FROM prodDB.llm_chat_settings;

INSERT OR REPLACE INTO llm_context(platform, chat_id, bot_id, role, content, created_at)
SELECT platform, chat_id, bot_id, role, content, created_at
FROM prodDB.llm_context;

DETACH DATABASE prodDB;
