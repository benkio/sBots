package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.Platform
import munit.FunSuite
import org.http4s.implicits.*
import org.http4s.Uri

class MediaSourcesSpec extends FunSuite {

  private val url1: Uri = uri"https://example.com/a.mp4"
  private val url2: Uri = uri"https://example.com/b.mp4"

  test("a fresh reference is put first and the URLs keep their order") {
    val sources: List[Either[String, Uri]] = List(Right(url1), Right(url2))
    assertEquals(
      MediaSources.withFileId(sources, Platform.Telegram, "new-id"),
      List(Left("telegram:new-id"), Right(url1), Right(url2))
    )
  }

  test("registering the same reference again does not duplicate it") {
    val sources: List[Either[String, Uri]] = List(Left("telegram:new-id"), Right(url1))
    assertEquals(
      MediaSources.withFileId(sources, Platform.Telegram, "new-id"),
      List(Left("telegram:new-id"), Right(url1))
    )
  }

  test("only the newest references of a platform are kept, up to maxFileIds") {
    val sources: List[Either[String, Uri]] =
      List(Left("telegram:old1"), Left("telegram:old2"), Left("telegram:old3"), Right(url1))
    val result = List("id4", "id3", "id2", "id1").foldLeft(sources)(MediaSources.withFileId(_, Platform.Telegram, _))
    assertEquals(
      result,
      List(Left("telegram:id1"), Left("telegram:id2"), Left("telegram:id3"), Right(url1))
    )
    assertEquals(result.collect { case Left(id) => id }.size, MediaSources.maxFileIds)
  }

  test("another platform gets its own references without evicting or seeing the first platform's") {
    val sources: List[Either[String, Uri]] = List(Left("telegram:tg"), Right(url1))
    val both                               = MediaSources.withFileId(sources, Platform.Discord, "dc")
    assertEquals(both, List(Left("discord:dc"), Right(url1), Left("telegram:tg")))
    val bothAgain = MediaSources.withFileId(both, Platform.Telegram, "tg2")
    assertEquals(
      bothAgain,
      List(Left("telegram:tg2"), Left("telegram:tg"), Right(url1), Left("discord:dc"))
    )
    assertEquals(MediaSources.belongsTo("telegram:tg2", Platform.Telegram), true)
    assertEquals(MediaSources.belongsTo("telegram:tg2", Platform.Discord), false)
    assertEquals(MediaSources.rawId("telegram:tg2"), "tg2")
  }

  test("an untagged reference matches no platform and is dropped on the next write") {
    val sources: List[Either[String, Uri]] = List(Left("legacy-id"), Right(url1))
    assertEquals(MediaSources.belongsTo("legacy-id", Platform.Telegram), false)
    assertEquals(MediaSources.belongsTo("legacy-id", Platform.Discord), false)
    assertEquals(
      MediaSources.withFileId(sources, Platform.Telegram, "new-id"),
      List(Left("telegram:new-id"), Right(url1))
    )
  }

  test("a blank reference leaves the sources unchanged") {
    val sources: List[Either[String, Uri]] = List(Left("telegram:id"), Right(url1))
    assertEquals(MediaSources.withFileId(sources, Platform.Telegram, "  "), sources)
  }

  test("replaceFileId removes the failed reference and puts the fresh one first") {
    val sources: List[Either[String, Uri]] =
      List(Left("telegram:failed"), Left("telegram:older"), Right(url1))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "failed", "fresh"),
      List(Left("telegram:fresh"), Left("telegram:older"), Right(url1))
    )
  }

  test("replaceFileId leaves other platforms, URLs and unrelated references untouched") {
    val sources: List[Either[String, Uri]] =
      List(Left("telegram:failed"), Left("discord:dc"), Left("telegram:other"), Right(url1), Right(url2))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "failed", "fresh"),
      List(Left("telegram:fresh"), Left("telegram:other"), Right(url1), Right(url2), Left("discord:dc"))
    )
  }

  test("replaceFileId does not grow the number of stored IDs across repeated failures") {
    val sources: List[Either[String, Uri]] =
      List(Left("telegram:old1"), Left("telegram:old2"), Left("telegram:old3"), Right(url1))
    val afterFirst  = MediaSources.replaceFileId(sources, Platform.Telegram, "old1", "new1")
    val afterSecond = MediaSources.replaceFileId(afterFirst, Platform.Telegram, "new1", "new2")
    assertEquals(afterFirst.collect { case Left(_) => () }.size, MediaSources.maxFileIds)
    assertEquals(afterSecond.collect { case Left(_) => () }.size, MediaSources.maxFileIds)
    assert(afterSecond.contains(Left("telegram:new2")))
    assert(!afterSecond.contains(Left("telegram:new1")))
    assert(afterSecond.contains(Right(url1)))
  }

  test("replaceFileId ignores a failed ID from another platform and a blank fresh ID") {
    val sources: List[Either[String, Uri]] = List(Left("discord:dc"), Left("telegram:id"), Right(url1))
    val otherPlatformFailed                = MediaSources.replaceFileId(sources, Platform.Telegram, "dc", "fresh")
    assert(otherPlatformFailed.contains(Left("discord:dc")), "another platform's reference is not removed")
    assert(otherPlatformFailed.contains(Left("telegram:fresh")))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "id", "  "),
      List(Left("discord:dc"), Right(url1))
    )
  }
}
