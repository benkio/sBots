package com.benkio.xahbot

import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import cats.effect._
import cats._
import org.http4s.client.blaze._
import scala.concurrent.ExecutionContext
import telegramium.bots.high._

class XahBot[F[_]]()(implicit
    timerF: Timer[F],
    parallelF: Parallel[F],
    effectF: Effect[F],
    api: telegramium.bots.high.Api[F]
) extends BotSkeleton[F]()(timerF, parallelF, effectF, api) {

  override val resourceSource: ResourceSource = XahBot.resourceSource

  override lazy val commandRepliesData: List[ReplyBundleCommand] = List.empty

  override lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty
}

object XahBot extends Configurations {

  val resourceSource: ResourceSource = FileSystem

  def token[F[_]](implicit effectF: Effect[F]): Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("xah_XahBot.token").map(_.map(_.toChar).mkString)
  def buildBot[F[_], A](
      executorContext: ExecutionContext,
      action: XahBot[F] => F[A]
  )(implicit
      timerF: Timer[F],
      parallelF: Parallel[F],
      contextShiftF: ContextShift[F],
      concurrentEffectF: ConcurrentEffect[F]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new XahBot[F]())
  })
}
