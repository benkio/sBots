package com.benkio.telegrambotinfrastructure.patterns

import cats.Applicative
import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import log.effect.LogWriter
import telegramium.bots.Message

import java.nio.file.Files
import scala.io.Source
import scala.util.Random

object CommandPatterns {

  object RandomLinkCommand {

    lazy val random = new Random()

    def selectRandomLinkReplyBundleCommand[F[_]: Async](
        resourceAccess: ResourceAccess[F],
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): F[ReplyBundleCommand[F]] =
      ReplyBundleCommand(
        trigger = CommandTrigger("randomshow"),
        text = Some(
          TextReply[F](
            _ =>
              selectRandomLinkByKeyword[F](
                "",
                resourceAccess,
                youtubeLinkSources
              )
                .use(optMessage => Applicative[F].pure(optMessage.toList)),
            true
          )
        ),
      ).pure[F]

    def selectRandomLinkByKeywordsReplyBundleCommand[F[_]: Async](
        resourceAccess: ResourceAccess[F],
        botName: String,
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): F[ReplyBundleCommand[F]] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("randomshowkeyword"),
        text = Some(
          TextReply[F](
            m =>
              handleCommandWithInput[F](
                m,
                "randomshowkeyword",
                botName,
                keywords =>
                  RandomLinkCommand
                    .selectRandomLinkByKeyword[F](
                      keywords,
                      resourceAccess,
                      youtubeLinkSources
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

    private def selectRandomLinkByKeyword[F[_]: Async](
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

  object TriggerListCommand {

    // TODO: Move the ReplyBundleCommand function over here and replace it in bots

    // TODO: Make this private
    def messageReplyDataStringChunks[F[_]](mdr: List[ReplyBundleMessage[F]]): List[String] = {
      val (triggers, lastTriggers) = mdr
        .map(_.trigger match {
          case TextTrigger(lt @ _*) => lt.mkString("[", " - ", "]")
          case _                    => ""
        })
        .foldLeft((List.empty[String], "")) { case ((acc, candidate), triggerString) =>
          if ((candidate ++ triggerString).length > 4090)
            (acc :+ candidate, triggerString)
          else (acc, candidate ++ triggerString)
        }
      triggers :+ lastTriggers
    }

  }

  object TriggerSearchCommand {}

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
