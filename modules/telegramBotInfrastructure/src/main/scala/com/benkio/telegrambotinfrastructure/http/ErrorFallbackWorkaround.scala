package com.benkio.telegrambotinfrastructure.http

import cats.effect.*
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.Text.TextType
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Chat
import telegramium.bots.Message

/*

Workaround in case of Error a message will be sent to me. At least being parametrized
Ideally we should have a separate support chat for that and/or a dedicated Dashboard

 */
object ErrorFallbackWorkaround:

  val supportmessage: Message =
    Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = -4145546019L, `type` = "private")
    ) // Only the chat id matters here
  def errorText(value: String): Text =
    Text(value, TextType.Markdown)

  def errorHandling[F[_]: Async: LogWriter: Api](
      msg: Message,
      mediaFile: MediaFile,
      repository: Repository[F],
      error: RepositoryError
  ) = error match {
    case RepositoryError.NoResourcesFoundFile(_) =>
      Async[F].pure(List.empty) // Skip this error because user could put strange input here
    case e =>
      TelegramReply[Text].reply(
        reply = ErrorFallbackWorkaround.errorText(
          s"""An Error Occurred for
             | - msg: $msg
             | - mediaFile: $mediaFile
             | - error: ${e.getMessage()}
             |""".stripMargin
        ),
        msg = ErrorFallbackWorkaround.supportmessage,
        repository = repository,
        replyToMessage = false
      )
  }
end ErrorFallbackWorkaround
