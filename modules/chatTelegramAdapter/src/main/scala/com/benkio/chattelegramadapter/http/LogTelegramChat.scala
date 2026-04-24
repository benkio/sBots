package com.benkio.chattelegramadapter.http

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.ChatIntId
import telegramium.bots.Message

/*

Sending messages to the predefined bot log chat to signal critical errors or important events

 */
object LogTelegramChat {

  val chatSupportGroupId: ChatIntId = ChatIntId(id = -4145546019L)

  def sendError[F[_]: Async](
      msg: Message,
      mediaFile: MediaFile,
      error: Throwable
  )(using api: Api[F]): F[Unit] = error match {
    case RepositoryError.NoResourcesFoundFile(_) =>
      Async[F].unit // Skip this error because user could put strange input here
    case e =>
      Methods
        .sendMessage(
          chatId = chatSupportGroupId,
          text = s"""An Error Occurred for
                    | - message Text: ${msg.text}
                    | - message Caption: ${msg.caption}
                    | - mediaFile: $mediaFile
                    | - error: ${e.getMessage()}
                    |""".stripMargin
        )
        .exec
        .void
  }

  def sendText[F[_]: Async](
      msg: String,
      sBotInfo: SBotInfo
  )(using api: Api[F]): F[Unit] =
    Methods
      .sendMessage(
        chatId = chatSupportGroupId,
        text = s"[${sBotInfo.botName}] $msg"
      )
      .exec
      .void
}
