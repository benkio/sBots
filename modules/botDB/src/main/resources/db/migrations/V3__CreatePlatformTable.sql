CREATE TABLE IF NOT EXISTS platform(
  id TEXT PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  url TEXT NOT NULL
);

INSERT OR REPLACE INTO platform (id, name, url) VALUES ('telegram', 'Telegram', 'https://telegram.org/');

INSERT OR REPLACE INTO platform (id, name, url) VALUES ('discord', 'Discord', 'https://discord.com/');

INSERT OR REPLACE INTO platform (id, name, url) VALUES ('irc', 'IRC', 'https://en.wikipedia.org/wiki/Internet_Relay_Chat');

INSERT OR REPLACE INTO platform (id, name, url) VALUES ('slack', 'Slack', 'https://slack.com/');
