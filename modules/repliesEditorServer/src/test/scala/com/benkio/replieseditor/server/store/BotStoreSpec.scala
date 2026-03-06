package com.benkio.replieseditor.server.store

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import io.circe.parser.parse
import io.circe.syntax.*
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class BotStoreSpec extends CatsEffectSuite {

  private def write(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.createDirectories(path.getParent)
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  private def tempRepo: Resource[IO, Path] =
    Resource.make(IO.blocking(Files.createTempDirectory("sbots-replies-editor-test")))(p =>
      IO.blocking {
        // best-effort cleanup
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

  test("BotStore getRepliesChunk slices items with correct indexes") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """
          |[
          |  { "filename": "test_a.mp3", "sources": ["http://example.com/test_a.mp3"] }
          |]
          |""".stripMargin

      val replies =
        List(
          ReplyBundleMessage.textToText("hello")("a"),
          ReplyBundleMessage.textToText("bye")("b"),
          ReplyBundleMessage.textToText("other")("c")
        )

      val repliesContent = replies.asJson.spaces2

      for {
        _      <- write(listJson, listContent)
        _      <- write(repliesJson, repliesContent)
        store  <- BotStore.build(root)
        chunkE <- store.getRepliesChunk(botId, offset = 1, limit = 1)
        chunk  <- IO.fromEither(chunkE.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(chunk.total, 3)
        assertEquals(chunk.offset, 1)
        assertEquals(chunk.items.length, 1)
        assertEquals(chunk.items.head.index, 1)
      }
    }
  }

  test("BotStore getFilteredRepliesChunk matches using trigger logic") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """
          |[
          |  { "filename": "test_a.mp3", "sources": ["http://example.com/test_a.mp3"] }
          |]
          |""".stripMargin

      val containsOnce = ReplyBundleMessage.textToText("hello")("a")
      val containsAll  = ReplyBundleMessage.textToText("foo", "bar")("b").copy(matcher = MessageMatches.ContainsAll)

      val repliesContent = List(containsOnce, containsAll).asJson.spaces2

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, repliesContent)
        store <- BotStore.build(root)
        // "hello there" should match first
        c1E <- store.getFilteredRepliesChunk(botId, message = "hello there", offset = 0, limit = 50)
        c1  <- IO.fromEither(c1E.left.map(e => new RuntimeException(e.error)))
        // "foo xxx bar" should match second because ContainsAll
        c2E <- store.getFilteredRepliesChunk(botId, message = "foo xxx bar", offset = 0, limit = 50)
        c2  <- IO.fromEither(c2E.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(c1.total, 1)
        assertEquals(c1.items.map(_.index), Vector(0))

        assertEquals(c2.total, 1)
        assertEquals(c2.items.map(_.index), Vector(1))
      }
    }
  }

  test("BotStore saveReplies trims triggers before writing replies json") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """
          |[
          |  { "filename": "test_a.mp3", "sources": ["http://example.com/test_a.mp3"] }
          |]
          |""".stripMargin

      val initialReplies = List(ReplyBundleMessage.textToText("hello")("a"))

      val padded =
        ReplyBundleMessage(
          trigger = TextTrigger(
            StringTextTriggerValue("  hello  "),
            RegexTextTriggerValue("  hi.*  ".r, "  hi.*  ".length)
          ),
          reply = initialReplies.head.reply,
          matcher = MessageMatches.ContainsOnce
        )

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, initialReplies.asJson.spaces2)
        store <- BotStore.build(root)
        saveE <- store.saveReplies(botId, List(padded))
        _     <- IO.fromEither(saveE.left.map(e => new RuntimeException(e.error))).void
        raw   <- IO.blocking(Files.readString(repliesJson, StandardCharsets.UTF_8))
        json  <- IO.fromEither(parse(raw).left.map(e => new RuntimeException(e.message)))
        rs    <- IO.fromEither(json.as[List[ReplyBundleMessage]].left.map(df => new RuntimeException(df.message)))
      } yield {
        val rbm = rs.head
        rbm.trigger match {
          case TextTrigger(tvs*) =>
            val stringTriggers = tvs.toList.collect { case StringTextTriggerValue(t) => t }
            val regexTriggers  = tvs.toList.collect { case RegexTextTriggerValue(r, _) => r.toString }

            assert(stringTriggers.contains("hello"))
            assert(regexTriggers.contains("hi.*"))
          case other =>
            fail(s"Expected TextTrigger, got: $other")
        }
      }
    }
  }
}
