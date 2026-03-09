package com.benkio.chatcore.model

import cats.effect.Async
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** Known, first-class command identifiers supported by `chatCore`.
  *
  * The string value is the canonical command without the leading '/' and without '@botname'.
  */
enum CommandKey(val asString: String) {
  case Random        extends CommandKey("random")
  case SearchShow    extends CommandKey("searchshow")
  case TriggerList   extends CommandKey("triggerlist")
  case TriggerSearch extends CommandKey("triggersearch")
  case Instructions  extends CommandKey("instructions")
  case Subscribe     extends CommandKey("subscribe")
  case Unsubscribe   extends CommandKey("unsubscribe")
  case Subscriptions extends CommandKey("subscriptions")
  case TopTwenty     extends CommandKey("toptwenty")
  case Timeout       extends CommandKey("timeout")

  def trigger: CommandTrigger = CommandTrigger(asString)
}

object CommandKey {
  val commandTriggers: List[CommandTrigger] = CommandKey.values.toList.map(_.trigger)

  def fromString(input: String): Option[CommandKey] = {
    val normalized = normalizeCommand(input)
    CommandKey.values.find(_.asString == normalized)
  }

  def toStringValue(key: CommandKey): String = key.asString

  private def normalizeCommand(input: String): String =
    input.trim
      .stripPrefix("/")
      .takeWhile(c => c != '@' && !c.isWhitespace)
      .toLowerCase(java.util.Locale.ROOT)

  def toCommandLogic[F[_]: Async: LogWriter](
      commandKey: CommandKey,
      sBotConfig: SBotConfig,
      message: ModelMessage,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, ModelMessage => F[List[Text]]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[ReplyValue]] = ???
  // TODO: maybe useless as you want to: filter and select the command -> extract the reply -> get the replyValue???
  // (commandKey match {
  //   case Random =>
  //     CommandPatterns.RandomDataCommand
  //       .randomCommandLogic(dbMedia = dbLayer.dbMedia, sBotInfo = sBotConfig.sBotInfo)
  //       .map(List(_))
  //   case SearchShow =>
  //     CommandPatterns.SearchShowCommand
  //       .searchShowCommandLogic(msg = message, dbLayer = dbLayer, sBotInfo = sBotConfig.sBotInfo, ttl = ttl)
  //       .widen
  //   case TriggerList =>
  //     Async[F]
  //       .pure(
  //         List(
  //           CommandPatterns.TriggerListCommand.triggerListLogic(
  //             sBotConfig.triggerListUri
  //           )
  //         )
  //       )
  //       .widen
  //   case TriggerSearch =>
  //     ???
  //   case Instructions =>
  //     ???
  //   case Subscribe =>
  //     CommandPatterns.SubscribeUnsubscribeCommand
  //       .subscribeCommandLogic[F](
  //         backgroundJobManager = backgroundJobManager,
  //         m = message,
  //         sBotInfo = sBotConfig.sBotInfo,
  //         ttl = ttl
  //       )
  //       .widen
  //   case Unsubscribe =>
  //     CommandPatterns.SubscribeUnsubscribeCommand
  //       .unsubcribeCommandLogic[F](
  //         backgroundJobManager = backgroundJobManager,
  //         m = message,
  //         sBotInfo = sBotConfig.sBotInfo,
  //         ttl = ttl
  //       )
  //       .widen
  //   case Subscriptions =>
  //     CommandPatterns.SubscribeUnsubscribeCommand
  //       .subscriptionsCommandLogic[F](
  //         dbSubscription = dbLayer.dbSubscription,
  //         backgroundJobManager = backgroundJobManager,
  //         sBotInfo = sBotConfig.sBotInfo,
  //         m = message
  //       )
  //       .widen
  //   case TopTwenty =>
  //     CommandPatterns.StatisticsCommands
  //       .topTwentyCommandLogic[F](
  //         sBotInfo = sBotConfig.sBotInfo,
  //         dbMedia = dbLayer.dbMedia
  //       )
  //       .widen
  //   case Timeout =>
  //     CommandPatterns.TimeoutCommand
  //       .timeoutLogic[F](
  //         msg = message,
  //         dbTimeout = dbLayer.dbTimeout,
  //         sBotInfo = sBotConfig.sBotInfo,
  //         ttl = ttl
  //       )
  //       .widen
  // }): F[List[ReplyValue]]
}
