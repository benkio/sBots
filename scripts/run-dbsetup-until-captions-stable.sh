#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

MAX_ITERATIONS="${MAX_ITERATIONS:-20}"
SBT_COMMAND="${SBT_COMMAND:-sbt dbSetup}"

caption_status_snapshot() {
  git status --porcelain -- "modules/bots" | awk '/showCaptions\// { print }'
}

previous_snapshot="$(caption_status_snapshot)"

for ((iteration = 1; iteration <= MAX_ITERATIONS; iteration++)); do
  echo "[caption-loop] Iteration ${iteration}: running '${SBT_COMMAND}'"
  eval "$SBT_COMMAND"

  current_snapshot="$(caption_status_snapshot)"

  if [[ "$current_snapshot" == "$previous_snapshot" ]]; then
    echo "[caption-loop] No new caption changes detected. Stopping."
    exit 0
  fi

  echo "[caption-loop] New caption changes detected. Continuing."
  previous_snapshot="$current_snapshot"
done

echo "[caption-loop] Reached MAX_ITERATIONS=${MAX_ITERATIONS} with continuing caption changes."
exit 1
