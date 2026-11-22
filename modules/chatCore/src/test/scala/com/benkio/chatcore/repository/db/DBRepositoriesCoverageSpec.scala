package com.benkio.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SubscriptionDraft
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.paths.SBotPaths
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.SubscriptionDraftStartResult
import com.benkio.chatcore.TestIds
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.Transactor
import io.circe.syntax.*
import io.circe.Json
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import scala.concurrent.duration.*
import scala.util.Using

class DBRepositoriesCoverageSpec extends CatsEffectSuite {

  private def tempDb: Resource[IO, Transactor[IO]] =
    Resource
      .make(IO.blocking {
        val dir = Files.createTempDirectory("sbots-db-test")
        val db  = dir.resolve("test.sqlite")
        (dir, db)
      }) { case (dir, _) =>
        IO.blocking {
          def deleteRec(x: Path): Unit = {
            if Files.isDirectory(x) then {
              val s = Files.list(x)
              try s.forEach(deleteRec)
              finally s.close()
            }
            Files.deleteIfExists(x)
            ()
          }
          deleteRec(dir)
        }
      }
      .map { case (_, db) =>
        Transactor.fromDriverManager[IO](
          "org.sqlite.JDBC",
          s"jdbc:sqlite:${db.toAbsolutePath.toString}",
          None
        )
      }

  private val migrationsDir: Path =
    SBotPaths.repoRoot
      .resolve("modules")
      .resolve("botDB")
      .resolve("src")
      .resolve("main")
      .resolve("resources")
      .resolve("db")
      .resolve("migrations")

  private def sqlStatements(fileContent: String): List[String] =
    fileContent.linesIterator
      .map(_.trim)
      .filterNot(_.startsWith("--"))
      .mkString("\n")
      .split(';')
      .toList
      .map(_.trim)
      .filter(_.nonEmpty)

  private def migrationFiles: IO[List[Path]] =
    IO.blocking {
      Using.resource(Files.list(migrationsDir)) { stream =>
        stream
          .filter(path => path.getFileName.toString.endsWith(".sql"))
          .toArray
          .map(_.asInstanceOf[Path])
          .toList
          .sortBy(_.getFileName.toString.stripPrefix("V").takeWhile(_.isDigit).toInt)
      }
    }

  private def setupSchema(xa: Transactor[IO]): IO[Unit] =
    for {
      paths <- migrationFiles
      _     <- paths.traverse_(path =>
        for {
          content <- IO.blocking(Files.readString(path))
          _ <- sqlStatements(content).traverse_(statement => Fragment.const(statement).update.run.transact(xa).void)
        } yield ()
      )
    } yield ()

  test("DBMedia insert/get/update paths execute") {
    tempDb.use { xa =>
      val botId = SBotId("test")
      val media =
        DBMediaData(
          media_name = "bot_test.mp3",
          bot_id = botId.value,
          kinds = List("k1", "k2").asJson.noSpaces,
          mime_type = "audio/mpeg",
          media_sources = List(Json.fromString("http://example.com/a.mp3")).asJson.noSpaces,
          media_count = 0,
          created_at = Instant.EPOCH.toString
        )

      for {
        _  <- setupSchema(xa)
        db <- DBMedia[IO](xa)
        _  <- db.insertMedia(media)
        // conflict path (UPDATE)
        _      <- db.insertMedia(media.copy(kinds = List("k1").asJson.noSpaces))
        m1     <- db.getMedia("bot_test.mp3")
        _      <- db.incrementMediaCount("bot_test.mp3")
        _      <- db.decrementMediaCount("bot_test.mp3")
        byKind <- db.getMediaByKind(kind = "k1", botId = botId, cache = false)
        top    <- db.getMediaByMediaCount(limit = 10, botId = Some(botId))
        rnd    <- db.getRandomMedia(botId)
      } yield {
        assert(m1.isDefined)
        assert(byKind.nonEmpty)
        assert(top.nonEmpty)
        assert(rnd.isDefined)
      }
    }
  }

