package com.benkio.telegrambotinfrastructure.repository

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.DropboxClient
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.DropboxClientMock
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import com.benkio.telegrambotinfrastructure.repository.db.DBRepository
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.io.File
import java.nio.file.*
import scala.util.Random

class ResourceRepositorySpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

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
        assert(obtained.getName().startsWith("test"))
        assert(obtained.getName().endsWith(".txt"))
        assertEquals(Files.readAllBytes(obtained.toPath).toSeq, inputContent.toSeq)
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

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val medias: List[DBMediaData] = List(
    DBMediaData(
      media_name = "bot_testMediaName.mp4",
      kinds = "[]",
      mime_type = "audio/mpeg",
      media_sources = """[ "http://benkio.github.io" ]""",
      media_count = 0,
      created_at = "1755687972"
    ),
    DBMediaData(
      media_name = "bot_testMediaName2.mp4",
      kinds = """["testkind"]""",
      mime_type = "audio/mpeg",
      media_sources = """[ "http://benkio.github.io" ]""",
      media_count = 0,
      created_at = "1755687972"
    )
  )
  val emptyDBLayer: DBLayer[IO]                                           = DBLayerMock.mock("bot")
  val fullDBLayer: DBLayer[IO]                                            = DBLayerMock.mock("bot", medias = medias)
  val testFile: File                                                      = File("test.mp4")
  def dropboxClientMockBuild(expectedFileName: String): DropboxClient[IO] = DropboxClientMock.mock((inputFileName, _) =>
    Resource.eval(
      IO.raiseUnless(inputFileName == expectedFileName)(
        Throwable(s"[DBRepositorySpec] Error DropboxClientMock. $inputFileName ≠ $expectedFileName")
      ).as(testFile)
    )
  )

  test("DBRepository.getResourceFile should return an error if the mediaFile doesn't exists") {
    val expectedFileName                     = "bot_testMediaName.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](
      emptyDBLayer.dbMedia,
      dropboxClientMock
    )
    val check: IO[Boolean] = dbRepository
      .getResourceFile(VideoFile(expectedFileName))
      .use(result =>
        result match {
          case Right(_) => false.pure
          case Left(_)  => true.pure
        }
      )
    assertIO(check, true)
  }
  test("DBRepository.getResourceFile should return the expected list of MediaResource") { ??? }
  test("DBRepository.getResourceKind should return an error if the criteria doesn't exists") { ??? }
  test("DBRepository.getResourceKind should return the expected list of MediaResource") {
    val expectedFileName                     = "bot_testMediaName2.mp4"
    val dropboxClientMock: DropboxClient[IO] = dropboxClientMockBuild(expectedFileName)
    val dbRepository                         = DBRepository.dbResources[IO](fullDBLayer.dbMedia, dropboxClientMock)
    val check: IO[Boolean]                   = dbRepository
      .getResourcesByKind("testkind")
      .flatMap(result =>
        result match {
          case Right(mediaResources) =>
            mediaResources.toList.flatTraverse(
              _.toList.traverse(
                _.getMediaResourceFile.sequence
              )
            )
          case _ => Resource.eval(IO.raiseError(Throwable(s"[RepositorySpec] getResourceKind didn't return anything")))
        }
      )
      .use(result => IO.pure(result == List(testFile.some)))
    assertIO(check, true)
  }
}

object RepositorySpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

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
