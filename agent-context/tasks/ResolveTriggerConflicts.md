# Resolve Trigger Conflicts

## Goal

Remove trigger collisions that appear after new data is added (or after a merge)
while preserving every affected reply bundle's media.

A collision is any pair of triggers that can both fire for the same user input:

- same trigger kind and same value (string/string or regex/regex),
- a regex in one bundle that matches a string trigger in another bundle,
- an exact string/regex value duplicated across bundles.

Trigger collisions must be split, merged, or adjusted; they must never be left in
two bundles.

## Terminology

- **A**: the bundle that contains the conflicting trigger (usually a new
  `new data` bundle or a freshly added one).
- **B**: another bundle that already contains a trigger matching the same input.
- **X**: the conflicting trigger value shared by A and B.
- **C**: a new bundle created only when B has additional triggers.

## Required Inputs (ask developer if missing)

- bot/module name (or bot id),
- bundles that contain the collisions (A) and their new media,
- confirmation that the media of A should be exposed for the conflicting input
  (the default rule below assumes yes).

If it is unclear whether the new media should replace or extend the existing
reply, stop and ask before editing.

## Detection

1. Open `<bot_id>_replies.json`.
2. For every trigger `X` of each new/affected bundle `A`, search all other
   bundles `B` for:
   - `StringTextTriggerValue` equal to `X`,
   - `RegexTextTriggerValue.trigger` equal to `X`,
   - a regex that matches `X` (regex/string cross-kind collision),
   - a string that a regex `X` matches.
3. Record each `A`, `X`, and matching `B` before editing.

Do not rely only on exact string equality: `testa di cazzo` collides with
`testa di (cazzo|minchia)` too.

## Resolution Rule

For each conflict `X` between bundle A and bundle B:

### Case 1 — B has only X as its trigger

1. Merge A's media list into B's media list (union, deduplicated by filepath).
2. Remove X from A.
3. Leave B in place (its trigger is still X and now returns both media sets).

### Case 2 — B has X plus other triggers

1. Create a new bundle C whose only trigger is X.
2. Populate C with the union of A's and B's media lists (deduplicated by
   filepath).
3. Remove X from A.
4. Remove X from B.
5. Keep B and its remaining triggers and media unchanged.

### After each conflict

- A keeps its remaining triggers and its media list.
- If A has no triggers left, remove the empty A bundle.
- Keep matcher (`ContainsOnce`, etc.) unchanged; C inherits A's matcher.
- If the same bundle B receives several conflicts from A, apply the rule per
  conflict and re-read B's trigger list before the next decision.
- Preserve bundle order where practical: insert C directly after B.

## Lowercase Normalization

All triggers must be lowercase. Normalize any uppercase trigger in the affected
bundles (including triggers that were not part of a collision) while applying
this task.

## Safety Checks

- No trigger value must remain in both A and B (or in C and its source).
- No exact duplicate triggers across all bundles.
- No new cross-kind regex/string collision is introduced.
- Media filenames must remain valid entries in `<bot_id>_list.json`.
- Do not delete media or bundles that are unrelated to the resolved conflicts.
- Do not duplicate a media filepath inside the same bundle.

## Worked Example

From the Pino `PapaDiJimFattoGodere` bundle:

| X | B | B trigger count | Action |
| --- | --- | --- | --- |
| `\bjack\b` | `pino_MaVafanguloVaJack` | 1 | merge A media into B, remove X from A |
| `pap[àa] di jim` | `pino_PapaDiJim` | 2 | create C with X + union media, remove X from A and B |
| `vita di merda` | `pino_MessaggiSentireMeglio` | 6 | create C with X + union media, remove X from A and B |
| `mitolo[g]+ico` | `pino_ColeotteroSputarsiInFaccia` | 4 | create C with X + union media, remove X from A and B |

After resolving all conflicts, A keeps only its non-conflicting triggers
(`grazifatto goderee, pino`, `non lamentiamoci noi`).

## Related Tasks

- [AdjustTriggers.md](AdjustTriggers.md): minimal, behavior-driven trigger edits.
- [MergeTriggerBundles.md](MergeTriggerBundles.md): general merge operations.
- [SplitTrigger.md](SplitTrigger.md): splitting a bundle when only part needs new media.
- [ConflictNewDataResolution.md](ConflictNewDataResolution.md): replacement vs.
  genuinely new data after `AddData`.

## Validation

After edits, run:

- `node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot <botId> --validate`
- `sbt "fix"` (**mandatory**; also regenerates `*_triggers.md` and calendars)
- `sbt "<AffectedBotProject>/test"` (at minimum)

If a command fails, report the failure and whether it is caused by this change
or was already present.

## Expected Agent Output

Return a short report with:

- every conflict found (`A`, `X`, `B`, B trigger count),
- whether Case 1 (merge into B) or Case 2 (create C) was applied,
- media files added to B or C,
- bundles removed because they became empty,
- lowercase normalizations,
- validation commands and results,
- any ambiguity requiring developer confirmation.
