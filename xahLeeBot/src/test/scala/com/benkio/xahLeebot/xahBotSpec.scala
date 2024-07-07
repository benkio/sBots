package com.benkio.xahleebot

import com.benkio.telegrambotinfrastructure.BaseBotSpec
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.model.ReplyValue
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import cats.effect.Async
import telegramium.bots.client.Method
import telegramium.bots.high.Api

import cats.implicits.*
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Message

import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class XahLeeBotSpec extends BaseBotSpec {

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

  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = XahLeeBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint).unsafeRunSync()
  )
}
