package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.KeyboardReply
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object ComputeReply {

  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle,
      message: Message,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = for {
    replyValue <- runReply(
      reply = replyBundle.reply,
      msg = message,
      backgroundJobManager = backgroundJobManager,
      dbLayer = dbLayer,
      effectfulCallbacks = effectfulCallbacks,
      ttl = ttl
    )
    result <-
      TelegramMessageReply.sendReplyValue[F](
        replyValue = replyValue,
        msg = message,
        repository = repository,
        replyToMessage = replyBundle.reply.replyToMessage
      )
  } yield result

  def runReply[F[_]: Async: LogWriter](
      reply: Reply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration],
      overridePage: Option[Int] = None
  ): F[ReplyValue] = reply match {
    case EffectfulReply(EffectfulKey.TopTwenty(sBotInfo, page), _) =>
      EffectfulKeyRunner
        .runEffectfulKey[F](
          effectfulKey = EffectfulKey.TopTwenty(sBotInfo),
          msg = msg,
          dbLayer = dbLayer,
          backgroundJobManager = backgroundJobManager,
          effectfulCallbacks = effectfulCallbacks,
          ttl = ttl
        )
        .map(mediaValues =>
          TelegramInlineKeyboard(
            keyboardTitle = "-----Top 20 Triggers-----",
            inlineKeyboard = KeyboardReply.buildInlineKeyboard(
              data = mediaValues,
              page = overridePage.getOrElse(page),
              commandKey = CommandKey.TopTwenty
            )
          )
        )
    case EffectfulReply(key, _) =>
      EffectfulKeyRunner
        .runEffectfulKey[F](
          effectfulKey = key,
          msg = msg,
          dbLayer = dbLayer,
          backgroundJobManager = backgroundJobManager,
          effectfulCallbacks = effectfulCallbacks,
          ttl = ttl
        )
        .flatMap(RandomSelection.select)
    case textReply: TextReply   => RandomSelection.select(textReply.text)
    case mediaReply: MediaReply => RandomSelection.select(mediaReply.mediaFiles)
  }

}
