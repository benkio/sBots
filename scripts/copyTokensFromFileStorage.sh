#!/usr/bin/env bash

# Copy the .token files of every bot from its cloud-storage sync folder into the
# repository. Safe to run from any directory:
#   /path/to/sBots/scripts/copyTokensFromFileStorage.sh

set -u

# Resolve the repo root from this script's own location so the target paths are
# correct regardless of the current working directory.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"

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

failures=0

copy_tokens() {
  local root="$1"
  local bot_name="$2"
  local source_dir="$root/$bot_name/src/main/resources"
  local target_dir="$REPO_ROOT/modules/bots/$bot_name/src/main/resources"

  if [[ ! -d "$source_dir" ]]; then
    printf 'FAIL  %-24s source folder not found (not synced?): %s\n' "$bot_name" "$source_dir"
    failures=$((failures + 1))
    return 1
  fi

  # Without nullglob an unmatched glob stays literal; -e tells the two apart.
  local tokens=("$source_dir"/*.token)
  if (( ${#tokens[@]} == 0 )) || [[ ! -e "${tokens[0]}" ]]; then
    printf 'FAIL  %-24s no .token files in %s\n' "$bot_name" "$source_dir"
    failures=$((failures + 1))
    return 1
  fi

  if ! mkdir -p "$target_dir"; then
    printf 'FAIL  %-24s cannot create %s\n' "$bot_name" "$target_dir"
    failures=$((failures + 1))
    return 1
  fi

  if ! cp "${tokens[@]}" "$target_dir/"; then
    printf 'FAIL  %-24s copy from %s failed\n' "$bot_name" "$source_dir"
    failures=$((failures + 1))
    return 1
  fi

  printf 'OK    %-24s %d token file(s) -> %s\n' "$bot_name" "${#tokens[@]}" "$target_dir"
}

for bot_name in "${dropbox_bots[@]}"; do
  copy_tokens "$DROPBOX_ROOT" "$bot_name"
done

for bot_name in "${mega_bots[@]}"; do
  copy_tokens "$MEGA_ROOT" "$bot_name"
done

if ((failures > 0)); then
  printf '\n%d bot(s) had problems; see the FAIL lines above.\n' "$failures"
  exit 1
fi

printf '\nAll tokens copied successfully.\n'
