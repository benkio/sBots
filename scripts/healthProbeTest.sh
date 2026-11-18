#!/bin/sh
# Tests scripts/healthProbe.sh against a fake curl: run it from the repository root.
set -u

here=$(cd "$(dirname "$0")" && pwd)
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir "$work/bin"

cat > "$work/bin/curl" <<'STUB'
#!/bin/sh
url=""
for a in "$@"; do url=$a; done
now=$(date +%s)
case "$*" in
  *"-w"*)
    case "$*" in *"Bot gooddiscord"*) printf 200 ;; *) printf 401 ;; esac
    exit 0 ;;
esac
case "$url" in
  */botbad/getMe | */botsecrettoken/getMe) echo '{"ok":false,"error_code":401}' ;;
  */botnowebhook/getMe) echo '{"ok":true}' ;;
  */botnowebhook/getWebhookInfo) echo '{"ok":false}' ;;
  */getMe) echo '{"ok":true}' ;;
  */botbacklog/getWebhookInfo) echo '{"ok":true,"result":{"pending_update_count":500}}' ;;
  */botrecenterror/getWebhookInfo)
    echo "{\"ok\":true,\"result\":{\"pending_update_count\":0,\"last_error_date\":$((now - 60)),\"last_error_message\":\"Connection refused\"}}" ;;
  */botolderror/getWebhookInfo)
    echo "{\"ok\":true,\"result\":{\"pending_update_count\":0,\"last_error_date\":$((now - 100000)),\"last_error_message\":\"old\"}}" ;;
  */getWebhookInfo) echo '{"ok":true,"result":{"pending_update_count":3}}' ;;
esac
STUB
chmod +x "$work/bin/curl"

failures=0
check() { # description, expected exit, telegram bots, discord bots, expected text in the summary (or "")
  description=$1; expected=$2; telegram=$3; discord=$4; text=$5
  PATH="$work/bin:$PATH" SUMMARY_FILE="$work/summary" TELEGRAM_BOTS="$telegram" DISCORD_BOTS="$discord" \
    sh "$here/healthProbe.sh" > "$work/out" 2>&1
  code=$?
  if [ "$code" != "$expected" ]; then
    echo "FAIL $description: exit $code, expected $expected"; cat "$work/out"; failures=$((failures + 1)); return
  fi
  if [ -n "$text" ] && ! grep -q "$text" "$work/summary"; then
    echo "FAIL $description: '$text' not in the summary"; cat "$work/summary"; failures=$((failures + 1)); return
  fi
  if [ "$expected" = 0 ] && [ -s "$work/summary" ]; then
    echo "FAIL $description: healthy but the summary is not empty"; failures=$((failures + 1)); return
  fi
  if grep -q "secrettoken" "$work/out" "$work/summary"; then
    echo "FAIL $description: a token was printed"; failures=$((failures + 1)); return
  fi
  echo "ok   $description"
}

check "healthy bots are silent" 0 "A:good B:olderror" "A:gooddiscord" ""
check "an invalid Telegram token" 1 "A:bad" "" "A (Telegram): getMe failed"
check "an unset token" 1 "A:" "" "A (Telegram): token not set"
check "a getWebhookInfo failure" 1 "A:nowebhook" "" "getWebhookInfo failed"
check "too many pending updates" 1 "A:backlog" "" "500 pending updates"
check "a recent webhook error" 1 "A:recenterror" "" "Connection refused"
check "an invalid Discord token" 1 "A:good" "D:baddiscord" "D (Discord)"
check "every problem is reported" 1 "A:bad B:backlog" "D:baddiscord" "B (Telegram): 500"

check "tokens are not printed" 1 "A:secrettoken" "" "A (Telegram)"

[ "$failures" = 0 ] && echo "all healthProbe checks passed" || { echo "$failures failed"; exit 1; }
