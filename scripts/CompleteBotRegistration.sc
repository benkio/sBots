#!/usr/bin/env -S scala-cli shebang
//> using scala "3"
//> using option "-no-indent"

/** Completes bot registration after copying from template (or running sbt newBot). Call from shell (from project root):
  * ./scripts/CompleteBotRegistration.sc <BotName> <id> [projectRoot] Example: ./scripts/CompleteBotRegistration.sc
  * MyNewBot mynew
  *
  * Edits:
  *   - build.sbt: defines the project, adds it to botProjects, adds data-entry alias (e.g. mynewAddData)
  *   - modules/main/.../BotsRegistry.scala: adds import and BotRegistryEntry (no commandEffectfulCallback)
  *   - modules/main/src/main/resources/application.conf: adds the new bot db block (main.<id>.db)
  *   - .github/workflows/healthcheck.yml: adds the new bot token to BOT_TOKENS
  *   - scripts/copyTokensFromDropbox.sh: adds the new bot to the list
  * (newBot does not edit main application.conf or healthcheck; only this script does.)
  */

if args.length < 2 then {
  println("Usage: ./scripts/CompleteBotRegistration.sc <BotName> <id> [projectRoot]")
  println("Example: ./scripts/CompleteBotRegistration.sc MyNewBot mynew")
  sys.exit(1)
}

val botName      = args(0)
val id           = args(1)
val root         = if args.length >= 3 then new java.io.File(args(2)) else new java.io.File(".")
val rootAbs      = root.getAbsoluteFile
val projectDir   = new java.io.File(rootAbs, "project")
val buildSbt     = new java.io.File(rootAbs, "build.sbt")
val botsRegistry = new java.io.File(rootAbs, "modules/main/src/main/scala/com/benkio/main/BotsRegistry.scala")

if !projectDir.isDirectory || !buildSbt.isFile || !botsRegistry.isFile then {
  println(
    s"Error: run from sBots project root, or pass project root as third argument. Looked at: ${rootAbs.getAbsolutePath}"
  )
  sys.exit(1)
}

def read(path: java.io.File): String =
  scala.io.Source.fromFile(path, "UTF-8").mkString

def write(path: java.io.File, content: String): Unit =
  java.nio.file.Files.write(path.toPath, content.getBytes(java.nio.charset.StandardCharsets.UTF_8))

// 1. build.sbt: add to botProjects seq and add project definition
val buildContent = read(buildSbt)
if buildContent.contains(s"lazy val $botName =") then {
  println(s"Project $botName already defined in build.sbt, skipping.")
} else {
  var newBuild = buildContent

  // Add to botProjects seq (last entry before closing )
  val seqPat     = """  ([A-Za-z0-9]+)\n\)""".r
  val seqMatches = seqPat.findAllMatchIn(newBuild).toList
  val lastInSeq  = seqMatches.lastOption
  if lastInSeq.isEmpty || newBuild.contains(s"  $botName\n)") then {
    // already added or can't find
  } else {
    val lastVal = lastInSeq.get.group(1)
    newBuild = newBuild.replace(s"  $lastVal\n)", s"  $lastVal,\n  $botName\n)")
  }

  // Add project definition: after last "lazy val X = Project(...bots/X...)" block, before "lazy val main"
  val projectBlock =
    s"""
lazy val $botName =
Project("$botName", file("modules/bots/$botName"))
  .settings(Settings.settings *)
  .settings(Settings.botProjectSettings("$botName") *)
  .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")
"""
  val mainAnchor = "\nlazy val main = project"
  val idx        = newBuild.indexOf(mainAnchor)
  if idx == -1 then {
    println("Could not find 'lazy val main' in build.sbt")
    sys.exit(1)
  }
  newBuild = newBuild.patch(idx, projectBlock + mainAnchor, mainAnchor.length)
  write(buildSbt, newBuild)
  println(s"Updated ${buildSbt.getPath}")
}

// 2. BotsRegistry.scala: add import and BotRegistryEntry
val registryContent = read(botsRegistry)
val pkg             = s"com.benkio.$botName"
if registryContent.contains(s"$pkg.$botName") then {
  println(s"$botName already in BotsRegistry.scala, skipping.")
} else {
  var newRegistry = registryContent

  // Add import (after last bot import)
  val importLine = s"import $pkg.$botName"
  if !newRegistry.contains(importLine) then {
    newRegistry = newRegistry.replace(
      "import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\nimport log.effect",
      s"import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\nimport $pkg.$botName\nimport log.effect"
    )
  }

  // Add BotRegistryEntry at end of list (simple entry, no commandEffectfulCallback)
  val entryLine = s"BotRegistryEntry[IO](sBotInfo = $botName.sBotInfo)"
  if !newRegistry.contains(s"sBotInfo = $botName.sBotInfo") then {
    // Insert before List closing: "      )\n    )\n  )" (last entry + List + BotRegistry)
    val listClosing = "      )\n    )\n  )"
    newRegistry = newRegistry.replace(
      listClosing,
      s"      ),\n      $entryLine\n    )\n  )"
    )
    if newRegistry == registryContent then {
      println("Could not find insertion point in BotsRegistry.scala")
      sys.exit(1)
    }
  }
  write(botsRegistry, newRegistry)
  println(s"Updated ${botsRegistry.getPath}")
}

