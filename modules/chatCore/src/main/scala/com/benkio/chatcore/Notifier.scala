package com.benkio.chatcore

import cats.Applicative
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import log.effect.LogWriter

/** Tells the people running the bots about the critical errors and the important events, e.g. a bot that starts. Where
  * the notification goes is up to the platform: a chat for the operators, a channel, a log.
  */
trait Notifier[F[_]] {

  /** An important event of a bot, e.g. its start. */
  def info(sBotInfo: SBotInfo, message: String): F[Unit]

  /** A media that could not be sent as the reply to the message. */
  def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit]
}

object Notifier {

  /** Notifies nobody. */
  def noop[F[_]: Applicative]: Notifier[F] = new Notifier[F] {
    override def info(sBotInfo: SBotInfo, message: String): F[Unit]                            = Applicative[F].unit
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
      Applicative[F].unit
  }

  /** Writes the notifications to the log: for the platforms that have no place for the operators yet. */
  def logging[F[_]](using log: LogWriter[F]): Notifier[F] = new Notifier[F] {
    override def info(sBotInfo: SBotInfo, message: String): F[Unit] = log.info(formatInfo(sBotInfo, message))
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
      log.error(formatMediaError(message = message, mediaFile = mediaFile, error = error))
  }

  def formatInfo(sBotInfo: SBotInfo, message: String): String =
    s"[${sBotInfo.botName}] $message"

  def formatMediaError(message: Message, mediaFile: MediaFile, error: Throwable): String =
    s"""An Error Occurred for
       | - message Text: ${message.text}
       | - message Caption: ${message.caption}
       | - mediaFile: $mediaFile
       | - error: ${error.getMessage()}
       |""".stripMargin

  /** Whether the error of a media is worth a notification: not when the user typed something with no media behind it.
    */
  def isReportable(error: Throwable): Boolean = error match {
    case RepositoryError.NoResourcesFoundFile(_) => false
    case _                                       => true
  }
}
