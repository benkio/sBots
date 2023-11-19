package com.benkio.telegrambotinfrastructure.resources

import cats.effect.IO
import munit.FunSuite

import java.nio.file.Files
import java.nio.file.*
import scala.util.Random

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

  def testFilename(filename: String)(using resourceAccess: ResourceAccess[IO]): IO[Boolean] =
    resourceAccess
      .getResourceByteArray(filename)
      .use((fileBytes: Array[Byte]) => {
        if (fileBytes.nonEmpty) IO(true)
        else
          IO {
            println(s"ERROR: filename $filename is missing!!!!")
            false
          }
      })
}
