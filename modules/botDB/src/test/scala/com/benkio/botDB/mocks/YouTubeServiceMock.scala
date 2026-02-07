package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.show.YouTubeBotIds
import com.benkio.botDB.show.YouTubeService
import com.google.api.services.youtube.model.Video

import java.nio.file.Path

class YouTubeServiceMock(
    onGetAllBotNameIds: IO[List[YouTubeBotIds]] = IO.raiseError(Throwable("[YouTubeServiceMock] Unexpected Call")),
    onGetYouTubeVideos: List[String] => IO[List[Video]] = _ =>
      IO.raiseError(Throwable("[YouTubeServiceMock] Unexpected Call")),
    onFetchCaption: (String, Path, String) => IO[Option[String]] = (_, _, _) =>
      IO.raiseError(Throwable("[YouTubeServiceMock] Unexpected Call"))
) extends YouTubeService[IO] {
  override def getAllBotNameIds: IO[List[YouTubeBotIds]] = onGetAllBotNameIds
  override def getYouTubeVideos(
      videoIds: List[String]
  ): IO[List[Video]] = onGetYouTubeVideos(videoIds)

  override def fetchCaption(videoId: String, tempDir: Path, captionLanguage: String): IO[Option[String]] =
    onFetchCaption(videoId, tempDir, captionLanguage)
}
