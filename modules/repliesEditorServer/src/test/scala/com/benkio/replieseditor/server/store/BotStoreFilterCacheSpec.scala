package com.benkio.replieseditor.server.store

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.either.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import io.circe.syntax.*
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** The filter cache must never change what the editor shows: these tests exercise every path that replaces the cached
  * decoded replies.
  */
class BotStoreFilterCacheSpec extends CatsEffectSuite {

  private val botId = "test"

  private def tempRepo: Resource[IO, Path] =
    Resource.make(IO.blocking(Files.createTempDirectory("sbots-replies-editor-filter-cache"))) { root =>
      IO.blocking {
        def deleteRec(path: Path): Unit = {
          if Files.isDirectory(path) then {
            val stream = Files.list(path)
            try stream.forEach(deleteRec)
            finally stream.close()
          }
          Files.deleteIfExists(path)
          ()
        }

        deleteRec(root)
      }
    }

  private def prepare(root: Path, replies: List[ReplyBundleMessage]): IO[BotStore] = {
    val botDir      = root.resolve("modules").resolve("bots").resolve("TestBot")
    val listJson    = botDir.resolve(s"${botId}_list.json")
    val repliesJson = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")
    IO.blocking {
      Files.createDirectories(repliesJson.getParent)
      Files.writeString(
        listJson,
        """[ { "filename": "a.mp3", "sources": ["http://example.com/a.mp3"] } ]""",
        StandardCharsets.UTF_8
      )
      Files.writeString(repliesJson, replies.asJson.spaces2, StandardCharsets.UTF_8)
    } *> BotStore.build(root)
  }

  private def matchingIndexes(store: BotStore, message: String): IO[Vector[Int]] =
    store
      .getFilteredRepliesChunk(botId, message = message, offset = 0, limit = 50)
      .flatMap(_.leftMap(err => new RuntimeException(err.error)).liftTo[IO])
      .map(_.items.map(_.index))

  test("getFilteredRepliesChunk reflects updateReplyAt after the cache is populated") {
    tempRepo.use { root =>
      for {
        store <- prepare(root, List(ReplyBundleMessage.textToText("hello")("a")))
        _     <- matchingIndexes(store, "hello")
        _     <- store
          .updateReplyAt(botId, 0, ReplyBundleMessage.textToText("updated")("a").asJson)
          .flatMap(_.leftMap(err => new RuntimeException(err.error)).liftTo[IO])
        before <- matchingIndexes(store, "hello")
        after  <- matchingIndexes(store, "updated")
      } yield {
        assertEquals(before, Vector.empty)
        assertEquals(after, Vector(0))
      }
    }
  }

  test("getFilteredRepliesChunk reflects insertAt and deleteAt after the cache is populated") {
    tempRepo.use { root =>
      for {
        store    <- prepare(root, List(ReplyBundleMessage.textToText("hello")("a")))
        _        <- matchingIndexes(store, "hello")
        _        <- store.insertAt(botId, 0, ReplyBundleMessage.textToText("inserted")("b").asJson)
        inserted <- matchingIndexes(store, "inserted")
        _        <- store.deleteAt(botId, 0)
        deleted  <- matchingIndexes(store, "inserted")
      } yield {
        assertEquals(inserted, Vector(0))
        assertEquals(deleted, Vector.empty)
      }
    }
  }

  test("getFilteredRepliesChunk returns commit-trimmed triggers after the cache is populated") {
    tempRepo.use { root =>
      val padded = ReplyBundleMessage
        .textToText("hello")("a")
        .copy(
          trigger = TextTrigger(StringTextTriggerValue("  hello  "), RegexTextTriggerValue("  hi.*  ".r, 8))
        )
      for {
        store <- prepare(root, List(padded))
        matchingEntry = store
          .getFilteredRepliesChunk(botId, message = "xx  hello  yy", offset = 0, limit = 50)
          .flatMap(_.leftMap(err => new RuntimeException(err.error)).liftTo[IO])
          .map(_.items.head.value.noSpaces)
        before <- matchingEntry
        _      <- store.commit(botId).flatMap(_.leftMap(err => new RuntimeException(err.error)).liftTo[IO])
        after  <- matchingEntry
      } yield {
        assert(before.contains("  hello  "))
        assert(!after.contains("  hello  "))
        assert(after.contains("hello"))
      }
    }
  }

  test("getFilteredRepliesChunk reflects a disk reload after the cache is populated") {
    tempRepo.use { root =>
      val repliesJson =
        root
          .resolve("modules")
          .resolve("bots")
          .resolve("TestBot")
          .resolve("src")
          .resolve("main")
          .resolve("resources")
          .resolve(s"${botId}_replies.json")
      for {
        store <- prepare(root, List(ReplyBundleMessage.textToText("hello")("a")))
        _     <- matchingIndexes(store, "hello")
        _     <- IO.blocking(
          Files.writeString(
            repliesJson,
            List(ReplyBundleMessage.textToText("reloaded")("a")).asJson.spaces2,
            StandardCharsets.UTF_8
          )
        )
        _      <- store.reloadBotFromDisk(botId).flatMap(_.leftMap(err => new RuntimeException(err.error)).liftTo[IO])
        before <- matchingIndexes(store, "hello")
        after  <- matchingIndexes(store, "reloaded")
      } yield {
        assertEquals(before, Vector.empty)
        assertEquals(after, Vector(0))
      }
    }
  }
}
