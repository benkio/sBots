#!/usr/bin/env -S scala-cli shebang
//> using scala "3"
//> using toolkit typelevel:default
//> using dep "com.lihaoyi::os-lib:0.11.8"
//> using dep "com.lihaoyi::ujson:4.3.2"

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import scala.concurrent.duration.*
import scala.util.matching.Regex

/** Validate Mega links in bot *_list.json files against local bot folders under ~/Mega/sBots.
  *
  * Run from project root:
  *   ./scripts/CheckMegaLinks.scala
  *
  * Useful options:
  *   ./scripts/CheckMegaLinks.scala --bot RichardPHJBensonBot --report /tmp/rphjb-mega-report.tsv
  *   ./scripts/CheckMegaLinks.scala --bot RichardPHJBensonBot --fix-invalid
  */
object CheckMegaLinks:
  final case class Config(
    projectRoot: os.Path = os.pwd,
    megaRoot: os.Path = os.home / "Mega" / "sBots",
    botFilter: Option[Set[String]] = None,
    retries: Int = 3,
    maxPreview: Int = 20,
    reportPath: Option[os.Path] = None,
    fixInvalid: Boolean = false
  )

  final case class SourceRef(filename: String, source: String, fileId: String)
  final case class BotTarget(name: String, resourcesDir: os.Path, listPath: os.Path)
  final case class BotReport(
    botName: String,
    listEntries: Int,
    filesOnDisk: Int,
    uniqueMegaLinks: Int,
    reachable: Int,
    invalidByApi: Int,
    transportErrors: Int,
    malformedSources: Vector[(String, String)],
    nonMegaSources: Vector[(String, String)],
    missingInJson: Vector[String],
    missingOnDisk: Vector[String],
    invalidLinks: Vector[(String, String, String)]
  )

  sealed trait LinkCheckResult
  object LinkCheckResult:
    case object Reachable                                extends LinkCheckResult
    final case class InvalidApi(code: Int)              extends LinkCheckResult
    final case class TransportError(message: String)    extends LinkCheckResult
    final case class MalformedResponse(message: String) extends LinkCheckResult

  final case class FixResult(
    botName: String,
    attempted: Int,
    resolved: Int,
    replaced: Int,
    unresolved: Vector[(String, String, String)]
  )

  val usage: String =
    """Usage: ./scripts/CheckMegaLinks.scala [options]
      |
      |Options:
      |  --project-root <path>    sBots project root (default: current directory)
      |  --mega-root <path>       Mega bots root (default: ~/Mega/sBots)
      |  --bot <name1,name2>      Restrict to specific bot folder names
      |  --retries <n>            Retries for transport errors (default: 3)
      |  --max-preview <n>        Max rows printed per detailed section (default: 20)
      |  --report <path>          Write full invalid-link report (TSV)
      |  --fix-invalid            Resolve invalid api links via mega-export and update *_list.json
      |  --help                   Show this help
      |""".stripMargin

  val MegaFilePattern: Regex = raw"mega\.nz/file/([^#?/]+)".r
  val MegaUrlPattern: Regex  = raw"https://mega\.nz/file/[^\s]+".r

  def ignoreInReport(filename: String): Boolean =
    filename == ".DS_Store" ||
      filename.endsWith(".sticker") ||
      filename.endsWith(".token")

  def toPath(input: String, base: os.Path): os.Path =
    if input.startsWith("/") then os.Path(input, os.root)
    else os.Path(input, base)

  def parseArgs(args: Array[String]): Either[String, Config] =
    def loop(i: Int, cfg: Config): Either[String, Config] =
      if i >= args.length then Right(cfg)
      else
        args(i) match
          case "--help" => Left(usage)
          case "--project-root" =>
            if i + 1 >= args.length then Left("Missing value for --project-root")
            else loop(i + 2, cfg.copy(projectRoot = toPath(args(i + 1), os.pwd)))
          case "--mega-root" =>
            if i + 1 >= args.length then Left("Missing value for --mega-root")
            else loop(i + 2, cfg.copy(megaRoot = toPath(args(i + 1), os.pwd)))
          case "--bot" =>
            if i + 1 >= args.length then Left("Missing value for --bot")
            else
              val names = args(i + 1).split(",").map(_.trim).filter(_.nonEmpty).toSet
              loop(i + 2, cfg.copy(botFilter = Some(names)))
          case "--retries" =>
            if i + 1 >= args.length then Left("Missing value for --retries")
            else
              val n = args(i + 1).toIntOption.getOrElse(-1)
              if n < 1 then Left("--retries must be >= 1")
              else loop(i + 2, cfg.copy(retries = n))
          case "--max-preview" =>
            if i + 1 >= args.length then Left("Missing value for --max-preview")
            else
              val n = args(i + 1).toIntOption.getOrElse(-1)
              if n < 1 then Left("--max-preview must be >= 1")
              else loop(i + 2, cfg.copy(maxPreview = n))
          case "--report" =>
            if i + 1 >= args.length then Left("Missing value for --report")
            else loop(i + 2, cfg.copy(reportPath = Some(toPath(args(i + 1), os.pwd))))
          case "--fix-invalid" =>
            loop(i + 1, cfg.copy(fixInvalid = true))
          case unknown => Left(s"Unknown argument: $unknown")
    loop(0, Config())

  def discoverBots(cfg: Config): IO[Vector[BotTarget]] =
    IO.blocking {
      val modulesBotsDir = cfg.projectRoot / "modules" / "bots"
      if !os.isDir(cfg.megaRoot) then
        throw new IllegalArgumentException(s"Mega root not found: ${cfg.megaRoot}")
      if !os.isDir(modulesBotsDir) then
        throw new IllegalArgumentException(s"modules/bots directory not found under: ${cfg.projectRoot}")

      os.list(cfg.megaRoot)
        .filter(os.isDir(_))
        .toVector
        .sortBy(_.last)
        .flatMap { botDir =>
          val botName = botDir.last
          if cfg.botFilter.exists(filter => !filter.contains(botName)) then Vector.empty
          else
            val resourcesDir = botDir / "src" / "main" / "resources"
            val moduleDir    = modulesBotsDir / botName
            if !os.isDir(resourcesDir) || !os.isDir(moduleDir) then Vector.empty
            else
              val listFiles = os
                .list(moduleDir)
                .filter(p => os.isFile(p) && p.last.endsWith("_list.json"))
                .toVector
                .sortBy(_.last)
              listFiles.headOption.map(listPath => BotTarget(botName, resourcesDir, listPath)).toVector
        }
    }

  def checkMegaFileId(httpClient: HttpClient, fileId: String, retries: Int): IO[LinkCheckResult] =
    val payload = s"""[{"a":"g","p":"$fileId"}]"""
    val request = HttpRequest
      .newBuilder()
      .uri(URI.create("https://g.api.mega.co.nz/cs"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(payload))
      .build()

    def oneAttempt: IO[LinkCheckResult] =
      IO.blocking {
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        val body     = response.body()
        val parsed   = ujson.read(body)
        if !parsed.isInstanceOf[ujson.Arr] || parsed.arr.isEmpty then
          LinkCheckResult.MalformedResponse(s"Unexpected response: $body")
        else
          val first = parsed.arr.head
          if first.isInstanceOf[ujson.Obj] && first.obj.contains("s") then LinkCheckResult.Reachable
          else if first.isInstanceOf[ujson.Num] then LinkCheckResult.InvalidApi(first.num.toInt)
          else LinkCheckResult.MalformedResponse(s"Unexpected item: $first")
      }

    def loop(attempt: Int): IO[LinkCheckResult] =
      oneAttempt.handleErrorWith { e =>
        val msg = Option(e.getMessage).getOrElse(e.getClass.getSimpleName)
        if attempt >= retries then IO.pure(LinkCheckResult.TransportError(msg))
        else IO.sleep((attempt * 200L).min(1000L).millis) *> loop(attempt + 1)
      }

    loop(attempt = 1)

  def botReport(bot: BotTarget, cfg: Config, httpClient: HttpClient): IO[BotReport] =
    IO.blocking(ujson.read(os.read(bot.listPath, StandardCharsets.UTF_8))).flatMap { json =>
      val arr = json match
        case a: ujson.Arr => a.value.toVector
        case _            => throw new IllegalArgumentException(s"Expected JSON array in ${bot.listPath}")

      val listEntries = arr.size
      val listedFileNames = arr.flatMap { item =>
        item.obj.get("filename").flatMap {
          case ujson.Str(s) => Some(s)
          case _            => None
        }
      }.filterNot(ignoreInReport).toSet

      val filesOnDisk = os.list(bot.resourcesDir).filter(os.isFile(_)).map(_.last).filterNot(ignoreInReport).toSet
      val missingInJson = (filesOnDisk -- listedFileNames).toVector.sorted
      val missingOnDisk = (listedFileNames -- filesOnDisk).toVector.sorted

      val malformedSources = Vector.newBuilder[(String, String)]
      val nonMegaSources   = Vector.newBuilder[(String, String)]
      val megaSources      = Vector.newBuilder[SourceRef]

      arr.foreach { item =>
        val filename = item.obj.get("filename") match
          case Some(ujson.Str(s)) => s
          case _                  => "<missing filename>"

        if !ignoreInReport(filename) then
          item.obj.get("sources") match
            case Some(s: ujson.Arr) if s.value.nonEmpty =>
              s.value.foreach {
                case ujson.Str(value) =>
                  MegaFilePattern.findFirstMatchIn(value) match
                    case Some(m) => megaSources += SourceRef(filename, value, m.group(1))
                    case None    => nonMegaSources += ((filename, value))
                case other => malformedSources += ((filename, s"Non-string source: $other"))
              }
            case Some(_: ujson.Arr) => malformedSources += ((filename, "Empty sources array"))
            case _                  => malformedSources += ((filename, "Missing sources array"))
      }

      val uniqueMegaSources =
        megaSources.result().groupBy(_.source).values.map(_.head).toVector.sortBy(_.filename)

      uniqueMegaSources.traverse { ref =>
        checkMegaFileId(httpClient, ref.fileId, cfg.retries).map(result => (ref, result))
      }.map { checked =>
        var reachable    = 0
        var invalidByApi = 0
        var transport    = 0
        val invalidLinks = Vector.newBuilder[(String, String, String)]

        checked.foreach { case (ref, result) =>
          result match
            case LinkCheckResult.Reachable =>
              reachable += 1
            case LinkCheckResult.InvalidApi(code) =>
              invalidByApi += 1
              invalidLinks += ((ref.filename, ref.source, s"api_$code"))
            case LinkCheckResult.TransportError(message) =>
              transport += 1
              invalidLinks += ((ref.filename, ref.source, s"transport:$message"))
            case LinkCheckResult.MalformedResponse(message) =>
              transport += 1
              invalidLinks += ((ref.filename, ref.source, s"response:$message"))
        }

        BotReport(
          botName = bot.name,
          listEntries = listEntries,
          filesOnDisk = filesOnDisk.size,
          uniqueMegaLinks = uniqueMegaSources.size,
          reachable = reachable,
          invalidByApi = invalidByApi,
          transportErrors = transport,
          malformedSources = malformedSources.result(),
          nonMegaSources = nonMegaSources.result(),
          missingInJson = missingInJson,
          missingOnDisk = missingOnDisk,
          invalidLinks = invalidLinks.result()
        )
      }
    }

  def resolveMegaLinkForFile(bot: BotTarget, filename: String): IO[Either[String, String]] =
    IO.blocking {
      val remotePath = s"/sBots/${bot.name}/src/main/resources/$filename"
      val result = os.proc("mega-export", remotePath).call(check = false, mergeErrIntoOut = true)
      val output = result.out.text()
      MegaUrlPattern.findFirstIn(output) match
        case Some(url) => Right(url)
        case None      => Left(s"mega-export failed for $filename (exit=${result.exitCode})")
    }

  def applyBulkFix(bot: BotTarget, report: BotReport): IO[FixResult] =
    val invalidApiEntries = report.invalidLinks.filter(_._3.startsWith("api_"))
    val attempted         = invalidApiEntries.size
    val uniqueTargets     = invalidApiEntries.map { case (filename, source, _) => (filename, source) }.distinct

    for
      mappings <- uniqueTargets.traverse { case (filename, oldSource) =>
        resolveMegaLinkForFile(bot, filename).map {
          case Right(newSource) => Right((filename, oldSource, newSource))
          case Left(err)        => Left((filename, oldSource, err))
        }
      }
      resolvedEntries = mappings.collect { case Right(v) => v }
      unresolved      = mappings.collect { case Left(v)  => v }.toVector
      replacementMap   = resolvedEntries.map { case (filename, oldSource, newSource) =>
        (filename, oldSource) -> newSource
      }.toMap
      rawJson <- IO.blocking(ujson.read(os.read(bot.listPath, StandardCharsets.UTF_8)))
      replaced <- IO.blocking {
        val arr = rawJson.arr
        var replacements = 0
        arr.foreach { item =>
          val filenameOpt = item.obj.get("filename").collect { case ujson.Str(s) => s }
          val sourcesOpt  = item.obj.get("sources").collect { case a: ujson.Arr => a }
          (filenameOpt, sourcesOpt) match
            case (Some(filename), Some(sources)) =>
              sources.value.indices.foreach { idx =>
                sources.value(idx) match
                  case ujson.Str(oldSource) =>
                    replacementMap.get((filename, oldSource)).foreach { newSource =>
                      if newSource != oldSource then
                        sources.value(idx) = ujson.Str(newSource)
                        replacements += 1
                    }
                  case _ => ()
              }
            case _ => ()
        }
        val backupPath = os.Path(bot.listPath.toString + ".bak", os.root)
        os.write.over(backupPath, os.read(bot.listPath, StandardCharsets.UTF_8), createFolders = true)
        os.write.over(bot.listPath, ujson.write(rawJson, indent = 2) + "\n", createFolders = true)
        replacements
      }
    yield FixResult(
      botName = bot.name,
      attempted = attempted,
      resolved = resolvedEntries.size,
      replaced = replaced,
      unresolved = unresolved
    )

  def printPreview[A](title: String, items: Vector[A], maxPreview: Int)(render: A => String): IO[Unit] =
    if items.nonEmpty then
      IO.println(s"  $title (${items.size}):") *>
        items.take(maxPreview).traverse_(item => IO.println(s"    ${render(item)}")) *>
        IO.whenA(items.size > maxPreview)(IO.println(s"    ... and ${items.size - maxPreview} more"))
    else IO.unit

  def printReport(report: BotReport, maxPreview: Int): IO[Unit] =
    for
      _ <- IO.println(s"BOT: ${report.botName}")
      _ <- IO.println(s"  media list entries: ${report.listEntries}")
      _ <- IO.println(s"  files on disk: ${report.filesOnDisk}")
      _ <- IO.println(s"  unique mega links checked: ${report.uniqueMegaLinks}")
      _ <- IO.println(s"  reachable mega links: ${report.reachable}")
      _ <- IO.println(s"  invalid mega links (api): ${report.invalidByApi}")
      _ <- IO.println(s"  invalid mega links (transport/response): ${report.transportErrors}")
      _ <- IO.println(s"  malformed source entries: ${report.malformedSources.size}")
      _ <- IO.println(s"  non-mega sources: ${report.nonMegaSources.size}")
      _ <- IO.println(s"  files missing in json: ${report.missingInJson.size}")
      _ <- IO.println(s"  files missing on disk: ${report.missingOnDisk.size}")
      _ <- printPreview("Files missing in json", report.missingInJson, maxPreview)(identity)
      _ <- printPreview("Files missing on disk", report.missingOnDisk, maxPreview)(identity)
      _ <- printPreview("Invalid links", report.invalidLinks, maxPreview) { case (filename, source, reason) =>
        s"$filename | $reason | $source"
      }
      _ <- IO.println("")
    yield ()

  def run(cfg: Config): IO[Unit] =
    for
      bots <- discoverBots(cfg)
      _ <- if bots.isEmpty then
        IO.raiseError(new IllegalArgumentException("No matching bots found. Check --mega-root, --project-root, or --bot values."))
      else IO.unit
      _ <- IO.println(s"Mega root: ${cfg.megaRoot}")
      _ <- IO.println(s"Project root: ${cfg.projectRoot}")
      _ <- IO.println(s"Bots checked: ${bots.map(_.name).mkString(", ")}")
      _ <- IO.println("")
      httpClient = HttpClient.newHttpClient()
      reports <- bots.traverse(bot => botReport(bot, cfg, httpClient))
      _ <- reports.traverse_(report => printReport(report, cfg.maxPreview))
      _ <- cfg.reportPath match
        case Some(reportPath) =>
          val lines = Vector("bot\tfilename\treason\tsource") ++ reports.flatMap { report =>
            report.invalidLinks.map { case (filename, source, reason) =>
              s"${report.botName}\t$filename\t$reason\t$source"
            }
          }
          IO.blocking(os.write.over(reportPath, lines.mkString("\n"), createFolders = true)) *>
            IO.println(s"Wrote invalid-link report to: $reportPath")
        case None => IO.unit
      _ <- if cfg.fixInvalid then
        for
          _ <- IO.println("Applying bulk fixer for invalid api links...")
          botMap = bots.map(b => b.name -> b).toMap
          fixResults <- reports.traverse { report =>
            botMap.get(report.botName) match
              case Some(bot) => applyBulkFix(bot, report)
              case None =>
                IO.pure(FixResult(report.botName, 0, 0, 0, Vector((report.botName, "", "bot lookup failed"))))
          }
          _ <- fixResults.traverse_ { fr =>
            IO.println(
              s"[fix] ${fr.botName}: attempted=${fr.attempted}, resolved=${fr.resolved}, replaced=${fr.replaced}, unresolved=${fr.unresolved.size}"
            ) *> printPreview("[fix] unresolved", fr.unresolved, cfg.maxPreview) { case (fn, old, reason) =>
              s"$fn | $reason | $old"
            }
          }
          _ <- IO.println("")
          _ <- IO.println("Re-running validation after fixes...")
          afterFixReports <- bots.traverse(bot => botReport(bot, cfg, httpClient))
          _ <- afterFixReports.traverse_(report => printReport(report, cfg.maxPreview))
        yield ()
      else IO.unit
      totalInvalid = reports.map(_.invalidLinks.size).sum
      _ <- if totalInvalid == 0 then IO.println("All checked Mega links are reachable.")
      else IO.println(s"Found $totalInvalid invalid/unreachable link(s).")
    yield ()

@main def main(args: String*): Unit =
  CheckMegaLinks.parseArgs(args.toArray) match
    case Left(message) =>
      println(message)
      if message != CheckMegaLinks.usage then
        println()
        println(CheckMegaLinks.usage)
      sys.exit(if message == CheckMegaLinks.usage then 0 else 1)
    case Right(cfg) =>
      CheckMegaLinks.run(cfg).handleErrorWith(e => IO.println(s"Error: ${e.getMessage}")).unsafeRunSync()
