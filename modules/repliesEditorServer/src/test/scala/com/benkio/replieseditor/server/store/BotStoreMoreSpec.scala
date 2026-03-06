package com.benkio.replieseditor.server.store

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import io.circe.parser.parse
import io.circe.syntax.*
import io.circe.Json
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class BotStoreMoreSpec extends CatsEffectSuite {

  private def write(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.createDirectories(path.getParent)
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  private def tempRepo: Resource[IO, Path] =
    Resource.make(IO.blocking(Files.createTempDirectory("sbots-replies-editor-test-more")))(p =>
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

  test("BotStore.listBots filters botName not ending with Bot") {
    tempRepo.use { root =>
      val okDir    = root.resolve("modules").resolve("bots").resolve("OkBot")
      val nopeDir  = root.resolve("modules").resolve("bots").resolve("Nope")
      val okId     = "ok"
      val nopeId   = "nope"
      val okList   = okDir.resolve(s"${okId}_list.json")
      val okReply  = okDir.resolve("src").resolve("main").resolve("resources").resolve(s"${okId}_replies.json")
      val nopeList = nopeDir.resolve(s"${nopeId}_list.json")
      val nopeRep  = nopeDir.resolve("src").resolve("main").resolve("resources").resolve(s"${nopeId}_replies.json")

      val listContent =
        """[
          |  { "filename": "x.mp3", "sources": ["http://example.com/x.mp3"] }
          |]""".stripMargin
      val repliesContent = List(ReplyBundleMessage.textToText("hello")("a")).asJson.spaces2

      for {
        _     <- write(okList, listContent)
        _     <- write(okReply, repliesContent)
        _     <- write(nopeList, listContent)
        _     <- write(nopeRep, repliesContent)
        store <- BotStore.build(root)
        bots  <- store.listBots
      } yield {
        assertEquals(bots.map(_.botId), Vector(okId))
        assertEquals(bots.map(_.botName), Vector("OkBot"))
      }
    }
  }

  test("BotStore.reloadBotFromDisk refreshes cached replies") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val r1 = List(ReplyBundleMessage.textToText("hello")("a"))
      val r2 = List(
        ReplyBundleMessage.textToText("hello")("a"),
        ReplyBundleMessage.textToText("bye")("b")
      )

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, r1.asJson.spaces2)
        store <- BotStore.build(root)
        c1E   <- store.getRepliesChunk(botId, offset = 0, limit = 50)
        c1    <- IO.fromEither(c1E.left.map(e => new RuntimeException(e.error)))
        _     <- write(repliesJson, r2.asJson.spaces2) // update on disk
        _     <- store
          .reloadBotFromDisk(botId)
          .flatMap(e => IO.fromEither(e.left.map(err => new RuntimeException(err.error))))
        c2E <- store.getRepliesChunk(botId, offset = 0, limit = 50)
        c2  <- IO.fromEither(c2E.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(c1.total, 1)
        assertEquals(c2.total, 2)
      }
    }
  }

  test("BotStore.updateReplyAt validates bounds") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val replies = List(ReplyBundleMessage.textToText("hello")("a"))

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, replies.asJson.spaces2)
        store <- BotStore.build(root)
        badE  <- store.updateReplyAt(botId, index = 99, value = Json.obj("x" -> Json.fromInt(1)))
        _ = assert(badE.isLeft)
        okE  <- store.updateReplyAt(botId, index = 0, value = Json.obj("updated" -> Json.fromBoolean(true)))
        _    <- IO.fromEither(okE.left.map(e => new RuntimeException(e.error)))
        repE <- store.getReplies(botId)
        rep  <- IO.fromEither(repE.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(rep.asArray.map(_.length), Some(1))
        assertEquals(
          rep.asArray.flatMap(_.headOption).flatMap(_.hcursor.downField("updated").as[Boolean].toOption),
          Some(true)
        )
      }
    }
  }

  test("BotStore insertAt and deleteAt update totals") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val replies = List(ReplyBundleMessage.textToText("hello")("a"))

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, replies.asJson.spaces2)
        store <- BotStore.build(root)
        insE  <- store.insertAt(botId, index = 0, value = Json.obj("x" -> Json.fromInt(1)))
        n1    <- IO.fromEither(insE.left.map(e => new RuntimeException(e.error)))
        delE  <- store.deleteAt(botId, index = 0)
        n2    <- IO.fromEither(delE.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(n1, 2)
        assertEquals(n2, 1)
      }
    }
  }

  test("BotStore reports load errors for invalid json files") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      for {
        _        <- write(listJson, "{ not json")
        _        <- write(repliesJson, "{ not json")
        store    <- BotStore.build(root)
        allowedE <- store.getAllowedFiles(botId)
        repliesE <- store.getReplies(botId)
      } yield {
        assert(allowedE.left.exists(_.error.contains("Failed to load allowed files")))
        assert(repliesE.left.exists(_.error.contains("Failed to load replies")))
      }
    }
  }

  test("BotStore commit returns an error when botId is unknown") {
    tempRepo.use { root =>
      for {
        store <- BotStore.build(root)
        res   <- store.commit("missing")
      } yield assert(res.isLeft)
    }
  }

  test("BotStore commit returns an error when replies are not an array") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, """{ "not": "an array" }""")
        store <- BotStore.build(root)
        res   <- store.commit(botId)
      } yield assert(res.left.exists(_.error.contains("Cannot commit, replies not loaded")))
    }
  }

  test("BotStore commit returns an error when allowed files are not loaded") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val replies = List(ReplyBundleMessage.textToText("hello")("a"))

      for {
        _     <- write(listJson, "{ not json")
        _     <- write(repliesJson, replies.asJson.spaces2)
        store <- BotStore.build(root)
        res   <- store.commit(botId)
      } yield assert(res.left.exists(_.error.contains("Cannot commit, allowed files not loaded")))
    }
  }

  test("BotStore commit trims triggers and writes both replies json and triggers txt") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")
      val triggersTxt = botDir.resolve(s"${botId}_triggers.txt")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val padded =
        ReplyBundleMessage
          .textToText("hello")("a")
          .copy(
            matcher = MessageMatches.ContainsOnce,
            trigger = TextTrigger(
              StringTextTriggerValue("  hello  "),
              RegexTextTriggerValue("  hi.*  ".r, "  hi.*  ".length)
            )
          )

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, List(padded).asJson.spaces2)
        store <- BotStore.build(root)
        okE   <- store.commit(botId)
        _     <- IO.fromEither(okE.left.map(e => new RuntimeException(e.error))).void
        rawR  <- IO.blocking(Files.readString(repliesJson, StandardCharsets.UTF_8))
        jsonR <- IO.fromEither(parse(rawR).left.map(e => new RuntimeException(e.message)))
        rs    <- IO.fromEither(jsonR.as[List[ReplyBundleMessage]].left.map(df => new RuntimeException(df.message)))
        rawT  <- IO.blocking(Files.readString(triggersTxt, StandardCharsets.UTF_8))
      } yield {
        rs.head.trigger match {
          case TextTrigger(tvs*) =>
            val stringTriggers = tvs.toList.collect { case StringTextTriggerValue(t) => t }
            val regexTriggers  = tvs.toList.collect { case RegexTextTriggerValue(r, _) => r.toString }
            assertEquals(stringTriggers, List("hello"))
            assertEquals(regexTriggers, List("hi.*"))
          case other =>
            fail(s"Expected TextTrigger, got: $other")
        }

        assert(rawT.contains("hello"))
        assert(rawT.contains("hi.*"))
        assert(!rawT.contains("  hello"))
        assert(!rawT.contains("  hi.*"))
      }
    }
  }

  test("BotStore getFilteredRepliesChunk handles MessageLengthTrigger and NewMemberTrigger") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val lenTrigger = ReplyBundleMessage.textToText("len")("a").copy(trigger = MessageLengthTrigger(5))
      val newMember  = ReplyBundleMessage.textToText("new")("b").copy(trigger = NewMemberTrigger)

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, List(lenTrigger, newMember).asJson.spaces2)
        store <- BotStore.build(root)
        c1E   <- store.getFilteredRepliesChunk(botId, message = "12345", offset = 0, limit = 50)
        c1    <- IO.fromEither(c1E.left.map(e => new RuntimeException(e.error)))
        c2E   <- store.getFilteredRepliesChunk(botId, message = "1234", offset = 0, limit = 50)
        c2    <- IO.fromEither(c2E.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(c1.total, 1)
        assertEquals(c1.items.map(_.index), Vector(0))

        assertEquals(c2.total, 0)
      }
    }
  }

  test("BotStore getAllowedFiles returns filenames from list json") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] },
          |  { "filename": "b.gif", "sources": ["http://example.com/b.gif"] }
          |]""".stripMargin

      val replies = List(ReplyBundleMessage.textToText("hello")("a"))

      for {
        _      <- write(listJson, listContent)
        _      <- write(repliesJson, replies.asJson.spaces2)
        store  <- BotStore.build(root)
        filesE <- store.getAllowedFiles(botId)
        files  <- IO.fromEither(filesE.left.map(e => new RuntimeException(e.error)))
      } yield {
        assertEquals(files, Vector("a.mp3", "b.gif"))
      }
    }
  }

  test("BotStore deleteAt validates bounds and does not mutate on error") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val replies = List(
        ReplyBundleMessage.textToText("hello")("a"),
        ReplyBundleMessage.textToText("bye")("b")
      )

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, replies.asJson.spaces2)
        store <- BotStore.build(root)
        bad1  <- store.deleteAt(botId, index = -1)
        bad2  <- store.deleteAt(botId, index = 99)
        cE    <- store.getRepliesChunk(botId, offset = 0, limit = 50)
        c     <- IO.fromEither(cE.left.map(e => new RuntimeException(e.error)))
      } yield {
        assert(bad1.isLeft)
        assert(bad2.isLeft)
        assertEquals(c.total, 2)
      }
    }
  }

  test("BotStore insertAt clamps index to [0, size]") {
    tempRepo.use { root =>
      val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
      val botId       = "test"
      val listJson    = botDir.resolve(s"${botId}_list.json")
      val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")

      val listContent =
        """[
          |  { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] }
          |]""".stripMargin

      val replies = List(ReplyBundleMessage.textToText("hello")("a"))

      for {
        _     <- write(listJson, listContent)
        _     <- write(repliesJson, replies.asJson.spaces2)
        store <- BotStore.build(root)
        _     <- store.insertAt(botId, index = -10, value = Json.obj("x" -> Json.fromInt(1)))
        _     <- store.insertAt(botId, index = 999, value = Json.obj("y" -> Json.fromInt(2)))
        repE  <- store.getReplies(botId)
        rep   <- IO.fromEither(repE.left.map(e => new RuntimeException(e.error)))
      } yield {
        val arr = rep.asArray.getOrElse(Vector.empty)
        assertEquals(arr.length, 3)
        assertEquals(arr.head.hcursor.downField("x").as[Int].toOption, Some(1))
        assertEquals(arr.last.hcursor.downField("y").as[Int].toOption, Some(2))
      }
    }
  }
}
