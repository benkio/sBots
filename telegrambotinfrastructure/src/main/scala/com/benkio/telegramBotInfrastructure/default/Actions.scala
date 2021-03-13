package com.benkio.telegramBotInfrastructure.default

import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import java.nio.file.Files
import java.nio.file.Path
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._
import telegramium.bots.Message
import scala.concurrent.Future
import cats.effect.Sync
import cats.implicits._
import telegramium.bots.high._
import telegramium.bots.{InputLinkFile, Message, ChatIntId}
import telegramium.bots.high.implicits._

trait DefaultActions {

  implicit def sendReply[F[_] : Sync](implicit api: telegramium.bots.high.Api[F]): Action[Reply, F] =
    (reply: Reply) =>
  (msg: Message) => for {
    _ <- Methods.sendChatAction().exec
    replyToMessage = if (reply.replyToMessage) Some(msg.messageId) else None
    message <- (reply match {
      case mp3 : Mp3File => Methods.sendAudio(ChatIntId(msg.chat.id), InputLinkFile(mp3.filepath)      , replyToMessageId = replyToMessage)
      case gif : GifFile => Methods.sendAnimation(ChatIntId(msg.chat.id), InputLinkFile(gif.filepath)  , replyToMessageId = replyToMessage)
      case photo : PhotoFile => Methods.sendPhoto(ChatIntId(msg.chat.id), InputLinkFile(photo.filepath), replyToMessageId = replyToMessage)
      case text : TextReply => Methods.sendMessage(ChatIntId(msg.chat.id), text.text(msg).fold("")(_ + "\n" + _), replyToMessageId = replyToMessage)
    }).exec
  } yield message

}

object Actions {
  type Action[T <: Reply, F[_]] =
    T => Message => F[Message]
}
