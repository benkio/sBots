package com.benkio.xahleebot

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatternsGroup
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import log.effect.LogWriter

object CommandRepliesData {

  def values[F[_]: Async](
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      botName: String,
      botPrefix: String
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    CommandPatternsGroup.ShowGroup.group[F](
      dbShow = dbLayer.dbShow,
      dbSubscription = dbLayer.dbSubscription,
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ) ++ customCommands(dbMedia = dbLayer.dbMedia, botName = botName, botPrefix = botPrefix)

  def customCommands[F[_]: Async](
      dbMedia: DBMedia[F],
      botName: String,
      botPrefix: String
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] =
    List(
      RandomDataCommand.randomDataReplyBundleCommand[F](
        botPrefix = botPrefix,
        dbMedia = dbMedia
      )
    ) ++ xahCustomCommands.map(command =>
      MediaByKindCommand.mediaCommandByKind(
        dbMedia = dbMedia,
        botName = botName,
        commandName = command,
        kind = command.some
      )
    )

  val xahCustomCommands: List[String] =
    List(
      "alanmackenzie",
      "ass",
      "ccpp",
      "crap",
      "emacs",
      "extra",
      "fak",
      "fakhead",
      "google",
      "idiocy",
      "idiots",
      "laugh",
      "linux",
      "millennial",
      "opensource",
      "opera",
      "python",
      "rantcompilation",
      "richardstallman",
      "sucks",
      "unix",
      "wtf",
      "zoomer"
    )
}
