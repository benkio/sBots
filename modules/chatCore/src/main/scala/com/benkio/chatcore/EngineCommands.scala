package com.benkio.chatcore

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatternsGroup
import com.benkio.chatcore.repository.db.DBLayer
import log.effect.LogWriter

/** The command reply bundles of the bot: the ones of the data entry, loaded once with the setup and served from memory
  * by the DBLayer, and the fixed ones, built from the message replies (the trigger list) and the instructions.
  *
  * @param messageRepliesData
  *   the message reply bundles the trigger list command is made of; read on first use of the fixed commands
  */
private[chatcore] trait EngineCommandReplies[F[_]] {

  def commandRepliesData: List[ReplyBundleCommand]
  def fixedCommands: List[ReplyBundleCommand]
  def allCommandRepliesData: List[ReplyBundleCommand]
}

/** The [[EngineCommandReplies]] built from the DB and the configuration of the bot. */
private[chatcore] class DefaultEngineCommandReplies[F[_]](
    sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    messageRepliesData: () => List[ReplyBundleMessage]
) extends EngineCommandReplies[F] {

  // Loaded once with the setup of the bot and served from memory by the DBLayer
  def commandRepliesData: List[ReplyBundleCommand] = dbLayer.dbCommandReplies.commandReplies

  lazy val fixedCommands: List[ReplyBundleCommand] = {
    val commandsPattersGroup =
      CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = sBotConfig.triggerListUri,
        sBotInfo = sBotConfig.sBotInfo,
        messageRepliesData = messageRepliesData(),
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
      )
    commandsPattersGroup :+ InstructionsCommand.instructionsReplyBundleCommand(
      sBotInfo = sBotConfig.sBotInfo,
      instructionsUri = sBotConfig.instructionsUri
    )
  }

  lazy val allCommandRepliesData: List[ReplyBundleCommand] =
    commandRepliesData ++ fixedCommands
}

/** The commands section of the [[BotEngine]]: the messages written as a command or as a file request.
  *
  * A command is answered by the guided `/subscribe` flow when the platform supports it and the command has no
  * arguments, otherwise by the reply bundle of the command, otherwise by the text mode commands of the platforms
  * without buttons (`!next`, `!pick`). A file request is answered with the media file it names. Both return whether a
  * reply was sent.
  *
  * @param commandReplies
  *   the command reply bundles the commands are selected from
  * @param textMode
  *   the text mode commands, the last resort of a command that matched no bundle
  * @param subscriptionFlow
  *   the guided flow the bare `/subscribe` starts
  * @param delivery
  *   sends the replies
  */
private[chatcore] trait EngineCommands[F[_]] {

  /** @return whether a reply was sent */
  def commandLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean]

  /** @return whether a reply was sent */
  def fileRequestLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean]
}

/** The [[EngineCommands]] that answers from the command replies, the guided flow and the text mode. */
private[chatcore] class DefaultEngineCommands[F[_]: Async: LogWriter](
    sBotConfig: SBotConfig,
    commandReplies: EngineCommandReplies[F],
    textMode: PagedListTextMode[F],
    subscriptionFlow: EngineSubscriptionFlow[F],
    delivery: EngineDelivery[F]
) extends EngineCommands[F] {

  /** @return whether a reply was sent */
  def commandLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    subscriptionFlow
      .requester(msg, platform)
      .filter(_ => subscriptionFlow.isBareSubscribeCommand(msg, platform))
      .fold(commandReplyBundles(msg)) { requester =>
        subscriptionFlow.start(msg, requester).as(true)
      }

  private def commandReplyBundles(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    ReplyBundleCommand
      .selectCommandReplyBundle(
        msg = msg,
        allCommandRepliesData = commandReplies.allCommandRepliesData,
        botName = sBotConfig.sBotInfo.botName,
        syntax = platform.capabilities.commandSyntax
      )
      .traverse(commandReply =>
        LogWriter.info(
          s"${sBotConfig.sBotInfo.botName}: Computing command ${msg.text} matching command reply bundle"
        ) *> delivery.reply(commandReply.reply, msg).as(true)
      )
      .flatMap(_.fold(textMode.handleCommand(msg))(_.pure[F]))

  /** @return whether a reply was sent */
  def fileRequestLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    msg.getContent
      .traverse(content =>
        delivery
          .deliver(
            replyValue = MediaFile.fromString(platform.capabilities.commandSyntax.fileRequestContent(content)),
            msg = msg,
            replyToMessage = true
          )
          .as(true)
      )
      .map(_.getOrElse(false))
}
