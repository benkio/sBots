# Bot template

This folder is the scaffold for a new bot. Do not use it as a project directly.

**To create a new bot from this template:**

1. Copy this entire `_template` folder to a new folder under `modules/bots/` with your bot's name (e.g. `MyNewBot`).
2. Rename all occurrences of `VittorioSgarbiBot` → your bot name and `sgar` → your bot's short id (e.g. `mynew`) in:
   - directory name: `com/benkio/VittorioSgarbiBot/` → `com/benkio/MyNewBot/`
   - file names: `VittorioSgarbiBot.scala`, `VittorioSgarbiBotMainPolling.scala`, `VittorioSgarbiBotMainDataEntry.scala` → `MyNewBot.scala`, `MyNewBotMainPolling.scala`, `MyNewBotMainDataEntry.scala`
   - file names: `sgar_*.json`, `sgar_*.txt` → `mynew_*.json`, `mynew_*.txt`
   - file contents: replace `VittorioSgarbiBot` and `sgar` in every Scala and config file.
3. Follow the steps in [docs/adding-a-bot.md](../../../docs/adding-a-bot.md) to register the bot in the build and BotsRegistry.

Alternatively, use the sbt task: `sbt "newBot MyNewBot mynew"` (see docs).
