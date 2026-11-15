package com.benkio.chatircadapter.outbound

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.conversions.MarkdownConversions
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.MarkdownFiles
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.model.IrcLimits
import log.effect.LogWriter

import scala.annotation.unused

/** Sends the reply values to IRC. The engine has already adapted them to the platform (the markdown is plain text, a
  * media other than a markdown file is its link, the time to live is gone, the text is split to the limit), what is
  * left is to send each line of a text as a message to the chat: the channel, or the nick of a private message. IRC has
  * no reply to a message.
  */
class IrcReplies[F[_]: Async: LogWriter: Notifier](
    api: IrcApi[F],
    repository: Repository[F],
    capabilities: PlatformCapabilities
) {

  def send(replyValue: ReplyValue, message: Message, @unused replyToMessage: Boolean): F[Unit] =
    sendAll(List(replyValue), message, replyToMessage)

  /** Sends all reply values as one logical reply, capping the total number of lines across the whole reply. */
  def sendAll(replyValues: List[ReplyValue], message: Message, @unused replyToMessage: Boolean): F[Unit] =
    replyValues
      .traverse(valueLines(_, message))
      .map(_.flatten)
      .flatMap(lines => sendPreparedLines(message.chatId.id, IrcLines.capLines(lines, IrcLimits.maxLinesPerReply)))

  private def valueLines(replyValue: ReplyValue, message: Message): F[List[String]] =
    replyValue match {
      case Text(value, _)             => textToLines(value).pure[F]
      case Markdown(value, _)         => textToLines(MarkdownConversions.toPlainText(value)).pure[F]
      case markdownFile: MarkdownFile => markdownFileLines(message, markdownFile)
      case other                      =>
        LogWriter.warn(s"[IrcReplies] Unsupported reply value ${other.getClass.getSimpleName}; sending nothing").as(Nil)
    }

  /** The content of the file as plain text. A failure is logged and notified, the other replies are not affected. */
  private def markdownFileLines(message: Message, markdownFile: MarkdownFile): F[List[String]] =
    MarkdownFiles
      .content(repository, markdownFile)
      .map(content => textToLines(MarkdownConversions.toPlainText(content)))
      .handleErrorWith(e =>
        LogWriter.error(s"[IrcReplies] Error when replying to ${message.chatId} with $markdownFile: $e") >>
          summon[Notifier[F]].mediaError(message, markdownFile, e).as(Nil)
      )

  private def textToLines(text: String): List[String] =
    IrcLines.toLines(text, capabilities.maxTextLength, IrcLimits.maxLineBytes)

  private def sendPreparedLines(target: String, lines: List[String]): F[Unit] =
    lines
      .traverse_(line => api.sendMessage(target, line))
      .handleErrorWith(e => LogWriter.error(s"[IrcReplies] Error while sending lines to $target: $e"))
}
