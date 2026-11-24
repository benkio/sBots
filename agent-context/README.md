# Agent Context Folder

This folder contains persistent context for AI agents across sessions and tools.

## Purpose

- Keep recurring operational knowledge in one place.
- Make future sessions faster and more consistent.
- Stay tool-agnostic: any agent that can read files can use this folder.

## Files

- `../specs/`: the Allium behavior specs; first stop for bot logic work. `sbots.allium` is the core, the others cover
  engine extras, bots, platform adapters, operations and the catalogue job (see `../AGENTS.md`). Not covered: the
  replies editor.
- `tasks/`: folder containing recurring activities, checklists, and useful commands.
- `tasks/LlmFollowUps.md`: handoff for the 2026-11-24 LLM reliability review (issues #1587-#1596); read before starting
  LLM follow-up work.
- `tools/`: reusable helper scripts referenced by the task files (for example
  `tools/conflict-resolution/`).
- `commands/`: shared slash-command prompts. `review.md` is the `/review` prompt used by both Claude Code and
  OpenCode; the files under `.claude/commands/` and `.opencode/commands/` symlink to it. Update it here when changing
  the review checklist.

## Maintenance Rules

- Prefer short, actionable entries.
- Keep commands copy-pastable from repo root.
- When a repeated request appears, add or refine a task file under `tasks/`.
