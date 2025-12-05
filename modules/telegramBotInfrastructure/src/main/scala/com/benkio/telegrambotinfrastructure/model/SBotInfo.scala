package com.benkio.telegrambotinfrastructure.model

import io.circe.Encoder
import io.circe.Decoder

opaque type SBotName = String

object SBotName {
  def apply(value: String): SBotName               = value
  extension (sBotName: SBotName) def value: String = sBotName
  given Encoder[SBotName] = Encoder[String].contramap(identity)
  given Decoder[SBotName] = Decoder[String].map(identity)
}

opaque type SBotId = String

object SBotId {
  def apply(value: String): SBotId             = value
  extension (sBotId: SBotId) def value: String = sBotId
  given Encoder[SBotId] = Encoder[String].contramap(identity)
  given Decoder[SBotId] = Decoder[String].map(identity)
}
