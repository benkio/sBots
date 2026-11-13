package com.benkio.chatdiscordadapter.outbound

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.MarkdownFiles
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.ReplyAdaptation
import com.benkio.chatdiscordadapter.model.DiscordLimits
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Path
import scala.concurrent.duration.FiniteDuration

/** Sends the reply values to Discord. The engine has already adapted them to the platform (text split to the limit,
  * what Discord cannot send as a link), what is left to do here depends on the data that is only known when the reply
  * is sent:
  *   - a media is read from the sources that are an url, in order, until one works, and uploaded; a file above the
  *     upload limit is sent as its link
  *   - a markdown file is sent as text (split to the limit)
  *   - the text with a time to live is deleted when it has passed
  *
  * @param repository
  *   the one of the platform: it gives only the resources the platform can use, see `Repository.usableFor`
  */
class DiscordReplies[F[_]: Async: LogWriter: Notifier](
    api: DiscordApi[F],
    repository: Repository[F],
    dbLayer: DBLayer[F],
    capabilities: PlatformCapabilities
) {

  def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit] = {
    val channelId = message.chatId.id
    val replyTo   = Option.when(replyToMessage)(message.messageId.map(_.value)).flatten
    replyValue match {
      case Text(value, ttl)     => sendText(channelId = channelId, text = value, timeToLive = ttl, replyTo = replyTo)
      case Markdown(value, ttl) => sendText(channelId = channelId, text = value, timeToLive = ttl, replyTo = replyTo)
      case markdownFile: MarkdownFile =>
        guarded(message, markdownFile)(
          sendMarkdownFile(channelId = channelId, markdownFile = markdownFile, replyTo = replyTo)
        )
      case mediaFile: MediaFile =>
        guarded(message, mediaFile)(sendMediaFile(channelId = channelId, mediaFile = mediaFile, replyTo = replyTo))
      case other =>
        LogWriter.warn(s"[DiscordReplies] Unsupported reply value ${other.getClass.getSimpleName}; sending nothing")
    }
  }

  private def sendText(
      channelId: String,
      text: String,
      timeToLive: Option[FiniteDuration],
      replyTo: Option[String]
  ): F[Unit] =
    if text.trim.isEmpty then LogWriter.warn("[DiscordReplies] Empty text, sending nothing")
    else
      api
        .sendText(channelId, text, replyTo)
        .flatMap(sent => timeToLive.traverse_(deleteAfter(channelId, sent, _)))
        .handleErrorWith(e => LogWriter.error(s"[DiscordReplies] Error while sending `$text`: $e"))

  private def deleteAfter(channelId: String, messageId: String, timeToLive: FiniteDuration): F[Unit] =
    Async[F]
      .start(
        (Async[F].sleep(timeToLive) >> api.deleteMessage(channelId, messageId))
          .handleErrorWith(e => LogWriter.error(s"[DiscordReplies] Error while deleting $messageId: $e"))
      )
      .void

  /** A failure is logged and notified: the other replies are not affected. */
  private def guarded(message: Message, mediaFile: MediaFile)(send: F[Unit]): F[Unit] =
    send.handleErrorWith(e =>
      LogWriter.error(s"[DiscordReplies] Error when replying to ${message.chatId} with $mediaFile: $e") >>
        summon[Notifier[F]].mediaError(message, mediaFile, e)
    )

  /** The resources to try, in order. */
  private def resources(mediaFile: MediaFile): cats.effect.Resource[F, NonEmptyList[MediaResource[F]]] =
    repository
      .getResourceFile(mediaFile)
      .evalMap(Async[F].fromEither)

  /** Each resource is tried in order, the next one if the previous fails. */
  private def firstThatWorks[A](
      resources: NonEmptyList[MediaResource[F]],
      mediaFile: MediaFile
  )(use: Path => F[A]): F[A] = {
    def attempt(resource: MediaResource[F]): F[A] = resource match {
      case MediaResource.MediaResourceFile(file) => file.use(use)
      case MediaResource.MediaResourceIFile(ref) =>
        Async[F].raiseError(new UnsupportedOperationException(s"Discord cannot use the file reference $ref"))
    }
    resources.reduceLeftTo(attempt)((previous, next) =>
      previous.handleErrorWith(e =>
        LogWriter.error(s"[DiscordReplies] Error with a resource of $mediaFile: $e. Trying the next one") >>
          attempt(next)
      )
    )
  }

  private def sendMediaFile(channelId: String, mediaFile: MediaFile, replyTo: Option[String]): F[Unit] =
    resources(mediaFile).use(rs =>
      firstThatWorks(rs, mediaFile)(path =>
        Async[F].blocking(Files.size(path)).flatMap { size =>
          if size > DiscordLimits.maxUploadBytes then LogWriter
            .info(s"[DiscordReplies] ${mediaFile.filename} is $size bytes, sending its link") >>
            sendLink(channelId = channelId, mediaFile = mediaFile, replyTo = replyTo)
          else api.sendFile(channelId, mediaFile.filename, path, replyTo).void
        }
      )
    )

  /** The link of the media, as a text: nothing is sent if there is none. */
  private def sendLink(channelId: String, mediaFile: MediaFile, replyTo: Option[String]): F[Unit] =
    ReplyAdaptation
      .adaptToPlatform(dbLayer, capabilities.copy(supportedMedia = Set.empty))(mediaFile)
      .flatMap(values =>
        values.traverse_ {
          case Text(link, ttl) => sendText(channelId = channelId, text = link, timeToLive = ttl, replyTo = replyTo)
          case other           => LogWriter.warn(s"[DiscordReplies] Unexpected value for a link: $other")
        } >> Async[F].whenA(values.isEmpty)(LogWriter.warn(s"[DiscordReplies] No link for $mediaFile, sending nothing"))
      )

  private def sendMarkdownFile(channelId: String, markdownFile: MarkdownFile, replyTo: Option[String]): F[Unit] =
    MarkdownFiles
      .content(repository, markdownFile)
      .flatMap(content =>
        ReplyTransformer
          .splitText(content, capabilities.maxTextLength)
          .zipWithIndex
          .traverse_((chunk, index) =>
            sendText(channelId = channelId, text = chunk, timeToLive = None, replyTo = replyTo.filter(_ => index == 0))
          )
      )
}
