package com.benkio.telegramBotInfrastructure.default

import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._
import cats.effect.Sync
import cats.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message
import telegramium.bots.ChatIntId
import telegramium.bots.high.implicits._
import java.io.File
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource

trait DefaultActions {

  val resourceSource: ResourceSource

  lazy val getResourceData: String => File = ResourceSource.selectResourceAccess(resourceSource).getResourceFile _

  implicit def sendReply[F[_]: Sync](implicit api: telegramium.bots.high.Api[F]): Action[Reply, F] =
    (reply: Reply) =>
      (msg: Message) =>
        for {
          _ <- Methods.sendChatAction().exec
          replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
          message <- (reply match {
            case mp3: Mp3File =>
              Methods.sendAudio(
                ChatIntId(msg.chat.id),
                InputPartFile(getResourceData(mp3.filepath)),
                replyToMessageId = replyToMessage
              )
            case gif: GifFile =>
              Methods.sendAnimation(
                ChatIntId(msg.chat.id),
                InputPartFile(getResourceData(gif.filepath)),
                replyToMessageId = replyToMessage
              )
            case photo: PhotoFile =>
              Methods.sendPhoto(
                ChatIntId(msg.chat.id),
                InputPartFile(getResourceData(photo.filepath)),
                replyToMessageId = replyToMessage
              )
            case text: TextReply =>
              Methods.sendMessage(
                ChatIntId(msg.chat.id),
                text.text(msg).fold("")(_ + "\n" + _),
                replyToMessageId = replyToMessage
              )
          }).exec
        } yield message

}

object Actions {
  type Action[T <: Reply, F[_]] =
    T => Message => F[Message]
}
