package com.benkio.telegrambotinfrastructure.default

import cats._
import cats.data.EitherT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model._
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.InputPartFile
import telegramium.bots.Message

import java.io.File

trait DefaultActions {

  val resourceSource: ResourceSource

  def getResourceData[F[_]: Async]: MediaFile => Resource[F, File] =
    ResourceSource.selectResourceAccess(resourceSource).getResourceFile[F] _

  implicit def sendReply[F[_]: Async](implicit
      api: telegramium.bots.high.Api[F]
  ): Action[Reply, F] =
    (reply: Reply) =>
      (msg: Message) => {
        val replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
        val chatId: ChatId = ChatIntId(msg.chat.id)
        (reply match {
          case mp3: Mp3File =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_voice").exec.attemptT
              message <-
                getResourceData[F]
                  .apply(mp3)
                  .use[Message](mp3File =>
                    Methods
                      .sendAudio(
                        chatId,
                        InputPartFile(mp3File),
                        replyToMessageId = replyToMessage
                      )
                      .exec
                  )
                  .attemptT
            } yield List(message)
          case gif: GifFile =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_document").exec.attemptT
              message <- getResourceData[F]
                .apply(gif)
                .use[Message](gifFile =>
                  Methods
                    .sendAnimation(
                      chatId,
                      InputPartFile(gifFile),
                      replyToMessageId = replyToMessage
                    )
                    .exec
                )
                .attemptT
            } yield List(message)
          case photo: PhotoFile =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_photo").exec.attemptT
              message <- getResourceData[F]
                .apply(photo)
                .use[Message](photoFile =>
                  Methods
                    .sendPhoto(
                      chatId,
                      InputPartFile(photoFile),
                      replyToMessageId = replyToMessage
                    )
                    .exec
                )
                .attemptT
            } yield List(message)
          case video: VideoFile =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_video").exec.attemptT
              message <- getResourceData[F]
                .apply(video)
                .use[Message](videoFile =>
                  Methods
                    .sendVideo(
                      chatId,
                      InputPartFile(videoFile),
                      replyToMessageId = replyToMessage
                    )
                    .exec
                )
                .attemptT
            } yield List(message)
          case text: TextReply[F] @unchecked =>
            for {
              _     <- Methods.sendChatAction(chatId, "typing").exec.attemptT
              texts <- EitherT.liftF(text.text(msg))(Functor[F])
              messages <- texts
                .traverse(m =>
                  Methods
                    .sendMessage(
                      chatId,
                      m,
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
