# Replies Editor (GUI)

This module provides a **local single-page web app** to view and edit bot `*_replies.json` files, with:

- A **GUI** (Scala.js + Laminar + Bootstrap)
- A local **HTTP server** (http4s) that reads/writes the real files on disk
- **Filename constraints** from each bot’s `*_list.json`
- Automatic regeneration of `*_triggers.md` after a successful save

The UI sources live in `modules/repliesEditorUI/` and are linked by sbt and copied into this server’s resources at build time.

## Run

From repository root:

```bash
SBT_NATIVE_CLIENT=false sbt -Dsbt.server.autostart=false "repliesEditorServer/run"
```

Then open:

- `http://127.0.0.1:8088/`

## What it edits

- **Replies**: `modules/bots/<BotName>/src/main/resources/<botId>_replies.json`
- **Allowed filenames**: `modules/bots/<BotName>/<botId>_list.json`
- **Triggers (generated)**: `modules/bots/<BotName>/<botId>_triggers.md`

On save, the server:

1. Decodes the posted JSON as `List[ReplyBundleMessage]`
2. Validates that every `mediaFile.filepath` exists in `<botId>_list.json`
3. Writes `<botId>_replies.json` (pretty-printed JSON)
4. Regenerates `<botId>_triggers.md`

## UI behavior

- The UI renders entries in a **3-column grid** to save space.
- Entries that don’t match the “TextTrigger + MediaReply” shape are currently shown as **non-editable** and are **preserved as-is** on save.

## API

- `GET /api/bots`
- `GET /api/bot/{botId}/replies`
- `GET /api/bot/{botId}/allowed-files`
- `POST /api/bot/{botId}/replies`

## Code layout

- `src/main/scala/com/benkio/replieseditor/server/module/`: API + internal models
- `src/main/scala/com/benkio/replieseditor/server/load/`: repo-root detection + bot discovery
- `src/main/scala/com/benkio/replieseditor/server/jsonio/`: read/write helpers for replies/list/triggers
- `src/main/scala/com/benkio/replieseditor/server/validation/`: save-time validation
- `src/main/scala/com/benkio/replieseditor/server/endpoints/`: one file per endpoint
- `src/main/scala/com/benkio/replieseditor/server/server/`: wiring + server builder

