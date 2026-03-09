package com.benkio.chatcore.model

import cats.syntax.all.*
import cats.effect.Async
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager

import scala.annotation.unused
import scala.concurrent.duration.FiniteDuration
import com.benkio.chatcore.patterns.CommandPatterns
import log.effect.LogWriter

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
      @unused repository: Repository[F],
      @unused backgroundJobManager: BackgroundJobManager[F],
      @unused effectfulCallbacks: Map[String, ModelMessage => F[List[Text]]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[ReplyValue]] =
    (commandKey match {
      case Random =>
        CommandPatterns.RandomDataCommand
          .randomCommandLogic(dbMedia = dbLayer.dbMedia, sBotInfo = sBotConfig.sBotInfo)
          .map(List(_))
      case SearchShow =>
        CommandPatterns.SearchShowCommand
          .searchShowCommandLogic(msg = message, dbLayer = dbLayer, sBotInfo = sBotConfig.sBotInfo, ttl = ttl)
          .widen
      case TriggerList =>
        Async[F]
          .pure(
            List(
              CommandPatterns.TriggerListCommand.triggerListLogic(
                sBotConfig.triggerListUri
              )
            )
          )
          .widen
      case TriggerSearch => ??? // searchTriggerLogic(...)
      case Instructions  => ??? // instructionCommandLogic(...)
      case Subscribe     => ??? // subscribeCommandLogic(...)
      case Unsubscribe   => ??? // unsubscribeCommandLogic(...)
      case Subscriptions => ??? // subscriptionsCommandLogic(...)
      case TopTwenty     => ??? // topTentyCommandLogic(...)
      case Timeout       => ??? // timeoutCommandLogic(...)
    }): F[List[ReplyValue]]
}
