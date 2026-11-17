#!/usr/bin/env -S scala-cli run
//> using scala "3"
//> using dep "io.circe::circe-core:0.14.17"
//> using dep "io.circe::circe-parser:0.14.17"

/** One-time data migration: tag every file reference in the bot list JSONs with the platform it belongs to.
  *
  * Only Telegram has written references so far, so untagged non-url sources become `telegram:<id>`. URLs and already
  * tagged references are left alone. The generated DB is rebuilt from these files on the next deploy, which carries
  * the migration to production without a separate DB script.
  *
  * Usage:
  *   scala-cli scripts/MigrateFileIdSources.scala -- --check   # report what would change (default)
  *   scala-cli scripts/MigrateFileIdSources.scala -- --write    # rewrite the files
  */

import io.circe.{Json, Printer}
import io.circe.parser.parse

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*

object MigrateFileIdSources:

  private val platformIds = Set("telegram", "discord", "irc", "slack")
  private val printer     = Printer.spaces2

  private def migrateSource(source: String): String =
    if source.startsWith("http://") || source.startsWith("https://") then source
    else
      val tag = source.takeWhile(_ != ':')
      if platformIds.contains(tag) then source
      else s"telegram:$source"

  private def migrate(json: Json): Json =
    json.mapArray(
      _.map { entry =>
        entry.asObject match {
          case Some(obj) if obj("sources").isDefined =>
            val migrated = obj("sources").get.mapArray(
              _.map(source => source.asString.fold(source)(s => Json.fromString(migrateSource(s))))
            )
            val fields = obj.toList.foldLeft(io.circe.JsonObject.empty) { case (acc, (key, value)) =>
              acc.add(key, if key == "sources" then migrated else value)
            }
            Json.fromJsonObject(fields)
          case _ => entry
        }
      }
    )

  private def listFiles: List[Path] =
    Files
      .walk(Paths.get("modules", "bots"))
      .iterator()
      .asScala
      .filter(p => Files.isRegularFile(p) && p.getFileName.toString.endsWith("_list.json"))
      .toList
      .sorted

  @main def main(mode: String = "--check"): Unit =
    if mode != "--check" && mode != "--write" then
      Console.err.println(s"Unknown mode '$mode': use --check or --write")
      sys.exit(2)

    val changed = listFiles.flatMap { path =>
      val content  = Files.readString(path)
      val original = parse(content).fold(e => sys.error(s"Invalid JSON in $path: ${e.getMessage}"), identity)
      val migrated = migrate(original)
      val rendered = printer.print(migrated) + (if content.endsWith("\n") then "\n" else "")
      Option.when(migrated != original && rendered != content)((path, rendered))
    }

    changed.foreach { case (path, _) => println(s"${if mode == "--write" then "MIGRATED" else "NEEDS MIGRATION"}: $path") }
    if mode == "--write" then changed.foreach { case (path, content) => Files.writeString(path, content) }
    println(s"${changed.size} file(s) ${if mode == "--write" then "migrated" else "need migration"}")
    if mode == "--check" && changed.nonEmpty then sys.exit(1)
