#!/usr/bin/env -S scala-cli run
//> using scala "3"
//> using dep "io.circe::circe-core:0.14.17"
//> using dep "io.circe::circe-parser:0.14.17"
//> using dep "org.xerial:sqlite-jdbc:3.41.2.1"
//> using dep "org.scalameta::munit:1.3.1"

/** Export the file references accumulated in a downloaded production DB back into the per-bot list JSONs.
  *
  * Only references are merged: URLs and every non-source field of the list entries stay as they are. A reference is
  * copied only into the entry with the same filename of the same bot, keeping its platform tag, so a Telegram ID never
  * moves to another bot or platform. References are deduped per platform, capped at the same three newest IDs the DB
  * keeps, and written before the URLs. Untagged references, unknown bot IDs and malformed JSON are rejected.
  *
  * Run from the project root:
  *   scala-cli scripts/ExportFileIds.scala -- --check            # report the files that would change (default)
  *   scala-cli scripts/ExportFileIds.scala -- --write [db path]  # rewrite them (default DB: ./botDB.sqlite3)
  *
  * Tests: scala-cli test scripts/ExportFileIds.scala
  */

import io.circe.{Json, JsonObject, Printer}
import io.circe.parser.parse

import java.nio.file.{Files, Path, Paths}
import java.sql.{DriverManager, ResultSet}
import scala.jdk.CollectionConverters.*

final class ExportFileIdsError(message: String) extends RuntimeException(message)

