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
INSERT OR REPLACE INTO timeout(chat_id, bot_id, timeout_value, last_interaction)
SELECT chat_id, bot_id, timeout_value, last_interaction
FROM prodDB.timeout;

INSERT OR REPLACE INTO chat(chat_id, chat_name, chat_url)
SELECT chat_id, chat_name, chat_url
FROM prodDB.chat;

INSERT OR REPLACE INTO subscription(subscription_id, chat_id, bot_id, cron, subscribed_at)
SELECT subscription_id, chat_id, bot_id, cron, subscribed_at
FROM prodDB.subscription;

INSERT OR REPLACE INTO log(log_time, message)
SELECT log_time, message
FROM prodDB.log;

DETACH DATABASE prodDB;
