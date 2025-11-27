package com.benkio.telegrambotinfrastructure.model

opaque type SBotName = String

object SBotName {
  def apply(value: String): SBotName               = value
  extension (sBotName: SBotName) def value: String = sBotName
}

opaque type SBotId = String

object SBotId {
  def apply(value: String): SBotId             = value
  extension (sBotId: SBotId) def value: String = sBotId
}
