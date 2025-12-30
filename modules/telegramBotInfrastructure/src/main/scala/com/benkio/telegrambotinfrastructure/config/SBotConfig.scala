package com.benkio.telegrambotinfrastructure.config

import org.http4s.Uri
import scala.concurrent.duration.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import cats.syntax.all.*

case class SBotConfig(
  disableForward      : Boolean,
  ignoreMessagePrefix : Option[String],
  messageTimeToLive   : Option[FiniteDuration],
  sBotInfo            : SBotInfo,
  triggerFilename     : String,
  triggerListUri      : Uri
)

object SBotConfig {
  def apply(
  sBotInfo: SBotInfo,
  triggerFilename: String,
    triggerListUri: Uri,
    messageTimeToLive: Option[FiniteDuration] = 10.seconds.some,
  ignoreMessagePrefix: Option[String] = Some("!"),
    disableForward : Boolean = true,
  ) : SBotConfig = SBotConfig(
disableForward      = disableForward      ,
ignoreMessagePrefix = ignoreMessagePrefix ,
messageTimeToLive   = messageTimeToLive   ,
sBotInfo            = sBotInfo            ,
triggerFilename     = triggerFilename     ,
triggerListUri      = triggerListUri      

)
}
