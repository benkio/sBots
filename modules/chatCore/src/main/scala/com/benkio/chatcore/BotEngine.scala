package com.benkio.chatcore

import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatternsGroup
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.repository.db.DBLayer
import log.effect.LogWriter

/** The logic of a bot, whatever the platform: what to reply to a message, a command or a file request. The platform is
  * reached through the [[PlatformAdapter]] only: to send and compute the replies, and for the way it writes the
  * commands (see [[com.benkio.chatcore.model.CommandSyntax]]).
  *
  * @param filteringMatchesMessages
  *   whether the reply bundle selected for a message can be used, e.g. not while the chat is in timeout
  * @param postComputation
  *   what to do after a message is handled, e.g. logging the last interaction of the chat
  */
class BotEngine[F[_]: Async: LogWriter](
    val sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean],
    postComputation: Message => F[Unit]
) {

  // Replies data ////////////////////////////////////////////////////////

  // Loaded once with the setup of the bot and served from memory by the DBLayer
  def messageRepliesData: List[ReplyBundleMessage] = dbLayer.dbMessageReplies.messageReplies
  def commandRepliesData: List[ReplyBundleCommand] = dbLayer.dbCommandReplies.commandReplies

  lazy val fixedCommands: List[ReplyBundleCommand] = {
    val commandsPattersGroup =
      CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = sBotConfig.triggerListUri,
        sBotInfo = sBotConfig.sBotInfo,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
      )
    commandsPattersGroup :+ InstructionsCommand.instructionsReplyBundleCommand(
      sBotInfo = sBotConfig.sBotInfo,
      commands = commandRepliesData ++ commandsPattersGroup,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    )
  }

  lazy val allCommandRepliesData: List[ReplyBundleCommand] =
    commandRepliesData ++ fixedCommands

  // Bot logic ////////////////////////////////////////////////////////////

  def messageLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    ReplyBundleMessage
      .selectReplyBundle(
        msg = msg,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix,
        disableForward = sBotConfig.disableForward
      )
      .traverse_(replyBundle =>
        for {
          _ <- LogWriter
            .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ")
          filter <- filteringMatchesMessages(replyBundle, msg)
          _      <- if filter then reply(replyBundle.reply, msg) else Async[F].unit
        } yield ()
      )

  def commandLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    ReplyBundleCommand
      .selectCommandReplyBundle(
        msg = msg,
        allCommandRepliesData = allCommandRepliesData,
        botName = sBotConfig.sBotInfo.botName,
        syntax = platform.capabilities.commandSyntax
      )
      .traverse_(commandReply =>
        LogWriter.info(
          s"${sBotConfig.sBotInfo.botName}: Computing command ${msg.text} matching command reply bundle"
        ) *> reply(commandReply.reply, msg)
      )

  def fileRequestLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    msg.getContent.traverse_(content =>
      deliver(
        replyValue = MediaFile.fromString(platform.capabilities.commandSyntax.fileRequestContent(content)),
        msg = msg,
        replyToMessage = true
      )
    )

  def botLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) match {
      case MessageType.Message =>
        LogWriter.trace("Message Type: Message") >> messageLogic(msg)
      case MessageType.Command =>
        LogWriter.trace("Message Type: Command") >> commandLogic(msg)
      case MessageType.FileRequest =>
        LogWriter.trace("Message Type: File Request") >> fileRequestLogic(msg)
    }

  /** The entry point: a message arrived. */
  def onMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = for {
    _ <- LogWriter.trace(s"${sBotConfig.sBotInfo.botName}: A message arrived: $msg")
    _ <- LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}")
    _ <- botLogic(msg)
    _ <- postComputation(msg)
  } yield ()

  private def reply(reply: Reply, msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    for {
      replyValue <- reply match {
        case plainReply: PlainReply         => BotEngine.runPlainReply[F](plainReply)
        case effectfulReply: EffectfulReply => platform.runEffectful(effectfulReply, msg)
      }
      _ <- deliver(replyValue = replyValue, msg = msg, replyToMessage = reply.replyToMessage)
    } yield ()

  // What the platform can send: what is not supported is adapted to it, see the ReplyTransformer
  private def deliver(replyValue: ReplyValue, msg: Message, replyToMessage: Boolean)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    ReplyAdaptation
      .adaptToPlatform(dbLayer, platform.capabilities)(replyValue)
      .flatMap(_.traverse_(platform.send(_, msg, replyToMessage)))
}

object BotEngine {

  /** The engine with the standard filtering (the timeout of the chat) and post computation (log the last interaction).
    */
  def apply[F[_]: Async: LogWriter](sBotConfig: SBotConfig, dbLayer: DBLayer[F]): BotEngine[F] =
    new BotEngine[F](
      sBotConfig = sBotConfig,
      dbLayer = dbLayer,
      filteringMatchesMessages = FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId),
      postComputation = PostComputationPatterns.timeoutPostComputation(
        dbTimeout = dbLayer.dbTimeout,
        sBotId = sBotConfig.sBotInfo.botId
      )
    )

  /** Picks one of the values of a reply that is just data. */
  def runPlainReply[F[_]: Sync](reply: PlainReply): F[ReplyValue] = reply match {
    case textReply: TextReply   => RandomSelection.select(textReply.text)
    case mediaReply: MediaReply => RandomSelection.select(mediaReply.mediaFiles)
  }
}
