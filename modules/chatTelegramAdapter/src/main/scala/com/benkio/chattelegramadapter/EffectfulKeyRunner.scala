package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.chatcore.patterns.CommandPatterns.RandomDataCommand
import com.benkio.chatcore.patterns.CommandPatterns.SearchShowCommand
import com.benkio.chatcore.patterns.CommandPatterns.StatisticsCommands
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.patterns.CommandPatterns.TimeoutCommand
import com.benkio.chatcore.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BackgroundJobManager
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

object EffectfulKeyRunner {

  private def toReplyValue[F[_]: Functor, R <: ReplyValue](reply: F[R]): F[List[ReplyValue]] =
    reply.map(replyValue => List(replyValue: ReplyValue))

  private def toReplyValues[F[_]: Functor, R <: ReplyValue](replies: F[List[R]]): F[List[ReplyValue]] =
    replies.map(_.map(reply => reply: ReplyValue))

  def runEffectfulKey[F[_]: Async: LogWriter](
      effectfulKey: EffectfulKey,
      msg: Message,
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration]
  ): F[List[ReplyValue]] = effectfulKey match {
    case EffectfulKey.Random(sBotInfo) =>
      toReplyValue(
        RandomDataCommand.randomCommandLogic[F](
          dbMedia = dbLayer.dbMedia,
          sBotInfo = sBotInfo
        )
      )
    case EffectfulKey.SearchShow(sBotInfo) =>
      toReplyValue(
        SearchShowCommand.searchShowCommandLogic(
          msg = msg,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
    case EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix) =>
      toReplyValue(
        TriggerSearchCommand.searchTriggerLogic(
          mdr = replyBundleMessage,
          m = msg,
          ignoreMessagePrefix = ignoreMessagePrefix,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
    case EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands) =>
      toReplyValue(
        InstructionsCommand.instructionCommandLogic(
          msg = msg,
          sBotInfo = sBotInfo,
          ignoreMessagePrefix = ignoreMessagePrefix,
          commands = commands,
          ttl = ttl
        )
      )
    case EffectfulKey.Subscribe(sBotInfo) =>
      toReplyValue(
        SubscribeUnsubscribeCommand.subscribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
    case EffectfulKey.Unsubscribe(sBotInfo) =>
      toReplyValue(
        SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
    case EffectfulKey.Subscriptions(sBotInfo) =>
      toReplyValue(
        SubscribeUnsubscribeCommand.subscriptionsCommandLogic(
          dbSubscription = dbLayer.dbSubscription,
          backgroundJobManager = backgroundJobManager,
          sBotInfo = sBotInfo,
          m = msg
        )
      )
    case EffectfulKey.TopTwenty(sBotInfo, _) =>
      toReplyValues(
        StatisticsCommands.topTwentyCommandLogic(
          dbMedia = dbLayer.dbMedia,
          sBotInfo = sBotInfo
        )
      )
    case EffectfulKey.Timeout(sBotInfo) =>
      toReplyValue(
        TimeoutCommand.timeoutLogic(
          msg = msg,
          dbTimeout = dbLayer.dbTimeout,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
    case EffectfulKey.MediaByKind(key, sBotInfo) =>
      toReplyValue(
        MediaByKindCommand.mediaCommandByKindLogic[F](
          dbMedia = dbLayer.dbMedia,
          commandName = key,
          sBotInfo = sBotInfo
        )
      )
    case EffectfulKey.Callback(key, sBotInfo) =>
      effectfulCallbacks
        .get(key)
        .fold(
          Async[F].raiseError(
            Throwable(
              s"[EffectfulKeyReply] callback not found. Check your sync between commands and callback in the ${sBotInfo.botName} code"
            )
          )
        )(callback => toReplyValue(callback(msg)))
  }

}
