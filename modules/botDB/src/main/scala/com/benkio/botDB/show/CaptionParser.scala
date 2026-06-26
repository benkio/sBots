package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.syntax.all.*
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Path
import scala.collection.immutable.SortedMap
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

trait CaptionParser[F[_]] {
  def parsePlainCaptionSrt(captionPath: Path): F[Option[String]]
  def parseCaptionSrt(captionPath: Path): F[SortedMap[FiniteDuration, String]]
}

object CaptionParser {
  def apply[F[_]: Async: LogWriter](): CaptionParser[F] = CaptionParserImpl[F]()

  class CaptionParserImpl[F[_]: Async: LogWriter]() extends CaptionParser[F] {
    private val timestampPattern =
      """^\d{2}:\d{2}:\d{2},\d{3}\s+-->\s+\d{2}:\d{2}:\d{2},\d{3}.*$""".r
    private val srtTimePattern =
      """(\d{2}):(\d{2}):(\d{2}),(\d{3})""".r

    private def srtTimeToFiniteDuration(value: String): Option[FiniteDuration] =
      value match {
        case srtTimePattern(hours, minutes, seconds, millis) =>
          Some(
            (
              hours.toLong * 3600000L +
                minutes.toLong * 60000L +
                seconds.toLong * 1000L +
                millis.toLong
            ).millis
          )
        case _ => None
      }

    private def splitSrtBlocks(lines: List[String]): List[List[String]] = {
      val (acc, last) =
        lines.foldLeft((List.empty[List[String]], List.empty[String])) { case ((currentAcc, current), line) =>
          if line.trim.isEmpty then (currentAcc :+ current, List.empty[String])
          else (currentAcc, current :+ line.trim)
        }
      (acc :+ last)
        .filter(_.nonEmpty)
    }

    private def parseSrtBlock(block: List[String]): Option[(FiniteDuration, String)] = {
      val timestampLineIndex = block.indexWhere(timestampPattern.matches)
      if timestampLineIndex < 0 then None
      else {
        val timestamp = block(timestampLineIndex).split("-->").headOption.map(_.trim).flatMap(srtTimeToFiniteDuration)
        val text      = block.drop(timestampLineIndex + 1).mkString(" ").trim
        timestamp.filter(_ => text.nonEmpty).map(_ -> text)
      }
    }

    private def parseSrtEntries(lines: List[String]): List[(FiniteDuration, String)] =
      splitSrtBlocks(lines).flatMap(parseSrtBlock)

    override def parsePlainCaptionSrt(captionPath: Path): F[Option[String]] = {
      val extractPlainCaption: F[Option[String]] = for {
        lines <- Async[F].delay(Files.readAllLines(captionPath).asScala.toList)
        _     <- LogWriter.info(s"[CaptionParser] Read ${lines.length} lines from $captionPath")
        content = lines
          .map(_.trim)
          .filter(_.nonEmpty)
          .filterNot(line => line.forall(_.isDigit) || timestampPattern.matches(line))
          .mkString(" ")
        _ <- LogWriter.info(s"[CaptionParser] Parsed caption length ${content.length}")
      } yield Some(content)

      val program = for {
        _      <- LogWriter.info(s"[CaptionParser] Parse caption file $captionPath")
        exists <- Async[F].delay(Files.exists(captionPath))
        result <- if !exists then Async[F].pure(Option.empty[String]) else extractPlainCaption

      } yield result

      program.handleErrorWith(e =>
        LogWriter.error(s"[CaptionParser] ❌ Error parsing caption file $captionPath: $e") >>
          Async[F].pure(Option.empty[String])
      )
    }

    override def parseCaptionSrt(captionPath: Path): F[SortedMap[FiniteDuration, String]] = {
      val extractSrtCaptions: F[SortedMap[FiniteDuration, String]] = for {
        lines <- Async[F].delay(Files.readAllLines(captionPath).asScala.toList)
        _     <- LogWriter.info(s"[CaptionParser] Read ${lines.length} lines from $captionPath")
        parsedEntries = parseSrtEntries(lines)
        _ <- LogWriter.info(s"[CaptionParser] Parsed ${parsedEntries.length} SRT entries from $captionPath")
      } yield SortedMap.from(parsedEntries)

      val program = for {
        _      <- LogWriter.info(s"[CaptionParser] Parse SRT caption file $captionPath")
        exists <- Async[F].delay(Files.exists(captionPath))
        result <- if !exists then Async[F].pure(SortedMap.empty[FiniteDuration, String]) else extractSrtCaptions
      } yield result

      program.handleErrorWith(e =>
        LogWriter.error(s"[CaptionParser] ❌ Error parsing SRT caption file $captionPath: $e") >>
          Async[F].pure(SortedMap.empty[FiniteDuration, String])
      )
    }
  }
}
