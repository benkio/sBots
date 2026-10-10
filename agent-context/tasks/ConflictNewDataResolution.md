# Conflict Resolution After AddData

## Goal

After `sbt <botId>AddData`, resolve conflicts when newly added media is a
replacement of existing media (better quality and/or completing an mp3/gif/mp4 trio).

The objective is to keep bot data coherent without touching unrelated, truly new data.

## Typical Input State

`AddData` usually appends:

- entries in `<bot_id>_list.json`
- placeholder `ReplyBundleMessage` entries in `<bot_id>_replies.json`
  with trigger `new data`

When the new media is actually a replacement, those placeholders are often wrong and must be merged into existing reply bundles.

## Conflict Detection Rules

Treat an item as a conflict candidate when one or more of these are true:

- the newly added media is the same semantic content as an existing one (replacement quality/version),
- the new media complements an existing base item (`mp3`, `.mp4`, `Gif.mp4` trio),
- two files share the same `<botid>_<filename>` base (for example `rphjb_Foo.mp4` and `rphjb_Foo.mp3`), even if `<bot_id>_list.json` does not contain duplicate rows,
- a new `new data` placeholder points to media that should belong to an existing trigger/reply bundle.

If uncertain whether it is a replacement or truly new content, stop and ask for clarification.

## Required Edits

### 1) Clean `<bot_id>_list.json`

- Keep only the canonical filename entry for each media item.
- Remove superseded/obsolete list entries for the replaced media.
- Ensure there are no duplicate filenames.

Note: bot tests include a duplicate-filename guard on list JSON.

### 2) Clean `<bot_id>_replies.json`

- Remove `new data` placeholder bundles that were created for replacement media.
- Update existing reply bundles so they reference the correct new media filenames.
- When relevant, ensure the full trio (`.mp3`, `.mp4`, `Gif.mp4`) is present in the same logical place where the old media was used.

### 3) Preserve real new content

- Do not delete `new data` placeholders that correspond to genuinely new content.
- Leave genuinely new entries for manual follow-up in Replies Editor.

## Validation

Run targeted checks after edits:

- bot test(s) that validate JSON/list integrity (especially duplicate filenames),
- optionally broader checks if requested by the user.
- ensure `sbt "fix; test"` succeed
- ensure the `inputTest.txt` is updated. Only go in addition,
  otherwise report it to human

If a command fails, report the failure and likely cause clearly.

## Expected Agent Output

Provide a short report including:

- conflict items detected,
- files/entries removed or merged,
- placeholders removed vs placeholders intentionally kept,
- validations executed and their outcome,
- any ambiguity requiring human confirmation.
- output a temporary report file where you list the changes you have done

## Suggested Workflow With Developer

1. Developer runs `sbt <botId>AddData ...`
2. Agent runs this conflict-resolution task
3. Developer uses Replies Editor to finish truly new `new data` entries
