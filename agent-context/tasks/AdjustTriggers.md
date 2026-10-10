# Adjust Triggers

## Goal

Fix incorrect bot trigger behavior when a bot:

- replies when it should not, or
- does not reply when it should.

The support team reports concrete cases; this task turns those reports into safe trigger updates.

## Required Inputs (ask developer if missing)

- bot/module name (or bot id),
- target filename(s) involved in the expected reply,
- input message text that reproduces the issue,
- expected behavior: `trigger` or `do not trigger`.

If any of these are unclear, pause and ask before editing.

## Execution Steps

### 1) Reproduce and locate the trigger

- Find the relevant bot reply entry in `*_replies.json`.
- Identify which trigger currently matches (or fails to match) the provided message.

### 2) Apply minimal trigger change

- Update only the necessary trigger/matcher logic to satisfy the expected behavior.
- Prefer the smallest safe change to avoid regressions on unrelated inputs.
- When the new case is a close variant of an existing trigger, prefer extending the
  existing trigger definition (for example with a scoped regex) instead of adding
  many near-duplicate strings.
- Any extension must preserve previous behavior: inputs that matched before must
  still match after the change, and unrelated inputs must not start matching.

### 3) Update regression input tests (when needed)

If the issue is "should trigger but currently does not trigger":

- add/update a line in that bot `src/test/resources/inputTest.txt` with format:
  `input message -> filename1, filename2`
- `inputTest.txt` uses **one input per line** only; multiline messages must be
  flattened into a single representative line.
- Keep the input as close as possible to the original reported message. You may
  redact personal names/handles, but preserve wording and structure when feasible.
- include only the expected media filenames for that input.
- This update is mandatory for wanted-trigger cases ("should trigger").

If the issue is "should not trigger", ignore.

## Validation

After edits, run:

- `sbt "fix; test"`

If runtime is too long, run at least the affected bot test suite first, then full test when requested.

## Expected Agent Output

Return a short report with:

- issue summary and expected behavior,
- trigger changes made,
- whether `inputTest.txt` was updated (and why),
- tests executed and results,
- any residual ambiguity or follow-up needed from the developer.
