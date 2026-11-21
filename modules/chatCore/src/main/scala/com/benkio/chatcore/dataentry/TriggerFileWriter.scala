package com.benkio.chatcore.dataentry

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

/** Renders the bot trigger markdown file. Shared by the CLI generator and the replies editor so both write exactly the
  * same format without the editor depending on the `main` module.
  */
object TriggerFileWriter {

  val header: String = s"""| File Name ${" " * 54} | Trigger ${" " * 179}|
| ${"-" * 64} | ${"-" * 186} |"""

  def write(path: Path, triggers: List[ReplyBundleMessage]): IO[Unit] =
    IO.blocking {
      val lines = (header :: triggers.map(_.prettyPrint().stripLineEnd)).asJava
      Files.write(path, lines, StandardCharsets.UTF_8)
      ()
    }
}
