#!/bin/sh
# Checks the Allium behavior specs (specs/*.allium). Fails on errors and on a failed run of the checker; the warnings
# (unused declarations, statuses no rule assigns yet) are reported but do not fail it.
# Needs the `allium` CLI on the PATH.
set -u

report="$(mktemp)"
trap 'rm -f "$report"' EXIT

allium check specs/*.allium > "$report" 2>&1
status=$?

if [ "$status" -ge 2 ]; then
  echo "allium check could not run (exit $status):"
  cat "$report"
  exit 1
fi

errors="$(grep -cE '"severity":[[:space:]]*"error"' "$report")"
warnings="$(grep -cE '"severity":[[:space:]]*"warning"' "$report")"

if [ "$errors" -gt 0 ]; then
  echo "allium check reported $errors error(s):"
  grep -E -B3 -A3 '"severity":[[:space:]]*"error"' "$report"
  exit 1
fi

echo "allium check: no errors ($warnings warning(s))"
