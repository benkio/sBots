package com.benkio.replieseditor.server.jsonio

import cats.effect.IO
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class TriggersTxtFileSpec extends CatsEffectSuite {

  private def tempDir(): Path =
    Files.createTempDirectory("triggers-txt-file-test-").toAbsolutePath.normalize()

  test("write creates a non-empty triggers file") {
    val dir     = tempDir()
    val path    = dir.resolve("cala_triggers.txt")
    val replies =
      List(
        ReplyBundleMessage.textToMedia("hello")(PhotoFile("cala_ok.jpg"))
      )

    for {
      _   <- TriggersTxtFile.write(path, replies)
      raw <- IO.blocking(Files.readString(path, StandardCharsets.UTF_8))
    } yield assert(raw.trim.nonEmpty)
  }
}
