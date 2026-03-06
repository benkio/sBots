package com.benkio.replieseditor.server.server

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.replieseditor.server.store.BotStore
import io.circe.syntax.*
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class HttpAppBuilderSpec extends CatsEffectSuite {

  private def write(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.createDirectories(path.getParent)
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  private def tempRepo: Resource[IO, Path] =
    Resource.make(IO.blocking(Files.createTempDirectory("sbots-replies-editor-httpapp")))(p =>
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

  test("HttpAppBuilder wires endpoints together") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("OkBot")
      val botId       = "ok"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin
      val repliesContent = List(ReplyBundleMessage.textToText("hello")("a")).asJson.spaces2

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, repliesContent)
        store <- BotStore.build(root)
        app = HttpAppBuilder.build(ServerDeps(repoRoot = root, botStore = store))
        resp <- app.run(Request[IO](Method.GET, uri"/api/bots"))
        body <- resp.as[Json]
      } yield {
        assertEquals(resp.status, Status.Ok)
        assertEquals(body.asArray.map(_.size), Some(1))
      }
    }
  }
}
