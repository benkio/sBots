package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.*
import cats.data.EitherT
import cats.data.NonEmptyList
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.ReplyValueCore
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Notifier
import com.benkio.chattelegramadapter.conversions.MediaResourceConversions.*
import com.benkio.chattelegramadapter.conversions.MessageConversions.toModel
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.IFile
import telegramium.bots.InputRichMessage
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

object TelegramMessageReply {
  final case class EmptyMarkdownValue() extends Throwable("[TelegramReply] the value of the input markdown is empty")

  def telegramFileReplyPattern[F[_]: Async: LogWriter: Api: Notifier](
      msg: TMessage,
      repository: Repository[F],
      chatAction: String,
      mediaFile: MediaFile,
      replyToMessage: Boolean,
      sendFileAPIMethod: (ChatId, IFile, Option[ReplyParameters]) => F[TMessage]
  ): F[List[TMessage]] = {
    val chatId: ChatId                                                     = ChatIntId(msg.chat.id)
    def computeMediaResource(mediaResource: MediaResource[F]): F[TMessage] =
      mediaResource.toTelegramApi.use(iFile =>
        LogWriter.trace(s"[TelegramReply - ${mediaFile.filename}] Send $mediaResource") >>
          sendFileAPIMethod(
            chatId,
            iFile,
            Option.when(replyToMessage)(ReplyParameters(Some(msg.messageId)))
          )
      )

    /** The file ID Telegram returns for a successful media send. Markdown replies have none and are skipped. */
    def fileIdOf(message: TMessage): Option[String] =
      message.audio
        .map(_.fileId)
        .orElse(message.animation.map(_.fileId))
        .orElse(message.video.map(_.fileId))
        .orElse(message.document.map(_.fileId))
        .orElse(message.sticker.map(_.fileId))
        .orElse(message.photo.lastOption.map(_.fileId))

    def failedReferenceOf(mediaResource: MediaResource[F]): Option[String] = mediaResource match {
      case MediaResource.MediaResourceIFile(reference) => reference.id.some
      case _                                           => none
    }

    /** A send by a stored reference that returns the same ID has nothing new to persist. */
    def alreadyStored(used: MediaResource[F], fileId: String, failedId: Option[String]): Boolean =
      failedId.isEmpty && failedReferenceOf(used).contains(fileId)

    def persistFileId(fileId: String, failedId: Option[String]): F[Unit] = {
      val write = OptionT
        .fromOption[F](failedId)
        .semiflatMap(failed => repository.replaceFileId(mediaFile, Platform.Telegram, failed, fileId))
        .getOrElseF(repository.registerFileId(mediaFile, Platform.Telegram, fileId))
      write.handleErrorWith(e =>
        LogWriter.error(s"[TelegramMessageReply] could not persist file id for $mediaFile: $e") >>
          summon[Notifier[F]].mediaError(msg.toModel(), mediaFile, e)
      )
    }

    /** Tries the sources in order, remembering the first stored reference that failed so the fresh ID returned by the
      * successful retry can replace it. A persistence failure never fails the reply.
      */
    def sendWithFallback(
        resources: NonEmptyList[MediaResource[F]],
        failedId: Option[String]
    ): F[TMessage] =
      computeMediaResource(resources.head).redeemWith(
        e => {
          val nextFailed = failedId.orElse(failedReferenceOf(resources.head))
          NonEmptyList.fromList(resources.tail) match {
            case Some(rest) =>
              LogWriter.error(
                s"[TelegramMessageReply] ERROR while executing media resource for $mediaFile with $e. Fallback to $rest"
              ) >> sendWithFallback(rest, nextFailed)
            case None => Async[F].raiseError(e)
          }
        },
        message =>
          OptionT
            .fromOption[F](fileIdOf(message))
            .semiflatMap(id =>
              Async[F]
                .unlessA(alreadyStored(used = resources.head, fileId = id, failedId = failedId))(
                  persistFileId(id, failedId)
                )
                .as(message)
            )
            .getOrElseF(
              LogWriter.warn(s"[TelegramMessageReply - ${mediaFile.filename}] successful send returned no file id") >>
                message.pure[F]
            )
      )

    val result: EitherT[F, Throwable, List[TMessage]] = for {
      _ <- EitherT.liftF(LogWriter.trace(s"[TelegramReply - ${mediaFile.filename}] Send Chat Action"))
      _ <- Methods.sendChatAction(chatId, chatAction).exec.void.attemptT
      _ <- EitherT.liftF(LogWriter.trace(s"[TelegramReply - ${mediaFile.filename}] Get media resources from the db"))
      message <-
        repository
          .getResourceFile(mediaFile)
          .use[List[TMessage]](mediaResources =>
            LogWriter.trace(s"[TelegramReply - ${mediaFile.filename}] Fetched $mediaResources") >>
              Async[F]
                .fromEither(mediaResources)
                .flatMap(sendWithFallback(_, none))
                .flatTap(_ =>
                  repository
                    .registerSend(mediaFile)
                    .handleErrorWith(e =>
                      LogWriter.error(s"[TelegramMessageReply] could not count the send of $mediaFile: $e")
                    )
                )
                .map(List(_))
          )
          .onError(e =>
            LogWriter.error(
              s"[TelegramMessageReply] ERROR when replying to $chatId with $mediaFile: $e"
            ) >> summon[Notifier[F]].mediaError(msg.toModel(), mediaFile, e)
          )
          .attemptT
    } yield message
    result.getOrElse(List.empty)
  }

  def sendReplyValue[F[_]: Async: LogWriter: Api: Notifier](
      replyValue: ReplyValue,
      msg: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = replyValue match {
    case core: ReplyValueCore =>
      core match {
        case mediaFile: MediaFile =>
          MediaFileReply.sendMediaFile(
            reply = mediaFile,
            msg = msg,
            repository = repository,
            replyToMessage = replyToMessage
          )
        case text: Text =>
          TextReply.sendText(
            reply = text,
            msg = msg,
            replyToMessage = replyToMessage
          )
        case markdown: Markdown =>
          Async[F]
            .raiseWhen(markdown.value.trim.isEmpty)(EmptyMarkdownValue()) >>
            RichTextReply.sendRichText(
              richMessage = InputRichMessage(markdown = markdown.value.some),
              msg = msg,
              replyToMessage = replyToMessage,
              timeToLive = markdown.timeToLive
            )
      }
    case pagedList: PagedList =>
      KeyboardReply.sendKeyboard(
        reply = KeyboardReply.toKeyboard(pagedList),
        msg = msg,
        replyToMessage = replyToMessage
      )
    case telegramInlineKeyboard: TelegramInlineKeyboard =>
      KeyboardReply.sendKeyboard(
        reply = telegramInlineKeyboard,
        msg = msg,
        replyToMessage = replyToMessage
      )
    case other =>
      LogWriter
        .warn(
          s"[TelegramMessageReply] Unsupported ReplyValue adapter type (${other.getClass.getSimpleName}); sending nothing"
        )
        .as(List.empty[TMessage])
  }
}
