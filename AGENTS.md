# Shared Agent Instructions

This repository keeps persistent AI context in `agent-context/`.

Any new agent session should read, in order:
1. `agent-context/README.md`
2. `agent-context/tasks/` (all files in this folder)

## Session Expectations

- Use task files in `agent-context/tasks/` as a checklist when relevant.
- If a task is repeated often, add/update a file in `agent-context/tasks/`.
- Keep instructions practical and repository-specific.
