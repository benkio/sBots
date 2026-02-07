package com.benkio.telegrambotinfrastructure.config

import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import org.http4s.Uri

import scala.concurrent.duration.*
import fs2.io.file.Path

case class SBotConfig(
    disableForward: Boolean,
    ignoreMessagePrefix: Option[String],
    messageTimeToLive: Option[FiniteDuration],
    sBotInfo: SBotInfo,
    triggerFilename: Path,
    triggerListUri: Uri,
    triggerJsonFilename: Path
)

object SBotConfig {
  def apply(
      sBotInfo: SBotInfo,
      triggerFilename: String,
    triggerListUri: Uri,
    triggerJsonFilename: String,
      messageTimeToLive: Option[FiniteDuration] = 10.seconds.some,
      ignoreMessagePrefix: Option[String] = Some("!"),
      disableForward: Boolean = true
  ): SBotConfig = SBotConfig(
    disableForward = disableForward,
    ignoreMessagePrefix = ignoreMessagePrefix,
    messageTimeToLive = messageTimeToLive,
    sBotInfo = sBotInfo,
    triggerFilename = triggerFilename,
    triggerJsonFilename = triggerJsonFilename,
    triggerListUri = triggerListUri
  )
}
