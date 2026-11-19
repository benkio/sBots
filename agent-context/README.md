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
- `tools/`: reusable helper scripts referenced by the task files (for example
  `tools/conflict-resolution/`).

## Maintenance Rules

- Prefer short, actionable entries.
- Keep commands copy-pastable from repo root.
- When a repeated request appears, add or refine a task file under `tasks/`.
