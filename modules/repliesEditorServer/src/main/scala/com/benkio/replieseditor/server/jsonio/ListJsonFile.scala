package com.benkio.replieseditor.server.jsonio

import cats.effect.IO
import com.benkio.chatcore.model.media.MediaFileSource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object ListJsonFile {
  def readAllowedFilenames(path: Path): IO[Set[String]] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8)).flatMap { raw =>
      io.circe.parser.decode[List[MediaFileSource]](raw) match {
        case Left(err) => IO.raiseError(new RuntimeException(err.getMessage))
        case Right(xs) => IO.pure(xs.map(_.filename).toSet)
      }
    }

  def readFilenamesSorted(path: Path): IO[List[String]] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8)).flatMap { raw =>
      io.circe.parser.decode[List[MediaFileSource]](raw) match {
        case Left(err) => IO.raiseError(new RuntimeException(err.getMessage))
        case Right(xs) => IO.pure(xs.map(_.filename).sorted)
      }
    }
}
