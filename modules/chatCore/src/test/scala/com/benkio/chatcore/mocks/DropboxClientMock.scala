package com.benkio.chatcore.mocks

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.http.DropboxClient
import org.http4s.Uri

import java.nio.file.Path

object DropboxClientMock {

  def mock(
      handler: (String, Uri) => Resource[IO, Path]
  ): DropboxClient[IO] = new DropboxClient[IO] {

    override def fetchFile(filename: String, url: Uri): Resource[IO, Path] =
      handler(filename, url)

  }
}
