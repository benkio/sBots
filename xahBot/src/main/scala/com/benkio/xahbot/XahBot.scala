package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.high._

class XahBotPolling[F[_]: Parallel: Async: Api: LogWriter] extends BotSkeletonPolling[F] with XahBot

class XahBotWebhook[F[_]: Async: Api: LogWriter](url: String, path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with XahBot

trait XahBot extends BotSkeleton {

  override def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand[F]]] = List(
    buildRandomReplyBundleCommand(
      "ass",
      "Ass",
    ),
    buildRandomReplyBundleCommand(
      "ccpp",
      "CC++",
    ),
    buildRandomReplyBundleCommand(
      "crap",
      "Crap",
    ),
    buildRandomReplyBundleCommand(
      "emacs",
      "Emacs",
    ),
    buildRandomReplyBundleCommand(
      "fakhead",
      "Fakhead",
    ),
    buildRandomReplyBundleCommand(
      "fak",
      "Fak",
    ),
    buildRandomReplyBundleCommand(
      "idiocy",
      "Idiocy",
    ),
    buildRandomReplyBundleCommand(
      "idiot",
      "Idiots",
    ),
    buildRandomReplyBundleCommand(
      "laugh",
      "Laugh",
    ),
    buildRandomReplyBundleCommand(
      "linux",
      "Linux",
    ),
    buildRandomReplyBundleCommand(
      "millennial",
      "Millennial",
    ),
    buildRandomReplyBundleCommand(
      "opensource",
      "OpenSource"
    ),
    buildRandomReplyBundleCommand(
      "python",
      "Python"
    ),
    buildRandomReplyBundleCommand(
      "rantcompilation",
      "RantCompilation"
    ),
    buildRandomReplyBundleCommand(
      "sucks",
      "Sucks"
    ),
    buildRandomReplyBundleCommand(
      "unix",
      "Unix"
    ),
    buildRandomReplyBundleCommand(
      "wtf",
      "WTF"
    ),
    buildRandomReplyBundleCommand(
      "extra",
      "Extra"
    ),
    randomLinkByKeywordReplyBundleF,
    randomLinkReplyBundleF
  ).sequence[F, ReplyBundleCommand[F]]

  override def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage[F]]] = List.empty.pure[F]

  private def randomLinkReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    RandomLinkCommand
      .selectRandomLinkByKeyword[F](
        "",
        resourceAccess,
        "xah_LinkSources"
      )
      .use[ReplyBundleCommand[F]](optMessage =>
        ReplyBundleCommand(
          trigger = CommandTrigger("randomshow"),
          text = Some(TextReply[F](_ => Applicative[F].pure(optMessage.toList), true)),
        ).pure[F]
      )

  private def buildRandomReplyBundleCommand[F[_]: Async](command: String, directory: String): F[ReplyBundleCommand[F]] =
    resourceAccess
      .getResourcesByKind[F](directory)
      .use[ReplyBundleCommand[F]](files =>
        ReplyBundleCommand[F](
          CommandTrigger(command),
          files.map(f => MediaFile(f.getPath)),
          replySelection = RandomSelection
        ).pure[F]
      )

  private def randomLinkByKeywordReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger("randomshowkeyword"),
      text = Some(
        TextReply[F](
          m =>
            handleCommandWithInput[F](
              m,
              "randomshowkeyword",
              "XahLeeBot",
              keywords =>
                RandomLinkCommand
                  .selectRandomLinkByKeyword[F](
                    keywords,
                    resourceAccess,
                    "xah_LinkSources"
                  )
                  .use(_.foldl(List(s"Nessuna puntata/show contenente '$keywords' è stata trovata")) { case (_, v) =>
                    List(v)
                  }.pure[F]),
              s"Inserisci una keyword da cercare tra le puntate/shows"
            ),
          true
        )
      ),
    ).pure[F]
}

object XahBot extends BotOps {

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray[F]("xah_XahBot.token").map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](httpClient: Client[F])(implicit log: LogWriter[F]): Resource[F, String] = for {
    tk                    <- token[F]
    _                     <- Resource.eval(log.info("[CalandroBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[CalandroBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[CalandroBot] Webhook deleted"))
  } yield tk

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: XahBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- BlazeClientBuilder[F].resource
    tk         <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk)).use(httpClient_tk => {
    implicit val api: Api[F] = BotApi(httpClient_tk._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk._2}")
    action(new XahBotPolling[F])
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, XahBotWebhook[F]] = for {
    tk <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot$tk"
    path    = s"/$tk"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new XahBotWebhook[F](
      url = webhookBaseUrl + path,
      path = path
    )
  }

}
