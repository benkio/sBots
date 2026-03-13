package com.benkio.chattelegramadapter.adapters.telegram

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chattelegramadapter.conversions.MediaResourceConversions.*
import munit.*
import telegramium.bots.InputLinkFile
import telegramium.bots.InputPartFile

import java.nio.file.Path
import java.nio.file.Paths

class MediaResourceConversionsSpec extends CatsEffectSuite {
  test("toTelegramApi should return the expected Telegram Type") {
    val path      = Paths.get(".")
    val actual1   = MediaResourceFile(Resource.pure[IO, Path](path)).toTelegramApi
    val expected1 = InputPartFile(path.toFile())
    // --------------------------------------------------
    val ifile                            = "ifile"
    val mediaResource: MediaResource[IO] = MediaResourceIFile(ifile)
    val actual2                          = mediaResource.toTelegramApi
    val expected2                        = InputLinkFile(ifile)
    actual1.use(assertEquals(_, expected1).pure[IO]) *>
      actual2.use(assertEquals(_, expected2).pure[IO])
  }
}
