# Agent Context Folder

This folder contains persistent context for AI agents across sessions and tools.

## Purpose

- Keep recurring operational knowledge in one place.
- Make future sessions faster and more consistent.
- Stay tool-agnostic: any agent that can read files can use this folder.

## Files

- `tasks/`: folder containing recurring activities, checklists, and useful commands.

## Maintenance Rules

- Prefer short, actionable entries.
- Keep commands copy-pastable from repo root.
- When a repeated request appears, add or refine a task file under `tasks/`.
