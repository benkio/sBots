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
        ita = """'/alanmackenzie': Restituisce un media file correlato a Alan Mackenzie"""",
        eng = """'/alanmackenzie': Returns a media file related to Alan Mackenzie"""
      ),
      "ass" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/ass': Restituisce un media file correlato alla parola "ass"""",
        eng = """'/ass': Returns a media file related to the word "ass""""
      ),
      "ccpp" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/ccpp': Restituisce un media file correlato al C e C++""",
        eng = """'/ccpp': Returns a media file related to the C and C++"""
      ),
      "crap" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/crap': Restituisce un media file correlato alla parola "crap"""",
        eng = """'/crap': Returns a media file related to the word "crap""""
      ),
      "emacs" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/emacs': Restituisce un media file correlato a Emacs""",
        eng = """'/emacs': Returns a media file related to Emacs"""
      ),
      "extra" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/extra': Restituisce un media file extra""",
        eng = """'/extra': Returns an extra media file"""
      ),
      "fak" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/fak': Restituisce un media file correlato alla parola "fak"""",
        eng = """'/fak': Returns a media file related to the word "fak""""
      ),
      "fakhead" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/fakhead': Restituisce un media file correlato alla parola "fakhead"""",
        eng = """'/fakhead': Returns a media file related to the word "fakhead""""
      ),
      "google" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/google': Restituisce un media file correlato a Google""",
        eng = """'/google': Returns a media file related to Google"""
      ),
      "idiocy" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/idiocy': Restituisce un media file correlato alla parola "idiocy"""",
        eng = """'/idiocy': Returns a media file related to the word "idiocy""""
      ),
      "idiots" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/idiots': Restituisce un media file correlato alla parola "idiots"""",
        eng = """'/idiots': Returns a media file related to the word "idiots""""
      ),
      "laugh" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/laugh': Restituisce un media file correlato alla risata di Xah Lee""",
        eng = """'/laugh': Returns a Xah Lee's laugh"""
      ),
      "linux" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/linux': Restituisce un media file correlato a Linux""",
        eng = """'/linux': Returns a media file related to Linux"""
      ),
      "millennial" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/millennial': Restituisce un media file correlato ai millennials""",
        eng = """'/millennial': Returns a media file related to the millennials"""
      ),
      "opensource" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/opensource': Restituisce un media file correlato all'open source""",
        eng = """'/opensource': Returns a media file related to open source"""
      ),
      "opera" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/opera': Restituisce un media file correlato a Opera""",
        eng = """'/opera': Returns a media file related to Opera"""
      ),
      "python" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/python': Restituisce un media file correlato a Python""",
        eng = """'/python': Returns a media file related to Python"""
      ),
      "rantcompilation" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/rantcompilation': Restituisce una delle compilation di rant""",
        eng = """'/rantcompilation': Returns a Xah Lee's rant compilation"""
      ),
      "richardstallman" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/richardstallman': Restituisce un media file correlato a Richard Stallman""",
        eng = """'/richardstallman': Returns a media file related to Richard Stallman"""
      ),
      "sucks" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/sucks': Restituisce un media file correlato alla parola "sucks"""",
        eng = """'/sucks': Returns a media file related to the word "sucks""""
      ),
      "unix" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/unix': Restituisce un media file correlato a Unix""",
        eng = """'/unix': Returns a media file related to Unix"""
      ),
      "wtf" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/wtf': Restituisce un media file correlato all'espressione "what the fak"""",
        eng = """'/wtf': Returns a media file related to the expression "what the fak""""
      ),
      "zoomer" -> CommandInstructionSupportedLanguages.Instructions(
        ita = """'/zoomer': Restituisce un media file correlato ai zoomers""",
        eng = """'/zoomer': Returns a media file related to zoomers"""
      )
    )
}
