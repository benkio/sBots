package com.benkio.chatcore.initialization

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

import java.nio.file.Path

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
      tokenFiles          <- tokenMediaResources match {
        case Left(_) =>
          Resource.eval[F, List[Path]](Async[F].raiseError(TokenError.TokenNotFound(tokenFilename)))
        case Right(mediaResources) =>
          mediaResources.toList.collect { case MediaResourceFile(rf) => rf }.sequence
      }
      tokenFileContent <-
        tokenFiles.headOption
          .fold(Resource.eval(Async[F].raiseError(TokenError.TokenNotFound(tokenFilename))))(Repository.fileToString)
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