  test("DBShow insert/query paths execute") {
    tempDb.use { xa =>
      val botId = SBotId("test")
      val row   =
        DBShowData(
          show_id = "s1",
          bot_id = botId.value,
          show_title = "Hello world",
          show_upload_date = "2020-01-01T00:00:00.000Z",
          show_duration = 123,
          show_description = Some("a description"),
          show_is_live = false,
          show_origin_automatic_caption = Some("caption hello"),
          show_origin_automatic_caption_srt = """{"00:00:00,000":"caption hello"}"""
        )

      for {
        _ <- setupSchema(xa)
        db = DBShow[IO](xa)
        _ <- db.insertShow(row)
        // conflict path (UPDATE)
        _   <- db.insertShow(row.copy(show_title = "Hello updated"))
        all <- db.getShows(botId)
        rnd <- db.getRandomShow(botId)
        q1  <- db.getShowBySimpleShowQuery(SimpleShowQuery("hello", "", ""), botId)
        q2  <- db.getShowByShowQuery(ShowQueryKeyword(Some(List("updated")), None, None, None, None, None, None), botId)
        _   <- db.deleteShow(row)
        post <- db.getShows(botId)
      } yield {
        assert(all.nonEmpty)
        assert(rnd.isDefined)
        assert(q1.nonEmpty)
        assert(q2.nonEmpty)
        assert(post.forall(_.show_id != row.show_id))
      }
    }
  }

  test("DBTimeout logLastInteraction failure leaves the stored timeout unchanged") {
    tempDb.use { xa =>
      val botId  = SBotId("test")
      val chatId = TestIds.chatId(1L)
      val impl   = new DBTimeout.DBTimeoutImpl[IO](xa, summon)
      val row    = DBTimeoutData(Timeout(chatId, botId)).copy(timeout_value = "15000", last_interaction = "100")
      for {
        _ <- setupSchema(xa)
        _ <- impl.setTimeout(row)
        // Force the UPDATE to fail: the old fallback replaced the row with a zero-timeout one here.
        _ <- Fragment
          .const(
            "CREATE TRIGGER fail_timeout_update BEFORE UPDATE ON timeout BEGIN SELECT RAISE(ABORT, 'forced failure'); END"
          )
          .update
          .run
          .transact(xa)
          .void
        failed <- impl.logLastInteraction(chatId, botId).attempt
        after  <- impl.getOrDefault(chatId, botId)
      } yield {
        assert(failed.isLeft)
        assertEquals(after, row)
      }
    }
  }

  test("DBTimeout getOrDefault/set/remove/logLastInteraction paths execute") {
    tempDb.use { xa =>
      val botId  = SBotId("test")
      val chatId = TestIds.chatId(1L)
      val impl   = new DBTimeout.DBTimeoutImpl[IO](xa, summon)
      val t0     = Timeout(chatId, botId)
      val row    = DBTimeoutData(t0)

      for {
        _  <- setupSchema(xa)
        d0 <- impl.getOrDefault(chatId, botId)
        _  <- impl.setTimeout(row)
        d1 <- impl.getOrDefault(chatId, botId)
        _  <- impl.logLastInteraction(chatId, botId)
        _  <- impl.removeTimeout(chatId, botId)
        d2 <- impl.getOrDefault(chatId, botId)
      } yield {
        assertEquals(d0.chat_id, chatId.id)
        assertEquals(d1.chat_id, chatId.id)
        assertEquals(d2.chat_id, chatId.id)
      }
    }
  }

  private def draft(key: SubscriptionFlowKey, savedAt: scala.concurrent.duration.FiniteDuration): SubscriptionDraft =
    SubscriptionDraft(
      key = key,
      step = SubscriptionDraftStep.Type,
      subscriptionType = Some(SubscriptionType.Show),
      schedule = None,
      target = None,
      savedAt = savedAt
    )

  test("DBSubscriptionDraft persists, scopes by bot, enforces bounds and expiry") {
    tempDb.use { xa =>
      val botA = SBotId("botA")
      val botB = SBotId("botB")
      val chat = ChatId(Platform.Telegram, "1")
      val keyA = SubscriptionFlowKey(chat, botA, "u1")
      val keyB = SubscriptionFlowKey(chat, botB, "u1")
      for {
        _     <- setupSchema(xa)
        now   <- IO.realTime
        dbA   <- IO.pure(DBSubscriptionDraft[IO](xa, botA, 10.minutes, 1))
        dbB   <- IO.pure(DBSubscriptionDraft[IO](xa, botB, 10.minutes, 1))
        r1    <- dbA.start(draft(keyA, now))
        r2    <- dbA.start(draft(SubscriptionFlowKey(chat, botA, "u2"), now + 1.second))
        r3    <- dbB.start(draft(keyB, now))
        gotA  <- dbA.get(keyA)
        gotB  <- dbB.get(keyB)
        sizeA <- dbA.size
        sizeB <- dbB.size
        // restart / new instance over same DB
        dbA2  <- IO.pure(DBSubscriptionDraft[IO](xa, botA, 10.minutes, 1))
        gotA2 <- dbA2.get(keyA)
        _     <- dbA.remove(keyA)
        gone  <- dbA.get(keyA)
      } yield {
        assertEquals(r1, SubscriptionDraftStartResult.Started)
        assertEquals(r2, SubscriptionDraftStartResult.Full)
        assertEquals(r3, SubscriptionDraftStartResult.Started)
        assert(gotA.isDefined)
        assert(gotB.isDefined)
        assertEquals(sizeA, 1)
        assertEquals(sizeB, 1)
        assertEquals(gotA2.map(_.key), Some(keyA))
        assertEquals(gone, None)
      }
    }
  }

