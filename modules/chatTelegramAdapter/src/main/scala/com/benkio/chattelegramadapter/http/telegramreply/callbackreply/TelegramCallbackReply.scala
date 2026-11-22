package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PagedListNavigation
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

import scala.concurrent.duration.FiniteDuration

object TelegramCallbackReply {

  private[callbackreply] def nextPage(currentPage: Int): Int = PagedListNavigation.nextPage(currentPage)

  private[callbackreply] def previousPage(currentPage: Int): Int = PagedListNavigation.previousPage(currentPage)

  /** A page button that is no longer in the keyboard of the message is stale: the page was changed by another press
    * after it was shown. Ignoring it avoids rendering an empty page when a stale `next` is pressed past the last one. A
    * message without keyboard information cannot be checked and is left alone.
    */
  private[callbackreply] def isStalePagePress(msg: MaybeInaccessibleMessage, action: PagedListAction): Boolean =
    action.toId.exists { id =>
      msg match {
        case m: TelegramMessage =>
          m.replyMarkup.exists(markup => !markup.inlineKeyboard.exists(_.exists(_.callbackData.contains(id))))
        case _ => false
      }
    }

  def reply[F[_]: Async: LogWriter: Api: Notifier](
      msg: MaybeInaccessibleMessage,
      callbackData: PagedListAction,
      repository: Repository[F],
      allCommandRepliesData: List[ReplyBundleCommand],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, EffectfulCallback[F]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[Unit] = {
    def paginate(page: PagedListAction, commandKey: CommandKey): F[Unit] =
      if isStalePagePress(msg, page) then LogWriter.info(
        s"[TelegramCallbackReply] stale page press ignored: ${page.toId}"
      )
      else
        Pagination.reply(
          msg = msg,
          action = page,
          commandKey = commandKey,
          allCommandRepliesData = allCommandRepliesData,
          backgroundJobManager = backgroundJobManager,
          effectfulCallbacks = effectfulCallbacks,
          dbLayer = dbLayer,
          ttl = ttl
        )
    callbackData match {
      case page @ PagedListAction.NextPage(_, commandKey)     => paginate(page, commandKey)
      case page @ PagedListAction.PreviousPage(_, commandKey) => paginate(page, commandKey)
      case PagedListAction.Media(value)           => Media.reply(msg = msg, mediaName = value, repository = repository)
      case PagedListAction.Show(value, timestamp) =>
        Show.reply(msg = msg, showId = value, timestamp = timestamp, showDb = dbLayer.dbShow)
      case PagedListAction.Trigger(value) =>
        Trigger.reply(
          msg = msg,
          triggerValue = value,
          repository = repository,
          allCommandRepliesData = allCommandRepliesData,
          backgroundJobManager = backgroundJobManager,
          effectfulCallbacks = effectfulCallbacks,
          dbLayer = dbLayer,
          ttl = ttl
        )
    }
  }
}
