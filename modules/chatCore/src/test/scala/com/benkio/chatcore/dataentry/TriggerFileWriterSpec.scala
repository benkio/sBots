package com.benkio.chatcore.dataentry

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class TriggerFileWriterSpec extends CatsEffectSuite {

  private val headerLines: List[String] = TriggerFileWriter.header.linesIterator.toList

  private def tempFile: IO[Path] =
    IO.blocking(Files.createTempFile("trigger-writer-", ".md"))

  private def readLines(path: Path): IO[List[String]] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8).linesIterator.toList)

  test("write renders the shared header and a row per reply") {
    for {
      path    <- tempFile
      reply    = ReplyBundleMessage.textToText("hello")("world")
      _       <- TriggerFileWriter.write(path, List(reply))
      content <- readLines(path)
    } yield {
      assertEquals(content.take(headerLines.size), headerLines)
      val replyRows = content.drop(headerLines.size)
      assert(replyRows.nonEmpty, "expected at least one rendered reply row")
      assert(replyRows.exists(_.contains("hello")), s"trigger missing from rendered rows: $replyRows")
      assert(replyRows.forall(_.startsWith("| ")), s"unexpected rendered row: $replyRows")
    }
  }

  test("write overwrites an existing file with exactly the provided replies") {
    for {
      path <- tempFile
      _ <- IO.blocking(Files.writeString(path, "stale content\n", StandardCharsets.UTF_8))
      _    <- TriggerFileWriter.write(path, List.empty)
      content <- readLines(path)
    } yield assertEquals(content, headerLines)
  }
}
