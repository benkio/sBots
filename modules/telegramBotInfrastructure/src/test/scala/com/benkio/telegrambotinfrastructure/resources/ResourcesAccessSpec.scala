package com.benkio.telegrambotinfrastructure.resources

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.nio.file.*
import scala.util.Random

class ResourcesAccessSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val testfile       = "testFile"
  val rootPath: Path = Paths.get("").toAbsolutePath()
  val random         = new Random()

  test("ResourceAccess - buildPath should return the expected path when the filename is provided") {
    val result = ResourceAccess.buildPath(testfile)
    assertEquals(result, Paths.get(rootPath.toString, "src", "main", "resources", testfile))
  }

  test("toTempFile should create a temporary file with the expected content and name") {
    val (inputFileName, inputContent) = ("test.txt", random.nextBytes(100))

    val obtainedResource = ResourceAccess.toTempFile[IO](inputFileName, inputContent)
    obtainedResource.use(obtained =>
      IO {
        assert(obtained.getName().startsWith("test"))
        assert(obtained.getName().endsWith(".txt"))
        assertEquals(Files.readAllBytes(obtained.toPath).toSeq, inputContent.toSeq)
      }
    )
  }

  test("ResourceAccess Local should retrieve a mediafile from the resources correctly") {
    val resourceAccess = ResourceAccess.fromResources[IO]()
    /*
    Use the class of tihs test becouse the local resource access will
    search in the `getClass()` that's convenient when packing
    everything with `assembly`
     */
    val filename = "test.txt"
    ResourceAccessSpec.testFilename(filename)(using resourceAccess).assert
  }
}

object ResourceAccessSpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def testFilename(filename: String)(using resourceAccess: ResourceAccess[IO]): IO[Boolean] =
    resourceAccess
      .getResourceFile(Document(filename))
      .use(_.exists {
        case MediaResourceFile(_)  => true
        case MediaResourceIFile(_) => false
      }.pure)
}
