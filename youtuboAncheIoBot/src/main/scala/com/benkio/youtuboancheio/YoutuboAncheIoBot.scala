package com.benkio.youtuboancheiobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.client.Client
import org.http4s.ember.client._
import telegramium.bots.high._

class YoutuboAncheIoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    rAccess: ResourceAccess[F]
) extends BotSkeletonPolling[F]
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

class YoutuboAncheIoBotWebhook[F[_]: Async: Api: LogWriter](url: String, rAccess: ResourceAccess[F], path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

trait YoutuboAncheIoBot[F[_]] extends BotSkeleton[F] {

  override val ignoreMessagePrefix: Option[String] = YoutuboAncheIoBot.ignoreMessagePrefix

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YoutuboAncheIoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
//    List(
//      randomLinkByKeywordReplyBundleF,
//      randomLinkReplyBundleF
// ).sequence.map(cs => cs ++
    YoutuboAncheIoBot.commandRepliesData[F].pure[F]
  // )

  // TODO: Add commands
  // private def randomLinkReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
  //   ReplyBundleCommand(
  //     trigger = CommandTrigger("randomshow"),
  //     text = Some(
  //       TextReply[F](
  //         _ =>
  //           RandomLinkCommand
  //             .selectRandomLinkByKeyword[F](
  //               "",
  //               resourceAccess,
  //               "abar_LinkSources"
  //             )
  //             .use(optMessage => Applicative[F].pure(optMessage.toList)),
  //         true
  //       )
  //     ),
  //   ).pure[F]

  // private def randomLinkByKeywordReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
  //   ReplyBundleCommand[F](
  //     trigger = CommandTrigger("randomshowkeyword"),
  //     text = Some(
  //       TextReply[F](
  //         m =>
  //           handleCommandWithInput[F](
  //             m,
  //             "randomshowkeyword",
  //             "YoutuboAncheIoBot",
  //             keywords =>
  //               RandomLinkCommand
  //                 .selectRandomLinkByKeyword[F](
  //                   keywords,
  //                   resourceAccess,
  //                   "abar_LinkSources"
  //                 )
  //                 .use(_.foldl(List(s"Nessuna puntata/show contenente '$keywords' è stata trovata")) { case (_, v) =>
  //                   List(v)
  //                 }.pure[F]),
  //             s"Inserisci una keyword da cercare tra le puntate/shows"
  //           ),
  //         true
  //       )
  //     ),
  //   ).pure[F]

}
object YoutuboAncheIoBot extends BotOps {

  val ignoreMessagePrefix: Option[String] = Some("!")

  def messageRepliesAudioData[
      F[_] // : Applicative
  ]: List[ReplyBundleMessage[F]] = List(
  )

  def messageRepliesGifData[
      F[_] // : Applicative
  ]: List[ReplyBundleMessage[F]] = List(
  )

  def messageRepliesSpecialData[
      F[_] // : Applicative
  ]: List[ReplyBundleMessage[F]] = List(
  )

  def messageRepliesData[
      F[_] // : Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.ordering[F])
      .reverse

  // def messageReplyDataStringChunks[F[_]: Applicative]: List[String] = {
  //   val (triggers, lastTriggers) = messageRepliesData[F]
  //     .map(_.trigger match {
  //       case TextTrigger(lt @ _*) => lt.mkString("[", " - ", "]")
  //       case _                    => ""
  //     })
  //     .foldLeft((List.empty[String], "")) { case ((acc, candidate), triggerString) =>
  //       if ((candidate ++ triggerString).length > 4090)
  //         (acc :+ candidate, triggerString)
  //       else
  //         (acc, candidate ++ triggerString)
  //     }
  //   triggers :+ lastTriggers
  // }
  // TODO: Add Commands
  def commandRepliesData[
      F[_] // : Applicative
  ]: List[ReplyBundleCommand[F]] = List(
    // ReplyBundleCommand(
    //   trigger = CommandTrigger("triggerlist"),
    //   text = Some(
    //     TextReply(
    //       m => {
    //         if (m.chat.`type` == "private")
    //           Applicative[F].pure(messageReplyDataStringChunks[F])
    //         else
    //           Applicative[F].pure(List("puoi usare questo comando solo in chat privata"))
    //       },
    //       false
    //     )
    //   )
    // ),
    // ReplyBundleCommand(
    //   trigger = CommandTrigger("triggersearch"),
    //   text = Some(
    //     TextReply[F](
    //       m =>
    //         handleCommandWithInput[F](
    //           m,
    //           "triggersearch",
    //           "YoutuboAncheIoBot",
    //           t =>
    //             messageRepliesData[F]
    //               .collectFirstSome(replyBundle =>
    //                 replyBundle.trigger match {
    //                   case TextTrigger(textTriggers @ _*)
    //                       if MessageMatches.doesMatch(replyBundle, m, ignoreMessagePrefix) =>
    //                     Some(textTriggers.toList)
    //                   case _ => None
    //                 }
    //               )
    //               .fold(List(s"No matching trigger for $t"))((textTriggers: List[TextTriggerValue]) =>
    //                 textTriggers.map(_.toString)
    //               )
    //               .pure[F],
    //           """Input Required: Insert the test keyword to check if it's in some bot trigger"""
    //         ),
    //       false
    //     )
    //   )
    // ),
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("ytai_YoutuboAncheIoBot.token").map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, (String, ResourceAccess[F])] = for {
    tk     <- token[F]
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"YoutuboAncheIoBot Configuration: $config"))
    transactor = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      "",
      ""
    )
    urlFetcher            <- Resource.eval(UrlFetcher[F](httpClient))
    dbResourceAccess      <- Resource.eval(DBResourceAccess(transactor, urlFetcher))
    _                     <- Resource.eval(log.info("[YoutuboAncheIoBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          "[YoutuboAncheIoBot] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _ <- Resource.eval(log.info("[YoutuboAncheIoBot] Webhook deleted"))
  } yield (tk, dbResourceAccess)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: YoutuboAncheIoBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    tk_ra      <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk_ra._1, tk_ra._2)).use(httpClient_tk_ra => {
    implicit val api: Api[F] =
      BotApi(httpClient_tk_ra._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk_ra._2}")
    action(new YoutuboAncheIoBotPolling[F](httpClient_tk_ra._3))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, YoutuboAncheIoBotWebhook[F]] = for {
    tk_ra <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot${tk_ra._1}"
    path    = s"/${tk_ra._1}"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new YoutuboAncheIoBotWebhook[F](
      url = webhookBaseUrl + path,
      rAccess = tk_ra._2,
      path = path
    )
  }
}
