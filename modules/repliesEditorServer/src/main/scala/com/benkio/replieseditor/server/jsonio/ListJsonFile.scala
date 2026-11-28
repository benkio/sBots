package com.benkio.replieseditor.server.jsonio

import cats.effect.IO
import cats.syntax.either.*
import com.benkio.chatcore.model.media.MediaFileSource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object ListJsonFile {

  def readFilenamesSorted(path: Path): IO[List[String]] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8)).flatMap { raw =>
      IO.fromEither(
        io.circe.parser
          .decode[List[MediaFileSource]](raw)
          .leftMap(err => new RuntimeException(err.getMessage))
      ).map(xs => xs.map(_.filename).sorted)
    }
}
