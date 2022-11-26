package com.benkio.xahbot

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class XahBotSpec extends CatsEffectSuite {

  implicit val log: LogWriter[IO]   = consoleLogUpToLevel(LogLevels.Info)
  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])
  val emptyDBLayer                  = DBLayerMock.mock()
  val emptyBackgroundJobManager = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    botName = "XahBot"
  ).unsafeRunSync()

  test("the csvs should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/xah_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile =
      CommandRepliesData
        .values[IO](
          botName = "xahBot",
          backgroundJobManager = emptyBackgroundJobManager,
          dbShow = emptyDBLayer.dbShow
        )
        .flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files =>
        assert(botFile.forall(filename => {
          if (!files.contains(filename)) {
            println(s"filename: " + filename)

          }
          files.contains(filename)
        }))
    )
  }
}
