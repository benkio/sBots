package com.benkio.telegramBotInfrastructure.default

import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._
import cats.effect.Sync
import cats.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message
import telegramium.bots.{ChatId, ChatIntId}
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
        val chatId : ChatId = ChatIntId(msg.chat.id)
          reply match {
            case mp3: Mp3File => for {
              _ <- Methods.sendChatAction(chatId, "upload_voice").exec
              message <- Methods.sendAudio(
                chatId,
                InputPartFile(getResourceData(mp3)),
                replyToMessageId = replyToMessage
              ).exec
            } yield message
            case gif: GifFile => for {
              _ <- Methods.sendChatAction(chatId, "upload_document").exec
message <- Methods.sendAnimation(
                chatId,
                InputPartFile(getResourceData(gif)),
                replyToMessageId = replyToMessage
              ).exec}yield message
            case photo: PhotoFile => for {
              _ <- Methods.sendChatAction(chatId, "upload_photo").exec
message <- Methods.sendPhoto(
                chatId,
                InputPartFile(getResourceData(photo)),
                replyToMessageId = replyToMessage
              ).exec} yield message
            case text: TextReply => for {
              _ <- Methods.sendChatAction(chatId, "typing").exec
              message <- Methods.sendMessage(
                chatId,
                text.text(msg).fold("")(_ + "\n" + _),
                replyToMessageId = replyToMessage
              ).exec} yield message
          }
      }
}

object Actions {
  type Action[T <: Reply, F[_]] =
    T => Message => F[Message]
}
