# Monitoring and logs

Part of the monitoring epic (#1438).

## Production logs in Grafana Cloud

The bots ship their logs to Grafana Cloud Loki (free tier, 14 days of retention).

- **Dashboard:** [sbots Alerts Overview](https://glowingdate3125.grafana.net/d/be4zdbd/sbots-alerts-overview): the state of the alert rules and the log activity at a glance.
- **Read the logs:** [Grafana Explore](https://glowingdate3125.grafana.net/explore) with the `grafanacloud-glowingdate3125-logs` data source, query `{app="sbots"}`.
  Anyone who needs access has to be invited to the Grafana Cloud stack (Administration, Users and access).
- **Labels:** `app` (always `sbots`) and `level`. Everything else is in the JSON line; filter it with
  `{app="sbots"} | json | line_format "{{.message}}"` or a text match like `{app="sbots"} |= "error"`.
- **Without Grafana:** the same lines go to `sbots.log` next to the jar on the VM (plain text).

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
