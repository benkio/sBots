package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.show.YouTubeBotIds
import com.benkio.botDB.show.YouTubeService
import com.google.api.services.youtube.model.Video

import java.nio.file.Path

class YouTubeServiceMock(
    onGetAllBotNameIds: IO[List[YouTubeBotIds]] = IO.raiseError(Throwable(YouTubeServiceMock.unexpectedCall)),
    onGetYouTubeVideos: List[String] => IO[List[Video]] = _ =>
      IO.raiseError(Throwable(YouTubeServiceMock.unexpectedCall)),
    onSaveCaption: (String, Path, String) => IO[Unit] = (_, _, _) =>
      IO.raiseError(Throwable(YouTubeServiceMock.unexpectedCall))
) extends YouTubeService[IO] {
  override def getAllBotNameIds: IO[List[YouTubeBotIds]] = onGetAllBotNameIds
  override def getYouTubeVideos(
      videoIds: List[String]
  ): IO[List[Video]] = onGetYouTubeVideos(videoIds)

  override def saveCaption(videoId: String, captionFolderPath: Path, captionLanguage: String): IO[Unit] =
    onSaveCaption(videoId, captionFolderPath, captionLanguage)
}

object YouTubeServiceMock {
  private val unexpectedCall = "[YouTubeServiceMock] Unexpected Call"
}
