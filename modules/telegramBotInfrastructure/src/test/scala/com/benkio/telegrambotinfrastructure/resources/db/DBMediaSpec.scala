package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.media.Media
import munit.*
import org.http4s.implicits.*
import org.http4s.Uri

import java.time.Instant

class DBMediaSpec extends FunSuite {
  test("DBMediaData should be correctly converted from Media") {
    val now = Instant.now
    val actual = Media(
      mediaName = "mediaName",
      kinds = List("kind"),
      mimeType = "video/mp4",
      mediaSources = List(Right(uri"http://something.com")),
      mediaCount = 0,
      createdAt = now
    )
    val expected = DBMediaData(
      media_name = actual.mediaName,
      kinds = """["kind"]""",
      media_sources = """["http://something.com"]""",
      mime_type = "application/octet-stream",
      media_count = actual.mediaCount,
      created_at = now.toString
    )
    assertEquals(DBMediaData(actual), expected)
  }
}
