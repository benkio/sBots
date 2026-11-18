package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.media.MediaSource.FileRef
import com.benkio.chatcore.model.media.MediaSource.Url
import com.benkio.chatcore.model.Platform
import munit.FunSuite
import org.http4s.implicits.*
import org.http4s.Uri

class MediaSourcesSpec extends FunSuite {

  private def tg(id: String): MediaSource = FileRef(Platform.Telegram, id)
  private def dc(id: String): MediaSource = FileRef(Platform.Discord, id)

  private val url1: Uri = uri"https://example.com/a.mp4"
  private val url2: Uri = uri"https://example.com/b.mp4"

  test("a fresh reference is put first and the URLs keep their order") {
    val sources: List[MediaSource] = List(Url(url1), Url(url2))
    assertEquals(
      MediaSources.withFileId(sources, Platform.Telegram, "new-id"),
      List(tg("new-id"), Url(url1), Url(url2))
    )
  }

  test("registering the same reference again does not duplicate it") {
    val sources: List[MediaSource] = List(tg("new-id"), Url(url1))
    assertEquals(
      MediaSources.withFileId(sources, Platform.Telegram, "new-id"),
      List(tg("new-id"), Url(url1))
    )
  }

  test("only the newest references of a platform are kept, up to maxFileIds") {
    val sources: List[MediaSource] =
      List(tg("old1"), tg("old2"), tg("old3"), Url(url1))
    val result = List("id4", "id3", "id2", "id1").foldLeft(sources)(MediaSources.withFileId(_, Platform.Telegram, _))
    assertEquals(
      result,
      List(tg("id1"), tg("id2"), tg("id3"), Url(url1))
    )
    assertEquals(result.collect { case FileRef(_, id) => id }.size, MediaSources.maxFileIds)
  }

  test("another platform gets its own references without evicting or seeing the first platform's") {
    val sources: List[MediaSource] = List(tg("tg"), Url(url1))
    val both                       = MediaSources.withFileId(sources, Platform.Discord, "dc")
    assertEquals(both, List(dc("dc"), Url(url1), tg("tg")))
    val bothAgain = MediaSources.withFileId(both, Platform.Telegram, "tg2")
    assertEquals(
      bothAgain,
      List(tg("tg2"), tg("tg"), Url(url1), dc("dc"))
    )
  }

  test("a blank reference leaves the sources unchanged") {
    val sources: List[MediaSource] = List(tg("id"), Url(url1))
    assertEquals(MediaSources.withFileId(sources, Platform.Telegram, "  "), sources)
  }

  test("replaceFileId removes the failed reference and puts the fresh one first") {
    val sources: List[MediaSource] =
      List(tg("failed"), tg("older"), Url(url1))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "failed", "fresh"),
      List(tg("fresh"), tg("older"), Url(url1))
    )
  }

  test("replaceFileId leaves other platforms, URLs and unrelated references untouched") {
    val sources: List[MediaSource] =
      List(tg("failed"), dc("dc"), tg("other"), Url(url1), Url(url2))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "failed", "fresh"),
      List(tg("fresh"), tg("other"), Url(url1), Url(url2), dc("dc"))
    )
  }

  test("replaceFileId does not grow the number of stored IDs across repeated failures") {
    val sources: List[MediaSource] =
      List(tg("old1"), tg("old2"), tg("old3"), Url(url1))
    val afterFirst  = MediaSources.replaceFileId(sources, Platform.Telegram, "old1", "new1")
    val afterSecond = MediaSources.replaceFileId(afterFirst, Platform.Telegram, "new1", "new2")
    assertEquals(afterFirst.collect { case FileRef(_, _) => () }.size, MediaSources.maxFileIds)
    assertEquals(afterSecond.collect { case FileRef(_, _) => () }.size, MediaSources.maxFileIds)
    assert(afterSecond.contains(tg("new2")))
    assert(!afterSecond.contains(tg("new1")))
    assert(afterSecond.contains(Url(url1)))
  }

  test("replaceFileId ignores a failed ID from another platform and a blank fresh ID") {
    val sources: List[MediaSource] = List(dc("dc"), tg("id"), Url(url1))
    val otherPlatformFailed        = MediaSources.replaceFileId(sources, Platform.Telegram, "dc", "fresh")
    assert(otherPlatformFailed.contains(dc("dc")), "another platform's reference is not removed")
    assert(otherPlatformFailed.contains(tg("fresh")))
    assertEquals(
      MediaSources.replaceFileId(sources, Platform.Telegram, "id", "  "),
      List(dc("dc"), Url(url1))
    )
  }
}
