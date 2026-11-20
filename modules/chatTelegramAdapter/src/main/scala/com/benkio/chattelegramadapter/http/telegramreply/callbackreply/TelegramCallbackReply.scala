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

import scala.concurrent.duration.FiniteDuration

object TelegramCallbackReply {

  private[callbackreply] def nextPage(currentPage: Int): Int = PagedListNavigation.nextPage(currentPage)

  private[callbackreply] def previousPage(currentPage: Int): Int = PagedListNavigation.previousPage(currentPage)

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
    }
  }
}
