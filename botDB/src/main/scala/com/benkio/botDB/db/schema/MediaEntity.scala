package com.benkio.botDB.db.schema

import java.io.File
import java.nio.file.Files
import java.sql.Timestamp
import java.time.Instant

final case class MediaEntity(
    media_name: String,
    kind: Option[String],
    media_content: Array[Byte],
    created_at: Timestamp
)

object MediaEntity {

  def fromFile(file: File, kind: Option[String]): MediaEntity = MediaEntity(
    media_name = file.getName(),
    kind = kind,
    media_content = Files.readAllBytes(file.toPath),
    created_at = Timestamp.from(Instant.now()),
  )

  // TODO: add the Meta instance to handle Blob
  // Look here: https://github.com/tpolecat/doobie/issues/266
}
