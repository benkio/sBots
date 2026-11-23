#!/bin/sh
# Checks DBMigration.sql + updateProdDB.sh on a throwaway copy: a fake "production" DB (the given generated DB plus
# runtime data) is migrated onto a copy of the generated DB, and the runtime data must come out unchanged.
# Usage (from the repo root): scripts/checkDBMigration.sh [generated DB, default botDB.sqlite3]
set -eu
ROOT=$(pwd)
GENERATED=${1:-botDB.sqlite3}
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
fail() { echo "[checkDBMigration] FAILED: $*" >&2; exit 1; }

cp "$GENERATED" "$WORK/botDB.sqlite3"          # fake production
cp "$GENERATED" "$WORK/botDBBase.sqlite3"      # new DB
cp "$ROOT/DBMigration.sql" "$WORK/DBMigration.sql"

BOT=$(sqlite3 "$WORK/botDB.sqlite3" "SELECT id FROM bot LIMIT 1")
MEDIA=$(sqlite3 "$WORK/botDB.sqlite3" "SELECT media_name FROM media LIMIT 1")
sqlite3 "$WORK/botDB.sqlite3" <<SQL
DELETE FROM subscription; DELETE FROM timeout; DELETE FROM chat;
DELETE FROM llm_chat_settings; DELETE FROM llm_context;
INSERT INTO chat(platform, chat_id, chat_name, chat_url) VALUES ('telegram', '-100123', 'group', 'https://example.org');
-- the production DB still has the log table dropped by V11: the migration must not carry it over
CREATE TABLE IF NOT EXISTS log(log_time BIGINT NOT NULL, message TEXT NOT NULL, PRIMARY KEY(log_time));
INSERT INTO log(log_time, message) VALUES (1700000000, 'a log');
INSERT INTO timeout(platform, chat_id, bot_id, timeout_value, last_interaction) VALUES ('telegram', '-100123', '$BOT', '00:00:10.000', '1700000000');
-- same chat id on another platform: must stay a different row
INSERT INTO timeout(platform, chat_id, bot_id, timeout_value, last_interaction) VALUES ('irc', '-100123', '$BOT', '00:00:20.000', '1700000000');
INSERT INTO subscription(subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at)
  VALUES ('11111111-1111-1111-1111-111111111111', 'telegram', '-100123', '$BOT', 'calendar', NULL, '0 0 9 * * ?', '1700000000');
INSERT INTO subscription(subscription_id, platform, chat_id, bot_id, subscription_type, target, cron, subscribed_at)
  VALUES ('22222222-2222-2222-2222-222222222222', 'irc', '#sbots', '$BOT', 'show', 'pino scotto', '0 30 9 * * ?', '1700000000');
INSERT INTO llm_chat_settings(platform, chat_id, bot_id, enabled, updated_at) VALUES ('telegram', '-100123', '$BOT', 1, 1700000000);
INSERT INTO llm_chat_settings(platform, chat_id, bot_id, enabled, updated_at) VALUES ('irc', '-100123', '$BOT', 0, 1700000000);
INSERT INTO llm_context(platform, chat_id, bot_id, role, content, created_at) VALUES ('telegram', '-100123', '$BOT', 'user', 'ciao', 1700000000);
INSERT INTO llm_context(platform, chat_id, bot_id, role, content, created_at) VALUES ('telegram', '-100123', '$BOT', 'assistant', 'buongiorno', 1700000001);
UPDATE media SET media_count = 4242 WHERE media_name = '$MEDIA';
SQL

sh "$ROOT/scripts/updateProdDB.sh" "$WORK" > /dev/null

q() { sqlite3 "$WORK/botDB.sqlite3" "$1"; }
[ "$(q "SELECT count(*) FROM subscription")" = 2 ] || fail "subscriptions not carried over"
[ "$(q "SELECT subscription_type FROM subscription WHERE subscription_id LIKE '1111%'")" = calendar ] || fail "subscription type lost"
[ "$(q "SELECT target FROM subscription WHERE subscription_id LIKE '2222%'")" = "pino scotto" ] || fail "subscription target lost"
[ "$(q "SELECT count(*) FROM timeout")" = 2 ] || fail "timeouts not carried over"
[ "$(q "SELECT timeout_value FROM timeout WHERE platform = 'irc' AND chat_id = '-100123'")" = "00:00:20.000" ] || fail "platform lost: the same chat id on two platforms was merged"
[ "$(q "SELECT platform FROM subscription WHERE subscription_id LIKE '2222%'")" = irc ] || fail "subscription platform lost"
[ "$(q "SELECT count(*) FROM sqlite_master WHERE name = 'log'")" = 0 ] || fail "the dropped log table is still there"
[ "$(q "SELECT count(*) FROM chat WHERE platform = 'telegram' AND chat_id = '-100123'")" = 1 ] || fail "chat not carried over"
[ "$(q "SELECT media_count FROM media WHERE media_name = '$MEDIA'")" = 4242 ] || fail "media_count not carried over"
[ "$(q "SELECT count(*) FROM llm_chat_settings")" = 2 ] || fail "LLM settings not carried over"
[ "$(q "SELECT enabled FROM llm_chat_settings WHERE platform = 'irc' AND chat_id = '-100123'")" = 0 ] || fail "LLM settings platform lost"
[ "$(q "SELECT updated_at FROM llm_chat_settings WHERE platform = 'telegram' AND chat_id = '-100123'")" = 1700000000 ] || fail "LLM settings updated_at lost"
[ "$(q "SELECT count(*) FROM llm_context")" = 2 ] || fail "LLM context not carried over"
[ "$(q "SELECT content FROM llm_context WHERE role = 'assistant'")" = "buongiorno" ] || fail "LLM context content lost"
[ "$(q "SELECT created_at FROM llm_context WHERE role = 'assistant'")" = 1700000001 ] || fail "LLM context created_at lost"
[ -f "$WORK/botDB.sqlite3.bak" ] || fail "previous production DB not kept as backup"

# a production DB from before V13 has no LLM tables: the guarded copy must still succeed, with empty tables
cp "$GENERATED" "$WORK/botDBBase.sqlite3"
sqlite3 "$WORK/botDB.sqlite3" "DROP TABLE llm_chat_settings; DROP TABLE llm_context;"
sh "$ROOT/scripts/updateProdDB.sh" "$WORK" > /dev/null
[ "$(q "SELECT count(*) FROM llm_chat_settings")" = 0 ] || fail "LLM settings invented for a legacy prod DB"
[ "$(q "SELECT count(*) FROM llm_context")" = 0 ] || fail "LLM context invented for a legacy prod DB"

# a failing migration must leave the production DB untouched
cp "$WORK/botDB.sqlite3" "$WORK/prod.expected"
cp "$GENERATED" "$WORK/botDBBase.sqlite3"
echo "THIS IS NOT SQL;" > "$WORK/DBMigration.sql"
if sh "$ROOT/scripts/updateProdDB.sh" "$WORK" > /dev/null 2>&1; then fail "a broken migration did not fail"; fi
cmp -s "$WORK/botDB.sqlite3" "$WORK/prod.expected" || fail "the production DB was modified by a failed migration"

echo "[checkDBMigration] OK"
