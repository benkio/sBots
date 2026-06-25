package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.syntax.all.*
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

trait CaptionParser[F[_]] {
  def parsePlainCaptionSrt(captionPath: Path): F[Option[String]]
}

object CaptionParser {
  def apply[F[_]: Async: LogWriter](): CaptionParser[F] = CaptionParserImpl[F]()

  class CaptionParserImpl[F[_]: Async: LogWriter]() extends CaptionParser[F] {
    private val timestampPattern =
      """^\d{2}:\d{2}:\d{2},\d{3}\s+-->\s+\d{2}:\d{2}:\d{2},\d{3}.*$""".r

    def parsePlainCaptionSrt(captionPath: Path): F[Option[String]] =
      (for {
        _      <- LogWriter.info(s"[CaptionParser] Parse caption file $captionPath")
        exists <- Async[F].delay(Files.exists(captionPath))
        result <-
          if !exists then Async[F].pure(None)
          else
            for {
              lines <- Async[F].delay(Files.readAllLines(captionPath).asScala.toList)
              _     <- LogWriter.info(s"[CaptionParser] Read ${lines.length} lines from $captionPath")
              content = lines
                .map(_.trim)
                .filter(_.nonEmpty)
                .filterNot(line => line.forall(_.isDigit) || timestampPattern.matches(line))
                .mkString(" ")
              _ <- LogWriter.info(s"[CaptionParser] Parsed caption length ${content.length}")
            } yield Some(content)
      } yield result).handleErrorWith(e =>
        LogWriter.error(s"[CaptionParser] ❌ Error parsing caption file $captionPath: $e") >>
          Async[F].pure(None)
      )
  }
}
