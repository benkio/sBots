package com.benkio.xahleebot

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
import cats.effect.Async
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.model.*
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogWriter
import cats.syntax.all.*

object CommandRepliesData {

  def values[F[_]: Async](
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      botName: String
  )(using
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
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "alanmackenzie",
      kind = "alanmackenzie".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "ass",
      kind = "ass".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "ccpp",
      kind = "ccpp".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "crap",
      kind = "crap".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "emacs",
      kind = "emacs".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "fakhead",
      kind = "fakhead".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "fak",
      kind = "fak".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "google",
      kind = "google".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "idiocy",
      kind = "idiocy".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "idiots",
      kind = "idiots".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "laugh",
      kind = "laugh".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "linux",
      kind = "linux".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "millennial",
      kind = "millennial".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "zoomer",
      kind = "zoomer".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "opensource",
      kind = "opensource".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "opera",
      kind = "opera".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "python",
      kind = "python".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "richardstallman",
      kind = "richardstallman".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "sucks",
      kind = "sucks".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "unix",
      kind = "unix".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "wtf",
      kind = "wtf".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "extra",
      kind = "extra".some
    ),
    MediaByKindCommand.mediaCommandByKind(
      dbMedia = dbLayer.dbMedia,
      botName = botName,
      commandName = "rantcompilation",
      kind = "rantcompilation".some
    )
  )
}
