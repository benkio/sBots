package com.benkio.chattelegramadapter

import cats.effect.std.Random
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.model.TelegramCalendarMessage
import com.benkio.chattelegramadapter.model.TelegramCapabilities
import log.effect.LogWriter
import telegramium.bots.high.Api

import scala.concurrent.duration.FiniteDuration

/** The subscriptions of a bot fired on Telegram: the schedule is [[CronBackgroundJobManager]], this only provides what
  * is Telegram specific, the way to send to a chat, its capabilities and the calendar sized for its limits.
  */
object TelegramBackgroundJobManager {

  def apply[F[_]: Async: Api: Random](
      repository: Repository[F],
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration]
  )(using log: LogWriter[F]): F[BackgroundJobManager[F]] =
    CronBackgroundJobManager[F](
      dbLayer = dbLayer,
      sBotInfo = sBotInfo,
      ttl = ttl,
      capabilities = TelegramCapabilities.value,
      send = sendToChat[F](repository),
      calendarMessage = (botName, shows) => TelegramCalendarMessage.render(botName, shows)
    )

  /** The Telegram methods that send a reply need a message: the chat id and the message to reply to, when asked. A
    * subscription has no message, only the chat id of the message matters here.
    */
  private def sendToChat[F[_]: Async: LogWriter: Api](
      repository: Repository[F]
  )(chatId: ChatId, replyValue: ReplyValue, replyToMessage: Boolean): F[Unit] =
    TelegramMessageReply
      .sendReplyValue[F](
        replyValue = replyValue,
        msg = Message(messageId = 0, date = 0L, chatId = chatId, chatType = "private"),
        repository = repository,
        replyToMessage = replyToMessage
      )
      .void
}
