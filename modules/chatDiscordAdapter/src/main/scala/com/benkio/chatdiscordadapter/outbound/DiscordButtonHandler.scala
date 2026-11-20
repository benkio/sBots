package com.benkio.chatdiscordadapter.outbound

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.PagedListNavigation
import log.effect.LogWriter

/** A button pressed on a list: where, who and what it carries. */
final case class DiscordButtonPress(channelId: String, messageId: String, userId: String, id: String)

/** What happens when a button of a list is pressed. [[decide]] is quick: it only answers whether the press is accepted,
  * so that the interaction can be acknowledged in the 3 seconds Discord gives; the work is in the effect it gives back.
  *
  * @param navigate
  *   the reply of an action on the list (see [[PagedListNavigation]]), run from the message that asked for the list
  * @param replies
  *   sends what a pick answers, the list is gone by then
  */
class DiscordButtonHandler[F[_]: Async: LogWriter](
    api: DiscordApi[F],
    lists: DiscordButtonLists[F],
    navigate: (PagedListAction, Message) => F[ReplyValue],
    replies: (ReplyValue, Message) => F[Unit]
) {

  val listGone: String      = "This list is not available anymore: ask for it again."
  val notYours: String      = "Only who asked for the list can use its buttons."
  val unknownButton: String = "This button is not valid."

  /** `Left` with the text to show only to who pressed, or the work to do. */
  def decide(press: DiscordButtonPress): F[Either[String, F[Unit]]] =
    lists.get(press.messageId).map {
      case None                                                     => Left(listGone)
      case Some(list) if list.requestedBy.exists(_ != press.userId) => Left(notYours)
      case Some(list)                                               =>
        PagedListAction.fromId(press.id).toRight(unknownButton).map(act(press, list, _))
    }

  private def act(press: DiscordButtonPress, list: DiscordButtonList, action: PagedListAction): F[Unit] =
    PagedListNavigation.newPage(action) match {
      case Some(_) => changePage(press = press, list = list, action = action)
      case None    => pick(press = press, list = list, action = action)
    }

  private def changePage(press: DiscordButtonPress, list: DiscordButtonList, action: PagedListAction): F[Unit] =
    navigate(action, list.request).flatMap {
      case pagedList: PagedList =>
        api.editButtons(press.channelId, press.messageId, DiscordPagedListLayout.render(pagedList)) >>
          lists.put(press.messageId, list.copy(pagedList = pagedList))
      case other => LogWriter.warn(s"[DiscordButtonHandler] A page change gave $other instead of a list")
    }

  /** The list disappears and the item is sent. */
  private def pick(press: DiscordButtonPress, list: DiscordButtonList, action: PagedListAction): F[Unit] =
    lists.remove(press.messageId) >>
      api.deleteMessage(press.channelId, press.messageId) >>
      navigate(action, list.request).flatMap(replies(_, list.request))
}
