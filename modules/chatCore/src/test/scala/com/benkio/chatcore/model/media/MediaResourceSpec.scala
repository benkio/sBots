package com.benkio.chatcore.model.media

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import munit.*

import java.nio.file.Path
import java.nio.file.Paths

class MediaResourceSpec extends CatsEffectSuite {
  test("getMediaResourceFile should extract the file or return None") {
    val path      = Paths.get(".")
    val actual1   = MediaResourceFile(Resource.pure[IO, Path](path)).getMediaResourceFile.sequence
    val expected1 = Some(path)

    val ifile                            = "ifile"
    val mediaResource: MediaResource[IO] = MediaResourceIFile(ifile)
    val actual2                          = mediaResource.getMediaResourceFile
    assertIO(actual1.use(_.pure[IO]), expected1) *>
      assertIO(actual2.pure[IO], None)
  }
}
