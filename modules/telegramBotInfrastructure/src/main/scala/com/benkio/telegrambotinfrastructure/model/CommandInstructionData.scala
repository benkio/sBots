package com.benkio.telegrambotinfrastructure.model

enum CommandInstructionData {
  case Instructions(ita: String, eng: String) extends CommandInstructionData
  case NoInstructions                         extends CommandInstructionData
}

extension (instructions: CommandInstructionData) {
  def toIta: Option[String] = instructions match {
    case CommandInstructionData.Instructions(ita, _) => Some(ita)
    case _                                           => None
  }
  def toEng: Option[String] = instructions match {
    case CommandInstructionData.Instructions(_, eng) => Some(eng)
    case _                                           => None
  }
}
