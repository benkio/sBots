package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.BotEngine
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

object ComputeReply {

  // Computations ///////////////////////////////////////////////////////////////////////////////////////////////////

  def runPlainReply[F[_]: Sync](reply: PlainReply): F[ReplyValue] = BotEngine.runPlainReply[F](reply)

  def runEffectfulReply[F[_]: Async: LogWriter](
      reply: EffectfulReply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration],
      commandSyntax: CommandSyntax,
      overridePage: Option[Int] = None
  ): F[ReplyValue] =
    EffectfulKeyRunner
      .runEffectfulKey[F](
        effectfulKey = reply.key.overridePage(overridePage),
        msg = msg,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl,
        commandSyntax = commandSyntax
      )
      .flatMap(RandomSelection.select)

  def runReply[F[_]: Async: LogWriter](
      reply: Reply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
      ttl: Option[FiniteDuration],
      commandSyntax: CommandSyntax,
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
        commandSyntax = commandSyntax,
        overridePage = overridePage
      )
  }

}
