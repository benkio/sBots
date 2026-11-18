#!/bin/sh
# Probes the bots from the outside and writes what is wrong to $SUMMARY_FILE (one "- ..." line per problem).
# Exits 1 when there is a problem, 0 and an empty summary when everything is healthy.
#
# Telegram bots: getMe (the token is valid) and getWebhookInfo (a recent delivery error, too many pending updates).
# Discord bots: the token is accepted by the API.
#
# Environment:
#   TELEGRAM_BOTS      space separated `<name>:<token>` entries
#   DISCORD_BOTS       space separated `<name>:<token>` entries
#   PENDING_THRESHOLD  pending updates above this are a problem (default 50)
#   ERROR_MAX_AGE      a webhook delivery error newer than this many seconds is a problem (default 2700)
#   SUMMARY_FILE       where the problems are written (default health-summary.txt)
#   TELEGRAM_API_URL, DISCORD_API_URL  the API bases (the defaults are the real ones; tests point them elsewhere)
#
# A token is never printed: the problems name the bot only.

set -u

TELEGRAM_API_URL=${TELEGRAM_API_URL:-https://api.telegram.org}
DISCORD_API_URL=${DISCORD_API_URL:-https://discord.com/api/v10}
PENDING_THRESHOLD=${PENDING_THRESHOLD:-50}
ERROR_MAX_AGE=${ERROR_MAX_AGE:-2700}
SUMMARY_FILE=${SUMMARY_FILE:-health-summary.txt}
TELEGRAM_BOTS=${TELEGRAM_BOTS:-}
DISCORD_BOTS=${DISCORD_BOTS:-}

: > "$SUMMARY_FILE"

problem() {
  echo "PROBLEM: $1"
  echo "- $1" >> "$SUMMARY_FILE"
}

now=$(date +%s)

for entry in $TELEGRAM_BOTS; do
  name=${entry%%:*}
  token=${entry#*:}
  if [ -z "$token" ]; then
    problem "$name (Telegram): token not set"
    continue
  fi
  me=$(curl -s --max-time 20 "$TELEGRAM_API_URL/bot$token/getMe")
  if [ "$(echo "$me" | jq -r '.ok // false' 2>/dev/null)" != "true" ]; then
    problem "$name (Telegram): getMe failed, the token is not valid or Telegram is not reachable"
    continue
  fi
  info=$(curl -s --max-time 20 "$TELEGRAM_API_URL/bot$token/getWebhookInfo")
  if [ "$(echo "$info" | jq -r '.ok // false' 2>/dev/null)" != "true" ]; then
    problem "$name (Telegram): getWebhookInfo failed"
    continue
  fi
  pending=$(echo "$info" | jq -r '.result.pending_update_count // 0')
  error_date=$(echo "$info" | jq -r '.result.last_error_date // 0')
  error_message=$(echo "$info" | jq -r '.result.last_error_message // ""')
  if [ "$pending" -gt "$PENDING_THRESHOLD" ]; then
    problem "$name (Telegram): $pending pending updates (more than $PENDING_THRESHOLD)"
  fi
  if [ "$error_date" -gt 0 ] && [ $((now - error_date)) -le "$ERROR_MAX_AGE" ]; then
    problem "$name (Telegram): webhook error $((now - error_date))s ago: $error_message"
  fi
  echo "ok: $name (Telegram)"
done

for entry in $DISCORD_BOTS; do
  name=${entry%%:*}
  token=${entry#*:}
  if [ -z "$token" ]; then
    problem "$name (Discord): token not set"
    continue
  fi
  status=$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 -H "Authorization: Bot $token" "$DISCORD_API_URL/users/@me")
  if [ "$status" = "200" ]; then
    echo "ok: $name (Discord)"
  else
    problem "$name (Discord): the API answered $status, the token is not valid or Discord is not reachable"
  fi
done

[ ! -s "$SUMMARY_FILE" ]
