package com.benkio.telegrambotinfrastructure.http.telegramreply

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply.given
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply.given
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message
import annotation.unused

object EffectfulKeyReply {

  given TelegramReply[EffectfulKey] = new TelegramReply[EffectfulKey] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: EffectfulKey,
        msg: Message,
        repository: Repository[F],
        dbLayer: DBLayer[F],
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
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix) =>
        sendTextReplies(
          repliesF = TriggerSearchCommand.searchTriggerLogic(replyBundleMessage, msg, ignoreMessagePrefix),
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case EffectfulKey.Instructions(sBotInfo, commands, ignoreMessagePrefix)  =>
        sendTextReplies(
          repliesF = instructionCommandLogic(
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commands = commands
          ),
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case EffectfulKey.Subscribe(sBotInfo)     => ???
      case EffectfulKey.Unsubscribe(sBotInfo)   => ???
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
      replyToMessage: Boolean,
      sBotInfo: SBotInfo
  ): F[List[Message]] = for {
    mediaFile <- RandomDataCommand.randomCommandLogic[F](dbMedia = dbLayer.dbMedia, sBotInfo = sBotInfo)
    messages  <- TelegramReply[MediaFile].reply(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      dbLayer = dbLayer,
      replyToMessage = replyToMessage
    )
  } yield messages

    private def sendTextReplies[F[_]: Async: LogWriter: Api](
      repliesF: F[List[Text]],
      msg: Message,
      repository: Repository[F],
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
        replyToMessage = replyToMessage
      ))
      yield messages
}
