#!/bin/sh
# Replaces the production DB with the freshly generated one, carrying over the production data (DBMigration.sql).
# Usage: updateProdDB.sh <deploy dir>
# Expects in <deploy dir>: botDBBase.sqlite3 (new DB), DBMigration.sql and, unless it is the first deploy, botDB.sqlite3 (prod).
# The production DB is only replaced when the migration succeeds: otherwise it is restored. On success the previous prod
# DB is kept as botDB.sqlite3.bak.
set -eu
cd "$1"

if [ ! -f botDB.sqlite3 ]; then
  echo "[updateProdDB] no production DB found: first deploy, using the new DB as is"
  mv botDBBase.sqlite3 botDB.sqlite3
  exit 0
fi

mv botDB.sqlite3 botDBProdOld.sqlite3
if sqlite3 -bail botDBBase.sqlite3 < DBMigration.sql; then
  mv botDBBase.sqlite3 botDB.sqlite3
  mv -f botDBProdOld.sqlite3 botDB.sqlite3.bak
  echo "[updateProdDB] migration done, previous production DB kept as botDB.sqlite3.bak"
else
  echo "[updateProdDB] migration FAILED: restoring the production DB" >&2
  mv -f botDBProdOld.sqlite3 botDB.sqlite3
  rm -f botDBBase.sqlite3
  exit 1
fi
