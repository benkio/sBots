package com.benkio.telegramBotInfrastructure.default

import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._
import cats.effect.Sync
import cats.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.high.implicits._
import java.io.File
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource

trait DefaultActions {

  val resourceSource: ResourceSource

  lazy val getResourceData: MediaFile => File = ResourceSource.selectResourceAccess(resourceSource).getResourceFile _

  implicit def sendReply[F[_]: Sync](implicit api: telegramium.bots.high.Api[F]): Action[Reply, F] =
    (reply: Reply) =>
      (msg: Message) => {
        val replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
        val chatId: ChatId = ChatIntId(msg.chat.id)
        (reply match {
          case mp3: Mp3File =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_voice").exec.attemptT
              message <- Methods
                .sendAudio(
                  chatId,
                  InputPartFile(getResourceData(mp3)),
                  replyToMessageId = replyToMessage
                )
                .exec
                .attemptT
            } yield List(message)
          case gif: GifFile =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_document").exec.attemptT
              message <- Methods
                .sendAnimation(
                  chatId,
                  InputPartFile(getResourceData(gif)),
                  replyToMessageId = replyToMessage
                )
                .exec
                .attemptT
            } yield List(message)
          case photo: PhotoFile =>
            for {
              _ <- Methods.sendChatAction(chatId, "upload_photo").exec.attemptT
              message <- Methods
                .sendPhoto(
                  chatId,
                  InputPartFile(getResourceData(photo)),
                  replyToMessageId = replyToMessage
                )
                .exec
                .attemptT
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
