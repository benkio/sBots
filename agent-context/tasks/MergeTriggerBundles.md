# Merge Trigger Bundles

## Goal

Consolidate semantically overlapping triggers into a canonical structure, typically:

- one dedicated trigger bundle for the normalized trigger phrase,
- media reply built from the union of source bundle media (when requested),
- removed duplicate/legacy trigger values from source bundles.

Use this task when developers request operations like:

- "merge trigger A and trigger B, keep only trigger B",
- "create a new bundle with trigger X and union of both old bundles",
- "remove old trigger values from existing bundles after merge".

## Required Inputs (ask developer if missing)

- bot/module name (or bot id),
- source triggers (all variants to consolidate),
- canonical trigger value to keep/use,
- desired target shape:
  - update one existing bundle only, or
  - create a dedicated new bundle,
- media policy:
  - keep existing media only, or
  - union media from selected source bundles.

If requested shape is ambiguous, pause and ask before editing.

## Execution Steps

### 1) Locate source bundles and trigger kinds

- Open `<bot_id>_replies.json`.
- Locate every relevant source trigger across:
  - `StringTextTriggerValue`
  - `RegexTextTriggerValue`
- Record source bundles and media sets before editing.

### 2) Apply merge strategy

- Normalize trigger usage according to request:
  - remove deprecated trigger variants from old bundles,
  - keep/add canonical trigger value in the target bundle.
- If a dedicated new bundle is requested:
  - create it with only the canonical trigger,
  - populate reply media per requested policy (often union of source media).
- Keep matcher (`ContainsOnce`, etc.) unchanged unless explicitly requested.

### 3) Prevent invalid overlap patterns

- Ensure no trigger value remains in both legacy and canonical locations.
- Avoid duplicate-overlap bundles (same trigger kind + same reply + same matcher)
  that violate repository tests.
- Ensure all media filenames exist in `<bot_id>_list.json`.

### 4) Regression input tests

- Update bot `inputTest.txt` when trigger behavior or returned media changed.
- Use one-line format:
  - `input message -> filename1, filename2`
- Include at least one input for the canonical trigger phrase.

## Validation

After edits, run:

- `sbt "fix"` (**mandatory**)
- `sbt "<AffectedBotProject>/test"` (at minimum)
- broader `sbt "test"` if requested.

If validation fails, report whether failure is:

- introduced by this merge, or
- pre-existing (e.g. unrelated `new data` collisions).

## Expected Agent Output

Return a short report with:

- triggers merged and canonical trigger retained,
- bundle shape chosen (existing-vs-new bundle),
- media union/selection applied,
- removed legacy triggers and affected bundles,
- test updates and validation results,
- any ambiguity requiring developer confirmation.
