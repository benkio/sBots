package com.benkio.botDB.mocks

import com.google.api.services.youtube.model.Video
import com.benkio.botDB.show.YouTubeBotIds
import com.benkio.botDB.show.YouTubeService
import cats.effect.IO

class YouTubeServiceMock(
  onGetAllBotNameIds: IO[List[YouTubeBotIds]] = IO.raiseError(Throwable("[YouTubeServiceMock] Unexpected Call")),
  onGetYouTubeVideos: List[String] => IO[List[Video]] = _ => IO.raiseError(Throwable("[YouTubeServiceMock] Unexpected Call"))
) extends YouTubeService[IO] {
  def getAllBotNameIds: IO[List[YouTubeBotIds]] = onGetAllBotNameIds
  def getYouTubeVideos(
      videoIds: List[String]
  ): IO[List[Video]] = onGetYouTubeVideos(videoIds)
}
