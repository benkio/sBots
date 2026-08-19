#!/usr/bin/env bash

# Run from the root of the project: ./scripts/copyTokensFromDropbox.sh

set -u

DROPBOX_ROOT="$HOME/Dropbox/sBots"
MEGA_ROOT="$HOME/Mega/sBots"

dropbox_bots=(
  "CalandroBot"
  "ABarberoBot"
  "M0sconiBot"
  "XahLeeBot"
  "YouTuboAncheI0Bot"
  "PinoScottoBot"
)

mega_bots=(
  "RichardPHJBensonBot"
  "Alessandro0rlandoBot"
  "VittorioSgarbiBot"
)

copy_tokens() {
  local root="$1"
  local bot_name="$2"
  target_dir="modules/bots/$bot_name/src/main/resources"
  source_dir="$root/$bot_name/src/main/resources"
  mkdir -p "$target_dir"
  cp "$source_dir"/*.token "$target_dir/" 2>/dev/null || true
  echo "Copied tokens for $bot_name from $source_dir"
}

for bot_name in "${dropbox_bots[@]}"; do
  copy_tokens "$DROPBOX_ROOT" "$bot_name"
done

for bot_name in "${mega_bots[@]}"; do
  copy_tokens "$MEGA_ROOT" "$bot_name"
done
