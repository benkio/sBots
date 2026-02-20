# Adding a new bot

This guide walks you through adding a new bot to the sBots project. After the improvements (single list, shared settings, template), you only need to touch a few places.

## Prerequisites

- Choose a **bot name** (e.g. `MyNewBot`) and a **short id** (e.g. `mynew`). The short id is used in `SBotId`, file names (`mynew_replies.json`), and config. Use lowercase.

## Step 1: Create the bot module from the template

**Option A – Copy manually**

1. Copy the folder `modules/bots/_template` to `modules/bots/YourBotName` (e.g. `modules/bots/MyNewBot`).
2. Rename and replace:
   - Directory: `src/main/scala/com/benkio/TemplateBot/` → `src/main/scala/com/benkio/MyNewBot/`
   - Files: `TemplateBot.scala` → `MyNewBot.scala`, `TemplateBotMainPolling.scala` → `MyNewBotMainPolling.scala`
   - Files: `tpl_replies.json` → `mynew_replies.json`, `tpl_commands.json` → `mynew_commands.json`, `tpl_list.json` → `mynew_list.json`, `tpl_shows.json` → `mynew_shows.json`, `tpl_triggers.txt` → `mynew_triggers.txt`
   - In **all** Scala and JSON/txt files: replace `TemplateBot` with `MyNewBot` and `tpl` with `mynew`.

**Option B – Use the sbt task**

From the project root:

```bash
sbt "newBot MyNewBot mynew"
```

This copies the template to `modules/bots/MyNewBot` and substitutes the name and id.

## Step 2: Register the bot in the build

**Option A – Automated (recommended)**

From the project root, run the script (requires [scala-cli](https://scala-cli.virtuslab.org/) for the shebang). If needed, make it executable first: `chmod +x scripts/CompleteBotRegistration.sc`

```bash
./scripts/CompleteBotRegistration.sc MyNewBot mynew
```

This updates `build.sbt` and `modules/main/.../BotsRegistry.scala` (defines the project, adds it to `botProjects`, adds the registry entry with no custom callbacks). If your bot needs `commandEffectfulCallback` (like RichardPHJBensonBot), add it manually in BotsRegistry after running the script.

**Option B – Manual**

1. **Define the project and add it to `botProjects`**
   In `build.sbt`:
   - Add a lazy val for your bot (same pattern as the others):
     ```scala
     lazy val MyNewBot =
       Project("MyNewBot", file("modules/bots/MyNewBot"))
         .settings(Settings.settings *)
         .settings(Settings.botProjectSettings("MyNewBot")*)
         .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")
     ```
   - Add it to the `botProjects` seq:
     ```scala
     lazy val botProjects = Seq(
       ...
       MyNewBot
     )
     ```

2. **Register in BotsRegistry**
   In `modules/main/src/main/scala/com/benkio/main/BotsRegistry.scala`:

   - Add the import:
   ```scala
   import com.benkio.MyNewBot.MyNewBot
   ```
   - Add an entry to the registry list:
   ```scala
   BotRegistryEntry[IO](sBotInfo = MyNewBot.sBotInfo),
   ```
   If your bot has custom command callbacks (like RichardPHJBensonBot), use:
   ```scala
   BotRegistryEntry[IO](
     sBotInfo = MyNewBot.sBotInfo,
     commandEffectfulCallback = MyNewBot.commandEffectfulCallback[IO]
   )
   ```

Aggregate and `main.dependsOn` will then include your bot automatically.

## Step 3: Verify

- From the project root: `sbt compile`
- Run the bot (e.g. `sbt "MyNewBot/run"`) once you have added a token and any needed resources.

## Step 4: Update README.md

Update the `README.md` file with the new bot entry in the table.

## Optional: Data entry alias

The template includes a main class for data entry (`TemplateBotMainDataEntry` → `MyNewBotMainDataEntry`). To run it from sbt, add a command alias in `build.sbt` (see existing `abarAddData`, `xahAddData`, etc.):

```scala
addCommandAlias("mynewAddData", "MyNewBot/runMain com.benkio.MyNewBot.MyNewBotMainDataEntry")
```

(Use a short alias name derived from your bot id, e.g. `mynewAddData` for id `mynew`.)

## Triggers

- **Triggers file**: The template includes an empty `{id}_triggers.txt`. The task `generateTriggerTxt` (run via `sbt generateTriggerTxt`) regenerates trigger files from the JSON data for all bots in the registry.

## Summary

| Step | What to do |
|------|------------|
| 1. Create module | Copy `_template` to `modules/bots/YourBotName` and replace TemplateBot → YourBotName, tpl → yourid (or run `sbt newBot YourBotName yourid`) |
| 2. Register in build & registry | Run `./scripts/CompleteBotRegistration.sc YourBotName yourid` **or** manually edit build.sbt and BotsRegistry.scala |
| 3. Verify | `sbt compile` and optionally run the bot |
| 4. Docs | Update the README with the new bot |

No changes are needed in `project/Settings.scala` or `project/Dependencies.scala`; the shared `botProjectSettings` and `BotDependencies` apply to every bot.
