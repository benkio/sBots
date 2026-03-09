package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.chatcore.patterns.CommandPatterns.RandomDataCommand
import com.benkio.chatcore.patterns.CommandPatterns.SearchShowCommand
import com.benkio.chatcore.patterns.CommandPatterns.StatisticsCommands
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.patterns.CommandPatterns.TimeoutCommand
import com.benkio.chatcore.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object EffectfulKeyReply {

  def sendEffectfulKey[F[_]: Async: LogWriter: Api](
      reply: EffectfulKey,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      replyToMessage: Boolean,
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = reply match {
    case EffectfulKey.Random(sBotInfo) =>
      randomTelegraReply(
        msg = msg,
        repository = repository,
        dbLayer = dbLayer,
        replyToMessage = replyToMessage,
        sBotInfo = sBotInfo
      )

    case EffectfulKey.SearchShow(sBotInfo) =>
      sendTextReplies(
        repliesF = SearchShowCommand.searchShowCommandLogic(
          msg = msg,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix) =>
      sendTextReplies(
        repliesF = TriggerSearchCommand.searchTriggerLogic(
          mdr = replyBundleMessage,
          m = msg,
          ignoreMessagePrefix = ignoreMessagePrefix,
          sBotInfo = sBotInfo,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands) =>
      sendTextReplies(
        repliesF = InstructionsCommand.instructionCommandLogic(
          msg = msg,
          sBotInfo = sBotInfo,
          ignoreMessagePrefix = ignoreMessagePrefix,
          commands = commands,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Subscribe(sBotInfo) =>
      sendTextReplies(
        repliesF = SubscribeUnsubscribeCommand.subscribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Unsubscribe(sBotInfo) =>

      sendTextReplies(
        repliesF = SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Subscriptions(sBotInfo) =>
      sendTextReplies(
        repliesF = SubscribeUnsubscribeCommand.subscriptionsCommandLogic(
          dbSubscription = dbLayer.dbSubscription,
          backgroundJobManager = backgroundJobManager,
          sBotInfo = sBotInfo,
          m = msg
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.TopTwenty(sBotInfo) =>
      sendKeyboardReplies(
        repliesF = StatisticsCommands.topTwentyCommandLogic(
          dbMedia = dbLayer.dbMedia,
          sBotInfo = sBotInfo
        ),
        commandKey = CommandKey.TopTwenty,
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Timeout(sBotInfo) =>
      sendTextReplies(
        repliesF = TimeoutCommand.timeoutLogic(
          msg = msg,
          dbTimeout = dbLayer.dbTimeout,
          sBotInfo = sBotInfo,
          ttl = ttl
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.MediaByKind(key, sBotInfo) =>
      mediaByKindReply(
        msg = msg,
        repository = repository,
        dbLayer = dbLayer,
        replyToMessage = replyToMessage,
        commandKey = key,
        sBotInfo = sBotInfo,
        effectfulCallbacks = effectfulCallbacks,
        backgroundJobManager = backgroundJobManager,
        ttl = ttl
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
        )(callback =>
          sendTextReplies(
            repliesF = callback(msg),
            msg = msg,
            replyToMessage = replyToMessage
          )
        )
  }

  private def randomTelegraReply[F[_]: Async: LogWriter: Api](
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean,
      sBotInfo: SBotInfo
  ): F[List[TMessage]] = for {
    mediaFile <- RandomDataCommand.randomCommandLogic[F](dbMedia = dbLayer.dbMedia, sBotInfo = sBotInfo)
    messages  <- MediaFileReply.sendMediaFile(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      replyToMessage = replyToMessage
    )
  } yield messages

  private def mediaByKindReply[F[_]: Async: LogWriter: Api](
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      replyToMessage: Boolean,
      commandKey: String,
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = for {
    mediaFile <- MediaByKindCommand.mediaCommandByKindLogic[F](
      dbMedia = dbLayer.dbMedia,
      commandName = commandKey,
      sBotInfo = sBotInfo
    )
    messages <- TelegramMessageReply.sendReplyValue(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      replyToMessage = replyToMessage,
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      effectfulCallbacks = effectfulCallbacks,
      ttl = ttl
    )
  } yield messages

  private def sendTextReplies[F[_]: Async: LogWriter: Api](
      repliesF: F[List[Text]],
      msg: Message,
      replyToMessage: Boolean
  ): F[List[TMessage]] =
    for replies <- repliesF
    messages    <- replies.flatTraverse(reply =>
      TextReply.sendText(
        reply = reply,
        msg = msg,
        replyToMessage = replyToMessage
      )
    )
    yield messages

  private def sendKeyboardReplies[F[_]: Async: LogWriter: Api](
      repliesF: F[List[MediaFile]],
      commandKey: CommandKey,
      msg: Message,
      replyToMessage: Boolean
  ): F[List[TMessage]] =
    for {
      replies  <- repliesF
      messages <-
        KeyboardReply.sendKeyboard(
          reply = replies,
          keyboardTitle = "Top 20",
          msg = msg,
          replyToMessage = replyToMessage,
          commandKey = commandKey
        )
    } yield messages
}
