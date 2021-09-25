package com.benkio.xahbot

import cats._
import cats.effect._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import org.http4s.blaze.client._
import telegramium.bots.high._

import scala.concurrent.ExecutionContext

class XahBot[F[_]: Parallel: Async: Api] extends BotSkeleton[F] {

  override val resourceSource: ResourceSource = XahBot.resourceSource

  override lazy val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      CommandTrigger("ass"),
      getRandomMediaFile("Ass"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("ccpp"),
      getRandomMediaFile("CC++"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("crap"),
      getRandomMediaFile("Crap"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("emacs"),
      getRandomMediaFile("Emacs"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("fakhead"),
      getRandomMediaFile("Fakhead"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("fak"),
      getRandomMediaFile("Fak"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("idiocy"),
      getRandomMediaFile("Idiocy"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("idiot"),
      getRandomMediaFile("Idiots"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("laugh"),
      getRandomMediaFile("Laugh"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("linux"),
      getRandomMediaFile("Linux"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("millennial"),
      getRandomMediaFile("Millennial"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("opensource"),
      getRandomMediaFile("OpenSource"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("python"),
      getRandomMediaFile("Python"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("rantcompilation"),
      getRandomMediaFile("RantCompilation"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("sucks"),
      getRandomMediaFile("Sucks"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("unix"),
      getRandomMediaFile("Unix"),
      replySelection = RandomSelection
    ),
    ReplyBundleCommand(
      CommandTrigger("wtf"),
      getRandomMediaFile("WTF"),
      replySelection = RandomSelection
    )
  )

  override lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty

  def getRandomMediaFile(directory: String): List[MediaFile] =
        ResourceSource
          .selectResourceAccess(XahBot.resourceSource)
          .getResourcesByKind(directory)
          .use[List[MediaFile]](x => Async[F].pure(x))
}

object XahBot extends Configurations {

  val resourceSource: ResourceSource = FileSystem

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("xah_XahBot.token").map(_.map(_.toChar).mkString)
  def buildBot[F[_]: Parallel: Async, A](
      executorContext: ExecutionContext,
      action: XahBot[F] => F[A]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new XahBot[F])
  })
}
