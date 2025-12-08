package com.benkio.telegrambotinfrastructure.model

import io.circe.*
import io.circe.generic.semiauto.*

enum CommandInstructionData {
  case Instructions(ita: String, eng: String) extends CommandInstructionData
  case NoInstructions                         extends CommandInstructionData
}

object CommandInstructionData {
  given decoder: Decoder[CommandInstructionData] = deriveDecoder[CommandInstructionData]
  given encoder: Encoder[CommandInstructionData] = deriveEncoder[CommandInstructionData]
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
