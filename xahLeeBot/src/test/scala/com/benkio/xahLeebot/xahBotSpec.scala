package com.benkio.xahleebot

import com.benkio.telegrambotinfrastructure.model.MediafileSource
import io.circe.parser.decode
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Message

import java.io.File
import scala.io.Source
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class XahLeeBotSpec extends CatsEffectSuite {

  implicit val log: LogWriter[IO]   = consoleLogUpToLevel(LogLevels.Info)
  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])
  val emptyDBLayer: DBLayer[IO]     = DBLayerMock.mock(XahLeeBot.botName)
  val emptyBackgroundJobManager: BackgroundJobManager[IO] = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    botName = "XahLeeBot"
  ).unsafeRunSync()

  test("the jsons should contain all the triggers of the bot") {
    val listPath      = new File(".").getCanonicalPath + "/xah_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val jsonFilenames = decode[List[MediafileSource]](jsonContent).map(_.map(_.filename))

    val botFile =
      CommandRepliesData
        .values[IO](
          botName = "XahLeeBot",
          backgroundJobManager = emptyBackgroundJobManager,
          dbLayer = emptyDBLayer
        )
        .flatMap(_.mediafiles.map(_.filename))

    assert(jsonFilenames.isRight)
    jsonFilenames.fold(
      e => fail("test failed", e),
      files =>
        botFile.foreach(filename => assert(files.contains(filename), s"$filename is not contained in xah data file"))
    )
  }
}
