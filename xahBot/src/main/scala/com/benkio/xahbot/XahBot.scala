package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.high._

import scala.concurrent.ExecutionContext

class XahBotPolling[F[_]: Parallel: Async: Api] extends BotSkeletonPolling[F] with XahBot

class XahBotWebhook[F[_]: Async](api: Api[F], url: String, path: String = "/")
    extends BotSkeletonWebhook[F](api, url, path)
    with XahBot

trait XahBot extends BotSkeleton {

  override val resourceSource: ResourceSource = XahBot.resourceSource

  override def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand]] = List(
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
    )
  ).sequence[F, ReplyBundleCommand]

  override def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage]] = List.empty.pure[F]

  def buildRandomReplyBundleCommand[F[_]: Async](command: String, directory: String): F[ReplyBundleCommand] =
    ResourceSource
      .selectResourceAccess(XahBot.resourceSource)
      .getResourcesByKind[F](directory)
      .use[ReplyBundleCommand](mediaFile =>
        ReplyBundleCommand(
          CommandTrigger(command),
          mediaFile,
          replySelection = RandomSelection
        ).pure[F]
      )
}

object XahBot extends Configurations {

  val resourceSource: ResourceSource = FileSystem

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("xah_XahBot.token").map(_.map(_.toChar).mkString)
  def buildPollingBot[F[_]: Parallel: Async, A](
      action: XahBotPolling[F] => F[A]
  )(implicit executorContext: ExecutionContext): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new XahBotPolling[F])
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  ): Resource[F, XahBotWebhook[F]] = for {
    tk <- token[F]
    baseUrl     = s"https://api.telegram.org/bot$tk"
    path        = s"/$tk"
    api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
  } yield new XahBotWebhook[F](
    api = api,
    url = webhookBaseUrl + path,
    path = path
  )

}
