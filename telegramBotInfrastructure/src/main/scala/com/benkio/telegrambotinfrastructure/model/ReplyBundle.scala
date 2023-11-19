package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import telegramium.bots.high.Api
import log.effect.LogWriter
import cats.effect.Async
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.messagefiltering.ContainsOnce
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.model.Reply.prettyPrint

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def reply: Reply[F]
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage[F[_]](
    trigger: MessageTrigger,
    reply: Reply[F],
    matcher: MessageMatches,
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleMessage {
  def apply[F[_]](
      trigger: MessageTrigger,
      reply: Reply[F],
      matcher: MessageMatches = ContainsOnce,
      replySelection: ReplySelection = RandomSelection
  ): ReplyBundleMessage[F] = new ReplyBundleMessage[F](
    trigger = trigger,
    reply = reply,
    matcher = matcher,
    replySelection = replySelection
  )

  def textToMedia[F[_]: Applicative](triggers: TextTriggerValue*)(mediaFiles: MediaFile*): ReplyBundleMessage[F] =
    ReplyBundleMessage[F](
      trigger = TextTrigger(triggers: _*),
      reply = MediaReply.fromList[F](mediaFiles = mediaFiles.toList)
    )

  def textToText[F[_]: Applicative](triggers: TextTriggerValue*)(texts: String*): ReplyBundleMessage[F] =
    ReplyBundleMessage[F](
      trigger = TextTrigger(triggers: _*),
      reply = TextReply.fromList[F](texts: _*)(false)
    )

  def prettyPrint[F[_]: Applicative](rbm: ReplyBundleMessage[F])(implicit triggerShow: Show[Trigger]): F[String] = {
    val triggerStrings: List[String] = triggerShow.show(rbm.trigger).split('\n').toList
    val replyString: F[List[String]] = rbm.reply.prettyPrint
    val result = replyString.map(x =>
      x.zipAll(triggerStrings, "", "")
        .map { case (mfs, trs) =>
          s"${mfs.padTo(25, ' ')} | $trs"
        }
        .mkString("\n")
    )
    result.map(r => ("-" * 50) + s"\n$r\n" + ("-" * 50) + "\n")
  }
}

final case class ReplyBundleCommand[F[_]](
    trigger: CommandTrigger,
    reply: Reply[F],
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]](
      trigger: CommandTrigger,
      reply: Reply[F],
      replySelection: ReplySelection = RandomSelection
  ): ReplyBundleCommand[F] = new ReplyBundleCommand[F](
    trigger = trigger,
    reply = reply,
    replySelection = replySelection
  )

  def textToMedia[F[_]: Applicative](trigger: String)(mediaFiles: MediaFile*): ReplyBundleCommand[F] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger(trigger),
      reply = MediaReply.fromList[F](mediaFiles = mediaFiles.toList)
    )
}

object ReplyBundle {

  implicit def orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  def containsMediaReply[F[_]](r: ReplyBundle[F]): Boolean =
    r.reply match {
      case MediaReply(_, _) => true
      case _                => false
    }

  def getMediaFiles[F[_]: Applicative](r: ReplyBundle[F]): F[List[MediaFile]] = r.reply match {
    case MediaReply(mediaFiles, _) => mediaFiles
    case _                         => List.empty.pure[F]
  }

  def computeReplyBundle[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle[F],
      message: Message,
      filter: F[Boolean],
      resourceAccess: ResourceAccess[F]
  )(using telegramReply: TelegramReply[ReplyValue]): F[List[Message]] = for {
    dataToReply <- Async[F].ifM(filter)(
      ifTrue = Async[F].pure(replyBundle.reply),
      ifFalse = Async[F].raiseError(new Exception(s"No replies for the given message: $message"))
    )
    replies <- replyBundle.replySelection.logic(dataToReply, message)
    result <- replies.traverse[F, List[Message]](reply =>
      telegramReply.reply[F](
        reply = reply,
        msg = message,
        resourceAccess = resourceAccess,
        replyToMessage = replyBundle.reply.replyToMessage
      )
    )
  } yield result.flatten
}
