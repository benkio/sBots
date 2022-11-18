package com.benkio.telegrambotinfrastructure.default

import cats._
import cats.data.EitherT
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import log.effect.LogWriter
import telegramium.bots.high._
import telegramium.bots.high.implicits._
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.InputPartFile
import telegramium.bots.Message

object Actions {

  type Action[F[_]] =
    Reply => Message => F[List[Message]]

  implicit def sendReply[F[_]](implicit
      api: telegramium.bots.high.Api[F],
      asyncF: Async[F],
      log: LogWriter[F],
      resourceAccess: ResourceAccess[F]
  ): Action[F] =
    (reply: Reply) =>
      (msg: Message) => {
        val replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
        val chatId: ChatId = ChatIntId(msg.chat.id)
        (reply match {
          case mp3: Mp3File =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_voice").exec.attemptT
              message <-
                resourceAccess
                  .getResourceFile(mp3)
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
              message <- resourceAccess
                .getResourceFile(gif)
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
              message <- resourceAccess
                .getResourceFile(photo)
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
              message <- resourceAccess
                .getResourceFile(video)
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
            log.error(s"********ERROR OCCURRED********\n ${e.getMessage}")
            List(msg)
        }
      }
}
