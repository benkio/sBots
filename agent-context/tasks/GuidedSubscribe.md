# Guided /subscribe (issue #1323)

Spec: `specs/engine-extras.allium` (Guided /subscribe) and `specs/platforms.allium` (guided flow adapter).

## Acceptance checklist

- [x] Bare `/subscribe` starts the guided flow on Telegram/Discord; one-shot syntax unchanged.
- [x] Draft key `(botId, requester, chatId)`; one flow per triple; two users get independent drafts.
- [x] Store in memory, idle TTL + bound from config; full store declines with one-shot command + "wait/retry".
- [x] Steps: type -> schedule -> target -> confirm; cancel everywhere, back from steps after the first only.
- [x] Schedule: five presets as buttons; typed cron accepted; invalid typed cron drops the draft with the validation error.
- [x] Target: free text plus random button (empty target); trigger step hints `/triggersearch`.
- [x] Confirm: reason + ready-to-run one-shot command on rejection; success uses the one-shot success reply.
- [x] Prompts edited in place; errors and final result are new messages.
- [x] Wrong-user press rejected privately by the adapter; no engine action emitted.
- [x] No workflow on IRC: bare `/subscribe` keeps the one-shot usage error.
- [x] Docs `modules/chatCore/CommandsDocumentation/subscribe.md` (IT + EN) updated.
- [x] `allium check specs/*.allium` has no new findings.
- [x] `sbt fix`, `sbt test`, `sbt "integration/mUnitTests"` pass.

## Decisions already taken with the user

- No requester => no flow (IRC out of scope); requester is mandatory.
- Store full => decline, never evict.
- Bare `/subscribe` while a draft exists for the same triple => drop and restart.
- `back` on the first step is not offered; cancel still is.
- Expiry is silent; the first prompt states the time limit; every answer/rejection resets the timer.
- Confirmation failure drops the draft and returns reason + executable one-shot command.
- Text-only platforms are not supported; there is no numbered-text fallback.
