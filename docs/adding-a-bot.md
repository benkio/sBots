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

This:

- Copies the template to `modules/bots/MyNewBot` and substitutes the name and id.
- **Updates `modules/botDB/src/main/resources/application.conf`**: adds the new bot to `json-location` and to `show-config.show-sources` with **empty sources** (`youtube-sources = []`, `caption-language = "it"`, `output-file-path = "../bots/<BotName>/<id>_shows.json"`). You can edit the config later to add YouTube sources or change the caption language.
- **Updates `modules/botDB/src/main/resources/db/migrations/V1__CreateBotTable.sql`**: adds an `INSERT` for the new bot (`id`, `bot_name`, `bot_full_name`). The full name is set to the bot name by default; you can edit the SQL to set a human-readable name later.
- Updates `.github/workflows/deploy.yml` so the deploy workflow injects the bot token (see Step 3 for adding the secret).

## Step 2: Register the bot in the build

**Option A – Automated (recommended)**

From the project root, run the script (requires [scala-cli](https://scala-cli.virtuslab.org/) for the shebang). If needed, make it executable first: `chmod +x scripts/CompleteBotRegistration.sc`

```bash
./scripts/CompleteBotRegistration.sc MyNewBot mynew
```

This updates `build.sbt` and `modules/main/.../BotsRegistry.scala`: defines the project, adds it to `botProjects`, adds the data-entry alias (e.g. `mynewAddData`), and adds the registry entry (no custom callbacks). If your bot needs `commandEffectfulCallback` (like RichardPHJBensonBot), add it manually in BotsRegistry after running the script.

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

## Manual steps after registration

After Step 2 (and before or alongside the steps below), do the following as needed:

- **Custom command callbacks**: If your bot needs `commandEffectfulCallback` (e.g. like RichardPHJBensonBot), edit `BotsRegistry.scala` and add it to the bot’s `BotRegistryEntry` (see Option B in Step 2).
- **Bot full name**: To set a human-readable name in the DB, edit `modules/botDB/src/main/resources/db/migrations/V1__CreateBotTable.sql` and change the `bot_full_name` value for your bot’s `INSERT`.
- **CI token**: Add the bot token as a repository secret so deploy works (Step 3).
- **Shows / YouTube sources**: To enable show fetching, edit `modules/botDB/src/main/resources/application.conf` and add YouTube sources (or other settings) to your bot’s entry in `show-config.show-sources`.

## Step 3: Add the bot token to GitHub Actions secrets (for deploy)

The `newBot` task updates `.github/workflows/deploy.yml` so the deploy workflow injects your bot's token during assembly. You must add the token as a repository secret so the workflow can use it:

1. Get the bot token from [@BotFather](https://t.me/BotFather) (create a bot or use /token for an existing one).
2. Add the default commands to the new bot. from the `commands.txt` file in the root of the new bot folder.
3. In your GitHub repository: **Settings** → **Secrets and variables** → **Actions**.
4. Click **New repository secret**.
5. **Name:** `<ID>_TOKEN` in **UPPERCASE** (e.g. for id `mynew` use `MYNEW_TOKEN`).
6. **Value:** paste the token from BotFather.

After that, the deploy workflow will be able to write the token into the bot's resources when running in CI.

## Step 4: Verify

- From the project root: `sbt compile`
- Run the bot (e.g. `sbt "MyNewBot/run"`) once you have added a token and any needed resources.

## Step 5: Update README

Update the `README.org` file with the new bot entry in the table.

## Data entry alias

The template includes a main class for data entry (`TemplateBotMainDataEntry` → `MyNewBotMainDataEntry`). The registration script adds the command alias automatically (e.g. `mynewAddData` for id `mynew`). If you register manually, add in `build.sbt`:

```scala
addCommandAlias("mynewAddData", "MyNewBot/runMain com.benkio.MyNewBot.MyNewBotMainDataEntry")
```

## Triggers

- **Triggers file**: The template includes an empty `{id}_triggers.txt`. The task `generateTriggerTxt` (run via `sbt generateTriggerTxt`) regenerates trigger files from the JSON data for all bots in the registry.

## Summary

| Step | What to do |
|------|------------|
| 1. Create module | Copy `_template` to `modules/bots/YourBotName` and replace TemplateBot → YourBotName, tpl → yourid (or run `sbt newBot YourBotName yourid`). The task also updates **botDB** `application.conf` (json-location + show-sources with empty sources), **V1__CreateBotTable.sql** (INSERT for the new bot), and **deploy.yml**. |
| 2. Register in build & registry | Run `./scripts/CompleteBotRegistration.sc YourBotName yourid` **or** manually edit build.sbt and BotsRegistry.scala |
| 2b. Manual steps after registration | As needed: add `commandEffectfulCallback` in BotsRegistry, set `bot_full_name` in V1__CreateBotTable.sql, add CI secret (Step 3), add YouTube sources in application.conf |
| 3. CI secret | In the repo: **Settings** → **Secrets and variables** → **Actions** → New repository secret: name `YOURID_TOKEN` (uppercase), value = token from BotFather |
| 4. Verify | `sbt compile` and optionally run the bot |
| 5. Docs | Update the README with the new bot |

No changes are needed in `project/Settings.scala` or `project/Dependencies.scala`; the shared `botProjectSettings` and `BotDependencies` apply to every bot. If you add the bot manually (Option A in Step 1), remember to add it to `modules/botDB/src/main/resources/application.conf` (`json-location` and `show-config.show-sources` with empty `youtube-sources`) and to `modules/botDB/src/main/resources/db/migrations/V1__CreateBotTable.sql` (e.g. `INSERT INTO bot (id, bot_name, bot_full_name) VALUES ('yourid', 'YourBotName', 'Your Bot Full Name');`).
