package com.benkio.botDB.db.schema

import java.net.URL
import java.sql.Timestamp


final case class MediaEntity(
    media_name: String,
    kind: Option[String],
    media_url: URL,
    created_at: Timestamp
)
