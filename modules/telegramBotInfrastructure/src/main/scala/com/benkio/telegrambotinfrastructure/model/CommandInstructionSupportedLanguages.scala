package com.benkio.telegrambotinfrastructure.model

enum CommandInstructionSupportedLanguages:
  case Instructions(ita: String, eng: String) extends CommandInstructionSupportedLanguages
  case NoInstructions                         extends CommandInstructionSupportedLanguages

extension (instructions: CommandInstructionSupportedLanguages)
  def toIta: Option[String] = instructions match {
    case CommandInstructionSupportedLanguages.Instructions(ita, _) => Some(ita)
    case _                                                         => None
  }
  def toEng: Option[String] = instructions match {
    case CommandInstructionSupportedLanguages.Instructions(_, eng) => Some(eng)
    case _                                                         => None
  }