// 3. main application.conf: add new bot db block
val mainAppConf = new java.io.File(rootAbs, "modules/main/src/main/resources/application.conf")
if mainAppConf.isFile then
  var mainConfContent = read(mainAppConf)
  if mainConfContent.contains(s"main.$id.") then
    println(s"main application.conf already contains $id, skipping.")
  else
    val idUpper     = id.toUpperCase
    val dbNameLine  = s"      db-name = $$${?${idUpper}_DB_NAME}"
    val urlLine     = s"""      url = "jdbc:sqlite:"$${main.$id.db.db-name}"""
    val urlEnvLine  = s"      url = $$${?${idUpper}_DB_CONNECTION_URL}"
    val newBlock =
      s"""  $id {
        |    db = {

        |      driver = "org.sqlite.JDBC"

        |      db-name = "../botDB.sqlite3"
        |      $dbNameLine

        |      $urlLine
        |      $urlEnvLine
        |    }
        |  }

        |""".stripMargin
    val mosBlock =
      """  mos {
        |    db = {

        |      driver = "org.sqlite.JDBC"

        |      db-name = "../botDB.sqlite3"
        |      db-name = ${?MOS_DB_NAME}

        |      url = "jdbc:sqlite:"${main.mos.db.db-name}
        |      url = ${?MOS_DB_CONNECTION_URL}
        |    }
        |  }

        |}""".stripMargin
    val mosStripped = mosBlock.stripMargin
    val replacement = mosStripped.dropRight(1) + newBlock + "}"
    mainConfContent = mainConfContent.replace(mosStripped, replacement)
    if mainConfContent != read(mainAppConf) then
      write(mainAppConf, mainConfContent)
      println(s"Updated ${mainAppConf.getPath} with $botName ($id)")
    else println("Could not find insertion point in main application.conf")
else println(s"main application.conf not found at ${mainAppConf.getPath}, skipping.")

// 4. Healthcheck workflow: add new bot token to BOT_TOKENS
val healthcheckYml = new java.io.File(rootAbs, ".github/workflows/healthcheck.yml")
if healthcheckYml.isFile then
  val secretName = s"${id.toUpperCase}_TOKEN"
  if secretName != "TOKEN" then
    var hcContent = read(healthcheckYml)
    if !hcContent.contains(secretName) then
      hcContent = hcContent.replace(
        " ${{ secrets.YTAI_TOKEN }}",
        s" $${{ secrets.YTAI_TOKEN }} $${{ secrets.$secretName }}"
      )
      write(healthcheckYml, hcContent)
      println(s"Updated .github/workflows/healthcheck.yml with $secretName")
    else println(s"Healthcheck already contains $secretName, skipping.")
  else ()
else println(s"Healthcheck workflow not found at ${healthcheckYml.getPath}, skipping.")

// 5. copyTokensFromDropbox.sh: add new bot to the list
val copyTokensScript = new java.io.File(rootAbs, "scripts/copyTokensFromDropbox.sh")
if copyTokensScript.isFile then
  var ctContent = read(copyTokensScript)
  if !ctContent.contains(botName) then
    ctContent = ctContent.replace("; do", s" $botName; do")
    if ctContent != read(copyTokensScript) then
      write(copyTokensScript, ctContent)
      println(s"Updated scripts/copyTokensFromDropbox.sh with $botName")
    else println("Could not find insertion point in copyTokensFromDropbox.sh")
  else println(s"copyTokensFromDropbox.sh already contains $botName, skipping.")
else println(s"copyTokensFromDropbox.sh not found at ${copyTokensScript.getPath}, skipping.")

// 6. build.sbt: add data-entry alias
val buildContentNow = read(buildSbt)
val aliasName       = s"${id}AddData"
val aliasLine       = s"""addCommandAlias("$aliasName", "$botName/runMain $pkg.${botName}MainDataEntry")"""
if !buildContentNow.contains(aliasName) then {
  val newBuildWithAlias = buildContentNow.replace(
    "\n\n// PROJECTS",
    s"\n$aliasLine\n\n// PROJECTS"
  )
  if newBuildWithAlias != buildContentNow then {
    write(buildSbt, newBuildWithAlias)
    println(s"Added data-entry alias: $aliasName")
  }
}

println(
  """Done. Remember to:
  | - Update the README.md file
  | - Delete the DB at the root of the project
  | - insert the Youtube Secret key in BotDB resources
  | - Run the `botSetup` with `run-show-caption-fetching` and `run-show-caption-fetching` to true to align the db""".stripMargin
)
