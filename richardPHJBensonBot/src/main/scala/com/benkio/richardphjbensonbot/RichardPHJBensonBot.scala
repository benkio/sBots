package com.benkio.richardphjbensonbot

import cats.effect._
import cats.implicits._
import cats.MonadThrow
import cats._
import com.benkio.richardphjbensonbot.Config
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.default.Actions._
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeoutData
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure.BotSkeleton
import com.benkio.telegrambotinfrastructure.BotSkeletonPolling
import com.benkio.telegrambotinfrastructure.BotSkeletonWebhook
import doobie._
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.Message
import telegramium.bots.high._

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit syncF: Sync[F]): Message => F[Unit] = m =>
    dbLayer.dbTimeout.logLastInteraction(m.chat.id)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_, m) =>
      for {
        dbTimeout <- dbLayer.dbTimeout.getOrDefault(m.chat.id)
        timeout   <- MonadThrow[F].fromEither(Timeout(dbTimeout))
      } yield Timeout.isExpired(timeout)
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](uri, path)
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit syncF: Sync[F]): Message => F[Unit] = m =>
    dbLayer.dbTimeout.logLastInteraction(m.chat.id)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_, m) =>
      for {
        dbTimeout <- dbLayer.dbTimeout.getOrDefault(m.chat.id)
        timeout   <- MonadThrow[F].fromEither(Timeout(dbTimeout))
      } yield Timeout.isExpired(timeout)
}

trait RichardPHJBensonBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = RichardPHJBensonBot.botName
  override val botPrefix: String                   = RichardPHJBensonBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = RichardPHJBensonBot.ignoreMessagePrefix
  val linkSources: String                          = RichardPHJBensonBot.linkSources
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    RichardPHJBensonBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    RichardPHJBensonBot
      .commandRepliesData[F](
        resourceAccess,
        backgroundJobManager,
        dbLayer,
        linkSources
      )
      .pure[F]

}

object RichardPHJBensonBot extends BotOps {

  import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
  import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
  import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
  import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData

  val botName: String                     = "RichardPHJBensonBot"
  val botPrefix: String                   = "rphjb"
  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerListUri: Uri =
    uri"https://github.com/benkio/myTelegramBot/blob/master/richardPHJBensonBot/rphjb_triggers.txt"
  val linkSources: String = "rphjb_LinkSources"

  def messageRepliesData[F[_]]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesMixData[
      F
    ] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  val timeoutCommandDescriptionIta: String =
    "'/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00"
  val timeoutCommandDescriptionEng: String =
    "'/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00"
  val bensonifyCommandDescriptionIta: String =
    "'/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio"
  val bensonifyCommandDescriptionEng: String =
    "'/bensonify 《text》': Translate the text in the same way benson would write it. Text input is mandatory"

