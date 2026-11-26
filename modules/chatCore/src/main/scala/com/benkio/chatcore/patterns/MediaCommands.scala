package com.benkio.chatcore.patterns

import cats.effect.Async
import cats.implicits.*
import cats.MonadThrow
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.*
import log.effect.LogWriter

/** Commands that answer with the bot media: by kind, random and top twenty. */
object MediaCommands {

  object MediaByKindCommand {

    def mediaCommandByKindLogic[F[_]: Async](
        dbMedia: DBMedia[F],
        commandName: String,
        sBotInfo: SBotInfo
    )(using log: LogWriter[F]): F[ReplyValue] =
      for {
        _            <- log.debug(s"[MediaCommandByKind] Fetching DBMediaData for $commandName")
        dbMediaDatas <- dbMedia.getMediaByKind(kind = commandName, botId = sBotInfo.botId)
        _            <- log.debug("[MediaCommandByKind] Convert to Media")
        medias       <- dbMediaDatas.traverse(dbMediaData => Async[F].fromEither(Media(dbMediaData)))
        mediaFiles = medias.map(media => MediaFile.fromMimeType(media))
        mediaFile <- RandomSelection.select(replies = mediaFiles)
      } yield mediaFile

    def mediaCommandByKind(
        commandName: String,
        instruction: CommandInstructionData,
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
        reply = EffectfulReply(
          key = EffectfulKey.MediaByKind(commandName, sBotInfo)
        ),
        instruction = instruction
      )
  }

  object RandomDataCommand {

    case object RandomMediaNotFound
        extends Throwable(
          "[CommandPatterns] RandomDataCommand En error occurred when fetching random media. None Was returned"
        )

    private[patterns] val randomDataCommandIta: String =
      s"'/random': Documentazione: ${CommandDocs.chatCoreCommandDocLink("random", "it")}"
    private[patterns] val randomDataCommandEng: String =
      s"'/random': Documentation: ${CommandDocs.chatCoreCommandDocLink("random", "en")}"

    def randomCommandLogic[F[_]: Async: LogWriter](dbMedia: DBMedia[F], sBotInfo: SBotInfo): F[MediaFile] =
      for {
        _              <- LogWriter.debug(s"[RandomCommand] Fetching random media for ${sBotInfo.botId}")
        dbMediaDataOpt <- dbMedia.getRandomMedia(sBotInfo.botId)
        _              <- LogWriter.debug("[RandomCommand] Convert DBMediaData to Media")
        media          <- Async[F]
          .fromOption(dbMediaDataOpt, RandomMediaNotFound)
          .flatMap(dbMediaData => Async[F].fromEither(Media(dbMediaData)))
      } yield MediaFile.fromMimeType(media)

    def randomDataReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Random.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Random(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = randomDataCommandIta,
          eng = randomDataCommandEng
        )
      )
  }

  object StatisticsCommands {

    private[patterns] val topTwentyTriggersCommandDescriptionIta: String =
      s"'/toptwenty': Documentazione: ${CommandDocs.chatCoreCommandDocLink("toptwenty", "it")}"
    private[patterns] val topTwentyTriggersCommandDescriptionEng: String =
      s"'/toptwenty': Documentation: ${CommandDocs.chatCoreCommandDocLink("toptwenty", "en")}"

    def topTwentyCommandLogic[F[_]: MonadThrow](sBotInfo: SBotInfo, dbMedia: DBMedia[F]): F[List[MediaFile]] =
      for {
        dbMedias <- dbMedia.getMediaByMediaCount(botId = sBotInfo.botId.some)
        medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
      } yield medias.map(media => MediaFile.fromMimeType(media))

    private[patterns] def topTwentyReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TopTwenty.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.TopTwenty(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = topTwentyTriggersCommandDescriptionIta,
          eng = topTwentyTriggersCommandDescriptionEng
        )
      )

  }
}
