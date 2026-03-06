package com.benkio.chatcore.repository

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.http.DropboxClient
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.DropboxClientMock
import com.benkio.chatcore.model.media.getMediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.nio.file.*
import scala.util.Random

class ResourceRepositorySpec extends CatsEffectSuite {

  val testfile       = "testFile"
  val rootPath: Path = Paths.get("").toAbsolutePath()
  val random         = new Random()

  test("Repository - buildPath should return the expected path when the filename is provided") {
    val result = Repository.buildPath(testfile)
    assertEquals(result, Paths.get(rootPath.toString, "src", "main", "resources", testfile))
  }

  test("toTempFile should create a temporary file with the expected content and name") {
    val (inputFileName, inputContent) = ("test.txt", random.nextBytes(100))

    val obtainedResource = Repository.toTempFile[IO](inputFileName, inputContent)
    obtainedResource.use(obtained =>
      IO {
        assert(obtained.getFileName().toString().startsWith("test"))
        assert(obtained.getFileName().toString().endsWith(".txt"))
        assertEquals(Files.readAllBytes(obtained).toSeq, inputContent.toSeq)
      }
    )
  }

  test("ResourcesRepository Local should retrieve a mediafile from the resources correctly") {
    val repository = ResourcesRepository.fromResources[IO]()
    /*
    Use the class of this test becouse the local resource access will
    search in the `getClass()` that's convenient when packing
    everything with `assembly`
     */
    val filename = "test.txt"
    RepositorySpec.testFilename(filename)(using repository).assert
  }
}

class DBRepositorySpec extends CatsEffectSuite {

  val medias: List[DBMediaData] = List(
    DBMediaData(
      media_name = "bot_testMediaName.mp4",
      bot_id = "bot",
      kinds = "[]",
      mime_type = "audio/mpeg",
      media_sources = """[ "http://benkio.github.io" ]""",
      media_count = 0,
      created_at = "1755687972"
    ),
    DBMediaData(
      media_name = "bot_testMediaName2.mp4",
      bot_id = "bot",
      kinds = """["testkind"]""",
      mime_type = "audio/mpeg",
      media_sources = """[ "http://benkio.github.io" ]""",
      media_count = 0,
      created_at = "1755687972"
    )
  )
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(SBotId("bot"))
  val fullDBLayer: DBLayer[IO]  = DBLayerMock.mock(SBotId("bot"), medias = medias)
  val testPath: Path            = Path.of("test.mp4")
  def dropboxClientMockBuild(expectedFileName: String): DropboxClient[IO] = DropboxClientMock.mock((inputFileName, _) =>
    Resource.eval(
      IO.raiseUnless(inputFileName == expectedFileName)(
        Throwable(s"[DBRepositorySpec] Error DropboxClientMock. $inputFileName ≠ $expectedFileName")
      ).as(testPath)
    )
  )

  test("DBRepository.getResourceFile should return an error if the mediaFile doesn't exists") {
    val expectedFileName                     = "bot_testMediaName.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](
      emptyDBLayer.dbMedia,
      dropboxClientMock
    )
    val expectedMediaFile: MediaFile = VideoFile(expectedFileName)
    val check: IO[Boolean]           = dbRepository
      .getResourceFile(expectedMediaFile)
      .use(result =>
        result match {
          case Left(RepositoryError.NoResourcesFoundFile(resultMediaFile)) =>
            (resultMediaFile == expectedMediaFile).pure
          case _ => false.pure
        }
      )
    assertIO(check, true)
  }

  test("DBRepository.getResourceFile should return the expected list of MediaResource") {
    val expectedFileName                     = "bot_testMediaName.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](
      fullDBLayer.dbMedia,
      dropboxClientMock
    )

    val check: IO[Boolean] = RepositorySpec.testFilename(expectedFileName)(using dbRepository)
    assertIO(check, true)
  }
  test("DBRepository.getResourceKind should return an error if the criteria doesn't exists") {
    val expectedFileName                     = "bot_testMediaName2.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](
      emptyDBLayer.dbMedia,
      dropboxClientMock
    )
    val botId: SBotId      = SBotId("bot")
    val check: IO[Boolean] = dbRepository
      .getResourcesByKind(criteria = "testkind", botId = botId)
      .use(result =>
        result match {
          case Left(RepositoryError.NoResourcesFoundKind(criteria, _)) =>
            (criteria == "testkind").pure
          case _ => false.pure
        }
      )
    assertIO(check, true)
  }
  test("DBRepository.getResourceKind should return the expected list of MediaResource") {
    val expectedFileName                     = "bot_testMediaName2.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](fullDBLayer.dbMedia, dropboxClientMock)
    val botId: SBotId                        = SBotId("bot")
    val check: IO[Boolean]                   = dbRepository
      .getResourcesByKind(criteria = "testkind", botId = botId)
      .flatMap(result =>
        result match {
          case Right(mediaResources) =>
            mediaResources.toList.flatTraverse(
              _.toList.traverse(
                _.getMediaResourceFile.sequence
              )
            )
          case _ => Resource.eval(IO.raiseError(Throwable("[RepositorySpec] getResourceKind didn't return anything")))
        }
      )
      .use(result => IO.pure(result == List(testPath.some)))
    assertIO(check, true)
  }
}

object RepositorySpec {

  def testFilename(filename: String)(using repository: Repository[IO]): IO[Boolean] =
    repository
      .getResourceFile(Document(filename))
      .use(
        _.fold(
          _ => false,
          _.exists {
            case MediaResourceFile(_)  => true
            case MediaResourceIFile(_) => false
          }
        ).pure
      )
}
