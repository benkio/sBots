package com.benkio.telegrambotinfrastructure.botCapabilities

import cats.Applicative
import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccess
import telegramium.bots.Message

import scala.io.Source
import scala.util.Random

object CommandPatterns {

  object RandomLinkCommand {

    lazy val random = new Random()

    def selectRandomLinkByKeyword[F[_]: Async](
        keywords: String,
        resourceAccess: ResourceAccess,
        youtubeLinkSources: String
    ): Resource[F, Option[String]] = for {
      sourceFiles <- resourceAccess.getResourcesByKind[F](youtubeLinkSources)
      sourceRawBytesArray <- sourceFiles.traverse(f =>
        resourceAccess
          .getResourceByteArray(f.getPath)
      )
      sourceRawBytes = sourceRawBytesArray.foldLeft(Array.empty[Byte]) { case (acc, bs) =>
        acc ++ (('\n'.toByte) +: bs)
      }
      youtubeLinkReplies = Source
        .fromRawBytes(sourceRawBytes)
        .getLines()
        .toList
        .filter(s =>
          keywords
            .split(' ')
            .map(_.toLowerCase)
            .forall(k => s.toLowerCase.contains(k))
        )
      lineSelectedIndex <-
        if (!youtubeLinkReplies.isEmpty)
          Resource.eval(Async[F].delay(random.between(0, youtubeLinkReplies.length)))
        else Resource.pure[F, Int](-1)
    } yield if (lineSelectedIndex == -1) None else Some(youtubeLinkReplies(lineSelectedIndex))
  }

  def handleCommandWithInput[F[_]: Applicative](
      msg: Message,
      command: String,
      botName: String,
      computation: String => F[List[String]],
      defaultReply: String
  ): F[List[String]] =
    msg.text
      .filterNot(t => t.trim == s"/$command" || t.trim == s"/$command@$botName")
      .map(t => computation(t.dropWhile(_ != ' ').tail))
      .getOrElse(List(defaultReply).pure[F])
}
