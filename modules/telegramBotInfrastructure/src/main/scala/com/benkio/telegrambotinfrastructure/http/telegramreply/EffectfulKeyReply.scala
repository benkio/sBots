package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.StatisticsCommands
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TimeoutCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message

object EffectfulKeyReply {

  def sendEffectfulKey[F[_]: Async: LogWriter: Api](
      reply: EffectfulKey,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      replyToMessage: Boolean
  ): F[List[Message]] = reply match {
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
        repliesF = SearchShowCommand.searchShowCommandLogic(msg, dbLayer, sBotInfo),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix) =>
      sendTextReplies(
        repliesF = TriggerSearchCommand.searchTriggerLogic(replyBundleMessage, msg, ignoreMessagePrefix, sBotInfo),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands) =>
      sendTextReplies(
        repliesF = InstructionsCommand.instructionCommandLogic(
          msg = msg,
          sBotInfo = sBotInfo,
          ignoreMessagePrefix = ignoreMessagePrefix,
          commands = commands
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Subscribe(sBotInfo) =>
      sendTextReplies(
        repliesF = SubscribeUnsubscribeCommand.subscribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Unsubscribe(sBotInfo) =>

      sendTextReplies(
        repliesF = SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
          backgroundJobManager = backgroundJobManager,
          m = msg,
          sBotInfo = sBotInfo
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
      sendTextReplies(
        repliesF = StatisticsCommands.topTwentyCommandLogic(
          dbMedia = dbLayer.dbMedia,
          sBotInfo = sBotInfo
        ),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case EffectfulKey.Timeout(sBotInfo) =>
      sendTextReplies(
        repliesF = TimeoutCommand.timeoutLogic(
          msg = msg,
          dbTimeout = dbLayer.dbTimeout,
          sBotInfo = sBotInfo
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
        backgroundJobManager = backgroundJobManager
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
  ): F[List[Message]] = for {
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
      sBotInfo: SBotInfo
  ): F[List[Message]] = for {
    mediaFiles <- MediaByKindCommand.mediaCommandByKindLogic[F](
      dbMedia = dbLayer.dbMedia,
      commandName = commandKey,
      sBotInfo = sBotInfo
    )
    mediaFile <- RandomSelection.select(MediaReply(mediaFiles = mediaFiles))
    messages  <- TelegramReply.sendReplyValue(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      replyToMessage = replyToMessage,
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      effectfulCallbacks = effectfulCallbacks
    )
  } yield messages

  private def sendTextReplies[F[_]: Async: LogWriter: Api](
      repliesF: F[List[Text]],
      msg: Message,
      replyToMessage: Boolean
  ): F[List[Message]] =
    for replies <- repliesF
    messages    <- replies.flatTraverse(reply =>
      TextReply.sendText(
        reply = reply,
        msg = msg,
        replyToMessage = replyToMessage
      )
    )
    yield messages
}
