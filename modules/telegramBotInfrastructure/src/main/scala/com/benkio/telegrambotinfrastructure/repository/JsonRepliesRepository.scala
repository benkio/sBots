package com.benkio.telegrambotinfrastructure.repository

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import io.circe.parser.decode
import log.effect.LogWriter

import java.nio.charset.StandardCharsets

trait JsonRepliesRepository[F[_]] {
  def loadReplies(jsonRepliesFilename: String): F[List[ReplyBundleMessage]]
}

object JsonRepliesRepository {

  enum JsonRepliesRepositoryError(msg: String) extends Throwable(msg) {
    case FileNotFound(jsonRepliesFilename: String)
        extends JsonRepliesRepositoryError(s"[JsonRepliesRepository] Could not load replies file: $jsonRepliesFilename")
    case DecodeError(jsonRepliesFilename: String, cause: Throwable)
        extends JsonRepliesRepositoryError(
          s"[JsonRepliesRepository] Failed to decode $jsonRepliesFilename: ${cause.getMessage}"
        )
  }

  def apply[F[_]: Async: LogWriter](): JsonRepliesRepository[F] =
    new JsonRepliesRepositoryImpl[F]()

  class JsonRepliesRepositoryImpl[F[_]: Async: LogWriter]() extends JsonRepliesRepository[F] {

    override def loadReplies(jsonRepliesFilename: String): F[List[ReplyBundleMessage]] = {
      val program = for {
        _ <- Resource.eval(LogWriter.info(s"[JsonRepliesRepository] Load resouces replied from: $jsonRepliesFilename"))
        eitherBytes <- Repository.getResourceByteArray(jsonRepliesFilename)
        jsonContent <- Resource.eval(
          eitherBytes.fold(
            err => Async[F].raiseError(err),
            bytes => String(bytes, StandardCharsets.UTF_8).pure[F]
          )
        )
        _       <- Resource.eval(LogWriter.info(s"[JsonRepliesRepository] jsonContent length: ${jsonContent.length()}"))
        decoded <- Resource.eval(
          Async[F]
            .fromEither(decode[List[ReplyBundleMessage]](jsonContent))
            .adaptError(e => JsonRepliesRepositoryError.DecodeError(jsonRepliesFilename, e))
        )
      } yield decoded

      program.use(Async[F].pure)
    }
  }
}
