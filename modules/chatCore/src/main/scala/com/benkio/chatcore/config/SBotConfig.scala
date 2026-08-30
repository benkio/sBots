package com.benkio.chatcore.config

import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo
import org.http4s.Uri

import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.*

case class SBotConfig(
    disableForward: Boolean,
    ignoreMessagePrefix: Option[String],
    messageTimeToLive: Option[FiniteDuration],
    sBotInfo: SBotInfo,
    triggersFilePath: Path,
    triggerListUri: Uri,
    listJsonFilePath: Path,
    showFilePath: Path,
    repliesJsonFilePath: Path,
    commandsJsonFilePath: Path,
    token: String
) {
  def triggerFilename: String      = triggersFilePath.getFileName.toString
  def listJsonFilename: String     = listJsonFilePath.getFileName.toString
  def showFilename: String         = showFilePath.getFileName.toString
  def repliesJsonFilename: String  = repliesJsonFilePath.getFileName.toString
  def commandsJsonFilename: String = commandsJsonFilePath.getFileName.toString
}

object SBotConfig {
  private def findProjectRoot(path: Path): Path = {
    val isProjectRoot =
      Files.exists(path.resolve("build.sbt")) &&
        Files.isDirectory(path.resolve("modules"))
    if isProjectRoot then path
    else
      Option(path.getParent) match {
        case Some(parent) => findProjectRoot(parent)
        case None         => path
      }
  }

  private lazy val projectRootPath: Path =
    findProjectRoot(Path.of("").toAbsolutePath.normalize())

  private def resolvePath(path: Path): Path =
    if path.isAbsolute then path.normalize()
    else projectRootPath.resolve(path).normalize()

  def apply(
      sBotInfo: SBotInfo,
      triggersFilePath: Path,
      triggerListUri: Uri,
      token: String,
      listJsonFilePath: Path,
      repliesJsonFilePath: Path,
      commandsJsonFilePath: Path,
      showFilePath: Path,
      messageTimeToLive: Option[FiniteDuration],
      ignoreMessagePrefix: Option[String],
      disableForward: Boolean
  ): SBotConfig = new SBotConfig(
    disableForward = disableForward,
    ignoreMessagePrefix = ignoreMessagePrefix,
    messageTimeToLive = messageTimeToLive,
    sBotInfo = sBotInfo,
    triggersFilePath = resolvePath(triggersFilePath),
    listJsonFilePath = resolvePath(listJsonFilePath),
    showFilePath = resolvePath(showFilePath),
    repliesJsonFilePath = resolvePath(repliesJsonFilePath),
    commandsJsonFilePath = resolvePath(commandsJsonFilePath),
    triggerListUri = triggerListUri,
    token = token
  )

  def apply(
      sBotInfo: SBotInfo,
      triggerFilename: String,
      triggerListUri: Uri,
      token: String,
      listJsonFilename: String,
      repliesJsonFilename: String,
      commandsJsonFilename: String,
      showFilename: String,
      messageTimeToLive: Option[FiniteDuration] = 10.seconds.some,
      ignoreMessagePrefix: Option[String] = Some("!"),
      disableForward: Boolean = true
  ): SBotConfig = {
    val botRoot = Path
      .of("modules", "bots", sBotInfo.botName.value)
    new SBotConfig(
      sBotInfo = sBotInfo,
      triggersFilePath = resolvePath(botRoot.resolve(triggerFilename)),
      triggerListUri = triggerListUri,
      token = token,
      listJsonFilePath = resolvePath(botRoot.resolve(listJsonFilename)),
      repliesJsonFilePath =
        resolvePath(botRoot.resolve("src").resolve("main").resolve("resources").resolve(repliesJsonFilename)),
      commandsJsonFilePath =
        resolvePath(botRoot.resolve("src").resolve("main").resolve("resources").resolve(commandsJsonFilename)),
      showFilePath = resolvePath(botRoot.resolve(showFilename)),
      messageTimeToLive = messageTimeToLive,
      ignoreMessagePrefix = ignoreMessagePrefix,
      disableForward = disableForward
    )
  }
}
