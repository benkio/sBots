package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.db.DatabaseRepository
import com.benkio.botDB.db.schema.MediaEntity

import java.io.File

class DatabaseRepositoryMock(expectedFiles: List[File]) extends DatabaseRepository[IO] {
  println(expectedFiles)
  override def insertMedia(mediaEntity: MediaEntity): IO[Unit] = ???
}
