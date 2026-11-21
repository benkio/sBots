package com.benkio.main

import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.paths.SBotPaths
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import io.circe.Decoder
import munit.FunSuite

import java.nio.file.Files
import scala.jdk.CollectionConverters.*

/** Guards the one-time migration of file references to platform-tagged sources: a regression that writes an untagged
  * reference in a list JSON fails the CI unit tests.
  */
class FileIdSourcesSpec extends FunSuite {

  test("every file reference in the bot list JSONs carries a known platform tag") {
    // The repo root is injected into the forked test JVM; do not derive it from the launch directory.
    val files = Files
      .walk(SBotPaths.repoRoot.resolve("modules").resolve("bots"))
      .iterator()
      .asScala
      .filter(p => Files.isRegularFile(p) && p.getFileName.toString.endsWith("_list.json"))
      .toList

    assert(files.nonEmpty, "no bot list JSON found")

    // The raw strings are checked, so the message names the offending source instead of a decoding failure.
    val invalid = files.flatMap { path =>
      decode[List[RawMedia]](Files.readString(path)).fold(
        error => List(s"$path: ${error.getMessage}"),
        _.flatMap(media =>
          media.sources.filter(MediaSource.parse(_).isEmpty).map(source => s"$path: ${media.filename}: $source")
        )
      )
    }

    assertEquals(invalid, Nil)
  }

  private final case class RawMedia(filename: String, sources: List[String])

  private given Decoder[RawMedia] = deriveDecoder
}
