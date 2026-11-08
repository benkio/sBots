# Conflict Resolution After AddData

## Goal

After `sbt <botId>AddData`, resolve conflicts when newly added media is a
replacement of existing media (better quality and/or completing an
mp3/gif/mp4/markdown set when applicable).

The objective is to keep bot data coherent without touching unrelated, truly new data.

## Typical Input State

`AddData` usually appends:

- entries in `<bot_id>_list.json`
- placeholder `ReplyBundleMessage` entries in `<bot_id>_replies.json`
  with trigger `new data`

When the new media is actually a replacement, those placeholders are often wrong and must be merged into existing reply bundles.

## Tooling

Reusable, dependency-free helper:
[`agent-context/tools/conflict-resolution/conflict-resolution.mjs`](../tools/conflict-resolution/conflict-resolution.mjs)
(usage: [`README.md`](../tools/conflict-resolution/README.md)).

```bash
# Inventory: duplicates, new media, placeholders, candidate bundles
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --analyze

# Dry-run/apply a reviewed merge plan
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --merge-plan /tmp/<botId>-plan.json
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --merge-plan /tmp/<botId>-plan.json --write

# Keep only the AddData list entry for each filename
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --dedupe-list --write

# Final consistency checks
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --validate
```

The helper writes circe `spaces2`-compatible JSON so untouched files stay
byte-identical. It does not choose the primary bundle for you: use `--analyze`
to inspect candidates and record the decision in the merge plan.

## Conflict Detection Rules

Treat an item as a conflict candidate when one or more of these are true:

- the newly added media is the same semantic content as an existing one (replacement quality/version),
- the new media complements an existing base item (`.mp3`, `.mp4`, `Gif.mp4`, optional `.md`),
- two files share the same `<botid>_<filename>` base (for example `rphjb_Foo.mp4` and `rphjb_Foo.mp3`), even if `<bot_id>_list.json` does not contain duplicate rows,
- a new `new data` placeholder points to media that should belong to an existing trigger/reply bundle.

If uncertain whether it is a replacement or truly new content, stop and ask for clarification.

### Base-name normalization

An item's base is the filename without the bot prefix and without the media
suffix (`Gif.mp4`, `.mp4`, `.mp3`, `.md`, `.jpg`, `.png`, `.sticker`).
`rphjb_Foo.mp4`, `rphjb_Foo.mp3`, `rphjb_FooGif.mp4` and `rphjb_Foo.md` all
share the base `Foo`, matching `MediaFileNameOps.getPrefix`.

### Primary bundle selection

When the same base is referenced by several existing bundles:

- add the full new media set only to the primary/semantic bundle for that
  media content,
- leave auxiliary bundles with the canonical `.mp4` reference unless there is
  a clear reason to add the full set everywhere,
- record the choice in the report as a judgment call.

## Required Edits

### 1) Clean `<bot_id>_list.json`

- Keep only the canonical filename entry for each media item.
- Remove superseded/obsolete list entries for the replaced media.
- When `AddData` re-adds an existing filename, keep the newly added entry
  (the last one in the list) and drop the superseded HEAD entry.
- Ensure there are no duplicate filenames.

Note: bot tests include a duplicate-filename guard on list JSON.

### 2) Clean `<bot_id>_replies.json`

- Remove `new data` placeholder bundles that were created for replacement media.
- Update existing reply bundles so they reference the correct new media filenames.
- When relevant, ensure the expected media set (`.mp3`, `.mp4`, `Gif.mp4`, optional `.md`) is present in the same logical place where the old media was used.

### 3) Preserve real new content

- Do not delete `new data` placeholders that correspond to genuinely new content.
- Leave genuinely new entries for manual follow-up in Replies Editor.

## Validation

Run targeted checks after edits:

- `sbt "fix"` must succeed (it also regenerates `*_triggers.md` and calendars).
- Run `sbt "test"`.
  - `new data` exact-trigger failures are expected when more than one genuinely
    new placeholder is preserved: every such bundle shares the trigger
    `new data`, while the bot spec can resolve it to only one bundle. The
    failure mentions
    `Only one reply bundle replies to: "new data"` and
    `BaseBotSpec.exactTriggerReturnExpectedReplyBundle`.
  - Report those failures explicitly as expected; fix every other failure.
  - If a green suite is required before handoff, either resolve the
    placeholders in Replies Editor first or remove them (historical precedent:
    commit `fd035d692`, "➖ new data. Finish later!"), and state which choice
    was made.
- Ensure `inputTest.txt` is updated by addition only; if an existing
  expectation must shrink, stop and report it to a human.
- Optionally run the helper's `--validate` mode.

If a command fails, report the failure and likely cause clearly.

## Expected Agent Output

Provide a short report including:

- conflict items detected,
- files/entries removed or merged,
- placeholders removed vs placeholders intentionally kept,
- validations executed and their outcome,
- any ambiguity requiring human confirmation.
- output a temporary report file where you list the changes you have done
  (use [`report-template.md`](../tools/conflict-resolution/report-template.md)).

## Checklist

- [ ] Inspect the AddData diff for the affected bots.
- [ ] Run the helper's `--analyze` and classify every placeholder as
      replacement/completion or genuinely new.
- [ ] Ask a human if the classification is uncertain.
- [ ] Deduplicate `<bot_id>_list.json` (keep the AddData entry).
- [ ] Apply the reviewed merge plan and remove replacement placeholders.
- [ ] Leave genuinely new placeholders untouched.
- [ ] Update `inputTest.txt` by addition only.
- [ ] Run `sbt "fix"`.
- [ ] Run `sbt "test"` and classify failures.
- [ ] Write the temporary report and list any judgment calls.

## Suggested Workflow With Developer

1. Developer runs `sbt <botId>AddData ...`
2. Agent runs this conflict-resolution task (start with the helper's `--analyze`)
3. Developer uses Replies Editor to finish truly new `new data` entries
