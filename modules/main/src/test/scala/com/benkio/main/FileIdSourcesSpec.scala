package com.benkio.main

import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.media.MediaFileSource.given
import com.benkio.chatcore.model.Platform
import io.circe.parser.decode
import munit.FunSuite

import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

/** Guards the one-time migration of file references to platform-tagged sources: a regression that writes an untagged
  * reference in a list JSON fails the CI unit tests.
  */
class FileIdSourcesSpec extends FunSuite {

  test("every file reference in the bot list JSONs carries a known platform tag") {
    // sbt forks tests with the module directory as working directory: modules/main -> ../bots
    val files = Files
      .walk(Paths.get("..", "bots"))
      .iterator()
      .asScala
      .filter(p => Files.isRegularFile(p) && p.getFileName.toString.endsWith("_list.json"))
      .toList

    assert(files.nonEmpty, "no bot list JSON found")

    val invalid = files.flatMap { path =>
      decode[List[MediaFileSource]](Files.readString(path)).fold(
        error => List(s"$path: ${error.getMessage}"),
        _.flatMap { media =>
          media.sources.collect {
            case Left(reference) if !reference.contains(':') || Platform.fromId(reference.takeWhile(_ != ':')).isLeft =>
              s"$path: ${media.filename}: $reference"
          }
        }
      )
    }

    assertEquals(invalid, Nil)
  }
}
