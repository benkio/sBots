package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.Applicative
import cats.effect.{Async, Resource}
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import log.effect.LogWriter
import telegramium.bots.Message

import java.nio.file.Files
import scala.io.Source
import scala.util.Random

object CommandPatterns {

  object RandomLinkCommand {

    lazy val random = new Random()

    def selectRandomLinkByKeyword[F[_]: Async](
        keywords: String,
        resourceAccess: ResourceAccess[F],
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): Resource[F, Option[String]] = for {
      _           <- Resource.eval(log.info(s"selectRandomLinkByKeyword for $keywords - $youtubeLinkSources"))
      sourceFiles <- resourceAccess.getResourcesByKind(youtubeLinkSources)
      sourceRawBytesArray = sourceFiles.map(f => Files.readAllBytes(f.toPath))
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
