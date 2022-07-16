package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.effect.IO
import munit.FunSuite

import java.nio.file._
import scala.language.reflectiveCalls

class ResourcesAccessSpec extends FunSuite {

  val testfile                     = "testFile"
  val resourcesAccessFromResources = ResourceAccess.fromResources
  val rootPath                     = Paths.get("").toAbsolutePath()

  test("ResourceAccess - buildPath should return the expected path when the filename is provided") {
    val result = resourcesAccessFromResources.buildPath(testfile)
    assertEquals(result, Paths.get(rootPath.toString, "src", "main", "resources", testfile))
  }
}

object ResourceAccessSpec {

  def testFilename(filename: String)(implicit resourceAccess: ResourceAccess): IO[Boolean] =
    resourceAccess
      .getResourceByteArray[IO](filename)
      .use((fileBytes: Array[Byte]) => {
        if (fileBytes.nonEmpty) IO(true)
        else
          IO {
            println(s"ERROR: filename $filename is missing!!!!")
            false
          }
      })
}
