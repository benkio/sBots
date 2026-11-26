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

/** Exercises the doobie settings of the `/llm` command against the real Flyway migration files: the tables, the keys
  * and the port semantics must agree with what the migration creates.
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

  private def insertContext(xa: Transactor[IO], chatId: ChatId, botId: SBotId, content: String): IO[Unit] =
    sql"""INSERT INTO llm_context (platform, chat_id, bot_id, role, content, created_at)
          VALUES (${chatId.platform.id}, ${chatId.id}, ${botId.value}, 'user', $content, 1)""".update.run
      .transact(xa)
      .void

  private def contexts(xa: Transactor[IO], chatId: ChatId, botId: SBotId): IO[List[String]] =
    sql"""SELECT content FROM llm_context
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"""
      .query[String]
      .to[List]
      .transact(xa)

  test("settings are off by default and toggled per chat and bot, stamping the update time") {
    tempDb.use { xa =>
      for {
        _     <- setupSchema(xa)
        clock <- IO.ref(100L)
        settingsA = LlmSettingsDB[IO](xa, botA, clock.get)
        settingsB = LlmSettingsDB[IO](xa, botB, clock.get)
        initial    <- settingsA.status(chat)
        _          <- settingsA.enable(chat)
        enabled    <- settingsA.status(chat)
        otherChatS <- settingsA.status(otherChat)
        otherBotS  <- settingsB.status(chat)
        _          <- clock.set(200L)
        _          <- settingsA.disable(chat)
        disabled   <- settingsA.status(chat)
        updatedAt  <- LlmSettingsDB.updatedAtQuery(chat, botA).unique.transact(xa)
      } yield {
        assertEquals(initial, LlmSettings.Status(enabled = false))
        assertEquals(enabled, LlmSettings.Status(enabled = true))
        assertEquals(otherChatS, LlmSettings.Status(enabled = false))
        assertEquals(otherBotS, LlmSettings.Status(enabled = false))
        assertEquals(disabled, LlmSettings.Status(enabled = false))
        assertEquals(updatedAt, 200L)
      }
    }
  }

  test("forget drops only the context of that chat and bot and keeps the switch") {
    tempDb.use { xa =>
      for {
        _ <- setupSchema(xa)
        settings = LlmSettingsDB[IO](xa, botA)
        _            <- settings.enable(chat)
        _            <- insertContext(xa = xa, chatId = chat, botId = botA, content = "to forget")
        _            <- insertContext(xa = xa, chatId = chat, botId = botB, content = "other bot")
        _            <- insertContext(xa = xa, chatId = otherChat, botId = botA, content = "other chat")
        _            <- settings.forget(chat)
        forgotten    <- contexts(xa = xa, chatId = chat, botId = botA)
        otherBot     <- contexts(xa = xa, chatId = chat, botId = botB)
        otherChatCtx <- contexts(xa = xa, chatId = otherChat, botId = botA)
        status       <- settings.status(chat)
      } yield {
        assertEquals(forgotten, Nil)
        assertEquals(otherBot, List("other bot"))
        assertEquals(otherChatCtx, List("other chat"))
        assertEquals(status, LlmSettings.Status(enabled = true))
      }
    }
  }
}
