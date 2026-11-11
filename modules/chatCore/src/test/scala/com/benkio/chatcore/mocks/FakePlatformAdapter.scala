package com.benkio.chatcore.mocks

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.PlatformAdapter

/** A platform without a network: [[run]] delivers the `inbound` messages to the handler set with [[onInbound]], what is
  * sent is recorded in `sent` and every effectful reply is computed as `effectfulValue`, by default
  * `Text("effectful")`.
  */
final class FakePlatformAdapter private (
    override val capabilities: PlatformCapabilities,
    inbound: List[Message],
    handler: Ref[IO, Option[Message => IO[Unit]]],
    effectfulValue: ReplyValue,
    val sent: Ref[IO, List[FakePlatformAdapter.Sent]]
) extends PlatformAdapter[IO] {

  def onInbound(onMessage: Message => IO[Unit]): IO[Unit] = handler.set(Some(onMessage))

  override def run: IO[Unit] =
    handler.get.flatMap(
      _.fold(IO.raiseError(new IllegalStateException("[FakePlatformAdapter] no handler: call onInbound first")))(h =>
        inbound.traverse_(h)
      )
    )

  override def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): IO[Unit] =
    sent.update(_ :+ FakePlatformAdapter.Sent(replyValue, message, replyToMessage))

  override def runEffectful(reply: EffectfulReply, message: Message): IO[ReplyValue] =
    IO.pure(effectfulValue)
}

object FakePlatformAdapter {

  final case class Sent(replyValue: ReplyValue, message: Message, replyToMessage: Boolean)

  /** What a text only platform like IRC can do. */
  val textOnlyCapabilities: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Irc,
    supportedMedia = Set.empty[MediaKind],
    maxTextLength = 400,
    markdown = MarkdownSupport.Plain,
    supportsDelete = false,
    supportsEdit = false,
    supportsButtons = false,
    acceptsFileIds = false,
    commandSyntax = CommandSyntax(prefix = "!")
  )

  def apply(
      inbound: List[Message] = List.empty,
      capabilities: PlatformCapabilities = textOnlyCapabilities,
      effectfulValue: ReplyValue = Text("effectful")
  ): IO[FakePlatformAdapter] = for {
    handler <- Ref.of[IO, Option[Message => IO[Unit]]](None)
    sent    <- Ref.of[IO, List[Sent]](List.empty)
  } yield new FakePlatformAdapter(capabilities, inbound, handler, effectfulValue, sent)
}
