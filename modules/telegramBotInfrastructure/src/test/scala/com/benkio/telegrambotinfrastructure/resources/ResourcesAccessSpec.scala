package com.benkio.telegrambotinfrastructure.resources

import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import log.effect.LogLevels
import com.benkio.telegrambotinfrastructure.model.reply.Document
import cats.effect.IO
import cats.syntax.all.*
import munit.FunSuite

import java.nio.file.*
import scala.util.Random
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
class ResourcesAccessSpec extends FunSuite {

  val testfile       = "testFile"
  val rootPath: Path = Paths.get("").toAbsolutePath()
  val random         = new Random()

  test("ResourceAccess - buildPath should return the expected path when the filename is provided") {
    val result = ResourceAccess.buildPath(testfile)
    assertEquals(result, Paths.get(rootPath.toString, "src", "main", "resources", testfile))
  }

  test("toTempFile should create a temporary file with the expected content and name") {
    val (inputFileName, inputContent) = ("test.txt", random.nextBytes(100))

    val obtained = ResourceAccess.toTempFile(inputFileName, inputContent)
    assert(obtained.getName().startsWith("test"))
    assert(obtained.getName().endsWith(".txt"))
    assertEquals(Files.readAllBytes(obtained.toPath).toSeq, inputContent.toSeq)
  }
}

object ResourceAccessSpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def testFilename(filename: String)(using resourceAccess: ResourceAccess[IO]): IO[Boolean] =
    resourceAccess
      .getResourceFile(Document(filename))
      .use {
        case MediaResource.MediaResourceFile(f) => Files.readAllBytes(f.toPath).map(_.toChar).mkString.nonEmpty.pure
        case MediaResource.MediaResourceIFile(x) =>
          IO.raiseError(Throwable(s"[ResourcesAccessSpec]: filename $filename is missing!!!!"))
      }
}
