package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply.given
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
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
    ): F[List[Message]] = reply match {
      case EffectfulKey.Random(sBotInfo) =>
        randomTelegraReply(
          dbLayer.dbMedia,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          sBotInfo = sBotInfo
        )

      case EffectfulKey.SearchShow(sBotInfo) =>
        searchShowTelegramReply(
          dbLayer.dbMedia,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage,
          sBotInfo = sBotInfo
        )
      case EffectfulKey.TriggerSearch(sBotInfo) => ???
      case EffectfulKey.Instructions(sBotInfo)  => ???
      case EffectfulKey.Subscribe(sBotInfo)     => ???
      case EffectfulKey.Unsubscribe(sBotInfo)   => ???
      case EffectfulKey.Subscriptions(sBotInfo) => ???
      case EffectfulKey.TopTwenty(sBotInfo)     => ???
      case EffectfulKey.Timeout(sBotInfo)       => ???
      case EffectfulKey.Custom(key, sBotInfo)   => ???
    }
  }

  def randomTelegraReply[F[_]: Async: LogWriter: Api](
      dbMedia: DBMedia[F],
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean,
      sBotInfo: SBotInfo
  ): F[List[Message]] = for {
    mediaFile <- RandomDataCommand.randomCommandLogic[F](dbMedia = dbMedia, sBotInfo = sBotInfo)
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
      replyToMessage: Boolean,
      sBotInfo: SBotInfo
  ): F[List[Message]] = ???

}
