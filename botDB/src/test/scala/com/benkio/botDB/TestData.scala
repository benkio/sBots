package com.benkio.botDB

import com.benkio.botDB.db.schema.MediaEntity

import java.sql.Timestamp
object TestData {

  val mediaEntity = MediaEntity(
    media_name = "media_name.mp3",
    kind = None,
    media_content = Array(),
    created_at = new Timestamp(1658054878L)
  )
}
