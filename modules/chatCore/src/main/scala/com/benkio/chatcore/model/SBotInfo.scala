package com.benkio.chatcore.model

import io.circe.generic.semiauto.*
import io.circe.Decoder
import io.circe.Encoder

final case class SBotInfo(botId: SBotInfo.SBotId, botName: SBotInfo.SBotName)

object SBotInfo {

  given Decoder[SBotInfo] = deriveDecoder[SBotInfo]
  given Encoder[SBotInfo] = deriveEncoder[SBotInfo]

  opaque type SBotName = String

  object SBotName {
    def apply(value: String): SBotName               = value
    extension (sBotName: SBotName) def value: String = sBotName
    given Encoder[SBotName]                          = Encoder.encodeString.contramap(_.value)
    given Decoder[SBotName]                          = Decoder.decodeString.map(SBotName(_))
  }

  opaque type SBotId = String

  object SBotId {
    def apply(value: String): SBotId             = value
    extension (sBotId: SBotId) def value: String = sBotId
    given Encoder[SBotId]                        = Encoder.encodeString.contramap(_.value)
    given Decoder[SBotId]                        = Decoder.decodeString.map(SBotId(_))
  }

}
