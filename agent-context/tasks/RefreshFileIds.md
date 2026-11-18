# Refresh File IDs

Telegram file references (`file_id`) are stored in `media.media_sources` as `telegram:<id>` after a successful send, and
are merged back into the repository `*_list.json` files through a PR.

## Flow

- GitHub Action `.github/workflows/fileIds.yml`: every day at 00:00 UTC, or manually (`workflow_dispatch`, `dry_run`
  input to only report). It downloads the prod DB, runs the export, and opens a refresh PR from a fresh branch
  (`bot/telegram-file-ids-<run id>-<attempt>`), reusing only an existing **open** refresh PR. It dispatches `CI.yml`
  on that branch (a `GITHUB_TOKEN` push does not trigger CI) and enables auto-merge: it merges only when the required
  checks pass, otherwise the PR stays open for review. Never reuse a merged PR's branch: it would edit the merged PR
  and its diff would show references already in `main`.
- Locally, with a downloaded `botDB.sqlite3`: `scala-cli scripts/ExportFileIds.scala -- --check` (diff) or `--write`.
- Tests of the tool: `scala-cli test scripts/ExportFileIds.scala`.

## Rules for a source

- A source is an url, or a file reference tagged `<platform.id>:<id>` (`telegram`, `discord`, `irc`, `slack`).
- An untagged reference is invalid: `FileIdSourcesSpec` (modules/main) and `filesCheck` (`MediaListSchema.ts`) fail.
- Tag old untagged data with `scala-cli scripts/MigrateFileIdSources.scala -- --write` (idempotent, `--check` first).
- References never cross bot or platform; do not copy an id by hand.