object ExportFileIds:
  private val printer               = Printer.spaces2
  private val maxFileIdsPerPlatform = 3
  private val platforms             = Set("telegram", "discord", "irc", "slack")
  private val defaultDb             = Paths.get("botDB.sqlite3")
  private val defaultBotsRoot       = Paths.get("modules", "bots")

  private def fail(message: String): Nothing = throw ExportFileIdsError(message)

  private def tagOf(reference: String): Option[String] = {
    val index = reference.indexOf(':')
    Option.when(index > 0)(reference.take(index)).filter(platforms.contains)
  }

  private def isUrl(reference: String): Boolean =
    reference.startsWith("http://") || reference.startsWith("https://")

  private def validateReference(reference: String, context: String): Unit =
    if !isUrl(reference) && tagOf(reference).isEmpty then
      fail(s"untagged reference '$reference' in $context")

  private def stringsOf(values: Vector[Json], context: String): List[String] =
    values.toList.map(value => value.asString.fold(fail(s"non-string entry in $context"))(identity))

  /** `media_sources` is a JSON-encoded JSON array of strings. Both that form and a plain array are accepted so a DB
    * produced by a different tool can still be exported.
    */
  private def decodeDbSources(column: String, context: String): List[String] = {
    val outer = parse(column).fold(e => fail(s"invalid media_sources for $context: ${e.getMessage}"), identity)
    val values = outer.asArray match {
      case Some(array) => array
      case None =>
        val inner = outer.asString.fold(fail(s"invalid media_sources for $context: expected an array or a string"))(identity)
        parse(inner).fold(
          e => fail(s"invalid media_sources for $context: ${e.getMessage}"),
          _.asArray.fold(fail(s"invalid media_sources for $context: expected an array"))(identity)
        )
    }
    val references = stringsOf(values, context)
    references.foreach(validateReference(_, context))
    references
  }

  private def query(dbPath: Path, sql: String)(read: ResultSet => Unit): Unit = {
    Class.forName("org.sqlite.JDBC")
    val connection = DriverManager.getConnection(s"jdbc:sqlite:$dbPath")
    try {
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery(sql)
      try while resultSet.next() do read(resultSet)
      finally resultSet.close()
      statement.close()
    } finally connection.close()
  }

  final case class MediaRow(filename: String, botId: String, sources: List[String])

  private def readBotIds(dbPath: Path): Set[String] = {
    val builder = Set.newBuilder[String]
    query(dbPath, "SELECT id FROM bot")(resultSet => builder += resultSet.getString("id"))
    builder.result()
  }

  private def readMedia(dbPath: Path): List[MediaRow] = {
    val builder = List.newBuilder[MediaRow]
    query(dbPath, "SELECT media_name, bot_id, media_sources FROM media") { resultSet =>
      val filename = resultSet.getString("media_name")
      builder += MediaRow(
        filename = filename,
        botId = resultSet.getString("bot_id"),
        sources = decodeDbSources(resultSet.getString("media_sources"), s"media $filename")
      )
    }
    builder.result()
  }

  private def listFiles(root: Path): List[Path] = {
    val stream = Files.walk(root)
    try
      stream.iterator().asScala
        .filter(path =>
          Files.isRegularFile(path)
            && path.getFileName.toString.endsWith("_list.json")
            && !path.getParent.getFileName.toString.startsWith("_") // the _template bot is not a real bot
        )
        .toList
        .sorted
    finally stream.close()
  }

  private def botIdOf(path: Path): String = path.getFileName.toString.stripSuffix("_list.json")

  /** DB references first, then the list's own references, deduped with at most [[maxFileIdsPerPlatform]] per platform;
    * the list's URLs follow, in their original order.
    */
  private def mergeSources(existing: List[String], fromDb: List[String]): List[String] = {
    val references = (fromDb.filterNot(isUrl) ++ existing.filterNot(isUrl)).distinct
    val keptPerPlatform = scala.collection.mutable.Map.empty[String, Int].withDefaultValue(0)
    val merged = references.filter { reference =>
      val platform = tagOf(reference).get // validated before merging
      if keptPerPlatform(platform) < maxFileIdsPerPlatform then
        keptPerPlatform.update(platform, keptPerPlatform(platform) + 1)
        true
      else false
    }
    merged ++ existing.filter(isUrl)
  }

  private def mergeEntry(entry: Json, dbSources: List[String], context: String): Json = {
    val obj        = entry.asObject.fold(fail(s"malformed entry in $context: expected an object"))(identity)
    val sourcesJson = obj("sources").fold(fail(s"malformed entry in $context: no sources"))(identity)
    val values = sourcesJson.asArray.fold(fail(s"malformed entry in $context: sources is not an array"))(identity)
    val existing = stringsOf(values, context)
    existing.foreach(validateReference(_, context))
    val merged = mergeSources(existing, dbSources)
    if merged == existing then entry
    else
      val fields = obj.toList.foldLeft(JsonObject.empty) { case (acc, (key, value)) =>
        acc.add(key, if key == "sources" then Json.arr(merged.map(Json.fromString)*) else value)
      }
      Json.fromJsonObject(fields)
  }

  private def mergeList(path: Path, rowsByName: Map[String, List[String]]): (Json, Boolean) = {
    val original = parse(Files.readString(path)).fold(e => fail(s"invalid JSON in $path: ${e.getMessage}"), identity)
    val entries = original.asArray.fold(fail(s"$path is not a JSON array"))(_.toVector)
    val merged = entries.map { entry =>
      val filename = entry.asObject
        .flatMap(_("filename"))
        .flatMap(_.asString)
        .fold(fail(s"entry without a filename in $path"))(identity)
      mergeEntry(entry, rowsByName.getOrElse(filename, Nil), s"$path/$filename")
    }
    val result = Json.arr(merged*)
    (result, result != original)
  }

  /** Returns the files that changed. Nothing is written unless `write` is true. */
  def exportFileIds(dbPath: Path, botsRoot: Path, write: Boolean): List[Path] = {
    val dbBotIds   = readBotIds(dbPath)
    val rows       = readMedia(dbPath)
    val files      = listFiles(botsRoot)
    val fileBotIds = files.map(botIdOf).toSet
    rows.map(_.botId).distinct.foreach(botId =>
      if !fileBotIds.contains(botId) then fail(s"unknown bot id in DB: $botId")
    )
    val sourcesByBot = rows.groupBy(_.botId).view.mapValues { mediaRows =>
      mediaRows.map(row => row.filename -> row.sources).toMap
    }.toMap

    files.flatMap { path =>
      val botId = botIdOf(path)
      if !dbBotIds.contains(botId) then fail(s"unknown bot id in list file: $path")
      val (merged, changed) = mergeList(path, sourcesByBot.getOrElse(botId, Map.empty))
      if changed && write then
        val content = Files.readString(path)
        Files.writeString(path, printer.print(merged) + (if content.endsWith("\n") then "\n" else ""))
      Option.when(changed)(path)
    }
  }

  @main def main(args: String*): Unit =
    val mode   = args.headOption.getOrElse("--check")
    val dbPath = args.lift(1).map(Paths.get(_)).getOrElse(defaultDb)
    try
      if mode != "--check" && mode != "--write" then fail(s"Unknown mode '$mode': use --check or --write")
      val changed = exportFileIds(dbPath, defaultBotsRoot, write = mode == "--write")
      changed.foreach(path => println(s"${if mode == "--write" then "MERGED" else "NEEDS MERGE"}: $path"))
      println(s"${changed.size} file(s) ${if mode == "--write" then "merged" else "need merging"}")
      if mode == "--check" && changed.nonEmpty then sys.exit(1)
    catch
      case error: ExportFileIdsError =>
        Console.err.println(s"[ExportFileIds] ${error.getMessage}")
        sys.exit(2)

class ExportFileIdsSpec extends munit.FunSuite {
  import ExportFileIds.*

  private def dbColumn(references: List[String]): String =
    Json.fromString(Json.arr(references.map(Json.fromString)*).noSpaces).noSpaces

  private def inTempDir[A](run: Path => A): A = {
    val root = Files.createTempDirectory("export-file-ids")
    try run(root)
    finally
      Files.walk(root).iterator().asScala.toList.reverse.foreach(Files.deleteIfExists(_))
      ()
  }

