package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PagedListNavigation
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.model.TelegramCapabilities
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

import scala.concurrent.duration.FiniteDuration

/** A trigger candidate picked from a fuzzy search keyboard: the exact trigger search runs for the candidate, so the
  * user gets the very same reply they would get by typing `/triggersearch <candidate>`.
  */
object Trigger {

  def reply[F[_]: Async: LogWriter: Api: Notifier](
      msg: MaybeInaccessibleMessage,
      triggerValue: String,
      repository: Repository[F],
      allCommandRepliesData: List[ReplyBundleCommand],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, EffectfulCallback[F]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    val modelMsg           = ModelMessageFromCallback.build(msg)

    for {
      _ <- LogWriter.info(s"[Trigger.reply] reply to callback for trigger $triggerValue")
      replyValue <- PagedListNavigation.reply(
        action = PagedListAction.Trigger(triggerValue),
        msg = modelMsg,
        allCommandRepliesData = allCommandRepliesData,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl,
        capabilities = TelegramCapabilities.value
      )
      _ <- TelegramMessageReply.sendReplyValue(
        replyValue = replyValue,
        msg = modelMsg,
        repository = repository,
        replyToMessage = false
      )
      _ <- Methods
        .deleteMessage(
          chatId = ChatIntId(telegramMessageIds.chatId),
          messageId = telegramMessageIds.messageId
        )
        .exec
    } yield ()
  }
}
