CREATE TABLE IF NOT EXISTS subscription (
    subscription_id TEXT PRIMARY KEY NOT NULL,
    platform TEXT NOT NULL,
    chat_id TEXT NOT NULL,
    bot_id TEXT NOT NULL,
    subscription_type TEXT NOT NULL DEFAULT 'show' CHECK (subscription_type IN ('show', 'trigger', 'calendar')),
    target TEXT NULL,
    cron TEXT NOT NULL,
    subscribed_at TEXT NOT NULL,
    FOREIGN KEY (platform) REFERENCES platform (id),
    FOREIGN KEY (bot_id) REFERENCES bot (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS subscription_one_calendar_per_chat_bot ON subscription (
    platform, chat_id, bot_id
) WHERE subscription_type = 'calendar';
