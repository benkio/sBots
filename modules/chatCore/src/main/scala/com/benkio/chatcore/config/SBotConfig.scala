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
    requestProcessingTimeout: Option[FiniteDuration],
    sBotInfo: SBotInfo,
    triggersFilePath: Path,
    triggerListUri: Uri,
    instructionsFilePath: Path,
    instructionsUri: Uri,
    listJsonFilePath: Path,
    showFilePath: Path,
    repliesJsonFilePath: Path,
    commandsJsonFilePath: Path,
    token: String,
    startupCheck: StartupCheckConfig
) {
  def triggerFilename: String      = triggersFilePath.getFileName.toString
  def instructionsFilename: String = instructionsFilePath.getFileName.toString
  def listJsonFilename: String     = listJsonFilePath.getFileName.toString
  def showFilename: String         = showFilePath.getFileName.toString
  def calendarFilePath: Path       = showFilePath.resolveSibling(s"${sBotInfo.botId.value}_calendar.ics")
  def repliesJsonFilename: String  = repliesJsonFilePath.getFileName.toString
  def commandsJsonFilename: String = commandsJsonFilePath.getFileName.toString
}

object SBotConfig {

  /** The default time a request can take before it is dropped (see `BotEngine.onMessage`): also the threshold of the
    * performance tests.
    */
  val defaultRequestProcessingTimeout: FiniteDuration = 10.seconds
  private def findProjectRoot(path: Path): Path       = {
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
      instructionsFilePath: Path,
      instructionsUri: Uri,
      token: String,
      listJsonFilePath: Path,
      repliesJsonFilePath: Path,
      commandsJsonFilePath: Path,
      showFilePath: Path,
      messageTimeToLive: Option[FiniteDuration],
      ignoreMessagePrefix: Option[String],
      disableForward: Boolean,
      requestProcessingTimeout: Option[FiniteDuration]
  ): SBotConfig = new SBotConfig(
    disableForward = disableForward,
    ignoreMessagePrefix = ignoreMessagePrefix,
    messageTimeToLive = messageTimeToLive,
    requestProcessingTimeout = requestProcessingTimeout,
    sBotInfo = sBotInfo,
    triggersFilePath = resolvePath(triggersFilePath),
    triggerListUri = triggerListUri,
    instructionsFilePath = resolvePath(instructionsFilePath),
    instructionsUri = instructionsUri,
    listJsonFilePath = resolvePath(listJsonFilePath),
    showFilePath = resolvePath(showFilePath),
    repliesJsonFilePath = resolvePath(repliesJsonFilePath),
    commandsJsonFilePath = resolvePath(commandsJsonFilePath),
    token = token,
    startupCheck = StartupCheckConfig()
  )

  /** The config of a bot from its info: every file is named after the bot id and the bot name. */
  def fromSBotInfo(sBotInfo: SBotInfo): SBotConfig =
    SBotConfig(
      sBotInfo = sBotInfo,
      triggerFilename = s"${sBotInfo.botId}_triggers.md",
      triggerListUri = Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_triggers.md"
      ),
      instructionsFilename = s"${sBotInfo.botId}_instructions.md",
      instructionsUri = Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_instructions.md"
      ),
      listJsonFilename = s"${sBotInfo.botId}_list.json",
      showFilename = s"${sBotInfo.botId}_shows.json",
      repliesJsonFilename = s"${sBotInfo.botId}_replies.json",
      commandsJsonFilename = s"${sBotInfo.botId}_commands.json",
      token = s"${sBotInfo.botId}_${sBotInfo.botName}.token"
    )

  def apply(
      sBotInfo: SBotInfo,
      triggerFilename: String,
      triggerListUri: Uri,
      instructionsFilename: String,
      instructionsUri: Uri,
      token: String,
      listJsonFilename: String,
      repliesJsonFilename: String,
      commandsJsonFilename: String,
      showFilename: String,
      messageTimeToLive: Option[FiniteDuration] = 10.seconds.some,
      ignoreMessagePrefix: Option[String] = Some("!"),
      disableForward: Boolean = true,
      requestProcessingTimeout: Option[FiniteDuration] = Some(defaultRequestProcessingTimeout),
      startupCheck: StartupCheckConfig = StartupCheckConfig()
  ): SBotConfig = {
    val botRoot = Path
      .of("modules", "bots", sBotInfo.botName.value)
    new SBotConfig(
      disableForward = disableForward,
      ignoreMessagePrefix = ignoreMessagePrefix,
      messageTimeToLive = messageTimeToLive,
      requestProcessingTimeout = requestProcessingTimeout,
      startupCheck = startupCheck,
      sBotInfo = sBotInfo,
      triggersFilePath = resolvePath(botRoot.resolve(triggerFilename)),
      triggerListUri = triggerListUri,
      instructionsFilePath = resolvePath(botRoot.resolve(instructionsFilename)),
      instructionsUri = instructionsUri,
      listJsonFilePath = resolvePath(botRoot.resolve(listJsonFilename)),
      showFilePath = resolvePath(botRoot.resolve(showFilename)),
      repliesJsonFilePath =
        resolvePath(botRoot.resolve("src").resolve("main").resolve("resources").resolve(repliesJsonFilename)),
      commandsJsonFilePath =
        resolvePath(botRoot.resolve("src").resolve("main").resolve("resources").resolve(commandsJsonFilename)),
      token = token
    )
  }
}
