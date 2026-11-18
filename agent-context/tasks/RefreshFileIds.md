# Refresh File IDs

Telegram file references (`file_id`) are stored in `media.media_sources` as `telegram:<id>` after a successful send, and
are merged back into the repository `*_list.json` files through a PR.

## Flow

- GitHub Action `.github/workflows/fileIds.yml`: every day at 00:00 UTC, or manually (`workflow_dispatch`, `dry_run`
  input to only report). It downloads the prod DB, runs the export, and opens a refresh PR from a fresh branch
  (`bot/telegram-file-ids-<run id>-<attempt>`), reusing only an existing **open** refresh PR, and enables auto-merge.
  Never reuse a merged PR's branch: it would edit the merged PR and its diff would show references already in `main`.
  The push uses `GITHUB_TOKEN`, which does not trigger the `push` CI; opening the PR triggers `CI.yml` on the
  `pull_request` event instead, whose checks are the ones rolled up on the PR. GitHub requires one approval from a
  writer for that run (PR created by `GITHUB_TOKEN`): on the PR page click **Approve** on the waiting workflow run.
  Then CI runs and auto-merge completes when the required checks are green.
- Locally, with a downloaded `botDB.sqlite3`: `scala-cli scripts/ExportFileIds.scala -- --check` (diff) or `--write`.
- Tests of the tool: `scala-cli test scripts/ExportFileIds.scala`.

## Rules for a source

- A source is an url, or a file reference tagged `<platform.id>:<id>` (`telegram`, `discord`, `irc`, `slack`).
- An untagged reference is invalid: `FileIdSourcesSpec` (modules/main) and `filesCheck` (`MediaListSchema.ts`) fail.
- Tag old untagged data with `scala-cli scripts/MigrateFileIdSources.scala -- --write` (idempotent, `--check` first).
- References never cross bot or platform; do not copy an id by hand.
- Keep only the newest reference per platform: a fresh DB id replaces the stale one in the `*_list.json` file, it does
  not pile up beside it. The DB's newest id is first in `media_sources` (`MediaSources.withFileId`).
