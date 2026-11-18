# Refresh File IDs

Telegram file references (`file_id`) are stored in `media.media_sources` as `telegram:<id>` after a successful send, and
are merged back into the repository `*_list.json` files through a PR.

## Flow

- GitHub Action `.github/workflows/fileIds.yml`: every day at 00:00 UTC, or manually (`workflow_dispatch`, `dry_run`
  input to only report). It downloads the prod DB, runs the export, and opens a refresh PR from a fresh branch
  (`bot/telegram-file-ids-<run id>-<attempt>`), reusing only an existing **open** refresh PR, and enables auto-merge.
  Never reuse a merged PR's branch: it would edit the merged PR and its diff would show references already in `main`.
  With the `APP_CLIENT_ID` variable and `APP_PRIVATE_KEY` secret configured, the push and PR are made by a GitHub App:
  the push triggers `push` CI, the PR needs no run approval, and auto-merge completes unattended. Without them the
  workflow falls back to `GITHUB_TOKEN`: that push does not trigger `push` CI, so `CI.yml` runs on the `pull_request`
  event instead, and GitHub requires one **Approve** click on that run from a writer before it starts.
- Locally, with a downloaded `botDB.sqlite3`: `scala-cli scripts/ExportFileIds.scala -- --check` (diff) or `--write`.
- Tests of the tool: `scala-cli test scripts/ExportFileIds.scala`.

## Rules for a source

- A source is an url, or a file reference tagged `<platform.id>:<id>` (`telegram`, `discord`, `irc`, `slack`).
- An untagged reference is invalid: `FileIdSourcesSpec` (modules/main) and `filesCheck` (`MediaListSchema.ts`) fail.
- Tag old untagged data with `scala-cli scripts/MigrateFileIdSources.scala -- --write` (idempotent, `--check` first).
- References never cross bot or platform; do not copy an id by hand.
- Keep only the newest reference per platform: a fresh DB id replaces the stale one in the `*_list.json` file, it does
  not pile up beside it. The DB's newest id is first in `media_sources` (`MediaSources.withFileId`).
