# Guided /subscribe (issue #1323)

Spec: `specs/engine-extras.allium` (Guided /subscribe) and `specs/platforms.allium` (guided flow adapter).

## Acceptance checklist

- [x] Bare `/subscribe` starts the guided flow on Telegram/Discord; one-shot syntax unchanged.
- [x] Draft key `(botId, requester, chatId)`; one flow per triple; two users get independent drafts.
- [x] Store persisted in shared bot DB, scoped by bot, idle TTL + bound from config; full store declines with one-shot command + "wait/retry". A restart keeps drafts still within TTL.
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

## Review follow-ups

- Discord packs prompt buttons into at most five rows of five and always reserves the last row for back/cancel: the five
  schedule presets used to push those buttons past the row budget.
- Telegram refreshes the prompt handle after an edit. The engine refreshes it for every flow answer through
  `PlatformAdapter.touchFlowPrompt`, including a typed answer that is rejected (too-long target, empty cron) and emits
  no new prompt, so a prompt stays usable for as long as the idle draft.
- `SubscriptionFlowService` serializes start/submit/plain answers with 64 lazily created striped semaphores instead of a
  lock per draft: bounded for the life of the bot, with collisions only costing a short serialization.
- The outcome `emit` runs inside the same lock, so a second press cannot deliver its reply before the first one's prompt
  edit; two parallel confirms cannot both create a subscription; `NotHandled` was dead and removed.
- Left for later (review suggestions, non-blocking): inject a clock and parse the cron once; replace the
  `AtomicReference` update idiom in the stores; align the Telegram/Discord prompt-send failure policy.

## DB persistence note

- Drafts, flow prompt handles, and paged-list text entries are persisted in the shared bot DB (V12), scoped by `bot_id`. They survive process restarts as long as they are still within their configured TTL.
