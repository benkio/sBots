# Conflict Resolution Helper

Reusable helper for `agent-context/tasks/ConflictNewDataResolution.md`.

The script only uses Node built-ins. It reads bot JSON, reports the
conflicts/changes introduced by `AddData`, and can apply a reviewed merge plan.
It writes JSON in the same circe `spaces2` shape as the bot data files, so
untouched files remain byte-identical.

## Quick start

From the repository root:

```bash
# 1. Inventory the bot after AddData
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --analyze

# 2. Review candidates, then write a merge plan
cat > /tmp/rphjb-plan.json <<'JSON'
{
  "merges": [
    { "base": "2Orecchie1Bocca2", "match": "sentire il doppio" },
    { "base": "Nodi", "match": "ci siamo sciolti" }
  ]
}
JSON

# 3. Dry run, then apply
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --merge-plan /tmp/rphjb-plan.json

node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --merge-plan /tmp/rphjb-plan.json --write

# 4. Remove duplicate list entries (keeps the last AddData entry)
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --dedupe-list --write

# 5. Validate the result
node agent-context/tools/conflict-resolution/conflict-resolution.mjs \
  --bot rphjb --validate
```

Use `--list <path> --replies <path>` instead of `--bot` when the bot cannot be
auto-resolved from `modules/bots/<BotDir>/<botId>_list.json`.

## What each action does

- `--analyze` (default): reports list duplicates, new AddData filenames,
  unreferenced new media, reply media missing from the list, every kept
  `new data` placeholder, and existing bundles that share the same media base.
- `--dedupe-list`: removes duplicate list entries. By default it keeps the last
  occurrence, which is the AddData one because `DataEntry` appends; use
  `--keep-first` to keep the original entry instead.
- `--merge-plan`: for each `{ base, match }` entry, finds the placeholder(s)
  for that media base, finds exactly one existing non-placeholder bundle whose
  trigger contains `match` (case-insensitive), appends the missing media, and
  removes the placeholder. Set `"removePlaceholder": false` to keep it.
- `--validate`: fails on duplicate list filenames or reply media missing from
  the list, and warns about new AddData media that no reply bundle references.

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

Use `report-template.md` to produce the temporary report requested by the task.
