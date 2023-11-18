package com.benkio.xahleebot

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.model.*
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogWriter

object CommandRepliesData {

  def values[F[_]: Async](
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      botName: String
  )(implicit
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    RandomLinkCommand.searchShowReplyBundleCommand[F](
      dbShow = dbLayer.dbShow,
      botName = botName,
    ),
    SubscribeUnsubscribeCommand.subscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand[F](
      dbSubscription = dbLayer.dbSubscription,
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("alanmackenzie"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("ass"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("ccpp"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("crap"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("emacs"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("fakhead"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("fak"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("google"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("idiocy"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("idiots"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("laugh"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("linux"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("millennial"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("opensource"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("opera"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("python"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("richardstallman"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("sucks"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("unix"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("wtf"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("extra"),
      reply = ???
    ),
    ReplyBundleCommand[F](
      trigger = CommandTrigger("rantcompilation"),
      reply = ???
    ),
  )
}
