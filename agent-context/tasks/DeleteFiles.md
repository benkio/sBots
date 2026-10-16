# Delete Bot Files Safely

## Goal

Delete one or more user-specified bot media files safely by:

- confirming target files,
- verifying presence across metadata and storage,
- requiring extra confirmation when files are referenced in replies,
- removing requested files plus sibling variants consistently,
- validating cloud sync and project health after deletion.

Use this task when the user asks to remove media files from a bot.

## Required Inputs (ask user if missing)

- bot/module name (or bot id),
- list of exact filenames to delete,
- confirmation that deletion should proceed when reply references are found.

If the user provides ambiguous names (missing extension, partial base name),
pause and ask for exact filenames before any deletion.

If the user explicitly says metadata was already cleaned in repo and asks for
storage-only cleanup, do not force bot-id/list/replies edits. Use the
storage-only branch below.

## Execution Steps

### 1) Ask which files must be deleted

- Ask the user for the file list to delete.
- Confirm the list back before applying changes.

### 2) Check existence in storage and list metadata

For each requested filename:

- verify existence in storage (`Mega` or `Dropbox`, based on bot setup),
- verify existence in `<bot_id>_list.json`.

If a file is missing from both storage and list metadata, report it and ask
whether to continue with remaining files.

#### 2a) Storage-only cleanup branch (already cleaned in repo)

Use this branch when the user confirms the target is intentionally absent from
`<bot_id>_list.json` / `<bot_id>_replies.json` and only cloud cleanup is needed.

- Verify presence in storage provider (`Mega`/`Dropbox`) or local sync path.
- If found in storage, delete it and verify absence after deletion.
- If not found in storage/filesystem, do nothing and report a no-op.
- Skip metadata edits and skip reply-reference confirmation for that file,
  because project references are already cleaned by design.

### 3) Resolve sibling family to delete

For each confirmed target, compute sibling files sharing the same base name:

- `<botid>_<name>.mp3`
- `<botid>_<name>.mp4`
- `<botid>_<name>Gif.mp4`
- `<botid>_<name>.md`
- plus any same-base variant matching `<botid>_<name>[Gif]?.<extension>` in
  current data.

The deletion set is: requested files + discovered siblings.

### 4) Check replies references and ask confirmation

- Search `<bot_id>_replies.json` for every file in the deletion set.
- If at least one file is referenced, explicitly ask the user:
  "These files are used in replies. Do you want to proceed with deletion?"
- If user answer is negative, stop deletion and leave files untouched.

Note: this step is skipped for files handled via the storage-only cleanup branch.

### 5) Delete from JSON metadata first

If deletion is confirmed (or no replies reference exists):

- remove deletion-set filenames from `<bot_id>_list.json`,
- remove deletion-set filename references from `<bot_id>_replies.json`,
- ensure no stale references remain after edit.

Note: skip this step for files handled via the storage-only cleanup branch.

### 6) Delete from filesystem/storage

- Remove the same deletion-set files from local synced storage path.
- Ensure each file no longer exists in filesystem.

### 7) Validate cloud synchronization

- Verify deletion has synchronized successfully with the configured provider
  (`Mega`/`Dropbox`).
- If sync status is unclear or failed, report and stop before closing task.

## Validation

After edits and deletions, run:

- targeted search confirming deleted filenames are absent from bot list/replies,
- `sbt "fix; test"` (or equivalent sequence `sbt "fix" "test"` if needed).

If the operation was storage-only with no repository file changes:

- still verify storage/cloud absence and sync status,
- skip `sbt` and report "no repo edits performed".

If checks fail, report clearly whether failures are deletion-related or pre-existing.

## Expected Agent Output

Return a short report with:

- requested files and final resolved deletion set (including siblings),
- whether reply-reference confirmation was required and user decision,
- files removed from list/replies/storage,
- cloud sync verification result for `Mega`/`Dropbox`,
- validation commands and results,
- any manual follow-up required.
