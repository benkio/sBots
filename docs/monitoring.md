# Monitoring and logs

Part of the monitoring epic (#1438).

## Production logs in Grafana Cloud

The bots ship their logs to Grafana Cloud Loki (free tier, 14 days of retention).

- **Dashboard:** [sbots Alerts Overview](https://glowingdate3125.grafana.net/d/be4zdbd/sbots-alerts-overview): the three alert queries, the usage and health stats and the raw logs, in three rows (Alerts, Usage and health, Logs).
  The JSON is in [`docs/grafana/sbots-overview-dashboard.json`](grafana/sbots-overview-dashboard.json): after a change of the
  stats lines or of the queries below, edit the dashboard in Grafana, export it (Share, Export, Export as JSON) and commit it
  here. To restore it: Dashboards, New, Import, paste the file (the `be4zdbd` uid replaces the existing dashboard).
  When a panel has more than one query, each query needs a distinct `refId` (`A`, `B`, `C`, ...): duplicated IDs crash the panel with a React error.
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
  load1=<n.nn> load5=<n.nn> load15=<n.nn> cpu_usage_pct=<n> mem_free_mb=<n> proc_rss_mb=<n> disk_total_mb=<n> disk_free_mb=<n>`: once a minute. `db_mb=-1`
  when the DB file is missing; `mem_total_mb=-1`, `mem_available_mb=-1`, `mem_free_mb=-1`, `proc_rss_mb=-1`, `load*=-1.00`,
  `cpu_usage_pct=-1`, `disk_total_mb=-1` and `disk_free_mb=-1` when the OS value cannot be read. On Linux `mem_available_mb` is `MemAvailable` from
  `/proc/meminfo` (memory usable by new processes, including reclaimable cache), `mem_free_mb` is its `MemFree` (unused
  RAM, excluding the page cache) and `proc_rss_mb` is `VmRSS` from `/proc/self/status` (resident memory of the bot
  process); the loads come from `/proc/loadavg`. Disk total/free are read from the filesystem containing the DB file.
  Elsewhere the JDK MX bean provides the 1-minute load, free memory and
  CPU load.

Panels of the dashboard (the lines are JSON: the `regexp` parser reads the `[stats]` text inside, no `json` step needed). Use
backticks for the regexp in the Grafana editor, so the backslashes are not escaped twice.

| Panel | LogQL |
|---|---|
| Messages per bot and platform | ``sum by (bot, platform) (count_over_time({app="sbots"} \|= "[stats] type=message" \| regexp `bot=(?P<bot>\S+) platform=(?P<platform>\S+)` [$__interval]))`` |
| Timeouts | ``sum by (bot) (count_over_time({app="sbots"} \|= "[stats] type=message" \|= "outcome=timeout" \| regexp `bot=(?P<bot>\S+)` [$__interval]))`` |
| Duplicates skipped | ``sum by (bot) (count_over_time({app="sbots"} \|= "[stats] type=message" \|= "outcome=duplicate" \| regexp `bot=(?P<bot>\S+)` [$__interval]))`` |
| Latency p95 | ``quantile_over_time(0.95, {app="sbots"} \|= "[stats] type=message" \| regexp `bot=(?P<bot>\S+).*latency_ms=(?P<l>\d+)` \| unwrap l [$__interval]) by (bot)`` |
| Heap used (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `heap_used_mb=(?P<h>\d+)` \| unwrap h [$__interval])`` |
| DB and disk size (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `db_mb=(?P<d>-?\d+)` \| unwrap d [$__interval])`` (db), ``max_over_time(... \| regexp `disk_total_mb=(?P<d>-?\d+)` \| unwrap d [$__interval])`` (disk total), ``min_over_time(... \| regexp `disk_free_mb=(?P<d>-?\d+)` \| unwrap d [$__interval])`` (disk free) |
| Memory available (MB) | ``min_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `mem_available_mb=(?P<m>-?\d+)` \| unwrap m [$__interval])`` (the lowest in the bucket, so pressure shows up) |
| Memory total (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `mem_total_mb=(?P<t>-?\d+)` \| unwrap t [$__interval])`` |
| Memory free (MB) | ``min_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `mem_free_mb=(?P<f>-?\d+)` \| unwrap f [$__interval])`` |
| Process RSS (MB) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `proc_rss_mb=(?P<r>-?\d+)` \| unwrap r [$__interval])`` |
| System load (1m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load1=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| System load (5m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load5=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| System load (15m) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `load15=(?P<l>-?\d+\.\d+)` \| unwrap l [$__interval])`` |
| CPU usage (%) | ``max_over_time({app="sbots"} \|= "[stats] type=system" \| regexp `cpu_usage_pct=(?P<c>-?\d+)` \| unwrap c [$__interval])`` |
| Media errors | ``sum(count_over_time({app="sbots", level=~"ERROR\|WARN"} \|~ "(?i)media" [$__interval]))`` (approximate: it matches the log lines that mention media) |

## LLM observability (#1493)

One JSON log line per completion the responder asked for (`LlmInteractionLog`): the `message` of the log record is a JSON
object, so it needs no regexp. Keep the keys stable (the LLM row of the dashboard depends on them). A key without a value
(`failure`, `local_rejection`, `provider_errors`, `provider_error`) is omitted: in Loki a missing label means "none".

Two plain INFO lines explain a non-reply that has no `llm_interaction` at all:

- `LLM not asked (chatId=...): reason=<reason>` from the engine, with `reason` one of `responder_off`, `not_directed`,
  `forwarded`, `too_old`, `ignored_prefix`, `disabled`, `reactive_blocked` (a matching bundle blocked by the chat
  timeout), `in_timeout`, `in_flight` (another call for the chat is running) and `no_reply` (the responder chose silence
  or failed). Filter with `{app="sbots"} |= "LLM not asked"`.
- `[ChatLlmResponder] <chatId>: no reply: <reason>` from the responder for every interaction that ends without a reply.
  The reason is the `failure` of the JSON line, with the output-guard rule appended as `output_rejected:<rule>` (for
  example `command`, `mention`, `empty_text`). A deliberate `silent` action reports `silent_action`.
  A text dropped by the output guard (`output_rejected:*`) is logged at WARN, every other reason at INFO.
  A `text` action with an empty text is no longer reached: it is `malformedjson` and the fallback provider is asked.

A failure to build the prompt, to log the interaction or to store the trace writes a WARN/ERROR line with the exception
class only; neither a logging failure nor a busy database can drop the other record or fail the reply.

```json
{"type":"llm_interaction","chat":"<hash>","bot":"<bot>","model":"<model>","prompt_tokens":471,
 "response_tokens":11,"latency_ms":820,"status":"answered","action":"text","failure":"<reason>",
 "attempts":"gemini:ratelimited,openrouter:success","fallback":true,"parsed":true,"stripped":false,"local_rejection":"<reason>",
 "provider_errors":"gemini:404","provider_error":"<provider message, capped at 200 characters>",
 "gemini_ms":850,"openrouter_ms":9200,"prompt_tokens_reported":468,"response_tokens_reported":12,
 "cached_tokens":380}
```

- `model` is the model the provider reported for the request (OpenRouter reports the one the free router picked, Gemini its
  `modelVersion`; capped at 100 characters), else the configured model of the provider (`openrouter/free` by default for
  OpenRouter). With the free router `model` has many values over time: do not chart it with an unbounded `by (model)`.
- `provider_errors` lists the provider calls that failed over HTTP as `provider:status` (`gemini:404` is a retired model,
  `openrouter:401` a bad key); `provider_error` is the first provider message, capped at 200 characters (the same text and
  status are in the ops notice when a provider rejects the request). Both are omitted when no call failed over HTTP.
- `gemini_ms` and `openrouter_ms` are how long the call to that provider took (a failed or timed-out call included); a
  provider that was not asked has no key. Their sum is close to `latency_ms`, so they tell which provider is slow.
- A provider reply that cannot be read as the structured reply also writes one ERROR line:
  `[LlmMalformedJson] provider=<p> model=<m> bot=<b> got: <what it returned>`, with the full raw model text (not
  capped, so a malformed JSON body can be examined). The line also carries the provider's own diagnosis after the text:
  for Gemini `[status=incomplete output_tokens=1024]` (`incomplete` is a reply cut by the output limit, `failed` an
  error), for OpenRouter `[finish_reason=length content=null choice_error=502 …]`, or
  `no model text: …` when there was none (`length` is a token limit hit, `content_filter` a block, `content=null` an
  empty answer, e.g. a reasoning model that spent its tokens thinking). The text is the
  model's reply and may echo chat content: it goes to the server logs (and Loki), unlike the prompt. The "sbots warnings
  and errors" panel of the Logs row (`{app="sbots"} | level=~"WARN|ERROR"`) lists these lines.
- `chat` is a salted hash (`llm.context.author-salt`), never the chat id. The line holds no chat content.
- `prompt_tokens` and `response_tokens` are estimates (4 characters per token), kept for continuity.
  `prompt_tokens_reported`, `response_tokens_reported` and `cached_tokens` are the provider's own counts (Gemini
  `usage.total_input_tokens` / `total_output_tokens` / `total_cached_tokens`; OpenRouter `usage.prompt_tokens` /
  `completion_tokens` / `prompt_tokens_details.cached_tokens`), appended at the end of the line. Each is omitted when the
  provider reported no such count, so a missing key means "not reported", not zero; `cached_tokens / prompt_tokens_reported`
  shows whether the stable prompt prefix gets cache hits. They are the sum over every provider attempt of the interaction
  (a fallback is billed on top of the failed primary, and an unreadable or malformed reply still cost tokens). For Gemini
  `response_tokens_reported` includes `total_thought_tokens` (thinking tokens are billed as output, and Gemini counts them apart from `total_output_tokens`). A response that
  arrives after the completion is already over (the engine timeout) is not recorded, like its attempt and result.
- `status`: `answered`, `failed`, `silent` (quota), `pending`, `error` (the client call threw) or `cancelled` (the call was
  cancelled before it ended: the engine timeout, a shutdown). A cancel during the client call keeps the `attempts` and
  `*_ms` of the provider calls that had already finished (a primary timeout before the fallback was cancelled); then
  `failure` is `timeout` (status stays `cancelled`), otherwise `cancelled`.
- `failure`: `oversized_prompt`, `missing_credentials` (local rejections, no provider asked), `quota_exhausted` (both
  providers, or the fallback after a retryable primary failure), the result of the last provider call (`ratelimited`,
  `servererror`, `timeout` (the last provider call went over `llm.provider-timeout`; a primary timeout that the fallback answered leaves `failure` out, look for `timeout` in `attempts` instead), `rejected`, `malformedjson`), `success_without_reply` (a 200 with no structured reply),
  `output_rejected` (the output guard dropped a text),
  `client_error:<ExceptionClass>` (the client call threw; the exception message is never logged here, it may hold chat
  content), `cancelled`. Omitted when it answered or the model chose silence.
- `fallback=true`: the provider other than the configured primary was asked. `stripped=true`: the reply parsed only after the
  fence/prose around the JSON object was cut away.
  A reply cut by the output-token limit is not recovered: it is `malformedjson`, with the full raw text logged.
- Both providers are asked for the reply contract `{action: text|silent, text}` as a schema: Gemini with `responseSchema`,
  OpenRouter with a strict `response_format.json_schema` and `provider.require_parameters=true` (endpoints that cannot
  honour it are skipped, so a request with none left fails as a provider error). Replies that still break it
  (`reuse`, `replyId`, prose) are `malformedjson` and keep the full raw text in the `[LlmMalformedJson]` line.

**Trace a weird reply.** Find the line in Loki (below, plus `| chat="<hash>"`): it gives the model and the
time. With the debug trace on, the full prompt and the raw model text are in `llm_trace` (joined by `chat_hash`, `bot_id`
and `created_at`):

```sh
sqlite3 botDB.sqlite3 "SELECT datetime(created_at,'unixepoch'), model, action, prompt, response FROM llm_trace WHERE chat_hash='<hash>' ORDER BY created_at DESC LIMIT 5"
```

The trace is **off by default** (`LLM_TRACE_ENABLED=true` to turn it on, `llm.trace.enabled`): the prompt holds chat content.
Rows expire after `llm.trace.ttl` (`LLM_TRACE_TTL`, default 1 day), removed by the same cron as the context cleanup, also
after the flag is turned off again. The table is not copied by `DBMigration.sql`.

Panels of the LLM row. The log record is JSON and so is its message: `| json | line_format "{{.message}}" | json` reads both
levels, and the keys above become labels. `BASE` below is
`{app="sbots"} |= "llm_interaction" | json | line_format "{{.message}}" | json`.

| Panel | Query |
|---|---|
| LLM latency p95 (ms) | ``quantile_over_time(0.95, BASE \| unwrap latency_ms [$__interval]) by (bot)`` |
| LLM quota exhausted | ``sum(count_over_time(BASE \| failure="quota_exhausted" [$__interval]))`` |
| LLM replies by action | ``sum by (action) (count_over_time(BASE [$__interval]))`` |
| LLM failures by reason | ``sum by (failure) (count_over_time(BASE \| failure!="" [$__interval]))`` |
| LLM fallbacks and local rejections | ``sum(count_over_time(BASE \| fallback="true" [$__interval]))`` (A) and ``sum(count_over_time(BASE \| local_rejection!="" [$__interval]))`` (B) |
| LLM provider latency p95 (ms) | ``quantile_over_time(0.95, BASE \| unwrap gemini_ms [$__interval])`` (A) and the same with ``openrouter_ms`` (B) |
| LLM provider HTTP errors | ``sum by (provider_errors) (count_over_time(BASE \| provider_errors!="" [$__interval]))`` |
| LLM prompt tokens (estimated) | ``sum by (bot) (sum_over_time(BASE \| unwrap prompt_tokens [$__interval]))`` |

**Quota alert** (Grafana alert rule, created in the UI like the other three): fire when
``sum(count_over_time({app="sbots"} |= "llm_interaction" | json | line_format "{{.message}}" | json | failure="quota_exhausted" [1h])) > 0``.
The ops message of `LimitedLlmClient` already tells once per exhaustion episode; the alert is the dashboard-side twin.

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
