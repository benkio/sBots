package com.benkio.chatdiscordadapter.outbound

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.PagedListNavigation
import com.benkio.chatcore.SubscriptionFlowPromptStore
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
  * @param onFlowAction
  *   submits a neutral guided-subscription action to the engine, with the owner of the prompt
  */
class DiscordButtonHandler[F[_]: Async: LogWriter](
    api: DiscordApi[F],
    lists: DiscordButtonLists[F],
    navigate: (PagedListAction, Message) => F[ReplyValue],
    replies: (ReplyValue, Message) => F[Unit],
    flowPrompts: SubscriptionFlowPromptStore[F],
    onFlowAction: (Message, SubscriptionFlowAction, String) => F[Unit]
) {

  val listGone: String      = "This list is not available anymore: ask for it again."
  val notYours: String      = "Only who asked for the list can use its buttons."
  val unknownButton: String = "This button is not valid."
  val promptGone: String    = "This prompt is not available anymore."
  val flowNotYours: String  = "Only who started this subscription setup can use these buttons."

  /** `Left` with the text to show only to who pressed, or the work to do. A flow press is checked against the owner of
    * its prompt before it reaches the engine; the engine never sees a press by another user.
    */
  def decide(press: DiscordButtonPress): F[Either[String, F[Unit]]] =
    SubscriptionFlowAction.fromId(press.id) match {
      case Some(action) =>
        flowPrompts.byMessageId(ChatId(Platform.Discord, press.channelId), press.messageId).map {
          case None                                         => Left(promptGone)
          case Some(handle) if handle.owner != press.userId => Left(flowNotYours)
          case Some(handle)                                 =>
            Right(onFlowAction(flowMessage(press, handle.owner), action, handle.owner))
        }
      case None if press.id.startsWith("flow-") => Either.left[String, F[Unit]](unknownButton).pure[F]
      case None                                 =>
        lists.get(press.messageId).map {
          case None                                                     => Left(listGone)
          case Some(list) if list.requestedBy.exists(_ != press.userId) => Left(notYours)
          case Some(list)                                               =>
            PagedListAction.fromId(press.id).toRight(unknownButton).map(act(press, list, _))
        }
    }

  /** The press is delivered as a message from the prompt's channel: the engine needs the chat and the bot, not the
    * prompt's text.
    */
  private def flowMessage(press: DiscordButtonPress, owner: String): Message =
    Message(
      messageId = Some(MessageId(press.messageId)),
      date = 0L,
      chatId = ChatId(Platform.Discord, press.channelId),
      chatType = "group",
      authorId = Some(owner)
    )

  private def act(press: DiscordButtonPress, list: DiscordButtonList, action: PagedListAction): F[Unit] =
    PagedListNavigation
      .newPage(action)
      .fold(pick(press = press, list = list, action = action))(_ =>
        changePage(press = press, list = list, action = action)
      )

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