  def commandRepliesData[F[_]: Async](
      resourceAccess: ResourceAccess[F],
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      linkSources: String
  )(implicit
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      mdr = messageRepliesData
    ),
    StatisticsCommands.topTwentyReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    RandomLinkCommand.selectRandomLinkReplyBundleCommand(
      resourceAccess = resourceAccess,
      youtubeLinkSources = linkSources
    ),
    RandomLinkCommand.selectRandomLinkByKeywordsReplyBundleCommand(
      resourceAccess = resourceAccess,
      botName = botName,
      youtubeLinkSources = linkSources
    ),
    SubscribeUnsubscribeCommand.subscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        RandomLinkCommand.randomLinkCommandDescriptionIta,
        RandomLinkCommand.randomLinkKeywordCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionIta,
        timeoutCommandDescriptionIta,
        bensonifyCommandDescriptionIta,
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        RandomLinkCommand.randomLinkCommandDescriptionEng,
        RandomLinkCommand.randomLinkKeywordCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionEng,
        timeoutCommandDescriptionEng,
        bensonifyCommandDescriptionEng
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("timeout"),
      text = Some(
        TextReply[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "timeout",
              botName,
              t => {
                Timeout(msg.chat.id, t)
                  .fold(
                    error =>
                      log.info(s"[ERROR] While parsing the timeout input: $error") *> List(
                        s"Timeout set failed: wrong input format for $t, the input must be in the form '\timeout 00:00:00'"
                      ).pure[F],
                    timeout =>
                      dbLayer.dbTimeout.setTimeout(DBTimeoutData(timeout)) *> List("Timeout set successfully").pure[F]
                  )
              },
              """Input Required: the input must be in the form '\timeout 00:00:00'"""
            ),
          true
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("bensonify"),
      text = Some(
        TextReply[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "bensonify",
              botName,
              t => List(Bensonify.compute(t)).pure[F],
              "E PARLAAAAAAA!!!!"
            ),
          true
        )
      )
    )
  )
  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess
      .fromResources[F]()
      .getResourceByteArray("rphjb_RichardPHJBensonBot.token")
      .map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](
      token: String,
      httpClient: Client[F],
      resourceAccess: ResourceAccess[F],
      dbLayer: DBLayer[F]
  )

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, BotSetup[F]] =
    for {
      tk     <- token[F]
      config <- Resource.eval(Config.loadConfig[F])
      _      <- Resource.eval(log.info(s"[$botName] Configuration: $config"))
      transactor = Transactor.fromDriverManager[F](
        config.driver,
        config.url,
        "",
        ""
      )
      urlFetcher <- Resource.eval(UrlFetcher[F](httpClient))
      dbLayer    <- Resource.eval(DBLayer[F](transactor))
      resourceAccess = ResourceAccess.dbResources[F](dbLayer.dbMedia, urlFetcher)
      _                     <- Resource.eval(log.info(s"[$botName] Delete webook..."))
      deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
      _ <- Resource.eval(
        Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
          new RuntimeException(
            s"[$botName] The delete webhook request failed: " + deleteWebhookResponse.as[String]
          )
        )
      )
      _ <- Resource.eval(log.info(s"[$botName] Webhook deleted"))
    } yield BotSetup(token = tk, httpClient = httpClient, resourceAccess = resourceAccess, dbLayer = dbLayer)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: RichardPHJBensonBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield botSetup).use {
    case BotSetup(token, httpClient, resourceAccess, dbLayer) => {
      implicit val api: Api[F] = BotApi(
        httpClient,
        baseUrl = s"https://api.telegram.org/bot$token"
      )
      implicit val ra: ResourceAccess[F] = resourceAccess
      for {
        backgroundJobManager <- BackgroundJobManager[F](
          dbSubscription = dbLayer.dbSubscription,
          resourceAccess = resourceAccess,
          youtubeLinkSources = RichardPHJBensonBot.linkSources,
          botName = RichardPHJBensonBot.botName
        )
        result <- action(
          new RichardPHJBensonBotPolling[F](
            resAccess = resourceAccess,
            dbLayer = dbLayer,
            backgroundJobManager = backgroundJobManager
          )
        )
      } yield result
    }
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
  )(implicit log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl  <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${botSetup.token}")))
    path     <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${botSetup.token}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
    api       = BotApi(httpClient, baseUrl = baseUrl.renderString)
    resAccess = botSetup.resourceAccess
    backgroundJobManager <- {
      implicit val implApi: Api[F] = api
      implicit val implResAccess   = resAccess
      Resource.eval(
        BackgroundJobManager[F](
          dbSubscription = botSetup.dbLayer.dbSubscription,
          resourceAccess = botSetup.resourceAccess,
          youtubeLinkSources = RichardPHJBensonBot.linkSources,
          botName = RichardPHJBensonBot.botName
        )
      )
    }
  } yield {
    implicit val implApi: Api[F] = api
    implicit val implResAccess   = resAccess
    new RichardPHJBensonBotWebhook[F](
      uri = webhookBaseUri,
      path = path,
      resAccess = botSetup.resourceAccess,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = backgroundJobManager
    )
  }
}
