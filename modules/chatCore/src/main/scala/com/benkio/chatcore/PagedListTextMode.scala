package com.benkio.chatcore

import cats.effect.Async
import cats.effect.Clock
import cats.syntax.all.*
import cats.ApplicativeThrow
import com.benkio.chatcore.conversions.YouTubeTimestamp.finiteDurationToYoutubeTimestamp
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.repository.db.DBLayer

import scala.concurrent.duration.FiniteDuration

/** The text mode of paged lists on a platform without buttons: owns the pages remembered per chat and handles the
  * `!next`, `!prev` and `!pick` commands, leaving a platform with buttons to the button navigation.
  *
  * The store is in memory and per instance: a restart forgets it. Everything the commands need (the command replies,
  * the shows and the platform) is given at construction or at the call, so the orchestrator holds no paging state.
  */
final class PagedListTextMode[F[_]: Async](
    botName: SBotName,
    allCommandRepliesData: () => List[ReplyBundleCommand],
    dbLayer: DBLayer[F],
    expiry: FiniteDuration,
    maxChats: Int
) {

  private val store = new PagedListTextStore.Store(expiry = expiry, maxChats = maxChats)

  /** Whether a text navigation command was handled and a reply sent. */
  def handleCommand(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    if platform.capabilities.supportsButtons then false.pure[F]
    else
      Clock[F].monotonic.flatMap { now =>
        PagedListTextCommands
          .decide(
            msg = msg,
            syntax = platform.capabilities.commandSyntax,
            botName = botName,
            store = store,
            now = now
          )
          .fold(false.pure[F])(handleAction(_, msg))
      }

  /** Remembers the paged list just sent, so the text commands can act on it. */
  def remember(request: Message, replyValue: ReplyValue)(using platform: PlatformAdapter[F]): F[Unit] =
    replyValue match {
      case pagedList: PagedList if !platform.capabilities.supportsButtons =>
        Clock[F].monotonic.flatMap { now =>
          store
            .save(
              chatId = request.chatId,
              commandKey = pagedList.commandKey,
              page = pagedList.page,
              pageItems = pagedList.pageItems,
              request = request,
              now = now
            )
            .pure[F]
        }
      case _ => ().pure[F]
    }

  private def handleAction(action: PagedListTextCommands.Action, msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Boolean] = action match {
    case PagedListTextCommands.Action.Rerun(entry, page) =>
      for {
        replyValue <- rerunStoredCommand(entry, page)
        _          <- remember(entry.request, replyValue)
        _          <- deliver(replyValue = replyValue, msg = msg, replyToMessage = true)
      } yield true
    case PagedListTextCommands.Action.Answer(text) =>
      deliver(replyValue = Text(text), msg = msg, replyToMessage = true).as(true)
    case PagedListTextCommands.Action.Pick(entry, number) =>
      pickStoredItem(entry, number)
        .flatMap(value => deliver(replyValue = value, msg = msg, replyToMessage = true))
        .as(true)
  }

  /** The command of the remembered list run again at a page (the navigation rule PagedListChangesPage). */
  private def rerunStoredCommand(entry: PagedListTextStore.Entry, page: Int)(using
      platform: PlatformAdapter[F]
  ): F[ReplyValue] =
    for {
      commandReply <- ApplicativeThrow[F].fromOption(
        ReplyBundleCommand.from(entry.commandKey, allCommandRepliesData()),
        new Throwable(s"[PagedListTextMode] Command reply not found for commandKey: ${entry.commandKey}")
      )
      replyValue <- commandReply.reply match {
        case effectfulReply: EffectfulReply => platform.runEffectful(effectfulReply, entry.request, Some(page))
        case plainReply: PlainReply         => BotEngine.runPlainReply[F](plainReply, platform.capabilities)
      }
    } yield replyValue

  /** The replies of an item picked on a remembered list, as if asked for directly (PagedListPicksItem). */
  private def pickStoredItem(entry: PagedListTextStore.Entry, number: Int)(using
      platform: PlatformAdapter[F]
  ): F[ReplyValue] =
    entry.pageItems.lift(number - 1) match {
      case Some(PagedItem.ShowItem(show)) =>
        PagedListNavigation
          .showText(show.id, show.timestamp.map(finiteDurationToYoutubeTimestamp), dbLayer.dbShow)
          .widen
      case Some(PagedItem.ValueItem(value))   => value.pure[F]
      case Some(PagedItem.TriggerItem(value)) =>
        for {
          commandReply <- ApplicativeThrow[F].fromOption(
            ReplyBundleCommand.from(CommandKey.TriggerSearch, allCommandRepliesData()),
            new Throwable(s"[PagedListTextMode] Command reply not found for commandKey: ${CommandKey.TriggerSearch}")
          )
          synthetic = PagedListNavigation.triggerSearchMessage(
            value,
            entry.request,
            platform.capabilities.commandSyntax
          )
          replyValue <- commandReply.reply match {
            case effectfulReply: EffectfulReply => platform.runEffectful(effectfulReply, synthetic, Some(0))
            case plainReply: PlainReply         => BotEngine.runPlainReply[F](plainReply, platform.capabilities)
          }
        } yield replyValue
      case None =>
        Async[F].raiseError(
          new Throwable(s"[PagedListTextMode] the item $number is out of the page of the remembered list")
        )
    }

  private def deliver(replyValue: ReplyValue, msg: Message, replyToMessage: Boolean)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    ReplyAdaptation
      .adaptToPlatform(dbLayer, platform.capabilities)(replyValue)
      .flatMap(platform.sendAll(_, msg, replyToMessage))
}
