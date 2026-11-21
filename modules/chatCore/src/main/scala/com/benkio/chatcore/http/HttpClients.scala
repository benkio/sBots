package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import org.http4s.Uri

import java.nio.file.Path

final case class HttpClients[F[_]](
    dropboxClient: DropboxClient[F],
    megaClient: MegaClient[F]
)

object HttpClients {
  enum ExpectedClient {
    case Dropbox
    case Mega
  }

  /** Typed error for a URI no configured client can handle. It is raised in `F` by [[fetchFile]], never thrown from a
    * pure helper.
    */
  final case class UnsupportedClient(uri: Uri) extends RuntimeException(s"Not supported client for uri: $uri")

  def expectedClient(uri: Uri): Either[UnsupportedClient, ExpectedClient] =
    uri.host match {
      case Some(host) if host.value.contains("mega.nz")     => Right(ExpectedClient.Mega)
      case Some(host) if host.value.contains("dropbox.com") => Right(ExpectedClient.Dropbox)
      case _                                                => Left(UnsupportedClient(uri))
    }

  def fetchFile[F[_]: Async](
      clients: HttpClients[F],
      filename: String,
      uri: Uri
  ): Resource[F, Path] =
    Resource.eval(Async[F].fromEither(expectedClient(uri))).flatMap {
      case ExpectedClient.Mega    => clients.megaClient.fetchFile(filename, uri)
      case ExpectedClient.Dropbox => clients.dropboxClient.fetchFile(filename, uri)
    }
}
