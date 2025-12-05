package com.benkio.telegrambotinfrastructure.http.telegramreply

import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply.given
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import cats.syntax.all.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message

object EffectfulKeyReply {

  given TelegramReply[EffectfulKey] = new TelegramReply[EffectfulKey] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: EffectfulKey,
        msg: Message,
        repository: Repository[F],
        dbLayer: DBLayer[F],
        replyToMessage: Boolean
    )(using botId: SBotId): F[List[Message]] = reply match {
      case EffectfulKey.Random =>
        randomTelegraReply(
          dbLayer.dbMedia,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )

      case EffectfulKey.SearchShow          =>
        searchShowTelegramReply(
          dbLayer.dbMedia,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case EffectfulKey.TriggerSearch       => ???
      case EffectfulKey.Instructions        => ???
      case EffectfulKey.Subscribe           => ???
      case EffectfulKey.Unsubscribe         => ???
      case EffectfulKey.Subscriptions       => ???
      case EffectfulKey.TopTwenty           => ???
      case EffectfulKey.Timeout             => ???
      case EffectfulKey.Custom(key: String) => ???
    }
  }

  def randomTelegraReply[F[_]: Async: LogWriter: Api](
      dbMedia: DBMedia[F],
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean
  )(using botId: SBotId): F[List[Message]] = for {
    mediaFile <- RandomDataCommand.randomCommandLogic[F](dbMedia = dbMedia, botId = botId)
    messages  <- TelegramReply[MediaFile].reply(
      reply = mediaFile,
      msg = msg,
      repository = repository,
      dbLayer = dbLayer,
      replyToMessage = replyToMessage
    )
  } yield messages

  def searchShowTelegramReply[F[_]: Async: LogWriter: Api](
      dbMedia: DBMedia[F],
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean
  )(using botId: SBotId): F[List[Message]] = ???

  
}
