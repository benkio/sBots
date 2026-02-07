package com.benkio.telegrambotinfrastructure.repository

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import log.effect.LogWriter

trait JsonRepliesRepository[F[_]] {
  def loadReplies(sBotConfig: SBotConfig): F[List[ReplyBundleMessage]]
}

object JsonRepliesRepository {

  enum JsonRepliesRepositoryError(msg: String) extends Throwable(msg) {
    case FileNotFound(filename: String)
        extends JsonRepliesRepositoryError(s"[JsonRepliesRepository] Could not load replies file: $filename")
    case DecodeError(filename: String, cause: Throwable)
        extends JsonRepliesRepositoryError(s"[JsonRepliesRepository] Failed to decode $filename: ${cause.getMessage}")
  }

  def apply[F[_]: Async: LogWriter](repository: Repository[F]): JsonRepliesRepository[F] =
    new JsonRepliesRepositoryImpl[F](repository)

  class JsonRepliesRepositoryImpl[F[_]: Async: LogWriter](repository: Repository[F])
      extends JsonRepliesRepository[F] {

    override def loadReplies(sBotConfig: SBotConfig): F[List[ReplyBundleMessage]] = ???
    // {
    //   val filename = sBotConfig.triggerJsonFilename
    //   (for {
    //     eitherRes <- repository.getResourceFile(Document(filename))
    //     nel       <- Resource.eval(
    //       eitherRes.fold(
    //         _ => Async[F].raiseError(JsonRepliesRepositoryError.FileNotFound(filename)),
    //         nel => Async[F].pure(nel)
    //       )
    //     )
    //     fileRes <- nel.head.getMediaResourceFile match {
    //       case Some(r) => r
    //       case None    => Resource.eval(Async[F].raiseError[java.io.File](JsonRepliesRepositoryError.FileNotFound(filename)))
    //     }
    //     jsonContent <- Repository.fileToString(fileRes)
    //     decoded     <- Resource.eval(
    //       Async[F].fromEither(decode[List[ReplyBundleMessage]](jsonContent))
    //         .adaptError(e => JsonRepliesRepositoryError.DecodeError(filename, e))
    //     )
    //   } yield decoded).use(Async[F].pure)
    // }
  }
}
