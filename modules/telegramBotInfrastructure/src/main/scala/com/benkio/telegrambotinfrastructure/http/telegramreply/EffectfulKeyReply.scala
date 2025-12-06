package com.benkio.telegrambotinfrastructure.http.telegramreply

import com.benkio.telegrambotinfrastructure.repository.Repository

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply.given
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply.given
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message

object EffectfulKeyReply {

  given TelegramReply[EffectfulKey] = new TelegramReply[EffectfulKey] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: EffectfulKey,
        msg: Message,
        repository: Repository[F],
        dbLayer: DBLayer[F],
        backgroundJobManager: BackgroundJobManager[F],
        replyToMessage: Boolean,
    ): F[List[Message]] = reply match {
      case EffectfulKey.Random(sBotInfo) =>
        randomTelegraReply(
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          sBotInfo = sBotInfo,
          backgroundJobManager=backgroundJobManager
        )

      case EffectfulKey.SearchShow(sBotInfo) =>
        sendTextReplies(
          repliesF = SearchShowCommand.searchShowCommandLogic(msg, dbLayer, sBotInfo),
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          backgroundJobManager=backgroundJobManager
        )
      case EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix) =>
        sendTextReplies(
          repliesF = TriggerSearchCommand.searchTriggerLogic(replyBundleMessage, msg, ignoreMessagePrefix, sBotInfo),
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          backgroundJobManager=backgroundJobManager
        )
      case EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands)  =>
        sendTextReplies(
          repliesF = InstructionsCommand.instructionCommandLogic(
            msg = msg,
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commands = commands
          ),
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          backgroundJobManager=backgroundJobManager
        )
      case EffectfulKey.Subscribe(sBotInfo)     =>
        
            sendTextReplies(
              repliesF = SubscribeUnsubscribeCommand.subscribeCommandLogic(
                backgroundJobManager = backgroundJobManager,
                m = msg,
                sBotInfo = sBotInfo
              ),
              msg = msg,
              repository = repository,
              dbLayer = dbLayer,
              replyToMessage = replyToMessage,
              backgroundJobManager=backgroundJobManager
            )
      case EffectfulKey.Unsubscribe(sBotInfo)   =>
 
            sendTextReplies(
              repliesF = SubscribeUnsubscribeCommand.unsubcribeCommandLogic(
                backgroundJobManager = backgroundJobManager,
                m = msg,
                sBotInfo = sBotInfo
              ),
              msg = msg,
              repository = repository,
              dbLayer = dbLayer,
              replyToMessage = replyToMessage,
              backgroundJobManager=backgroundJobManager
            )
      case EffectfulKey.Subscriptions(sBotInfo) => ???
      case EffectfulKey.TopTwenty(sBotInfo)     => ???
      case EffectfulKey.Timeout(sBotInfo)       => ???
      case EffectfulKey.Custom(key, sBotInfo)   => ???
    }
  }

  def randomTelegraReply[F[_]: Async: LogWriter: Api](
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
    backgroundJobManager: BackgroundJobManager[F],
      replyToMessage: Boolean,
    sBotInfo: SBotInfo,
  ): F[List[Message]] = for {
    mediaFile <- RandomDataCommand.randomCommandLogic[F](dbMedia = dbLayer.dbMedia, sBotInfo = sBotInfo)
    messages  <- TelegramReply[MediaFile].reply(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      dbLayer = dbLayer,
      backgroundJobManager=backgroundJobManager,
      replyToMessage = replyToMessage
    )
  } yield messages

    private def sendTextReplies[F[_]: Async: LogWriter: Api](
      repliesF: F[List[Text]],
      msg: Message,
      repository: Repository[F],
    backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean
    ): F[List[Message]] =
      for
      replies <- repliesF
      messages <- replies.flatTraverse(reply => TelegramReply[Text].reply(
        reply = reply,
        msg = msg,
        repository = repository,
        dbLayer = dbLayer,
        backgroundJobManager=backgroundJobManager,
        replyToMessage = replyToMessage
      ))
      yield messages
}
