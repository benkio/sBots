package com.benkio.chatcore.http

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

  def expectedClient(uri: Uri): ExpectedClient =
    uri.host match {
      case Some(host) if host.value.contains("mega.nz")     => ExpectedClient.Mega
      case Some(host) if host.value.contains("dropbox.com") => ExpectedClient.Dropbox
      case _                                                =>
        throw new IllegalArgumentException(s"Not supported client for uri: $uri")
    }

  def fetchFile[F[_]](
      clients: HttpClients[F],
      filename: String,
      uri: Uri
  ): Resource[F, Path] =
    expectedClient(uri) match {
      case ExpectedClient.Mega    => clients.megaClient.fetchFile(filename, uri)
      case ExpectedClient.Dropbox => clients.dropboxClient.fetchFile(filename, uri)
    }
}
