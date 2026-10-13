# Split Trigger Bundle

## Goal

Split one existing reply bundle into two bundles when only a subset of triggers
must receive additional media/data.

Use this task during/after data additions when a combined trigger bundle must be
separated into:

- a narrowed trigger subset with augmented media/data,
- and the remaining original triggers without the augmentation.

## Required Inputs (ask developer if missing)

- bot/module name (or bot id),
- source trigger bundle to split (how to identify it: trigger text and/or expected media),
- trigger list that must move to the new bundle (string and/or regex triggers),
- media/data to add only to the new bundle,
- expected behavior for the original bundle after split (usually keep existing media unchanged).

If any input is unclear, pause and ask before editing.

## Execution Steps

### 1) Locate and inspect source bundle

- Open `<bot_id>_replies.json`.
- Find the single bundle that currently contains all relevant triggers.
- Check both trigger kinds:
  - `StringTextTriggerValue`
  - `RegexTextTriggerValue`
- Confirm it contains the triggers-to-move and the existing media baseline.

### 2) Perform the split

- Create a new bundle with:
  - only the specified triggers-to-move,
  - original media/data plus the new media/data requested by the developer.
- Update the original bundle with:
  - original triggers minus the moved subset,
  - original media/data only (unless the developer explicitly asks otherwise).
- Keep relative ordering stable when possible (replace one bundle with two adjacent bundles).

### 3) Safety checks on split quality

- Ensure no trigger is lost and no unrelated trigger is added.
- Ensure moved triggers are not duplicated in the original bundle.
- Ensure media references are valid filenames present in `<bot_id>_list.json`.
- Preserve matcher semantics (`ContainsOnce`, etc.) unless explicitly requested.
- Avoid duplicate-overlap structures that tests reject:
  - do not introduce two `TextTrigger` bundles with identical reply+matcher payloads.
  - if needed, ask for clarification or adjust split shape to keep unique bundle semantics.

### 4) Regression input tests

When behavior changed for user-visible trigger paths, update
`src/test/resources/inputTest.txt` with representative inputs:

- one line per input: `input message -> filename1, filename2`
- include cases for both:
  - moved trigger(s) (should return augmented bundle),
  - remaining trigger(s) (should return original bundle behavior).

## Validation

After edits, run:

- `sbt "fix"` (**mandatory**)
- `sbt "<AffectedBotProject>/test"` (at minimum),
- optionally broader `sbt "test"` when requested.

If a command fails, report failure and likely cause clearly.

## Expected Agent Output

Return a short report with:

- source bundle identified,
- triggers moved vs triggers kept,
- media/data added to new bundle,
- test-input updates made (or why not),
- validation commands and results,
- any ambiguity requiring developer confirmation.
