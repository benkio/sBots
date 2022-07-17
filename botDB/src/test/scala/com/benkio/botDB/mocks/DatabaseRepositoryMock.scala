package com.benkio.botDB.mocks

import cats.effect.IO
import cats.effect.kernel.Ref
import com.benkio.botDB.db.DatabaseRepository
import com.benkio.botDB.db.schema.MediaEntity

class DatabaseRepositoryMock(expectedMediaFilesRef: Ref[IO, List[MediaEntity]]) extends DatabaseRepository[IO] {
  override def insertMedia(mediaEntity: MediaEntity): IO[Unit] =
    expectedMediaFilesRef.get.flatMap(expectedMediaFiles =>
      if (expectedMediaFiles.map(_.media_name).contains(mediaEntity.media_name)) {
        IO(())
      } else {
        IO.raiseError(
          new RuntimeException(
            s"mediaEntity: $mediaEntity is not contained in the expected mediaEntities: $expectedMediaFiles"
          )
        )
      }
    )
}
