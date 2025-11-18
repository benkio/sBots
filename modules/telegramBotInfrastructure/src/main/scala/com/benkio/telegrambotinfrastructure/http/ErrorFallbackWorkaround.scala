package com.benkio.telegrambotinfrastructure.http

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.ChatIntId
import telegramium.bots.Message

/*

Workaround in case of Error a message will be sent to me. At least being parametrized
Ideally we should have a separate support chat for that and/or a dedicated Dashboard

 */
object ErrorFallbackWorkaround {

  val chatSupportGroupId: ChatIntId = ChatIntId(id = -4145546019L)

  def errorHandling[F[_]: Async](
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
                    | - msg: ${msg.getContent}
                    | - mediaFile: $mediaFile
                    | - error: ${e.getMessage()}
                    |""".stripMargin
        )
        .exec
        .void
  }
}
