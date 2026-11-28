package com.benkio.chatcore.repository.db

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
class DBLlmSettingsSpec extends CatsEffectSuite {

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

  test("settings are off by default and toggled per chat and bot, stamping the update time") {
    tempDb.use { xa =>
      for {
        _     <- setupSchema(xa)
        clock <- IO.ref(100L)
        settingsA = DBLlmSettings[IO](xa, botA, clock.get)
        settingsB = DBLlmSettings[IO](xa, botB, clock.get)
        initial    <- settingsA.status(chat)
        _          <- settingsA.enable(chat)
        enabled    <- settingsA.status(chat)
        otherChatS <- settingsA.status(otherChat)
        otherBotS  <- settingsB.status(chat)
        _          <- clock.set(200L)
        _          <- settingsA.disable(chat)
        disabled   <- settingsA.status(chat)
        updatedAt  <- DBLlmSettings.updatedAtQuery(chat, botA).unique.transact(xa)
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
}
