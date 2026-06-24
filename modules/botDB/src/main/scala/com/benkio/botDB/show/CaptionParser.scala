package com.benkio.botDB.show

import java.nio.file.Path

trait CaptionParser[F[_]] {
  def parseCaption(videoId: String, captionFolderPath: Path): F[String]
}

object CaptionParser {
  def apply[F[_]]() : CaptionParser[F] = CaptionParserImpl[F]()

  class CaptionParserImpl[F[_]]() extends CaptionParser[F] {
    def parseCaption(videoId: String, captionFolderPath: Path): F[String] = {
      // TODO: Fetch the srt file. parse it. return the caption

      /*
        captionFile <- Async[F].delay(captionFolderPath.resolve(s"${videoId}.$captionLanguage.srt"))
        _           <- LogWriter.info(
          s"[ShowUpdater] ${videoId} - $captionLanguage: Parse result file: $captionFile"
        )
        captionFileContent <- Async[F].fromTry(
          Try(
            Files
              .readAllLines(captionFile)
              .asScala
              .mkString("\n")
          )
        )
        captionJson <- Async[F].fromEither(parse(captionFileContent))
        caption = captionJson.findAllByKey("utf8").map(_.as[String]).collect { case Right(value) => value }.mkString
        _ <- LogWriter.info(
          s"[ShowUpdater] ${videoId} - $captionLanguage: caption length ${caption.length}"
        )
      } yield Some(caption.replace("\n", " "))
      captionDownloadLogic
        .handleErrorWith(e =>
          LogWriter.error(
            s"[ShowUpdater] ❌ ${videoId} - $captionLanguage Downloading Caption: $e"
          ) >> Async[F]
            .pure(None)
        )
       */
      ???
    }
  }
}
