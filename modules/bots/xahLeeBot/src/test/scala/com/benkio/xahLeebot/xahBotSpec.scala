package com.benkio.xahleebot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Message

class XahLeeBotSpec extends BaseBotSpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(XahLeeBot.botName)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(mediaResource)).pure[IO])
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = Async[F].pure(List.empty[Message])
  }

  val commandRepliesData: IO[List[ReplyBundleCommand[IO]]] = BackgroundJobManager[IO](
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    resourceAccess = resourceAccessMock,
    botName = "XahLeeBot"
  ).map(bjm =>
    XahLeeBot
      .commandRepliesData[IO](
        backgroundJobManager = bjm,
        dbLayer = emptyDBLayer
      )
  )
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    XahLeeBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint)

  exactTriggerReturnExpectedReplyBundle(XahLeeBot.messageRepliesData[IO])

  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = messageRepliesDataPrettyPrint
  )
}
