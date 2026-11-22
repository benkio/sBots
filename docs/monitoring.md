# Monitoring and logs

Part of the monitoring epic (#1438).

## Production logs in Grafana Cloud

The bots ship their logs to Grafana Cloud Loki (free tier, 14 days of retention).

- **Dashboard:** [sbots Alerts Overview](https://glowingdate3125.grafana.net/d/be4zdbd/sbots-alerts-overview): the three alert queries, the usage and health stats and the raw logs, in three rows (Alerts, Usage and health, Logs).
  The JSON is in [`docs/grafana/sbots-overview-dashboard.json`](grafana/sbots-overview-dashboard.json): after a change of the
  stats lines or of the queries below, edit the dashboard in Grafana, export it (Share, Export, Export as JSON) and commit it
  here. To restore it: Dashboards, New, Import, paste the file (the `be4zdbd` uid replaces the existing dashboard).
- **Read the logs:** [Grafana Explore](https://glowingdate3125.grafana.net/explore) with the `grafanacloud-glowingdate3125-logs` data source, query `{app="sbots"}`.
  Anyone who needs access has to be invited to the Grafana Cloud stack (Administration, Users and access).
- **Labels:** `app` (always `sbots`) and `level`. Everything else is in the JSON line; filter it with
  `{app="sbots"} | json | line_format "{{.message}}"` or a text match like `{app="sbots"} |= "error"`.
- **Without Grafana:** the same lines go to `sbots.log` next to the jar on the VM (plain text).

## Usage and health stats

Two kinds of `[stats]` lines, logged at `info` in the `logfmt` style (`UsageStats`, `StatsLogger`). Keep the keys stable: the
dashboard depends on them.

- `[stats] type=message bot=<bot> platform=<platform> outcome=<outcome> latency_ms=<n>`: one per message that reaches a bot
  (`BotEngine.onMessage`). `outcome` is `handled` (also when nothing matched), `duplicate` (skipped, #1432), `timeout`
  (dropped after the request timeout) or `failed` (an error ended the processing).
- `[stats] type=system heap_used_mb=<n> heap_max_mb=<n> uptime_s=<n> db_mb=<n> mem_total_mb=<n> mem_available_mb=<n>
  load1=<n.nn> load5=<n.nn> load15=<n.nn> cpu_usage_pct=<n>`: once a minute. `db_mb=-1` when the DB file is missing;
  `mem_total_mb=-1`, `mem_available_mb=-1`, `load*=-1.00` and `cpu_usage_pct=-1` when the OS value cannot be read.
  On Linux `mem_available_mb` is `MemAvailable` from `/proc/meminfo` (not `MemFree`, which excludes the page cache) and
  the loads come from `/proc/loadavg`; elsewhere the JDK MX bean provides the 1-minute load, free memory and CPU load.

Panels of the dashboard (the lines are JSON: the `regexp` parser reads the `[stats]` text inside, no `json` step needed). Use
backticks for the regexp in the Grafana editor, so the backslashes are not escaped twice.

| Panel | LogQL |
|---|---|
| Messages per bot and platform | ``sum by (bot, platform) (count_over_time({app="sbots"} \|= "[stats] type=message" \| regexp `bot=(?P<bot>\S+) platform=(?P<platform>\S+)` [$__interval]))`` |
| Timeouts | ``sum by (bot) (count_over_time({app="sbots"} \|= "[stats] type=message" \|= "outcome=timeout" \| regexp `bot=(?P<bot>\S+)` [$__interval]))`` |
| Duplicates skipped | ``sum by (bot) (count_over_time({app="sbots"} \|= "[stats] type=message" \|= "outcome=duplicate" \| regexp `bot=(?P<bot>\S+)` [$__interval]))`` |
| Latency p95 | ``quantile_over_time(0.95, {app="sbots"} \|= "[stats] type=message" \| regexp `bot=(?P<bot>\S+).*latency_ms=(?P<l>\d+)` \| unwrap l [$__interval]) by (bot)`` |
| Heap used (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `heap_used_mb=(?P<h>\d+)` \| unwrap h [$__interval])`` |
| Memory available (MB) | ``min_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `mem_available_mb=(?P<m>-?\d+)` \| unwrap m [$__interval])`` (the lowest in the bucket, so pressure shows up) |
| Memory total (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `mem_total_mb=(?P<t>-?\d+)` \| unwrap t [$__interval])`` |
| System load (1m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load1=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| System load (5m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load5=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| System load (15m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load15=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| CPU usage (%) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `cpu_usage_pct=(?P<c>-?\d+)` \| unwrap c [$__interval])`` |
| Media errors | ``sum(count_over_time({app="sbots", level=~"ERROR\|WARN"} \|~ "(?i)media" [$__interval]))`` (approximate: it matches the log lines that mention media) |

## How it works

- `chatcore.Logger` is the only `LogWriter`: it goes through log4s and logback.
- Production starts with `-Dlogback.configurationFile=logback-main.xml` (`deploy.yml`). Logback only reads this from a
  system property, and every module ships its own `logback.xml`, so the file needs a unique name.
- `logback-main.xml` logs to the console and includes `${LOG_EXTRA:-logback-noop.xml}`. When `LOG_EXTRA=logback-loki.xml`
  the loki4j appender is added. An empty Loki url breaks the whole logback startup, so the appender is declared only
  when it is enabled.
- Local runs and tests do not set `LOG_EXTRA`, so nothing is shipped.

## Setup

| Name | Kind | Value |
|---|---|---|
| `LOKI_URL` | repository variable | base url of the stack's Loki, e.g. `https://logs-prod-XXX.grafana.net` (the push path is added by the deploy) |
| `LOKI_USER` | repository variable | numeric Loki user (instance id) |
| `LOKI_TOKEN` | repository secret | Grafana Cloud access policy token with the `logs:write` scope |

Url and user are in Grafana Cloud, My Account, the stack, Loki, Details. The deploy writes them into the OpenRC
script (`/etc/init.d/sbots`, readable by root only) only when all three are set; otherwise shipping stays off.

## Fallback

If the appender causes trouble (memory, blocking), remove `LOG_EXTRA` from the deploy and tail `sbots.log` with
Grafana Alloy instead (see #1445).
