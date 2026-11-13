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
    replyValue match {
      case Text(value, _)             => sendLines(message.chatId.id, value)
      case Markdown(value, _)         => sendLines(message.chatId.id, MarkdownConversions.toPlainText(value))
      case markdownFile: MarkdownFile => sendMarkdownFile(message, markdownFile)
      case other                      =>
        LogWriter.warn(s"[IrcReplies] Unsupported reply value ${other.getClass.getSimpleName}; sending nothing")
    }

  /** The content of the file as plain text. A failure is logged and notified, the other replies are not affected. */
  private def sendMarkdownFile(message: Message, markdownFile: MarkdownFile): F[Unit] =
    MarkdownFiles
      .content(repository, markdownFile)
      .flatMap(content => sendLines(message.chatId.id, MarkdownConversions.toPlainText(content)))
      .handleErrorWith(e =>
        LogWriter.error(s"[IrcReplies] Error when replying to ${message.chatId} with $markdownFile: $e") >>
          summon[Notifier[F]].mediaError(message, markdownFile, e)
      )

  private def sendLines(target: String, text: String): F[Unit] =
    IrcLines
      .split(text, capabilities.maxTextLength, IrcLimits.maxLineBytes, IrcLimits.maxLinesPerReply)
      .traverse_(line => api.sendMessage(target, line))
      .handleErrorWith(e => LogWriter.error(s"[IrcReplies] Error while sending `$text` to $target: $e"))
}
