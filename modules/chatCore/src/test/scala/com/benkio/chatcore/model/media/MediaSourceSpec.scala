package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.media.MediaSource.FileRef
import com.benkio.chatcore.model.media.MediaSource.Url
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.repository.db.DBMediaData
import io.circe.syntax.*
import munit.FunSuite
import org.http4s.implicits.*

class MediaSourceSpec extends FunSuite {

  test("an http(s) url is an url") {
    assertEquals(MediaSource.parse("https://example.com/a.mp3?dl=1"), Some(Url(uri"https://example.com/a.mp3?dl=1")))
  }

  test("platform:id is a reference of that platform, the id keeps its own colons") {
    assertEquals(MediaSource.parse("telegram:AwAC:x"), Some(FileRef(Platform.Telegram, "AwAC:x")))
    assertEquals(MediaSource.parse("discord:123"), Some(FileRef(Platform.Discord, "123")))
  }

  test("an untagged reference, an unknown platform or an empty id is not a source") {
    assertEquals(MediaSource.parse("AwACAgQ"), None)
    assertEquals(MediaSource.parse("whatsapp:123"), None)
    assertEquals(MediaSource.parse("telegram:"), None)
    assertEquals(MediaSource.parse(":123"), None)
  }

  test("render is the inverse of parse") {
    List("https://example.com/a.mp3", "telegram:AwAC", "irc:x").foreach(value =>
      assertEquals(MediaSource.parse(value).map(MediaSource.render), Some(value))
    )
  }

  test("a list of sources encodes as the stored strings") {
    val sources: List[MediaSource] = List(FileRef(Platform.Telegram, "id"), Url(uri"https://example.com/a.mp3"))
    assertEquals(sources.asJson.noSpaces, """["telegram:id","https://example.com/a.mp3"]""")
  }

  test("Media drops an entry that is not a source without losing the valid ones") {
    val db = DBMediaData(
      media_name = "bot_a.mp3",
      bot_id = "bot",
      kinds = "[]",
      mime_type = "audio/mpeg",
      media_sources = """["untagged-id","telegram:ok","https://example.com/a.mp3"]""",
      media_count = 0,
      created_at = "0"
    )
    assertEquals(
      Media(db).map(_.mediaSources),
      Right(List(FileRef(Platform.Telegram, "ok"), Url(uri"https://example.com/a.mp3")))
    )
  }
}
