package com.benkio.chatcore

import cats.effect.Async
import cats.syntax.all.*
import cats.ApplicativeThrow
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.addTimestamp
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBShow
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** What happens on a paged list when the user acts on it, with no platform in it: the adapters turn a press or a typed
  * command into a [[PagedListAction]] and send back the reply value.
  */
object PagedListNavigation {

  def nextPage(currentPage: Int): Int = currentPage + 1

  def previousPage(currentPage: Int): Int = (currentPage - 1).max(0)

  /** The message of the exact trigger search a picked candidate asks for. */
  def triggerSearchMessage(triggerValue: String, msg: Message, commandSyntax: CommandSyntax): Message =
    msg.copy(
      text = Some(s"${commandSyntax.invocation(CommandKey.TriggerSearch.asString)} $triggerValue"),
      caption = None
    )

  /** The page the action moves to, `None` for the picks. */
  def newPage(action: PagedListAction): Option[Int] = action match {
    case PagedListAction.NextPage(page, _)     => Some(nextPage(page))
    case PagedListAction.PreviousPage(page, _) => Some(previousPage(page))
    case _                                     => None
  }

  /** The reply of the action: the command run again for the new page (`msg` carries the input of the command), or the
    * item picked, a media file or the text of a show.
    */
  def reply[F[_]: Async: LogWriter](
      action: PagedListAction,
      msg: Message,
      allCommandRepliesData: List[ReplyBundleCommand],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, EffectfulCallback[F]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration],
      capabilities: PlatformCapabilities
  ): F[ReplyValue] = action match {
    case PagedListAction.NextPage(_, commandKey) =>
      runCommand(
        commandKey = commandKey,
        page = newPage(action),
        msg = msg,
        allCommandRepliesData = allCommandRepliesData,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl,
        capabilities = capabilities
      )
    case PagedListAction.PreviousPage(_, commandKey) =>
      runCommand(
        commandKey = commandKey,
        page = newPage(action),
        msg = msg,
        allCommandRepliesData = allCommandRepliesData,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl,
        capabilities = capabilities
      )
    case PagedListAction.Trigger(value) =>
      runCommand(
        commandKey = CommandKey.TriggerSearch,
        page = Some(0),
        msg = triggerSearchMessage(value, msg, capabilities.commandSyntax),
        allCommandRepliesData = allCommandRepliesData,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl,
        capabilities = capabilities
      )
    case PagedListAction.Media(name)         => (MediaFile.fromString(name): ReplyValue).pure[F]
    case PagedListAction.Show(id, timestamp) =>
      showText(showId = id, timestamp = timestamp, dbShow = dbLayer.dbShow).widen
  }

  private def runCommand[F[_]: Async: LogWriter](
      commandKey: com.benkio.chatcore.model.CommandKey,
      page: Option[Int],
      msg: Message,
      allCommandRepliesData: List[ReplyBundleCommand],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, EffectfulCallback[F]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration],
      capabilities: PlatformCapabilities
  ): F[ReplyValue] =
    for {
      commandReply <- ApplicativeThrow[F].fromOption(
        ReplyBundleCommand.from(commandKey, allCommandRepliesData),
        new Throwable(s"[PagedListNavigation.reply] Command reply not found for commandKey: $commandKey")
      )
      replyValue <- ComputeReply.runReply(
        reply = commandReply.reply,
        msg = msg,
        backgroundJobManager = backgroundJobManager,
        dbLayer = dbLayer,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl,
        capabilities = capabilities,
        overridePage = page
      )
    } yield replyValue

  /** The text of the show picked. */
  def showText[F[_]: Async](
      showId: String,
      timestamp: Option[String],
      dbShow: DBShow[F]
  ): F[Text] =
    for {
      optShowData <- dbShow.getShowById(showId)
      optShow     <- optShowData.traverse(Show(_))
      rendered = optShow.fold("Unexpected error when fetching the show")(show =>
        timestamp.fold(show.show)(t => show.addTimestamp(t).show)
      )
    } yield Text(rendered)
}
