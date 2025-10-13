package com.benkio.botDB.media

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.media.MediaUpdater.MediaUpdaterImpl
import com.benkio.botDB.TestData.*
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import org.http4s.Uri

import java.io.File

class MediaUpdaterSpec extends CatsEffectSuite {

  given log: LogWriter[IO]             = consoleLogUpToLevel(LogLevels.Info)
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)

  val repositoryMock = new RepositoryMock(location =>
    NonEmptyList
      .one(
        NonEmptyList.fromListUnsafe(
          File(getClass.getResource(location).toURI).listFiles
            .map(f => MediaResourceFile(Resource.pure(f)): MediaResource[IO])
            .toList
        )
      )
      .pure[IO]
  )
  val dbLayerMock = DBLayerMock.mock(
    botId = SBotId("testbot"),
    medias = mediaEntities
  )
  val mediaUpdater: MediaUpdaterImpl[IO] = MediaUpdaterImpl[IO](
    config = config,
    dbLayer = dbLayerMock,
    repository = repositoryMock
  )

  test("MediaUpdater.fetchRootBotFiles should return the expected root files") {
    assertIO(
      mediaUpdater.fetchRootBotFiles
        .flatMap(_.map(_.getMediaResourceFile).flatten.sequence)
        .use(_.map(_.getPath()).pure[IO]),
      config.jsonLocation.flatMap(location => File(getClass.getResource(location).toURI).listFiles.map(_.getPath()))
    )
  }

  test("MediaUpdater.filterMediaJsonFiles should return the expected json files") {
    assertIO(
      mediaUpdater.fetchRootBotFiles.flatMap(roots => mediaUpdater.filterMediaJsonFiles(roots)).use(_.pure[IO]),
      List(File(getClass.getResource("/testdata/test_list.json").toURI))
    )
  }

  test("MediaUpdater.parseMediaJsonFiles should parse valid json file") {
    val input: List[File]               = List(File(getClass.getResource("/testdata/test_list.json").toURI))
    val expected: List[MediaFileSource] = List(
      MediaFileSource(
        filename = "test_testData.mp3",
        kinds = List(
          "kind1",
          "kind2"
        ),
        mime = MimeType.MPEG,
        sources = List(
          Right(
            value = Uri.unsafeFromString("https://www.dropbox.com/scl/fi/hYPb0/test_testData.mp3?rlkey=BbLKm&dl=1")
          )
        )
      )
    )
    assertIO(
      mediaUpdater.parseMediaJsonFiles(input).use(_.pure[IO]),
      expected
    )
  }
}
