package com.benkio.chatllm.repository.db

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.paths.SBotPaths
import com.benkio.chatcore.Logger.given
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.Transactor
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import scala.util.Using

/** Exercises the doobie repositories of #1487 against the real Flyway migration files: the tables, the keys and the
  * port semantics must agree with what the migration creates.
  */
class LlmSettingsDBSpec extends CatsEffectSuite {

  private val botA      = SBotId("bot-a")
  private val botB      = SBotId("bot-b")
  private val chat      = ChatId(Platform.Telegram, "-100123")
  private val otherChat = ChatId(Platform.Irc, "#sbots")

  private def tempDb: Resource[IO, Transactor[IO]] =
    Resource
      .make(IO.blocking {
        val dir = Files.createTempDirectory("sbots-llm-db-test")
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

  private def setupSchema(xa: Transactor[IO]): IO[Unit] =
    IO.blocking {
      Using.resource(Files.list(migrationsDir)) { stream =>
        stream
          .filter(path => path.getFileName.toString.endsWith(".sql"))
          .toArray
          .map(_.asInstanceOf[Path])
          .toList
          .sortBy(_.getFileName.toString.stripPrefix("V").takeWhile(_.isDigit).toInt)
      }
    }.flatMap(_.traverse_ { path =>
      for {
        content <- IO.blocking(Files.readString(path))
        _       <- sqlStatements(content).traverse_(statement => Fragment.const(statement).update.run.transact(xa).void)
      } yield ()
    })

  test("settings are off by default, toggled per chat and bot, and forget only purges that chat's context") {
    tempDb.use { xa =>
      for {
        _ <- setupSchema(xa)
        settingsA = LlmSettingsDB[IO](xa, botA)
        settingsB = LlmSettingsDB[IO](xa, botB)
        store     = LlmContextStore[IO](xa)
        initial           <- settingsA.status(chat)
        _                 <- settingsA.enable(chat)
        enabled           <- settingsA.status(chat)
        otherBotStatus    <- settingsB.status(chat)
        otherChatStatus   <- settingsA.status(otherChat)
        _                 <- store.append(chat, botA, LlmContextRole.User, "ciao")
        _                 <- store.append(chat, botA, LlmContextRole.Assistant, "buongiorno")
        _                 <- store.append(chat, botB, LlmContextRole.User, "other bot")
        _                 <- store.append(otherChat, botA, LlmContextRole.User, "other chat")
        before            <- store.all(chat, botA)
        _                 <- settingsA.forget(chat)
        after             <- store.all(chat, botA)
        otherBotContext   <- store.all(chat, botB)
        otherChatContext  <- store.all(otherChat, botA)
        switchAfterForget <- settingsA.status(chat)
        _                 <- settingsA.disable(chat)
        disabled          <- settingsA.status(chat)
      } yield {
        assertEquals(initial, LlmSettings.Status(enabled = false))
        assertEquals(enabled, LlmSettings.Status(enabled = true))
        assertEquals(otherBotStatus, LlmSettings.Status(enabled = false))
        assertEquals(otherChatStatus, LlmSettings.Status(enabled = false))
        assertEquals(
          before.map(m => (m.role, m.content)),
          List(
            (LlmContextRole.User, "ciao"),
            (LlmContextRole.Assistant, "buongiorno")
          )
        )
        assertEquals(after, Nil)
        assertEquals(otherBotContext.map(_.content), List("other bot"))
        assertEquals(otherChatContext.map(_.content), List("other chat"))
        assertEquals(switchAfterForget, LlmSettings.Status(enabled = true))
        assertEquals(disabled, LlmSettings.Status(enabled = false))
      }
    }
  }

  test("recent skips expired rows, trim keeps the newest and a negative limit fails in F") {
    tempDb.use { xa =>
      val store = LlmContextStore[IO](xa)
      for {
        _ <- setupSchema(xa)
        _ <- List("old", "one", "two", "three").zipWithIndex.traverse_ { case (content, index) =>
          LlmContextStore.appendQuery(chat, botA, LlmContextRole.User, content, 50L + index * 50L).run.transact(xa).void
        }
        fresh   <- store.recent(chat, botA, 10, 100L)
        trimmed <- store.trim(chat, botA, 2)
        left    <- store.all(chat, botA)
        failed  <- store.recent(chat, botA, -1, 0L).attempt
      } yield {
        assertEquals(fresh.map(_.content), List("one", "two", "three"))
        assertEquals(trimmed, 2)
        assertEquals(left.map(_.content), List("two", "three"))
        assert(failed.left.exists(_.isInstanceOf[IllegalArgumentException]))
      }
    }
  }

  // obligation: contract.LlmContextWindow.BoundedWindow
  test("recent returns the newest messages, oldest first, even inside the same second") {
    tempDb.use { xa =>
      val store = LlmContextStore[IO](xa)
      for {
        _ <- setupSchema(xa)
        _ <- List("one", "two", "three", "four").zipWithIndex.traverse_ { case (content, index) =>
          LlmContextStore
            .appendQuery(chat, botA, LlmContextRole.User, content, 100L + (index / 2))
            .run
            .transact(xa)
            .void
        }
        window  <- store.recent(chat, botA, 3, 0L)
        allRows <- store.all(chat, botA)
      } yield {
        assertEquals(window.map(_.content), List("two", "three", "four"))
        assertEquals(allRows.map(_.content), List("one", "two", "three", "four"))
      }
    }
  }

  // obligation: rule.LlmContextCleanupDeletesExpired
  test("deleteOlderThan removes only the expired rows, for every chat and bot") {
    tempDb.use { xa =>
      val store = LlmContextStore[IO](xa)
      for {
        _ <- setupSchema(xa)
        _ <- List((chat, botA), (otherChat, botB)).traverse_ { case (c, b) =>
          LlmContextStore
            .appendQuery(c, b, LlmContextRole.User, "old", 100L)
            .run
            .transact(xa)
            .void
        }
        _ <- LlmContextStore
          .appendQuery(chat, botA, LlmContextRole.Assistant, "new", 101L)
          .run
          .transact(xa)
          .void
        deleted        <- store.deleteOlderThan(101L)
        remaining      <- store.all(chat, botA)
        otherRemaining <- store.all(otherChat, botB)
      } yield {
        assertEquals(deleted, 2)
        assertEquals(remaining.map(_.content), List("new"))
        assertEquals(otherRemaining, Nil)
      }
    }
  }

  // obligation: rule.LlmEnabledForChat, rule.LlmDisabledForChat
  test("enable and disable stamp updated_at with the current clock") {
    tempDb.use { xa =>
      for {
        _     <- setupSchema(xa)
        clock <- Ref[IO].of(1700000000L)
        settings = LlmSettingsDB[IO](xa, botA, now = clock.get)
        before       <- LlmSettingsDB.updatedAtQuery(chat, botA).option.transact(xa)
        _            <- settings.enable(chat)
        afterEnable  <- LlmSettingsDB.updatedAtQuery(chat, botA).option.transact(xa)
        _            <- clock.set(1700000001L)
        _            <- settings.disable(chat)
        afterDisable <- LlmSettingsDB.updatedAtQuery(chat, botA).option.transact(xa)
      } yield {
        assertEquals(before, None)
        assertEquals(afterEnable, Some(1700000000L))
        assertEquals(afterDisable, Some(1700000001L))
      }
    }
  }

  test("the new tables exist with the columns of the issue") {
    tempDb.use { xa =>
      for {
        _               <- setupSchema(xa)
        settingsColumns <- sql"PRAGMA table_info(llm_chat_settings)"
          .query[(Int, String)]
          .to[List]
          .transact(xa)
          .map(_.map(_._2))
        contextColumns <- sql"PRAGMA table_info(llm_context)"
          .query[(Int, String)]
          .to[List]
          .transact(xa)
          .map(_.map(_._2))
      } yield {
        assertEquals(settingsColumns, List("platform", "chat_id", "bot_id", "enabled", "updated_at"))
        assertEquals(contextColumns, List("platform", "chat_id", "bot_id", "role", "content", "created_at"))
      }
    }
  }

  test("the trace table keeps full prompt and response and the TTL drops the old rows (#1493)") {
    tempDb.use { xa =>
      val traces          = com.benkio.chatllm.observability.LlmTraceStore[IO](xa)
      def trace(at: Long) = com.benkio.chatllm.observability.LlmTrace(
        createdAt = at,
        chatHash = "abc",
        botId = botA.value,
        model = Some("m"),
        status = "answered",
        action = "text",
        prompt = s"prompt $at",
        response = Some("""{"action":"text"}""")
      )
      for {
        _       <- setupSchema(xa)
        _       <- traces.append(trace(100))
        _       <- traces.append(trace(200))
        recent  <- traces.recent("abc", 10)
        other   <- traces.recent("zzz", 10)
        deleted <- traces.deleteOlderThan(150)
        left    <- traces.recent("abc", 10)
      } yield {
        assertEquals(recent.map(_.createdAt), List(200L, 100L))
        assertEquals(recent.head.prompt, "prompt 200")
        assertEquals(recent.head.response, Some("""{"action":"text"}"""))
        assertEquals(other, Nil)
        assertEquals(deleted, 1)
        assertEquals(left.map(_.createdAt), List(200L))
      }
    }
  }
}
