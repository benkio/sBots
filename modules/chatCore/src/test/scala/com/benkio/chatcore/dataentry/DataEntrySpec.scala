package com.benkio.chatcore.dataentry

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import io.circe.parser.parse
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.*

class DataEntrySpec extends CatsEffectSuite {

  private def write(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.createDirectories(path.getParent)
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  private def tempDir: Resource[IO, Path] =
    Resource.make(IO.blocking(Files.createTempDirectory("sbots-data-entry-test")))(p =>
      IO.blocking {
        def deleteRec(x: Path): Unit = {
          if Files.isDirectory(x) then {
            val stream = Files.list(x)
            try stream.forEach(deleteRec)
            finally stream.close()
          }
          Files.deleteIfExists(x)
          ()
        }
        deleteRec(p)
      }
    )

  test("DataEntry merges list + replies json and writes back") {
    tempDir.use { dir =>
      val listPath    = dir.resolve("rphjb_list.json")
      val repliesPath = dir.resolve("rphjb_replies.json")

      val initialList =
        """[
          |  { "filename": "rphjb_existing.mp3", "sources": ["http://example.com/rphjb_existing.mp3"] }
          |]""".stripMargin

      val initialReplies =
        List(ReplyBundleMessage.textToText("hello")("existing")).asJson.spaces2

      val config =
        SBotConfig(
          disableForward = true,
          ignoreMessagePrefix = Some("!"),
          messageTimeToLive = Some(5.seconds),
          sBotInfo = SBotInfo(SBotId("rphjb"), SBotName("RichardPHJBensonBot")),
          triggerFilename = "x",
          triggerListUri = uri"http://example.com",
          listJsonFilename = listPath.toString,
          showFilename = "x",
          repliesJsonFilename = repliesPath.toString,
          commandsJsonFilename = "x",
          token = "x"
        )

      val input = List(
        "https://example.com/rphjb_new_a.mp3?dl=0",
        "https://example.com/rphjb_new_b.mp3?dl=0"
      )

      for {
        _          <- write(listPath, initialList)
        _          <- write(repliesPath, initialReplies)
        _          <- DataEntry.dataEntryLogic(input, config)
        outListRaw <- IO.blocking(Files.readString(listPath, StandardCharsets.UTF_8))
        outRepRaw  <- IO.blocking(Files.readString(repliesPath, StandardCharsets.UTF_8))
        outListJ   <- IO.fromEither(parse(outListRaw).left.map(e => new RuntimeException(e.message)))
        outRepJ    <- IO.fromEither(parse(outRepRaw).left.map(e => new RuntimeException(e.message)))
      } yield {
        assertEquals(outListJ.asArray.map(_.size), Some(3))
        assertEquals(outRepJ.asArray.map(_.size), Some(3))
      }
    }
  }
}
