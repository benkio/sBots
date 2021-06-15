package com.benkio.telegrambotinfrastructure.default

import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model._
import cats.effect._
import cats.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.high.implicits._
import java.io.File
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource

trait DefaultActions {

  val resourceSource: ResourceSource

  lazy val getResourceData: MediaFile => Resource[IO, File] = ResourceSource.selectResourceAccess(resourceSource).getResourceFile _

  implicit def sendReply[F[_] >: IO[_]](implicit api: telegramium.bots.high.Api[F], sync: Sync[F]): Action[Reply, F] =
    (reply: Reply) =>
  (msg: Message) => {
    val replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
    val chatId: ChatId = ChatIntId(msg.chat.id)
      (reply match {
        case mp3: Mp3File =>
          for {
            _ <- Methods.sendChatAction(chatId, "upload_voice").exec.attemptT
            message <-
            getResourceData(mp3).use[F, Message](mp3File =>
              Methods
                .sendAudio(
                  chatId,
                  InputPartFile(mp3File),
                  replyToMessageId = replyToMessage
                )
                .exec
            )(sync).attemptT
          } yield List(message)
        case gif: GifFile =>
          for {
            _ <- Methods.sendChatAction(chatId, "upload_document").exec.attemptT
            message <- getResourceData(gif).use[F, Message](gifFile =>
              Methods
                .sendAnimation(
                  chatId,
                  InputPartFile(gifFile),
                  replyToMessageId = replyToMessage
                )
                .exec
            )(sync).attemptT
          } yield List(message)
        case photo: PhotoFile =>
          for {
            _ <- Methods.sendChatAction(chatId, "upload_photo").exec.attemptT
            message <- getResourceData(photo).use[F, Message](photoFile =>
              Methods
                .sendPhoto(
                  chatId,
                  InputPartFile(photoFile),
                  replyToMessageId = replyToMessage
                )
                .exec
            )(sync).attemptT
          } yield List(message)
        case text: TextReply =>
          for {
            _ <- Methods.sendChatAction(chatId, "typing").exec.attemptT
            messages <- text
            .text(msg)
            .traverse(m =>
              Methods
                .sendMessage(
                  chatId,
                  m.fold("")(_ + "\n" + _),
                  replyToMessageId = replyToMessage
                )
                .exec
            )
            .attemptT
          } yield messages
      }).value.map {
        case Right(x) => x
        case Left(e) =>
          println(s"********ERROR OCCURRED********\n ${e.getMessage}")
          List(msg)
      }
  }
}

object Actions {
  type Action[T <: Reply, F[_]] =
    T => Message => F[List[Message]]
}
