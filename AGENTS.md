# Shared Agent Instructions

This repository keeps persistent AI context in `agent-context/`.

Any new agent session should read, in order:
1. `agent-context/README.md`
2. `agent-context/tasks/` (all files in this folder)
3. `agent-context/agentDocs/` when the work touches the LLM data (persona files, prompt template)

## Behavior Spec

`specs/` ([Allium](https://juxt.github.io/allium/language)) holds the reviewable specs of the bot behavior. When working
on bot logic read the relevant spec first, and update it in the same change as the behavior. Validate with
`allium check specs/*.allium`.

- `sbots.allium`: the core (dispatch, triggers, filters, timeout, data entry); the other specs `use` it.
- `engine-extras.allium`: per-chat timeout commands, subscriptions, media counts.
- `bots.allium`: the individual bots and their special behaviours.
- `platforms.allium`: the Telegram, Discord and IRC adapters (inbound mapping, reply delivery).
- `llm.allium`: the `/llm` switch and the noop LLM entry point; the replies are being rebuilt on LLM4s.
- `operations.allium`: runtimes, health check, stats, startup checks, generators.
- `catalogue.allium`: the botDB job loading media and show catalogues.

The replies editor (`repliesEditorServer`, `repliesEditorUI`) is deliberately not specified. A `known defect` comment in
a spec marks where the code differs from the intended behavior.

## Session Expectations

- `agent-context/tasks/` holds the tasks the user asks the agent directly to perform; use them as a checklist when
  relevant.
- `agent-context/agentDocs/` holds the agent's reference instructions for an area (today the persona files and the prompt template), read while
  working on that area rather than on user request.
- If a task is repeated often, add/update a file in `agent-context/tasks/`.
- Keep instructions practical and repository-specific.
