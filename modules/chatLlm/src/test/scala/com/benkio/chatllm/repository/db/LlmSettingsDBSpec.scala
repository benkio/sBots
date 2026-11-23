package com.benkio.chatllm.repository.db

import cats.effect.IO
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
}
