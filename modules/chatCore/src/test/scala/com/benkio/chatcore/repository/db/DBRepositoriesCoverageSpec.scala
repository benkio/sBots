package com.benkio.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.show.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.Logger.given
import doobie.implicits.*
import doobie.Transactor
import io.circe.syntax.*
import io.circe.Json
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

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

  private def setupSchema(xa: Transactor[IO]): IO[Unit] =
    (sql"""
      CREATE TABLE IF NOT EXISTS media (
        media_name TEXT PRIMARY KEY,
        bot_id TEXT NOT NULL,
        kinds TEXT NOT NULL,
        mime_type TEXT NOT NULL,
        media_sources TEXT NOT NULL,
        created_at TEXT NOT NULL,
        media_count INTEGER NOT NULL
      );
    """.update.run *>
      sql"""
      CREATE TABLE IF NOT EXISTS show (
        show_id TEXT PRIMARY KEY,
        bot_id TEXT NOT NULL,
        show_title TEXT NOT NULL,
        show_upload_date TEXT NOT NULL,
        show_duration INTEGER NOT NULL,
        show_description TEXT,
        show_is_live BOOLEAN NOT NULL,
        show_origin_automatic_caption TEXT
      );
    """.update.run *>
      sql"""
      CREATE TABLE IF NOT EXISTS timeout (
        chat_id INTEGER NOT NULL,
        bot_id TEXT NOT NULL,
        timeout_value TEXT NOT NULL,
        last_interaction TEXT NOT NULL,
        PRIMARY KEY (chat_id, bot_id)
      );
    """.update.run).transact(xa).void

  test("DBMedia insert/get/update paths execute") {
    tempDb.use { xa =>
      val botId = SBotId("bot")
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
      val botId = SBotId("bot")
      val row   =
        DBShowData(
          show_id = "s1",
          bot_id = botId.value,
          show_title = "Hello world",
          show_upload_date = "2020-01-01T00:00:00.000Z",
          show_duration = 123,
          show_description = Some("a description"),
          show_is_live = false,
          show_origin_automatic_caption = Some("caption hello")
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
        assert(post.isEmpty)
      }
    }
  }

  test("DBTimeout getOrDefault/set/remove/logLastInteraction paths execute") {
    tempDb.use { xa =>
      val botId  = SBotId("bot")
      val chatId = ChatId(1L)
      val impl   = new DBTimeout.DBTimeoutImpl[IO](xa, summon)
      val t0     = Timeout(chatId, botId)
      val row    = DBTimeoutData(t0)

      for {
        _  <- setupSchema(xa)
        d0 <- impl.getOrDefault(chatId.value, botId)
        _  <- impl.setTimeout(row)
        d1 <- impl.getOrDefault(chatId.value, botId)
        _  <- impl.logLastInteraction(chatId.value, botId)
        _  <- impl.removeTimeout(chatId.value, botId)
        d2 <- impl.getOrDefault(chatId.value, botId)
      } yield {
        assertEquals(d0.chat_id, chatId.value)
        assertEquals(d1.chat_id, chatId.value)
        assertEquals(d2.chat_id, chatId.value)
      }
    }
  }
}
