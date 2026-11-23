# Shared Agent Instructions

This repository keeps persistent AI context in `agent-context/`.

Any new agent session should read, in order:
1. `agent-context/README.md`
2. `agent-context/tasks/` (all files in this folder)

## Behavior Spec

`specs/` ([Allium](https://juxt.github.io/allium/language)) holds the reviewable specs of the bot behavior. When working
on bot logic read the relevant spec first, and update it in the same change as the behavior. Validate with
`allium check specs/*.allium`.

- `sbots.allium`: the core (dispatch, triggers, filters, timeout, data entry); the other specs `use` it.
- `engine-extras.allium`: per-chat timeout commands, subscriptions, media counts.
- `bots.allium`: the individual bots and their special behaviours.
- `platforms.allium`: the Telegram, Discord and IRC adapters (inbound mapping, reply delivery).
- `llm.allium`: the LLM replies epic (ports built so far, open questions for the rest).
- `operations.allium`: runtimes, health check, stats, startup checks, generators.
- `catalogue.allium`: the botDB job loading media and show catalogues.

The replies editor (`repliesEditorServer`, `repliesEditorUI`) is deliberately not specified. A `known defect` comment in
a spec marks where the code differs from the intended behavior.

## Session Expectations

- Use task files in `agent-context/tasks/` as a checklist when relevant.
- If a task is repeated often, add/update a file in `agent-context/tasks/`.
- Keep instructions practical and repository-specific.
