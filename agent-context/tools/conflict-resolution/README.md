# Conflict Resolution Helper

Reusable helper for `agent-context/tasks/ConflictNewDataResolution.md`.

The script only uses Node built-ins. It reads bot JSON, reports the
conflicts/changes introduced by `AddData`, can classify and merge replacement
media, checks `inputTest.txt` with the bot's exact matching logic, and can write
a first-draft report. It writes JSON in the same circe `spaces2` shape as the
bot data files, so untouched files remain byte-identical.

## Quick start (preferred)

From the repository root:

```bash
# 1. See exactly what AddData changed (no ad-hoc diff scripts needed)
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --changes

# 2. Classify placeholders and get a ready-to-review merge plan
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --suggest            # writes /tmp/rphjb-suggested-plan.json with --write

# 3. One-shot dry run: dedupe list + apply merges + extend inputTest.txt
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --resolve

# 4. Apply, then validate (validate now checks inputTest.txt too)
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --resolve --write
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --validate
```

Review the printed plan before step 4. `--resolve` still prints every decision,
and it refuses to touch `inputTest.txt` if any existing expectation would need
to **shrink** (that is a stop-and-report condition).

Use `--list <path> --replies <path> [--inputtest <path>]` instead of `--bot` when
the bot cannot be auto-resolved from
`modules/bots/<BotDir>/<botId>_list.json`. Paths are resolved relative to the
repo root (absolute paths also work).

## Manual workflow (when judgment calls are needed)

```bash
# Inventory: duplicates, new media, placeholders, candidate bundles
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot rphjb --analyze

# Dry run, then apply a hand-reviewed merge plan
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot rphjb --merge-plan /tmp/rphjb-plan.json
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot rphjb --merge-plan /tmp/rphjb-plan.json --write

# Keep only the AddData list entry for each filename
node agent-context/tools/conflict-resolution/conflict-resolution.mjs --bot rphjb --dedupe-list --write
```

## What each action does

- `--analyze` (default): reports list duplicates, new AddData filenames,
  unreferenced new media, reply media missing from the list, every kept
  `new data` placeholder, and existing bundles that share the same media base.
- `--changes`: shows list files added/replaced and reply bundles whose media
  changed compared to `HEAD`, with triggers and added files. Replaces the
  ad-hoc `git diff` + Node scripts previously written each run.
- `--suggest`: classifies each `new data` placeholder as genuinely new or a
  replacement/set-completion, finds a unique target bundle, and prints a
  merge plan. Ambiguous bases (multiple candidates, no unique trigger) are
  printed as `SKIPPED` for a human to decide. Use `--plan-out <path>` or
  `--write` to save the plan.
- `--resolve`: one-shot `dedupe-list` + auto-merge + `inputTest.txt` patch.
  Only genuinely-safe merges are applied; skipped placeholders are reported as
  warnings and kept. Dry-run unless `--write` is passed.
- `--check-inputtest`: checks every `inputTest.txt` expectation against the
  bundle the bot would actually resolve, using the same scoring as
  `MessageMatches.doesMatch` (for `ContainsOnce`, the **longest matching**
  trigger wins; for `ContainsAll`, trigger lengths are summed). Reports lines
  that can be extended and lines that would shrink.
- `--validate`: checks duplicate filenames, reply media missing from the list,
  AddData media references, **and** `inputTest.txt` (use `--no-inputtest` to
  skip the last check).
- `--dedupe-list`: removes duplicate list entries. By default it keeps the last
  occurrence, which is the AddData one because `DataEntry` appends; use
  `--keep-first` to keep the original entry instead.
- `--merge-plan`: for each `{ base, match }` entry, finds the placeholder(s)
  for that media base, finds exactly one existing non-placeholder bundle whose
  trigger contains `match` (case-insensitive), appends the missing media, and
  removes the placeholder. Set `"removePlaceholder": false` to keep it.
- `--report <path>`: writes a first-draft report from `report-template.md`,
  filled with the classification, counts, duplicate details and merge table.
  Validation results and judgment calls are left as placeholders for the agent
  to complete after `sbt "fix"` / `sbt "test"`.

Nothing is written unless `--write` is passed.

## Merge-plan semantics

`base` is the filename without the bot prefix and without the media suffix:

| filename | base |
| --- | --- |
| `rphjb_Foo.mp4` | `Foo` |
| `rphjb_Foo.mp3` | `Foo` |
| `rphjb_FooGif.mp4` | `Foo` |
| `rphjb_Foo.md` | `Foo` |

`match` is a case-insensitive substring of a trigger value, or of the raw body
of a regex trigger. It must select exactly one bundle; the script aborts
otherwise so the decision stays explicit.

When a base is referenced by more than one bundle, add the full set only to the
primary semantic bundle. The other bundles keep the canonical `.mp4` reference
unless a human decides otherwise.

## Placeholders are deliberate

Genuinely new content must keep its `new data` placeholder for Replies Editor.
Keeping more than one placeholder makes the bot spec's exact-trigger test fail
(`new data` resolves to only one bundle). That is expected while the
placeholders exist; see the task file for how to report it.

## Report

Use `--report <path>` (or fill `report-template.md` by hand) to produce the
temporary report requested by the task. Always review the judgment-call lines
before handoff — the helper cannot know which bundle is "primary" or whether
you chose to keep placeholders for a green suite.
