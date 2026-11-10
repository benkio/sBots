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
DELETE FROM subscription; DELETE FROM timeout; DELETE FROM log; DELETE FROM chat;
INSERT INTO chat(chat_id, chat_name, chat_url) VALUES (-100123, 'group', 'https://example.org');
INSERT INTO timeout(chat_id, bot_id, timeout_value, last_interaction) VALUES (-100123, '$BOT', '00:00:10.000', '1700000000');
INSERT INTO log(log_time, message) VALUES (1700000000, 'a log');
INSERT INTO subscription(subscription_id, chat_id, bot_id, subscription_type, target, cron, subscribed_at)
  VALUES ('11111111-1111-1111-1111-111111111111', -100123, '$BOT', 'calendar', NULL, '0 0 9 * * ?', '1700000000');
INSERT INTO subscription(subscription_id, chat_id, bot_id, subscription_type, target, cron, subscribed_at)
  VALUES ('22222222-2222-2222-2222-222222222222', -100123, '$BOT', 'show', 'pino scotto', '0 30 9 * * ?', '1700000000');
UPDATE media SET media_count = 4242 WHERE media_name = '$MEDIA';
SQL

sh "$ROOT/scripts/updateProdDB.sh" "$WORK" > /dev/null

q() { sqlite3 "$WORK/botDB.sqlite3" "$1"; }
[ "$(q "SELECT count(*) FROM subscription")" = 2 ] || fail "subscriptions not carried over"
[ "$(q "SELECT subscription_type FROM subscription WHERE subscription_id LIKE '1111%'")" = calendar ] || fail "subscription type lost"
[ "$(q "SELECT target FROM subscription WHERE subscription_id LIKE '2222%'")" = "pino scotto" ] || fail "subscription target lost"
[ "$(q "SELECT count(*) FROM timeout")" = 1 ] || fail "timeouts not carried over"
[ "$(q "SELECT count(*) FROM log")" = 1 ] || fail "logs not carried over"
[ "$(q "SELECT count(*) FROM chat WHERE chat_id = -100123")" = 1 ] || fail "chat not carried over"
[ "$(q "SELECT media_count FROM media WHERE media_name = '$MEDIA'")" = 4242 ] || fail "media_count not carried over"
[ -f "$WORK/botDB.sqlite3.bak" ] || fail "previous production DB not kept as backup"

# a failing migration must leave the production DB untouched
cp "$WORK/botDB.sqlite3" "$WORK/prod.expected"
cp "$GENERATED" "$WORK/botDBBase.sqlite3"
echo "THIS IS NOT SQL;" > "$WORK/DBMigration.sql"
if sh "$ROOT/scripts/updateProdDB.sh" "$WORK" > /dev/null 2>&1; then fail "a broken migration did not fail"; fi
cmp -s "$WORK/botDB.sqlite3" "$WORK/prod.expected" || fail "the production DB was modified by a failed migration"

echo "[checkDBMigration] OK"
