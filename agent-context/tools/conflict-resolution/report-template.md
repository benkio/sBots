# Conflict Resolution After AddData — YYYY-MM-DD

Bot(s): `<bot ids>`
Temporary report path: `<this file>`

## Conflicts detected

### Replacement / set-completion

- `<base>`: new media completes/replaces `<existing media set>`.

### Genuinely new content

- `<base>`: no existing bundle or media base match; kept as `new data`.

## Edits

### `<bot>_list.json`

- Removed `<n>` superseded duplicate entries:
  - `<filename>` (`<old source>`)
- Final count: `<n>` entries, `<n>` duplicate filenames.

### `<bot>_replies.json`

- Removed `<n>` replacement placeholders.
- Merged media sets into existing bundles:

| Base | Target trigger(s) | Added media |
| --- | --- | --- |
| `<base>` | `<triggers>` | `<files>` |

- Final count: `<n>` bundles, `<n>` placeholders.

### `inputTest.txt`

- `<input>`: added `<media files>`.
- Confirm this was an addition only.

### Generated files

- `sbt "fix"` regenerated `*_triggers.md` and calendars.

## Placeholders

Removed: `<n>` (replacement/completion).

Kept intentionally: `<n>` genuinely new placeholders:

- `<base>`: `<media>`

## Validation

- `sbt "fix"`: passed.
- `sbt "test"`: `<result>`.
  - Known `new data` exact-trigger failures: `<bot/spec>`.
  - Other failures: `<none / details>`.
- List duplicate check: `<result>`.
- New AddData media referenced: `<result>`.

## Ambiguities / follow-up

- `<multi-bundle primary choice, if any>`
- `<keep-vs-green decision>`
- `<anything requiring human confirmation>`
