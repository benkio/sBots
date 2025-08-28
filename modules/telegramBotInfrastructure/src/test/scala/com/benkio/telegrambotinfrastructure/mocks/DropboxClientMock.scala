package com.benkio.telegrambotinfrastructure.mocks

import com.benkio.telegrambotinfrastructure.http.DropboxClient
import cats.effect.IO
import cats.effect.Resource
import org.http4s.Uri
import java.io.File

object DropboxClientMock:

  def mock(
    handler: (String, Uri) => Resource[IO, File]
  ): DropboxClient[IO] = new DropboxClient[IO] {

    override def fetchFile(filename: String, url: Uri): Resource[IO, File] =
      handler(filename, url)

  }
