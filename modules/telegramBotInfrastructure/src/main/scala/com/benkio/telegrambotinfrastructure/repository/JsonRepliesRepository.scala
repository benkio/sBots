package com.benkio.telegrambotinfrastructure.repository

import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import io.circe.parser.decode
import cats.effect.Resource
import cats.syntax.all.*
import cats.effect.Async
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import log.effect.LogWriter
import com.benkio.telegrambotinfrastructure.model.reply.Document

trait JsonRepliesRepository[F[_]] {
  def loadReplies(jsonRepliesFilename: String): F[List[ReplyBundleMessage]]
}

object JsonRepliesRepository {

  enum JsonRepliesRepositoryError(msg: String) extends Throwable(msg) {
    case FileNotFound(jsonRepliesFilename: String)
        extends JsonRepliesRepositoryError(s"[JsonRepliesRepository] Could not load replies file: $jsonRepliesFilename")
    case DecodeError(jsonRepliesFilename: String, cause: Throwable)
        extends JsonRepliesRepositoryError(s"[JsonRepliesRepository] Failed to decode $jsonRepliesFilename: ${cause.getMessage}")
  }

  def apply[F[_]: Async: LogWriter](repository: Repository[F]): JsonRepliesRepository[F] =
    new JsonRepliesRepositoryImpl[F](repository)

  class JsonRepliesRepositoryImpl[F[_]: Async: LogWriter](repository: Repository[F])
      extends JsonRepliesRepository[F] {

    override def loadReplies(jsonRepliesFilename: String): F[List[ReplyBundleMessage]] = {
      val program = for {
        eitherRes <- repository.getResourceFile(Document(jsonRepliesFilename))
        nel       <- Resource.eval(
          eitherRes.fold(
            _ => Async[F].raiseError(JsonRepliesRepositoryError.FileNotFound(jsonRepliesFilename)),
            nel => Async[F].pure(nel)
          )
        )
        fileRes <- nel.head.getMediaResourceFile match {
          case Some(r) => r
          case None    => Resource.eval(Async[F].raiseError[java.io.File](JsonRepliesRepositoryError.FileNotFound(jsonRepliesFilename)))
        }
        jsonContent <- Repository.fileToString(fileRes)
        decoded     <- Resource.eval(
          Async[F].fromEither(decode[List[ReplyBundleMessage]](jsonContent))
            .adaptError(e => JsonRepliesRepositoryError.DecodeError(jsonRepliesFilename, e))
        )
      } yield decoded

      program.use(Async[F].pure)
    }
  }
}
