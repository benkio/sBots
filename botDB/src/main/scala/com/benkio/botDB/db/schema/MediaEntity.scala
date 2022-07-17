package com.benkio.botDB.db.schema

import java.sql.Timestamp

final case class MediaEntity(
  media_name: String,
  media_content: Array[Byte],
  changed_at: Timestamp
)

object MediaEntity {

  // TODO: add the Meta instance to handle Blob
  // Look here: https://github.com/tpolecat/doobie/issues/266
}
