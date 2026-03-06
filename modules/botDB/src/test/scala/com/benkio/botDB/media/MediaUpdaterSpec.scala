package com.benkio.botDB.media

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.media.MediaUpdater.MediaUpdaterImpl
import com.benkio.botDB.Logger.given
import com.benkio.botDB.TestData.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.getMediaResourceFile
import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBMediaData
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

class MediaUpdaterSpec extends CatsEffectSuite {

  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)
  val botId                            = SBotId("testbot")

  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (location, inputBotId) =>
      IO.raiseUnless(inputBotId == botId)(
        Throwable(s"[MediaUpdaterSpec] getResourceByKindHandler called with unexpected botId: $inputBotId")
      ).as(
        NonEmptyList
          .one(
            NonEmptyList.fromListUnsafe(
              Files
                .list(Paths.get(getClass.getResource(location).toURI))
                .iterator()
                .asScala
                .map(p => MediaResourceFile(Resource.pure(p)): MediaResource[IO])
                .toList
            )
          )
      )
  )
  val dbLayerMock = DBLayerMock.mock(
    botId = botId,
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
        .use(_.pure[IO]),
      config.jsonLocation.flatMap(location =>
        Files.list(Paths.get(getClass.getResource(location.value).toURI)).iterator().asScala.toList
      )
    )
  }

  test("MediaUpdater.filterMediaJsonFiles should return the expected json files") {
    assertIO(
      mediaUpdater.fetchRootBotFiles.flatMap(roots => mediaUpdater.filterMediaJsonFiles(roots)).use(_.pure[IO]),
      List(Paths.get(getClass.getResource("/testdata/test_list.json").toURI))
    )
  }

  test("MediaUpdater.parseMediaJsonFiles should parse valid json file") {
    val input: List[Path]               = List(Paths.get(getClass.getResource("/testdata/test_list.json").toURI))
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
            value = uri"https://www.dropbox.com/scl/fi/hYPb0/test_testData.mp3?rlkey=BbLKm&dl=1"
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
