package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.db.DatabaseRepository

import java.io.File

class DatabaseRepositoryMock(expectedFiles: List[File]) extends DatabaseRepository[IO] {
  println(expectedFiles)
  override def insertMedia(f: File): IO[Unit] = ???
}
