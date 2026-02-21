-- -----------------------------------------------------------------------------
-- Modifications to this file must be done carefully. The new-bot process
-- (sbt newBot) appends an INSERT for each new bot after the ytai row. When
-- editing, preserve that structure so adding future bots continues to work.
-- See docs/adding-a-bot.md.
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS bot(
  id TEXT NOT NULL,
  bot_name TEXT NOT NULL,
  bot_full_name TEXT NOT NULL,
  PRIMARY KEY(id)
);

INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('abar', 'ABarberoBot', 'Alessandro Barbero');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('cala', 'CalandroBot', 'Francesco Calandra');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('mos', 'M0sconiBot', 'Germano Mosconi');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('rphjb', 'RichardPHJBensonBot', 'Richard Philip Henry John Benson');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('xah', 'XahLeeBot', 'Xah Lee');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('ytai', 'YouTuboAncheI0Bot', 'Omar Palermo');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('pino', 'PinoScottoBot', 'Pino Scotto');
INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('test', 'testBot', 'Mario Rossi');
