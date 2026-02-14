package com.benkio.telegrambotinfrastructure.repository

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import io.circe.parser.decode
import io.circe.Decoder
import log.effect.LogWriter

import java.nio.charset.StandardCharsets

trait JsonDataRepository[F[_]] {
  def loadData[T: Decoder](jsonDataFilename: String): F[List[T]]
}

object JsonDataRepository {

  enum JsonDataRepositoryError(msg: String) extends Throwable(msg) {
    case FileNotFound(jsonDataFilename: String)
        extends JsonDataRepositoryError(s"[JsonDataRepository] Could not load data file: $jsonDataFilename")
    case DecodeError(jsonDataFilename: String, cause: Throwable)
        extends JsonDataRepositoryError(
          s"[JsonDataRepository] Failed to decode $jsonDataFilename: ${cause.getMessage}"
        )
  }

  def apply[F[_]: Async: LogWriter](): JsonDataRepository[F] =
    new JsonDataRepositoryImpl[F]()

  class JsonDataRepositoryImpl[F[_]: Async: LogWriter]() extends JsonDataRepository[F] {

    override def loadData[T: Decoder](jsonDataFilename: String): F[List[T]] = {
      val program = for {
        _           <- Resource.eval(LogWriter.info(s"[JsonDataRepository] Load resouces from: $jsonDataFilename"))
        eitherBytes <- Repository.getResourceByteArray(jsonDataFilename)
        jsonContent <- Resource.eval(
          eitherBytes.fold(
            err => Async[F].raiseError(err),
            bytes => String(bytes, StandardCharsets.UTF_8).pure[F]
          )
        )
        _       <- Resource.eval(LogWriter.info(s"[JsonDataRepository] jsonContent length: ${jsonContent.length()}"))
        decoded <- Resource.eval(
          Async[F]
            .fromEither(decode[List[T]](jsonContent))
            .adaptError(e => JsonDataRepositoryError.DecodeError(jsonDataFilename, e))
        )
      } yield decoded

      program.use(Async[F].pure)
    }
  }
}
