package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.Media
import org.http4s.Uri
import java.time.Instant
import munit._
import org.http4s.implicits.*

class DBMediaSpec extends FunSuite {
  test("DBMediaData should be correctly converted from Media") {
    val now = Instant.now
    val actual = Media(
      mediaName = "mediaName",
      kinds = List("kind"),
      mediaUrl = uri"http://something.com",
      mediaCount = 0,
      createdAt = now,
    )
    val expected = DBMediaData(
      media_name = actual.mediaName,
      kinds = Some("""["kind"]"""),
      media_url = "http://something.com",
      media_count = actual.mediaCount,
      created_at = now.toString
    )
    assertEquals(DBMediaData(actual), expected)
  }
}
