# LLM Follow-ups (2026-11-24 review)

## Goal

Give a fresh LLM session enough context to continue the reliability work without re-deriving the investigation. Start with the GitHub issue, then come back here for the decisions and constraints.

## Read first

- `../AGENTS.md` and `../specs/llm.allium` (behaviour specs; `sbots.allium` is the core, `llm.allium` is this epic).
- `../docs/monitoring.md` — log queries and the exact Grafana raw-log filter.
- `../docs/grafana/sbots-overview-dashboard.json` — the applied raw-log query.
- `PromptTuning.md` before touching the prompt template.
- `../README.org` section "LLM Replies" for provider defaults, privacy and timeout sizing.

## Already done (commit `cbed57a8`, pushed to `origin/main`)

- Engine skip logs with stable `reason=`: `responder_off`, `not_directed`, `forwarded`, `too_old`, `ignored_prefix`, `disabled`, `reactive_blocked`, `in_timeout`, `in_flight`, `no_reply`.
- Responder candidate/decision logs, prompt-build phase log, class-only outer failure log and explicit `success_without_reply`.
- `OutputGuard.check` now checks raw mentions before plain mentions; failure reasons include `output_rejected:<rule>`.
- `LlmObserver` interaction line, malformed-reply lines and trace are independent best-effort sinks; sink failures log exception classes only.
- Full malformed provider text stays uncapped in `[LlmMalformedJson]`; do not reintroduce a cap.
- Ops notices for missing credentials and exhausted fallback quota; `Completion.fallbackQuotaExhausted` prevents false quota notices.
- Default primary provider is Gemini, OpenRouter is fallback (smaller free daily quota), in `application.conf`, `README.org` and `specs/llm.allium`.
- Dashboard raw-log query is already applied: `{app="sbots"} != "[stats]" != "[HealthcheckPing] heartbeat"` (committed before `cbed57a8`).
- Validation before the commit: `chatCore/test; chatLlm/test`, `scalafmtCheckAll`, `ConfigSpec` (7 tests), `sh scripts/checkSpecs.sh` (no errors, 3 pre-existing warnings).

## Decisions and constraints

- **Uncapped malformed JSON is intentional.** The user removed the cap to see the full payload. Never restore `LlmObserver.MaxLoggedChars` or a `.take(...)` on that text.
- **No exception messages in logs.** They can carry chat content. Plain engine/responder logs use raw `chatId`; structured `llm_interaction` uses only the salted SHA-256 prefix. The remaining `$e` in `BotEngine` is tracked in #1595.
- **Grafana filter must use exact strings.** `!= "stats" != "heartbeat"` would also hide heartbeat failure lines; keep the exact literals above.
- **Gemini primary / OpenRouter fallback** is deliberate because OpenRouter's free daily quota is small. `#1567`/`#1569` already exposed per-provider quota config.
- **Interactions API is deferred**, not urgent: `generateContent` is legacy but fully supported. If migrated, keep it behind `LlmProviderClientApi` and set `store=false` unless retention is explicitly approved (#1591).
- **One logical reply contract, two envelopes.** `StructuredReply.parseDetailed` is the single payload parser; provider envelope parsing stays client-specific. A shared `ReplySchema` should only render the schema dialects (#1588/#1589).
- **OpenRouter should use strict `json_schema` + `provider.require_parameters`**, not `json_object`; keep `StructuredReply` as defence in depth.
- **Prompt prefix is already cache-friendly** (instructions, persona, catalogue before context/message). Explicit caching needs lifecycle/privacy decisions (#1592); caching improves cost/latency, not quality.
- **More free providers help only with independent failure domains** (#1593); another model through the same OpenRouter account shares its quota.
- **Reactive replies stay preferred.** The candidates/`reuse` defect is a product decision, not just a code fix (#1594).
- `prompts/v1/system.md` is released and immutable: create `v2` instead of editing it.

## Issue index (LLM milestone, epic #1499)

| Issue | Follow-up | First move |
| --- | --- | --- |
| [#1587](https://github.com/benkio/sBots/issues/1587) | Split provider timeout from whole-answer timeout | Add `LLM_PROVIDER_TIMEOUT`; keep `LLM_TIMEOUT` as total budget. |
| [#1588](https://github.com/benkio/sBots/issues/1588) | Strict OpenRouter JSON schema | Replace `json_object`, add `require_parameters`, keep `StructuredReply`. |
| [#1589](https://github.com/benkio/sBots/issues/1589) | Shared `ReplySchema` | Extract the contract; render Gemini/OpenRouter dialects. |
| [#1590](https://github.com/benkio/sBots/issues/1590) | Real token/cached-token usage | Add usage metadata through `ProviderResponse`; append JSON keys. |
| [#1591](https://github.com/benkio/sBots/issues/1591) | Interactions API decision | Spike, document, decide `store=false` if migrating. |
| [#1592](https://github.com/benkio/sBots/issues/1592) | Context caching decision | Measure with #1590 evidence, then decide implicit vs explicit. |
| [#1593](https://github.com/benkio/sBots/issues/1593) | Third independent free provider | One week of 429 evidence before implementation. |
| [#1594](https://github.com/benkio/sBots/issues/1594) | Candidates/`reuse` known defect | Decide pass-candidates vs remove-`reuse`. |
| [#1595](https://github.com/benkio/sBots/issues/1595) | Log hygiene | Remove remaining `$e`; observe cancellation before client call. |
| [#1596](https://github.com/benkio/sBots/issues/1596) | Model pinning and quota calibration | Decide `latest` vs pin; tune quotas from 429s. |

Related existing issues: [#1499](https://github.com/benkio/sBots/issues/1499) epic, [#1497](https://github.com/benkio/sBots/issues/1497) feedback/evaluation set, [#1496](https://github.com/benkio/sBots/issues/1496) proactive phase 2, [#1584](https://github.com/benkio/sBots/issues/1584) alerts. Do not duplicate the alert work in #1584.

## Suggested order

1. #1595 and #1587 — small, high-value privacy/reliability fixes.
2. #1588 and #1589 — schema certainty and one source of truth.
3. #1590, then #1592 and #1596 — measurement-driven cost/cache/quota decisions.
4. #1594 — product decision on candidates/`reuse`.
5. #1591 and #1593 — deferred decisions; implement only if the evidence justifies it.

## Starting a follow-up

1. Check `git status` and read the issue (`gh issue view <n>`). `main` is protected; prefer a branch and a PR for follow-up work.
2. Read the relevant section of `specs/llm.allium`; update the spec in the same change as behaviour.
3. Keep `llm_interaction` JSON keys stable: append new keys, omit missing values, never rename existing ones.
4. Update `docs/monitoring.md` and the dashboard only when log shape or queries change.
5. Validate:
   ```bash
   sbt --client 'chatCore/test; chatLlm/test'
   sbt --client scalafmtCheckAll
   sh scripts/checkSpecs.sh
   ```
   Add `sbt --client 'main/testOnly com.benkio.main.ConfigSpec'` for config changes. The three Allium warnings are pre-existing; errors are not acceptable.

## Expected session output

Report the issue number, the spec/code/docs files changed, validation results, and any decision that still needs the developer's input.
