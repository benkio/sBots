package com.benkio.chatcore.patterns

import cats.data.NonEmptyList
import cats.effect.std.Random
import cats.implicits.*
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.addTimestamp
import com.benkio.chatcore.model.show.RandomQuery
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowQuery
import com.benkio.chatcore.model.show.ShowQueryKeyword
import com.benkio.chatcore.model.show.SimpleShowQuery
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.*
import log.effect.LogWriter

import scala.concurrent.duration.*

/** Commands that search the bot content: shows and triggers. */
object SearchCommands {

  object SearchShowCommand {

    private[patterns] val searchShowCommandIta: String =
      s"'/searchshow': Documentazione: ${CommandDocs.chatCoreCommandDocLink("searchshow", "it")}"
    private[patterns] val searchShowCommandEng: String =
      s"'/searchshow': Documentation: ${CommandDocs.chatCoreCommandDocLink("searchshow", "en")}"

    def searchShowCommandLogic[F[_]: MonadThrow: LogWriter](
        msg: Message,
        dbLayer: DBLayer[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        showTransformation: String => NonEmptyList[Show] => F[ReplyValue],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] =
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = msg,
        command = CommandKey.SearchShow.asString,
        sBotInfo = sBotInfo,
        ttl = ttl,
        computation = keywords =>
          SearchShowCommand
            .selectLinkByKeyword[F](
              keywords = keywords,
              dbShow = dbLayer.dbShow,
              sBotInfo = sBotInfo,
              ttl = ttl,
              showTransformation = showTransformation(keywords)
            ): F[ReplyValue],
        defaultReply = "Input non riconosciuto. Controlla le instruzioni per i dettagli",
        allowEmptyString = true
      )

    private[patterns] def searchShowReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.SearchShow.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.SearchShow(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = searchShowCommandIta,
          eng = searchShowCommandEng
        )
      )

    def defaultShowTransformation[F[_]: MonadThrow: Random](shows: NonEmptyList[Show]): F[ReplyValue] =
      Random[F].elementOf(shows.toList).map(show => Text(show.show): ReplyValue)

    def selectLinkByKeyword[F[_]: MonadThrow](
        keywords: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        showTransformation: NonEmptyList[Show] => F[ReplyValue]
    )(using log: LogWriter[F]): F[ReplyValue] = {
      val query: ShowQuery            = ShowQuery(keywords)
      val dbCall: F[List[DBShowData]] = query match {
        case RandomQuery         => dbShow.getRandomShow(sBotInfo.botId).map(_.toList)
        case q: SimpleShowQuery  => dbShow.getShowBySimpleShowQuery(q, sBotInfo.botId)
        case q: ShowQueryKeyword => dbShow.getShowByShowQuery(q, sBotInfo.botId)
      }

      for {
        _           <- log.info(s"Select random Show: ${sBotInfo.botId} - $keywords - $query")
        dbResults   <- dbCall
        showResults <- dbResults.traverse(Show.apply[F](_).map(_.addTimestamp(query)))
        output      <- NonEmptyList
          .fromList(showResults)
          .fold(
            MonadThrow[F].pure(
              Text(
                value = s"Nessuna puntata/show contenente '$keywords' è stata trovata",
                timeToLive = ttl
              )
            )
          )(showTransformation(_))
      } yield output
    }

    /** The show of the bot with the given id, otherwise the result of the keyword search (a random show when the target
      * is empty).
      */
    def selectShowByIdOrKeyword[F[_]: MonadThrow: Random](
        target: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    )(using log: LogWriter[F]): F[ReplyValue] = {
      val trimmedTarget = target.trim
      for {
        byId <- Option.when(trimmedTarget.nonEmpty)(trimmedTarget).flatTraverse(dbShow.getShowById)
        // a show id of another bot is not a match
        ownShow = byId.filter(_.bot_id == sBotInfo.botId.value)
        reply <- ownShow.fold(
          selectLinkByKeyword[F](
            keywords = trimmedTarget,
            dbShow = dbShow,
            sBotInfo = sBotInfo,
            ttl = ttl,
            showTransformation = defaultShowTransformation[F]
          ): F[ReplyValue]
        )(dbShowData => Show.apply[F](dbShowData).map(show => Text(show.show): ReplyValue))
      } yield reply
    }
  }

  object TriggerSearchCommand {

    private[patterns] val triggerSearchCommandDescriptionIta: String =
      s"'/triggersearch': Documentazione: ${CommandDocs.chatCoreCommandDocLink("triggersearch", "it")}"
    private[patterns] val triggerSearchCommandDescriptionEng: String =
      s"'/triggersearch': Documentation: ${CommandDocs.chatCoreCommandDocLink("triggersearch", "en")}"

    def searchTriggerLogic[F[_]: ApplicativeThrow](
        mdr: List[ReplyBundleMessage],
        m: Message,
        ignoreMessagePrefix: Option[String],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        replyBundleTransformation: ReplyBundleMessage => ReplyValue = replyBundleMessage =>
          Text(replyBundleMessage.prettyPrint()),
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {

      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = m,
        command = CommandKey.TriggerSearch.asString,
        sBotInfo = sBotInfo,
        ttl = ttl,
        computation = t => {
          // match the input only, not the whole command message (it would match words of the command itself)
          val inputMessage = m.copy(text = Some(t), caption = None)
          val matches      = mdr
            .mapFilter(MessageMatches.doesMatch(_, inputMessage, ignoreMessagePrefix))
            .sortBy { case (trigger, replyBundleMessage) =>
              -Trigger.triggerLongestString(trigger, replyBundleMessage.matcher)
            }
            .headOption
          matches.fold(Text(value = s"No matching trigger for $t", timeToLive = ttl).pure[F]) { case (_, rbm) =>
            replyBundleTransformation(rbm).pure[F]
          }
        },
        defaultReply = """Input Required: Insert the test keyword to check if it's in some bot trigger"""
      )
    }

    // TODO: #782 Return the closest match on failure
    private[patterns] def triggerSearchReplyBundleCommand(
        sBotInfo: SBotInfo,
        replyBundleMessage: List[ReplyBundleMessage],
        ignoreMessagePrefix: Option[String]
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TriggerSearch.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = triggerSearchCommandDescriptionIta,
          eng = triggerSearchCommandDescriptionEng
        )
      )

  }
}
