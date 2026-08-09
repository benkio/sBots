package com.benkio.chatcore.mocks

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.http.MegaClient
import org.http4s.Uri

import java.nio.file.Path

object MegaClientMock {

  def mock(
      handler: (String, Uri) => Resource[IO, Path]
  ): MegaClient[IO] = new MegaClient[IO] {

    override def fetchFile(filename: String, url: Uri): Resource[IO, Path] =
      handler(filename, url)

  }
}
