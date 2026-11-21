package com.benkio.chatcore.model.media

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.paths.SBotPaths
import munit.*

import java.nio.file.Path

class MediaResourceSpec extends CatsEffectSuite {
  test("getMediaResourceFile should extract the file or return None") {
    val path      = SBotPaths.projectRoot.resolve("media-resource-test")
    val actual1   = MediaResourceFile(Resource.pure[IO, Path](path)).getMediaResourceFile.sequence
    val expected1 = Some(path)

    val ifile: MediaSource.FileRef       = MediaSource.FileRef(Platform.Telegram, "ifile")
    val mediaResource: MediaResource[IO] = MediaResourceIFile(ifile)
    val actual2                          = mediaResource.getMediaResourceFile
    assertIO(actual1.use(_.pure[IO]), expected1) *>
      assertIO(actual2.pure[IO], None)
  }
}
