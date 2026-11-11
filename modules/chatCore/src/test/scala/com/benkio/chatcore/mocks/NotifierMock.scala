package com.benkio.chatcore.mocks

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier

object NotifierMock {

  /** Notifies nobody: for the tests that do not look at the notifications. */
  given noop: Notifier[IO] = Notifier.noop[IO]

  final case class MediaError(message: Message, mediaFile: MediaFile, error: Throwable)

  /** A notifier that records the notifications. */
  final class RecordingNotifier(
      val infos: Ref[IO, List[(SBotInfo, String)]],
      val mediaErrors: Ref[IO, List[MediaError]]
  ) extends Notifier[IO] {
    override def info(sBotInfo: SBotInfo, message: String): IO[Unit] = infos.update(_ :+ (sBotInfo -> message))
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): IO[Unit] =
      mediaErrors.update(_ :+ MediaError(message, mediaFile, error))
  }

  def recording: IO[RecordingNotifier] = for {
    infos       <- Ref.of[IO, List[(SBotInfo, String)]](List.empty)
    mediaErrors <- Ref.of[IO, List[MediaError]](List.empty)
  } yield new RecordingNotifier(infos, mediaErrors)
}
