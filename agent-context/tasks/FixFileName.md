# Fix Media Filename

## Goal

Rename an existing bot media file to a new filename and keep repository metadata
consistent, including the shared source URL in `<bot_id>_list.json`.

Use this task when the developer asks to "rename file X to Y" for bot media.
This task is also valid when the rename is a manual correction (for example typo
fixes) even when `filesCheck` did not report an error.

## Required Inputs (ask developer if missing)

- bot/module name (or bot id),
- current filename,
- target filename,
- storage provider (`Mega` or `Dropbox`) if not inferable.
- rename scope:
  - only this file, or
  - all sibling media sharing the same base name (typical trio `.mp3`, `.mp4`,
    `Gif.mp4`), or
  - another explicitly requested group.

If the request might be a typo correction and scope is not explicit, ask the
developer whether sibling files with the same base name must be renamed too.

If the target filename violates repository naming rules, pause and ask before
renaming.

## Execution Steps

### 1) Confirm rename intent and sibling propagation

- Detect sibling files with same base name (for example:
  `botid_Name.mp3`, `botid_Name.mp4`, `botid_NameGif.mp4`).
- If siblings exist and the developer did not explicitly constrain scope, ask
  whether the rename must be propagated to the full sibling family.
- For typo-style corrections, default to asking for sibling propagation instead
  of silently renaming only one file.

### 2) Validate target filename(s) and locate JSON entry

- Open `<bot_id>_list.json` and find the entry with the current filename.
- Confirm there is exactly one matching entry.
- For each file in scope, ensure the target filename:
  - starts with `<bot_id>_`,
  - uses supported extension (`.mp3`, `.mp4`, `Gif.mp4`, `.jpg` where applicable),
  - does not exceed naming constraints used by checks (max 64 chars).
- Ensure no existing `<bot_id>_list.json` entry already uses any target
  filename in scope.

### 3) Locate and rename file(s) in storage

- Determine storage from existing source URL in `<bot_id>_list.json`:
  - `https://mega.nz/...` => Mega
  - `https://www.dropbox.com/...` => Dropbox
- Locate the physical file under the bot resources folder in the synced cloud dir.
- Rename every file in scope from current to target name in storage.
- If rename cannot be completed safely, stop and report the blocker.

### 4) Regenerate/re-validate shared URL(s) for renamed file(s)

- Create or retrieve a valid shared URL for the new filename using the same
  provider type used by that bot entry.
- Do not reuse an old URL when it points to an old path/name.
- The pair `filename -> URL` must be explicitly verified for every renamed file.
- Check that each URL resolves to the intended renamed asset (or provider item)
  and that provider type matches storage (`Mega` vs `Dropbox`).
- If provider links are item-id based and remain valid after rename, explicitly
  verify and document that behavior for each renamed file.
- If remote verification cannot be completed, stop and report the rename as
  incomplete until human confirmation is provided.

### 5) Update bot metadata

- In `<bot_id>_list.json`:
  - replace each `"filename"` from old to new value for every renamed file,
  - replace corresponding `"sources"` URL(s) when needed.
- Search `*_replies.json`, `*_triggers.md`, and bot `inputTest.txt` for old
  filename(s) and update references when present.

### 6) Safety checks

- Verify each renamed file exists on disk in storage with the new name.
- Verify old filename(s) no longer appear in the bot media list JSON.
- Verify exactly one media list entry matches each new filename.
- Verify each renamed media list entry has exactly one aligned, reachable source
  URL for that filename.
- When propagation was requested, verify no sibling still keeps the old base
  name.

## Validation

After edits, run:

- targeted search to confirm no stale references to old filename in the bot files,
- `npm run start` in `filesCheck/` when the change affects media naming/URL
  integrity for that bot,
- mandatory URL alignment check for each renamed file (`filename -> source URL`),
- additional project tests if requested by the developer.

If validation fails, report whether it is caused by this rename or pre-existing.

## Expected Agent Output

Return a short report with:

- bot id and rename performed (`old -> new`),
- storage location/provider used,
- verified filename-to-URL mapping for every renamed file,
- files updated in repo,
- validation commands and results,
- any manual follow-up needed from the developer.