  private def createDb(dbPath: Path, rows: List[(String, (String, List[String]))]): Unit = {
    Class.forName("org.sqlite.JDBC")
    val connection = DriverManager.getConnection(s"jdbc:sqlite:$dbPath")
    try
      val statement = connection.createStatement()
      statement.executeUpdate("CREATE TABLE bot (id TEXT PRIMARY KEY, name TEXT)")
      statement.executeUpdate(
        "CREATE TABLE media (media_name TEXT PRIMARY KEY, bot_id TEXT, kinds TEXT, mime_type TEXT, " +
          "media_sources TEXT, media_count INTEGER, created_at TEXT)"
      )
      statement.executeUpdate("INSERT INTO bot VALUES ('rphjb', 'Richard')")
      rows.foreach { case (filename, (botId, sources)) =>
        statement.executeUpdate(
          s"INSERT INTO media VALUES ('$filename', '$botId', '[]', 'audio/mpeg', '${dbColumn(sources).replace("'", "''")}', 0, '0')"
        )
      }
      statement.close()
    finally connection.close()
  }

  private def writeList(root: Path, botId: String, content: String): Path = {
    val dir = root.resolve("bots").resolve(botId)
    Files.createDirectories(dir)
    val path = dir.resolve(s"${botId}_list.json")
    Files.writeString(path, content)
    path
  }

  private def sourcesOf(content: String): List[String] =
    parse(content).fold(e => sys.error(s"bad test JSON: ${e.getMessage}"), identity)
      .asArray.get.head.asObject.get("sources").get.asArray.get.toList.map(_.asString.get)

  test("references from two platforms in one DB row are merged, URLs and other fields preserved") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("rphjb", List("telegram:tg-1", "discord:dc-1", "https://db-url"))))
      val list = writeList(
        root,
        "rphjb",
        """[{"filename":"a.mp3","mime":"audio/mpeg","sources":["https://original"]},""" +
          """{"filename":"untouched.mp3","sources":["https://untouched"]}]"""
      )
      val before = Files.readString(list)
      val changed = exportFileIds(db, root.resolve("bots"), write = true)
      assertEquals(changed, List(list))

      val entries = parse(Files.readString(list)).fold(e => fail(e.getMessage), identity).asArray.get.toList
      assertEquals(sourcesOf(Files.readString(list)), List("telegram:tg-1", "discord:dc-1", "https://original"))
      assertEquals(entries.head.asObject.get("mime").get.asString.get, "audio/mpeg")
      assertEquals(
        entries(1),
        parse("""{"filename":"untouched.mp3","sources":["https://untouched"]}""").fold(e => sys.error(e.getMessage), identity)
      )
      assert(!before.contains("telegram:tg-1"))
    }
  }

  test("references are deduped and capped per platform, with the DB ones first") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("rphjb", List("telegram:one", "telegram:two", "telegram:three", "telegram:four"))))
      val list = writeList(root, "rphjb", """[{"filename":"a.mp3","sources":["telegram:two","https://url"]}]""")
      assertEquals(exportFileIds(db, root.resolve("bots"), write = true), List(list))
      assertEquals(
        sourcesOf(Files.readString(list)),
        List("telegram:one", "telegram:two", "telegram:three", "https://url")
      )
    }
  }

  test("check mode reports changes without writing anything") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("rphjb", List("telegram:fresh"))))
      val list = writeList(root, "rphjb", """[{"filename":"a.mp3","sources":["https://url"]}]""")
      val before = Files.readString(list)
      assertEquals(exportFileIds(db, root.resolve("bots"), write = false), List(list))
      assertEquals(Files.readString(list), before)
    }
  }

  test("a DB with no new references leaves the list file byte-identical") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("rphjb", List("telegram:same"))))
      val list = writeList(root, "rphjb", """[{"filename":"a.mp3","sources":["telegram:same","https://url"]}]""")
      val before = Files.readString(list)
      assertEquals(exportFileIds(db, root.resolve("bots"), write = true), Nil)
      assertEquals(Files.readString(list), before)
    }
  }

  test("untagged references are rejected") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("rphjb", List("legacy-reference"))))
      writeList(root, "rphjb", """[{"filename":"a.mp3","sources":["https://url"]}]""")
      intercept[ExportFileIdsError](exportFileIds(db, root.resolve("bots"), write = true))
    }
  }

  test("a DB reference for a bot without a list file is rejected") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, List("a.mp3" -> ("unknown", List("telegram:id"))))
      writeList(root, "rphjb", """[{"filename":"a.mp3","sources":["https://url"]}]""")
      intercept[ExportFileIdsError](exportFileIds(db, root.resolve("bots"), write = true))
    }
  }

  test("malformed JSON is rejected") {
    inTempDir { root =>
      val db = root.resolve("db.sqlite3")
      createDb(db, Nil)
      writeList(root, "rphjb", "not json")
      intercept[ExportFileIdsError](exportFileIds(db, root.resolve("bots"), write = true))
    }
  }
}
