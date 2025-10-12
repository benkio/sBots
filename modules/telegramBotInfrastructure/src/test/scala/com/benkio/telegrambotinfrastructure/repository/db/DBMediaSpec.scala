package com.benkio.telegrambotinfrastructure.repository.db

import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.MimeType
import munit.*
import org.http4s.implicits.*
import org.http4s.Uri

import java.time.Instant

class DBMediaSpec extends FunSuite {
  test("DBMediaData should be correctly converted from Media") {
    val now    = Instant.now
    val actual = Media(
      mediaName = "botid_mediaName.mp4",
      botId = "botid",
      kinds = List("kind"),
      mimeType = MimeType.MP4,
      mediaSources = List(Right(uri"http://something.com")),
      mediaCount = 0,
      createdAt = now
    )
    val expected = DBMediaData(
      media_name = actual.mediaName,
      bot_id = actual.botId,
      kinds = """["kind"]""",
      media_sources = """["http://something.com"]""",
      mime_type = "video/mp4",
      media_count = actual.mediaCount,
      created_at = now.toString
    )
    assertEquals(DBMediaData(actual), expected)
  }
}
