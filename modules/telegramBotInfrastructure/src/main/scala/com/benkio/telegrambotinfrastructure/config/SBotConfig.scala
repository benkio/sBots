package com.benkio.telegrambotinfrastructure.config

import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import org.http4s.Uri

import scala.concurrent.duration.*

case class SBotConfig(
    disableForward: Boolean,
    ignoreMessagePrefix: Option[String],
    messageTimeToLive: Option[FiniteDuration],
    sBotInfo: SBotInfo,
    triggerFilename: String,
    triggerListUri: Uri,
    repliesJsonFilename: String,
  commandsJsonFilename: String,
  token: String
)

object SBotConfig {
  def apply(
      sBotInfo: SBotInfo,
      triggerFilename: String,
    triggerListUri: Uri,
    token: String,
      repliesJsonFilename: String,
      commandsJsonFilename: String,
      messageTimeToLive: Option[FiniteDuration] = 10.seconds.some,
      ignoreMessagePrefix: Option[String] = Some("!"),
      disableForward: Boolean = true
  ): SBotConfig = SBotConfig(
    disableForward = disableForward,
    ignoreMessagePrefix = ignoreMessagePrefix,
    messageTimeToLive = messageTimeToLive,
    sBotInfo = sBotInfo,
    triggerFilename = triggerFilename,
    repliesJsonFilename = repliesJsonFilename,
    commandsJsonFilename = commandsJsonFilename,
    triggerListUri = triggerListUri,
    token = token
  )
}
