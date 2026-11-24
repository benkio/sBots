package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import com.benkio.chatllm.prompt.ReplyCatalogue
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.Printer

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** Tooling for issue #1490. It does not change runtime behavior: it generates the deterministic reply catalogue from
  * each bot's replies JSON and checks that the curated persona files and the catalogues stay within the prompt budget.
  * Personas are long-form research documents (up to roughly 8k words); the catalogue stays compact.
  */
object PersonaTool extends IOApp {

  final case class CatalogueEntry(id: String, summary: String)

  object CatalogueEntry {
    given Decoder[CatalogueEntry] = Decoder.forProduct2("id", "summary")(CatalogueEntry.apply)
    given Encoder[CatalogueEntry] = Encoder.forProduct2("id", "summary")(e => (e.id, e.summary))
  }

  final case class BotFiles(id: String, replies: Path, persona: Path, catalogue: Path)

  private val PersonaMaxBytes   = 64 * 1024
  private val CatalogueMaxBytes = 8192
  private val MaxEntries        = 50
  private val SummaryMaxChars   = 180
  private val IdPattern         = "r[0-9a-f]{12}".r

  /** Accept both the compact four-heading form and long, numbered research personas. Matching is done on level-2
    * headings only, so `### Esempio` cannot satisfy the examples requirement.
    */
  private val PersonaSectionPatterns = List(
    "description"  -> """(?iu)^##\s+(?:\d+\.\s+)?(?:descrizione|identità|identita|profilo|identity|description).*""".r,
    "speech style" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:stile|voce|linguaggio|style|voice|language|communication).*""".r,
    "few-shot examples" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:esempi|esempio|conversazioni|dialoghi|examples?|dialogue|conversation).*""".r,
    "rules" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:regole|accuratezza|rules?|accuracy).*""".r
  )

  override def run(args: List[String]): IO[ExitCode] = {
    val generate  = args.exists(a => a == "--generate-catalogues" || a == "generate")
    val allBots   = BotRegistry.toList(BotRegistry.value).map(botFiles)
    val operation = if generate then allBots.traverse_(generateCatalogue) else allBots.traverse_(checkBot)
    operation.as(ExitCode.Success)
  }

  private def botFiles(entry: BotRegistryEntry[IO]): BotFiles = {
    val config       = entry.sBotConfig
    val id           = config.sBotInfo.botId.value
    val replies      = config.repliesJsonFilePath
    val resourcesDir = replies.getParent
    BotFiles(
      id = id,
      replies = replies,
      persona = resourcesDir.resolve(s"${id}_persona.md"),
      catalogue = resourcesDir.resolve(s"${id}_reply_catalogue.json")
    )
  }

  private def readBundles(path: Path): IO[List[Json]] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8)).flatMap { content =>
      IO.fromEither(
        decode[List[Json]](content).left
          .map(error => new RuntimeException(s"[PersonaTool] Cannot decode $path: ${error.getMessage}"))
      )
    }

  private def triggersOf(bundle: Json): List[String] =
    bundle.hcursor
      .downField("trigger")
      .downField("TextTrigger")
      .downField("triggers")
      .as[List[Json]]
      .toOption
      .getOrElse(Nil)
      .flatMap { value =>
        value.hcursor
          .get[String]("StringTextTriggerValue")
          .toOption
          .orElse(
            value.hcursor.downField("RegexTextTriggerValue").get[String]("trigger").toOption
          )
      }

  private def idOf(bundle: Json): String   = ReplyCatalogue.idOf(bundle)
  private def clean(value: String): String = value.replaceAll("\\s+", " ").trim

  private def shorten(value: String, maxLength: Int): String = {
    val cleaned = clean(value)
    if cleaned.length <= maxLength then cleaned else cleaned.take(maxLength - 1) + "…"
  }

  private def mediaKind(kind: String): String = kind match {
    case "Mp3File"               => "audio"
    case "VideoFile" | "GifFile" => "video"
    case "MarkdownFile"          => "testo"
    case "PhotoFile"             => "foto"
    case "Sticker"               => "sticker"
    case other                   => other
  }

  private def replyDescription(reply: Json): String = {
    val cursor = reply.hcursor
    cursor.downField("TextReply").get[List[String]]("text").toOption match {
      case Some(texts) =>
        val text = texts.map(shorten(_, 90)).find(_.nonEmpty).getOrElse("testo")
        s"risponde: $text"
      case None =>
        cursor.downField("MediaReply").get[List[Json]]("mediaFiles").toOption match {
          case Some(media) =>
            val kinds = media.flatMap(_.asObject.flatMap(_.toList.headOption.map(_._1))).map(mediaKind).distinct.sorted
            s"invia un media (${kinds.mkString(", ")})"
          case None =>
            cursor
              .downField("EffectfulReply")
              .downField("key")
              .focus
              .flatMap(_.asObject.flatMap(_.toList.headOption.map(_._1))) match {
              case Some(key) => s"esegue l'azione $key"
              case None      => "risponde"
            }
        }
    }
  }

  private def summaryOf(bundle: Json): String = {
    val triggers   = triggersOf(bundle).map(clean).filter(_.nonEmpty)
    val target     = if triggers.isEmpty then "qualsiasi messaggio" else triggers.map(t => s"«$t»").mkString(" / ")
    val reply      = bundle.hcursor.downField("reply").focus.getOrElse(Json.Null)
    val raw        = clean(s"A $target ${replyDescription(reply)}")
    val punctuated =
      if raw.endsWith(".") || raw.endsWith("!") || raw.endsWith("?") then raw else s"$raw."
    shorten(punctuated, SummaryMaxChars)
  }

  private def catalogueEntries(bundles: List[Json]): List[CatalogueEntry] = {
    val all = bundles.map(bundle => CatalogueEntry(idOf(bundle), summaryOf(bundle)))
    if all.size <= MaxEntries then all
    else {
      val last = all.size - 1
      (0 until MaxEntries).map(i => all(math.round(i.toDouble * last / (MaxEntries - 1)).toInt)).distinct.toList
    }
  }

  private def writeCatalogue(files: BotFiles): IO[Unit] =
    for {
      bundles <- readBundles(files.replies)
      entries = catalogueEntries(bundles)
      content = Printer.spaces2.print(entries.asJson) + "\n"
      _ <- IO.println(s"[PersonaTool] Generate ${files.catalogue}")
      _ <- IO.blocking {
        Option(files.catalogue.getParent).foreach(Files.createDirectories(_))
        Files.writeString(files.catalogue, content, StandardCharsets.UTF_8)
        ()
      }
    } yield ()

  private def generateCatalogue(files: BotFiles): IO[Unit] =
    IO.println(s"[PersonaTool] Bot ${files.id}").flatMap(_ => writeCatalogue(files))

  private def validate(files: BotFiles): List[String] = {
    val errors = List.newBuilder[String]

    if !Files.exists(files.persona) then errors += s"missing ${files.persona.getFileName}"
    else {
      val bytes   = Files.readAllBytes(files.persona)
      val content = new String(bytes, StandardCharsets.UTF_8)
      val lines   = content.linesIterator.toList
      if bytes.isEmpty then errors += "persona is empty"
      if bytes.length > PersonaMaxBytes then errors += s"persona too large (${bytes.length} bytes)"
      PersonaSectionPatterns.foreach { case (section, pattern) =>
        if !lines.exists(pattern.matches) then errors += s"persona missing a '$section' section"
      }
      if lines.exists(line => line.trim.matches("(?i)(assistant|system)\\s*:.*"))
      then errors += "persona contains a role label"
      if content.contains("/llm") then errors += "persona contains a command"
    }

    if !Files.exists(files.catalogue) then errors += s"missing ${files.catalogue.getFileName}"
    else {
      val bytes   = Files.readAllBytes(files.catalogue)
      val content = new String(bytes, StandardCharsets.UTF_8)
      if bytes.length > CatalogueMaxBytes then errors += s"catalogue too large (${bytes.length} bytes)"
      decode[List[CatalogueEntry]](content) match {
        case Left(error)    => errors += s"catalogue decode failed: ${error.getMessage}"
        case Right(entries) =>
          if entries.isEmpty then errors += "catalogue is empty"
          if entries.size > MaxEntries then errors += s"catalogue has ${entries.size} entries (max $MaxEntries)"
          if entries.map(_.id).distinct.size != entries.size then errors += "catalogue has duplicate ids"
          entries.zipWithIndex.foreach { case (entry, index) =>
            if !IdPattern.matches(entry.id) then errors += s"entry $index has invalid id '${entry.id}'"
            if entry.summary.isEmpty then errors += s"entry $index has an empty summary"
            if entry.summary.contains("\n") || entry.summary.contains("\r")
            then errors += s"entry $index summary is not one line"
            if entry.summary.length > SummaryMaxChars then errors += s"entry $index summary is too long"
          }
          decode[List[Json]](Files.readString(files.replies, StandardCharsets.UTF_8)) match {
            case Left(error)    => errors += s"replies decode failed: ${error.getMessage}"
            case Right(bundles) =>
              val expectedIds = catalogueEntries(bundles).map(_.id)
              if entries.map(_.id) != expectedIds
              then errors += "catalogue ids do not match replies JSON (run generatePersona)"
          }
      }
    }

    errors.result()
  }

  private def checkBot(files: BotFiles): IO[Unit] =
    IO.blocking(validate(files)).flatMap { errors =>
      if errors.nonEmpty then IO.raiseError(
        new RuntimeException(s"[PersonaTool] ${files.id}: ${errors.mkString("; ")}")
      )
      else IO.println(s"[PersonaTool] OK: ${files.id}")
    }
}
