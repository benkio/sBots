package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.BotEngine
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object ComputeReply {

  // Entry points ///////////////////////////////////////////////////////////////////////////////////////////////////

  /** The general one: any bundle, plain or effectful. */
  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle,
      message: Message,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = replyBundle.reply match {
    case plainReply: PlainReply =>
      executePlain(
        reply = plainReply,
        message = message,
        repository = repository
      )
    case effectfulReply: EffectfulReply =>
      executeEffectful(
        reply = effectfulReply,
        message = message,
        repository = repository,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl
      )
  }

  /** For the replies that are just data (the ones of a `ReplyBundleMessage`): it needs nothing but the repository to
    * send the media. `replyToMessage` overrides the one of the reply, e.g. when the message is a synthetic one.
    */
  def executePlain[F[_]: Async: LogWriter: Api](
      reply: PlainReply,
      message: Message,
      repository: Repository[F],
      replyToMessage: Option[Boolean] = None
  ): F[List[TMessage]] =
    for {
      replyValue <- runPlainReply[F](reply)
      result     <- send(
        replyValue = replyValue,
        message = message,
        repository = repository,
        replyToMessage = replyToMessage.getOrElse(reply.replyToMessage)
      )
    } yield result

  /** For the replies that need an effectful computation (the ones of a `ReplyBundleCommand`). */
  def executeEffectful[F[_]: Async: LogWriter: Api](
      reply: EffectfulReply,
      message: Message,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] =
    for {
      replyValue <- runEffectfulReply(
        reply = reply,
        msg = message,
        backgroundJobManager = backgroundJobManager,
        dbLayer = dbLayer,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl
      )
      result <- send(
        replyValue = replyValue,
        message = message,
        repository = repository,
        replyToMessage = reply.replyToMessage
      )
    } yield result

  private def send[F[_]: Async: LogWriter: Api](
      replyValue: ReplyValue,
      message: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] =
    TelegramMessageReply.sendReplyValue[F](
      replyValue = replyValue,
      msg = message,
      repository = repository,
      replyToMessage = replyToMessage
    )

  // Computations ///////////////////////////////////////////////////////////////////////////////////////////////////

  def runPlainReply[F[_]: Sync](reply: PlainReply): F[ReplyValue] = BotEngine.runPlainReply[F](reply)

  def runEffectfulReply[F[_]: Async: LogWriter](
      reply: EffectfulReply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration],
      overridePage: Option[Int] = None
  ): F[ReplyValue] =
    EffectfulKeyRunner
      .runEffectfulKey[F](
        effectfulKey = reply.key.overridePage(overridePage),
        msg = msg,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl
      )
      .flatMap(RandomSelection.select)

  def runReply[F[_]: Async: LogWriter](
      reply: Reply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration],
      overridePage: Option[Int] = None
  ): F[ReplyValue] = reply match {
    case plainReply: PlainReply         => runPlainReply[F](plainReply)
    case effectfulReply: EffectfulReply =>
      runEffectfulReply(
        reply = effectfulReply,
        msg = msg,
        backgroundJobManager = backgroundJobManager,
        dbLayer = dbLayer,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl,
        overridePage = overridePage
      )
  }

}
