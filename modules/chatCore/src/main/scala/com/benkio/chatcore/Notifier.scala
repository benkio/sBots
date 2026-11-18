package com.benkio.chatcore

import cats.effect.Async
import cats.implicits.*
import cats.Applicative
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** Tells the people running the bots about the critical errors and the important events, e.g. a bot that starts. Where
  * the notification goes is up to the platform: a chat for the operators, a channel, a log.
  */
trait Notifier[F[_]] {

  /** An important event of a bot, e.g. its start. */
  def info(sBotInfo: SBotInfo, message: String): F[Unit]

  /** A media that could not be sent as the reply to the message. */
  def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit]

  /** An error of a bot, with what it was doing: e.g. a background job that failed or a request that timed out. */
  def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit]
}

object Notifier {

  /** Notifies nobody. */
  def noop[F[_]: Applicative]: Notifier[F] = new Notifier[F] {
    override def info(sBotInfo: SBotInfo, message: String): F[Unit]                            = Applicative[F].unit
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
      Applicative[F].unit
    override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] = Applicative[F].unit
  }

  /** Writes the notifications to the log: for the platforms that have no place for the operators yet. */
  def logging[F[_]](using log: LogWriter[F]): Notifier[F] = new Notifier[F] {
    override def info(sBotInfo: SBotInfo, message: String): F[Unit] = log.info(formatInfo(sBotInfo, message))
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] = {
      val formatted = formatMediaError(message = message, mediaFile = mediaFile, error = error)
      if isMissingRights(error) then log.warn(formatted) else log.error(formatted)
    }
    override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] = {
      val formatted = formatError(sBotInfo = sBotInfo, context = context, error = error)
      if isMissingRights(error) then log.warn(formatted) else log.error(formatted)
    }
  }

  /** A notifier that never breaks what it is called from: a failing notification is logged and swallowed. */
  def guarded[F[_]: Async](underlying: Notifier[F])(using log: LogWriter[F]): Notifier[F] = new Notifier[F] {
    private def guard(what: String)(notification: F[Unit]): F[Unit] =
      notification.handleErrorWith(e => log.error(s"[Notifier] The notification of $what failed: $e"))

    override def info(sBotInfo: SBotInfo, message: String): F[Unit] =
      guard("an info")(underlying.info(sBotInfo, message))
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
      guard("a media error")(underlying.mediaError(message = message, mediaFile = mediaFile, error = error))
    override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] =
      guard("an error")(underlying.error(sBotInfo = sBotInfo, context = context, error = error))
  }

  /** The errors that repeat (same bot, context and error) are notified once per window, followed by how many more were
    * suppressed. The infos and the media errors are not throttled.
    */
  def throttled[F[_]](underlying: Notifier[F], throttle: Throttle[F]): Notifier[F] = new Notifier[F] {
    override def info(sBotInfo: SBotInfo, message: String): F[Unit] = underlying.info(sBotInfo, message)
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
      underlying.mediaError(message = message, mediaFile = mediaFile, error = error)
    override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] =
      throttle(errorKey(sBotInfo = sBotInfo, context = context, error = error))(
        send = underlying.error(sBotInfo = sBotInfo, context = context, error = error),
        summary = count => underlying.info(sBotInfo, s"$count more of: $context, ${describe(error)}")
      )
  }

  /** The notifier for the production: throttled by `window` and guarded. */
  def resilient[F[_]: Async: LogWriter](underlying: Notifier[F], window: FiniteDuration): F[Notifier[F]] =
    Throttle[F](window).map(throttle => guarded(throttled(underlying, throttle)))

  def errorKey(sBotInfo: SBotInfo, context: String, error: Throwable): String =
    s"${sBotInfo.botId}|$context|${describe(error)}"

  private def describe(error: Throwable): String =
    s"${error.getClass.getSimpleName}: ${Option(error.getMessage).getOrElse("").take(200)}"

  def formatError(sBotInfo: SBotInfo, context: String, error: Throwable): String =
    s"[${sBotInfo.botName}] Error in $context: ${describe(error)}"

  def formatInfo(sBotInfo: SBotInfo, message: String): String =
    s"[${sBotInfo.botName}] $message"

  def formatMediaError(message: Message, mediaFile: MediaFile, error: Throwable): String =
    s"""An Error Occurred for
       | - message Text: ${message.text}
       | - message Caption: ${message.caption}
       | - mediaFile: $mediaFile
       | - error: ${error.getMessage()}
       |""".stripMargin

  /** Whether the error means that the bot has no rights to send that to the chat, e.g. the `400` of Telegram
    * `method=sendAnimation code=400 description=Bad Request: not enough rights to send animations to the chat`. The
    * chat is the one to change, not the bot: a false positive for the people running the bots.
    */
  def isMissingRights(error: Throwable): Boolean =
    Option(error.getMessage).exists(text => text.contains("code=400") && text.contains("not enough rights"))

  /** Whether the error of a media is worth a notification: not when the user typed something with no media behind it,
    * nor when the bot has no rights in the chat.
    */
  def isReportable(error: Throwable): Boolean = error match {
    case RepositoryError.NoResourcesFoundFile(_) => false
    case other                                   => !isMissingRights(other)
  }
}