  test("DBSubscriptionDraft expires by real-time TTL") {
    tempDb.use { xa =>
      val botId = SBotId("botA")
      val key   = SubscriptionFlowKey(ChatId(Platform.Telegram, "1"), botId, "u1")
      for {
        _      <- setupSchema(xa)
        now    <- IO.realTime
        db     <- IO.pure(DBSubscriptionDraft[IO](xa, botId, 10.minutes, 10))
        _      <- db.start(draft(key, now))
        before <- db.get(key)
        _      <- sql"""UPDATE subscription_draft SET saved_at_millis = 0 WHERE bot_id = ${botId.value}""".update.run
          .transact(xa)
        after <- db.get(key)
      } yield {
        assert(before.isDefined)
        assertEquals(after, None)
      }
    }
  }

  test("DBSubscriptionFlowPrompt persists, scopes by bot, and message lookup is deterministic") {
    tempDb.use { xa =>
      val botA = SBotId("botA")
      val botB = SBotId("botB")
      val chat = ChatId(Platform.Telegram, "1")
      val keyA = SubscriptionFlowKey(chat, botA, "u1")
      val keyB = SubscriptionFlowKey(chat, botB, "u1")
      for {
        _    <- setupSchema(xa)
        dbA  <- IO.pure(DBSubscriptionFlowPrompt[IO](xa, botA, 10.minutes, 10))
        dbB  <- IO.pure(DBSubscriptionFlowPrompt[IO](xa, botB, 10.minutes, 10))
        _    <- dbA.put(keyA, "42", "u1")
        _    <- dbB.put(keyB, "43", "u1")
        byA  <- dbA.byKey(keyA)
        byB  <- dbB.byKey(keyB)
        msgA <- dbA.byMessageId(chat, "42")
        msgB <- dbB.byMessageId(chat, "43")
        _    <- dbA.touch(keyA)
        _    <- dbA.remove(keyA)
        gone <- dbA.byKey(keyA)
      } yield {
        assertEquals(byA.map(_.messageId), Some("42"))
        assertEquals(byB.map(_.messageId), Some("43"))
        assertEquals(msgA.map(_.key), Some(keyA))
        assertEquals(msgB.map(_.key), Some(keyB))
        assertEquals(gone, None)
      }
    }
  }

  test("DBPagedListText persists across instances, scopes by bot, and enforces expiry/bounds") {
    tempDb.use { xa =>
      val botA  = SBotId("botA")
      val botB  = SBotId("botB")
      val chat  = ChatId(Platform.Telegram, "1")
      val req   = Message(None, 0L, chat, "private", text = Some("!toptwenty"))
      val items = List(PagedItem.ValueItem(Text("item 1")))
      for {
        _    <- setupSchema(xa)
        dbA  <- IO.pure(DBPagedListText[IO](xa, botA, 10.minutes, 2))
        dbB  <- IO.pure(DBPagedListText[IO](xa, botB, 10.minutes, 2))
        now  <- IO.realTime
        _    <- dbA.save(chat, CommandKey.TopTwenty, 0, items, req, now)
        _    <- dbB.save(chat, CommandKey.TopTwenty, 1, items, req, now)
        gotA <- dbA.get(chat, now)
        gotB <- dbB.get(chat, now)
        // restart/new instance over same DB
        dbA2  <- IO.pure(DBPagedListText[IO](xa, botA, 10.minutes, 2))
        gotA2 <- dbA2.get(chat, now)
        exp   <- dbA.isExpired(chat, now + 11.minutes)
        gone  <- dbA.get(chat, now + 11.minutes)
      } yield {
        assertEquals(gotA.map(_.page), Some(0))
        assertEquals(gotB.map(_.page), Some(1))
        assertEquals(gotA2.map(_.page), Some(0))
        assertEquals(exp, true)
        assertEquals(gone, None)
      }
    }
  }
}
