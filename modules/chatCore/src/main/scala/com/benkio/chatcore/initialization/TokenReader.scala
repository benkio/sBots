package com.benkio.chatcore.initialization

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

object TokenReader {

  enum TokenError(msg: String) extends Throwable(msg) {
    case TokenNotFound(tokenFilename: String) extends TokenError(s"[TokenReader] Cannot find the token $tokenFilename")
    case TokenIsEmpty(tokenFilename: String)
        extends TokenError(s"[TokenReader] the retrieved token $tokenFilename is empty")
  }

  def token[F[_]: Async: LogWriter](
      tokenFilename: String,
      repository: Repository[F]
  ): Resource[F, String] =
    for {
      _                   <- Resource.eval(LogWriter.info(s"[TokenReader] Retrieving Token $tokenFilename"))
      tokenMediaResources <- repository.getResourceFile(Document(tokenFilename))
      tokenFiles          <- Resource
        .eval(Async[F].fromEither(tokenMediaResources.leftMap(_ => TokenError.TokenNotFound(tokenFilename))))
        .flatMap(mediaResources => mediaResources.toList.collect { case MediaResourceFile(rf) => rf }.sequence)
      tokenFileContent <-
        Resource
          .eval(Async[F].fromOption(tokenFiles.headOption, TokenError.TokenNotFound(tokenFilename)))
          .flatMap(Repository.fileToString)
      _ <- Resource.eval(
        Async[F].raiseWhen(tokenFileContent.isEmpty)(
          TokenError.TokenIsEmpty(tokenFilename)
        )
      )
      _ <- Resource.eval(
        LogWriter.info(s"[TokenReader] Token $tokenFilename successfully retrieved")
      )
    } yield tokenFileContent
}
