# Add a Platform Adapter

## Goal

Make the bots run on a new chat platform (IRC, Discord, Slack, ...) next to Telegram, by adding an adapter module and a
runtime in `main`, without touching the logic of the bots.

Use this task when the developer asks to "support IRC/Discord/Slack", "add a platform", or similar. The overview of the
architecture is in `docs/bot-logic-overview.md` (section 3) and in `modules/chatCore/README.org` (Platforms). The Telegram
adapter (`modules/chatTelegramAdapter`) is the reference implementation: read it first.

## Required Inputs (ask developer if missing)

- the platform (`Platform.Irc`, `Platform.Discord`, `Platform.Slack`, or a new one),
- the client library to use (and a decision on how messages arrive: a connection the bot keeps open, or a webhook on an
  HTTP server),
- the credentials the platform needs and where they come from (config, env vars, token files),
- what the platform can do: kinds of media, maximum length of a text, markdown (and its dialect), delete/edit messages,
  buttons,
- how a command is written (prefix, mention of the bot) and how a chat is identified (the `chat_id` string).

If any of these is unclear, pause and ask before writing code.

## What is already there (do not redo)

- `Platform` has `Telegram`, `Discord`, `Irc`, `Slack`; the `platform` table of the DB has the same four rows
  (`V3__CreatePlatformTable.sql`). A fifth platform needs a new `case` in `Platform` **and** a row in that migration: the
  checksum changes, so the DB must be regenerated and uploaded like in the `README.org` (Database Setup / Deploy).
- `timeout` and `subscription` are keyed by `(platform, chat_id, bot_id)`; `ChatId(platform, id)` carries both.
- `BotEngine`, `ReplyTransformer`, `ReplyAdaptation`, `PagedList`, `CronBackgroundJobManager`, `Notifier`,
  `CommandSyntax`, the commands (`CommandPatterns`) are in `chatCore` and are platform agnostic.

## Execution Steps

### 1) Module

- `build.sbt`: define `chat<Platform>Adapter` like `chatTelegramAdapter`
  (`Settings.settings`, a `Settings.Chat<Platform>AdapterSettings`, `.dependsOn(chatCore % "compile->compile;test->test")`,
  `.disablePlugins(AssemblyPlugin)`), add it to the `sBots` aggregate and to the `dependsOn` of `main` (and of
  `integration` if its tests need it).
- `project/Settings.scala` and `project/Dependencies.scala`: the settings and a `Chat<Platform>AdapterDependencies`
  (`ChatCoreDependencies ++ Seq(<the client library>)`, see `ChatTelegramAdapterDependencies`).
- The bot modules must stay on `chatCore` only: never add the adapter to them.

### 2) Capabilities

- A `<Platform>Capabilities.value` of type `PlatformCapabilities` (see `TelegramCapabilities`): `platform`,
  `supportedMedia` (a `Set[MediaKind]`, empty for a text only platform), `maxTextLength`, `markdown`
  (`MarkdownSupport.Native` or `Plain`), `supportsDelete`, `supportsEdit`, `supportsButtons`, `acceptsFileIds` (only
  Telegram has file ids), `commandSyntax` (`CommandSyntax(prefix, mentionSeparator, fileRequestMarker)`).
- What the platform cannot do is handled for free by the `ReplyTransformer` through these values: do not special-case it
  in the adapter.

### 3) Adapter

- Implement `PlatformAdapter[F]` (`chatCore`): `capabilities`, `run` (connect and keep receiving), `send(replyValue, message,
  replyToMessage)` and `runEffectful(reply, message)`.
- Inbound: map what the platform delivers to a `Message` with `chatId = ChatId(Platform.X, "<platform chat id>")` and hand it
  to `engine.onMessage(message)(using adapter)`. Ignore the messages of the bot itself.
- Build the engine with `BotEngine(sBotConfig, dbLayer)` (standard timeout filter and post computation), with the replies
  loaded by `DBLayer.loadReplies`.
- `send` receives values already adapted to the capabilities: a `Text`, a `Markdown` (only if the platform has markdown), a
  `MediaFile` of a supported kind. Values you do not know (anything else) must be logged and not sent.
- `runEffectful`: the commands that need the database are computed by `EffectfulKeyRunner`, which today lives in
  `chatTelegramAdapter` although it only uses `chatCore` types. **Move it to `chatCore` first** (it is a known follow-up)
  rather than depending on the Telegram module.
- Per-bot configuration, token reading and the DB layer are built by `BotSetup` for Telegram: copy the shape, not the
  Telegram specifics.

### 4) Notifier and subscriptions

- A `Notifier[F]` for the platform (`info`, `mediaError`; see `TelegramNotifier`): where the operators get the errors.
- The subscriptions: `CronBackgroundJobManager(dbLayer, sBotInfo, ttl, capabilities, send, calendarMessage)` with a
  `send: (ChatId, ReplyValue, Boolean) => F[Unit]` that delivers to a chat (the boolean is "reply to the message": a
  subscription has no message, decide what it means for the platform). `calendarMessage` defaults to the unlimited
  calendar; the transformer splits it.

### 5) `main`

- The config: a block `main.<platform>` in `modules/main/src/main/resources/application.conf` and a case class like
  `TelegramConfig` in `Config.scala` (credentials from env vars, with `${?ENV}`), exposed by `MainSetup`.
- A `PlatformRuntime` in `modules/main/.../runner` (see `TelegramRuntimes.scala`): `platform` and
  `start(entries, mainSetup)`, which gives the work to keep going. If the platform receives its messages on an HTTP
  server, the server belongs to that runtime.
- Register it: `Runner.runWebhook` and `Runner.runPolling` pass the list of runtimes to `Runner.start`; add the new one to
  the mode(s) where it must run.
- Bind the bots: `BotRegistryEntry(..., platforms = List(Platform.Telegram, Platform.X))` in `BotsRegistry.scala`. A bot
  on a platform with no runtime makes the startup fail with a clear error.

### 6) Docs

- Update `docs/bot-logic-overview.md` (the table in section 3 and the known gaps), `modules/chatCore/README.org` if the
  abstractions change, and the README section on the config blocks.

## Validation

Run from the repo root and report the real results:

- `sbt fix` (scalafmt + scalafix) must finish with no `[error]`; run it again and check `git status` has nothing new.
- `sbt Test/compile`, then `sbt "<module>/testOnly *Spec"` for `chatCore`, the new adapter, `main` and the bots (the sbt 2
  test cache can print `Total 0`: `testOnly` forces a real run).
- Tests to add: the capabilities and the transformation they imply (`ReplyTransformerSpec` style), the inbound mapping, the
  `send` with a recording API, the notifier, a runtime in `RunnerSpec` style (a fake `PlatformRuntime` shows the wiring),
  and engine behavior on the platform with `FakePlatformAdapter` (`chatCore` test mocks) when it needs a capability set
  that is not covered.
- `sbt check` and `sbt main/assembly` must still work; the bot assemblies must not contain the adapter.
- A real smoke test (connect, receive a message, reply) needs the real credentials: if you cannot run it, say it clearly.

## Known gaps to keep in mind

- `ReplyTransformer.usableSources` (drop the `Left` file-id sources where `acceptsFileIds` is false) is not applied by the
  repository yet: apply it where the sources of a media are chosen if the platform must not see them.
- A platform without buttons shows only the requested page of a `PagedList`; there is no page argument.
- The help texts of the commands write `/command`, whatever the syntax.
- `Message.messageId` is an `Int`: a platform with string message ids (Slack) needs that decided first.
