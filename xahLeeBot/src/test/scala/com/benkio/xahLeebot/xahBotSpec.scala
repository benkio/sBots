package com.benkio.xahleebot

import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.model.ReplyValue
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import cats.effect.Async
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import io.circe.parser.decode
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
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

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(XahLeeBot.botName)
  val resourceAccessMock        = new ResourceAccessMock(List.empty)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = Async[F].pure(List.empty[Message])
  }
  given api: Api[IO] = new Api[IO] {
    def execute[Res](method: Method[Res]): IO[Res] = IO(???)
  }

  val emptyBackgroundJobManager: BackgroundJobManager[IO] = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    resourceAccess = resourceAccessMock,
    botName = "XahLeeBot"
  ).unsafeRunSync()

  test("the jsons should contain all the triggers of the bot") {
    val listPath      = new File(".").getCanonicalPath + "/xah_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val jsonFilenames = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val botFile =
      CommandRepliesData
        .values[IO](
          botName = "XahLeeBot",
          backgroundJobManager = emptyBackgroundJobManager,
          dbLayer = emptyDBLayer
        )
        .flatMap(_.reply.prettyPrint.unsafeRunSync())

    assert(jsonFilenames.isRight)
    jsonFilenames.fold(
      e => fail("test failed", e),
      files => {
        botFile.foreach(filename => assert(files.contains(filename), s"$filename is not contained in xah data file"))
      }
    )
  }
}
