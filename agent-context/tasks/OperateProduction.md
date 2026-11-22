# Operate Production

Runbook of the production VM: the full text is in `README.org`, section "Operations" (logs, dashboards and alerts,
restart, rollback). Grafana details: `docs/monitoring.md`.

## Quick reference

- Logs without SSH: Grafana Explore, query `{app="sbots"}`; or run the workflow "Download Prod DB and log files"
  (`downloadDB.yml`): artifacts `prod sqlite-db`, `prod logTxt`, `prod sbots logs` (`sbots.log`, `sbots.err.log`).
- Logs on the VM (`DEPLOY_DIR`): `sbots.log`, `sbots.err.log`, `log.txt` (how the app ended; the service sets
  `sbots.logFile` explicitly, so an unconfigured launch creates no crash-history file). Rotated at 20 MB, 5 kept.
- Restart: `sudo rc-service sbots restart`; or the workflow "Deploy" (`workflow_dispatch`).
- Rollback: revert the commit on `main` (the deploy follows). Database: stop the service and copy `botDB.sqlite3.bak` over
  `botDB.sqlite3` (only the previous deploy's DB, the data written since is lost).

## When something changes

- A new log line the dashboard or an alert depends on (`[stats] ...`): keep the keys, see `UsageStats` and the table in
  `docs/monitoring.md`; export the dashboard again to `docs/grafana/sbots-overview-dashboard.json`.
- A new file on the VM worth downloading: add it to `downloadDB.yml` and to the "Where the logs are" list of the README.
- A table that is not needed anymore: drop it with a new Flyway migration, `DBMigration.sql` and `scripts/checkDBMigration.sh`
  in the same change (see `V11__DropLogTable.sql`).

- Ephemeral flow/pagination state is persisted in DB (V12: `paged_list_text`, `subscription_draft`, `subscription_flow_prompt`), scoped by `bot_id` and bounded by TTL/max rows. Rows survive restart within TTL; they are safe to drop on rollback/migration troubleshooting, but active guided subscribe flows and text-mode paged lists will be forgotten.
