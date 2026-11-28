package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.repository.db.DBMediaData
import munit.*
import org.http4s.syntax.literals.*
import org.http4s.Uri


class MediaSpec extends FunSuite {
  test("Media.apply should correctly parse a valid db record") {
    val input: DBMediaData = DBMediaData(
      media_name = "rphjb_Animali.mp3",
      bot_id = "rphjb",
      kinds = """"[\"kind\"]"""",
      media_sources =
        """"[\"telegram:CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE\",\"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1\"]"""",
      mime_type = "audio/mpeg",
      media_count = 0,
      created_at = "1662126018293"
    )

    val result = Media(input)
    assert(result.isRight)
    assert(
      result.map(_.mediaName) == Right("rphjb_Animali.mp3"),
      s"""[MediaSpec] Expected: Right("rphjb_Animali.mp3"), got: ${result.map(_.mediaName)}"""
    )
    assert(
      result.map(_.kinds) == Right(List("kind")),
      s"""[MediaSpec] Expected: Right(List("kind")), got: ${result.map(_.kinds)}"""
    )
    assert(
      result.map(_.mediaSources) == Right(
        List(
          MediaSource
            .FileRef(Platform.Telegram, "CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE"),
          MediaSource.Url(
            uri"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1"
          )
        )
      ),
      s"""[MediaSpec] Expected: List(Left("CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE"),Right(uri"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1")), got: ${result
          .map(_.mediaSources)}"""
    )
    assert(
      result.map(_.mediaCount) == Right(0),
      s"""[MediaSpec] Expected: Right(0), got: ${result.map(_.mediaCount)}"""
    )
  }
}
