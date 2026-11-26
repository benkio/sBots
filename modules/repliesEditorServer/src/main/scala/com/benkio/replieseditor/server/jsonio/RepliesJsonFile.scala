package com.benkio.replieseditor.server.jsonio

import cats.effect.IO
import cats.syntax.either.*
import io.circe.parser.parse
import io.circe.Json

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object RepliesJsonFile {
  def read(path: Path): IO[Json] =
    IO.blocking(Files.readString(path, StandardCharsets.UTF_8)).flatMap { raw =>
      IO.fromEither(parse(raw).leftMap(pf => new RuntimeException(pf.message)))
    }

  def writePretty(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.createDirectories(path.getParent)
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }
}
