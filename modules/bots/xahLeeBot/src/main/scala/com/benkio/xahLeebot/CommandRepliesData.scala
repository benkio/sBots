package com.benkio.xahleebot

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.CommandInstructionSupportedLanguages
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
    ) ++ xahCustomCommands.map { case (command, instruction) =>
      MediaByKindCommand.mediaCommandByKind(
        dbMedia = dbMedia,
        botName = botName,
        commandName = command,
        kind = command.some,
        instruction = instruction
      )
    }

  val xahCustomCommands: List[(String, CommandInstructionSupportedLanguages)] =
    List(
      "alanmackenzie" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Alan Mackenzie"""",
        eng = """Returns a media file related to Alan Mackenzie"""
      ),
      "ass" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "ass"""",
        eng = """Returns a media file related to the word "ass""""
      ),
      "ccpp" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato al C e C++""",
        eng = """Returns a media file related to the C and C++"""
      ),
      "crap" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "crap"""",
        eng = """Returns a media file related to the word "crap""""
      ),
      "emacs" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Emacs""",
        eng = """Returns a media file related to Emacs"""
      ),
      "extra" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file extra""",
        eng = """Returns an extra media file"""
      ),
      "fak" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "fak"""",
        eng = """Returns a media file related to the word "fak""""
      ),
      "fakhead" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "fakhead"""",
        eng = """Returns a media file related to the word "fakhead""""
      ),
      "google" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Google""",
        eng = """Returns a media file related to Google"""
      ),
      "idiocy" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "idiocy"""",
        eng = """Returns a media file related to the word "idiocy""""
      ),
      "idiots" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "idiots"""",
        eng = """Returns a media file related to the word "idiots""""
      ),
      "laugh" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla risata di Xah Lee""",
        eng = """Returns a Xah Lee's laugh"""
      ),
      "linux" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Linux""",
        eng = """Returns a media file related to Linux"""
      ),
      "millennial" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato ai millennials""",
        eng = """Returns a media file related to the millennials"""
      ),
      "opensource" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato all'open source""",
        eng = """Returns a media file related to open source"""
      ),
      "opera" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Opera""",
        eng = """Returns a media file related to Opera"""
      ),
      "python" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Python""",
        eng = """Returns a media file related to Python"""
      ),
      "rantcompilation" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce una delle compilation di rant""",
        eng = """Returns a Xah Lee's rant compilation"""
      ),
      "richardstallman" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Richard Stallman""",
        eng = """Returns a media file related to Richard Stallman"""
      ),
      "sucks" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato alla parola "sucks"""",
        eng = """Returns a media file related to the word "sucks""""
      ),
      "unix" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato a Unix""",
        eng = """Returns a media file related to Unix"""
      ),
      "wtf" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato all'espressione "what the fak"""",
        eng = """Returns a media file related to the expression "what the fak""""
      ),
      "zoomer" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """Restituisce un media file correlato ai zoomers""",
        eng = """Returns a media file related to zoomers"""
      )
    )
}
