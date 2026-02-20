#!/usr/bin/env -S scala-cli shebang
//> using scala "3"
//> using option "-no-indent"

/** Completes bot registration after copying from template (or running sbt newBot).
  * Call from shell (from project root): ./scripts/CompleteBotRegistration.sc <BotName> <id> [projectRoot]
  * Example: ./scripts/CompleteBotRegistration.sc MyNewBot mynew
  *
  * Edits:
  * - build.sbt: defines the project and adds it to botProjects
  * - modules/main/.../BotsRegistry.scala: adds import and BotRegistryEntry (no commandEffectfulCallback)
  */

if (args.length < 2) {
  println("Usage: ./scripts/CompleteBotRegistration.sc <BotName> <id> [projectRoot]")
  println("Example: ./scripts/CompleteBotRegistration.sc MyNewBot mynew")
  sys.exit(1)
}

val botName = args(0)
val id      = args(1)
val root    = if (args.length >= 3) new java.io.File(args(2)) else new java.io.File(".")
val rootAbs = root.getAbsoluteFile
val projectDir   = new java.io.File(rootAbs, "project")
val buildSbt     = new java.io.File(rootAbs, "build.sbt")
val botsRegistry = new java.io.File(rootAbs, "modules/main/src/main/scala/com/benkio/main/BotsRegistry.scala")

if (!projectDir.isDirectory || !buildSbt.isFile || !botsRegistry.isFile) {
  println(s"Error: run from sBots project root, or pass project root as third argument. Looked at: ${rootAbs.getAbsolutePath}")
  sys.exit(1)
}

def read(path: java.io.File): String =
  scala.io.Source.fromFile(path, "UTF-8").mkString

def write(path: java.io.File, content: String): Unit =
  java.nio.file.Files.write(path.toPath, content.getBytes(java.nio.charset.StandardCharsets.UTF_8))

// 1. build.sbt: add to botProjects seq and add project definition
val buildContent = read(buildSbt)
if (buildContent.contains(s"lazy val $botName =")) {
  println(s"Project $botName already defined in build.sbt, skipping.")
} else {
  var newBuild = buildContent

  // Add to botProjects seq (last entry before closing )
  val seqPat = """  ([A-Za-z0-9]+)\n\)""".r
  val seqMatches = seqPat.findAllMatchIn(newBuild).toList
  val lastInSeq = seqMatches.lastOption
  if (lastInSeq.isEmpty || newBuild.contains(s"  $botName\n)")) {
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
  val idx = newBuild.indexOf(mainAnchor)
  if (idx == -1) {
    println("Could not find 'lazy val main' in build.sbt")
    sys.exit(1)
  }
  newBuild = newBuild.patch(idx, projectBlock + mainAnchor, mainAnchor.length)
  write(buildSbt, newBuild)
  println(s"Updated ${buildSbt.getPath}")
}

// 2. BotsRegistry.scala: add import and BotRegistryEntry
val registryContent = read(botsRegistry)
val pkg = s"com.benkio.$botName"
if (registryContent.contains(s"$pkg.$botName")) {
  println(s"$botName already in BotsRegistry.scala, skipping.")
} else {
  var newRegistry = registryContent

  // Add import (after last bot import)
  val importLine = s"import $pkg.$botName"
  if (!newRegistry.contains(importLine)) {
    newRegistry = newRegistry.replace(
      "import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\nimport log.effect",
      s"import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot\nimport $pkg.$botName\nimport log.effect"
    )
  }

  // Add BotRegistryEntry at end of list (simple entry, no commandEffectfulCallback)
  val entryLine = s"BotRegistryEntry[IO](sBotInfo = $botName.sBotInfo)"
  if (!newRegistry.contains(s"sBotInfo = $botName.sBotInfo")) {
    // Insert before List closing: "      )\n    )\n  )" (last entry + List + BotRegistry)
    val listClosing = "      )\n    )\n  )"
    newRegistry = newRegistry.replace(
      listClosing,
      s"      ),\n      $entryLine\n    )\n  )"
    )
    if (newRegistry == registryContent) {
      println("Could not find insertion point in BotsRegistry.scala")
      sys.exit(1)
    }
  }
  write(botsRegistry, newRegistry)
  println(s"Updated ${botsRegistry.getPath}")
}

println(s"Done. Optional: add data-entry alias in build.sbt: addCommandAlias(\"${id}AddData\", \"$botName/runMain $pkg.${botName}MainDataEntry\")")
