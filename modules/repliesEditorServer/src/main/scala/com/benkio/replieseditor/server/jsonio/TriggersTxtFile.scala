package com.benkio.replieseditor.server.jsonio

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

object TriggersTxtFile {
  def write(path: Path, replies: List[ReplyBundleMessage]): IO[Unit] =
    IO.blocking {
      val lines = replies.map(_.prettyPrint().stripLineEnd).asJava
      Files.write(path, lines, StandardCharsets.UTF_8)
      ()
    }
}
