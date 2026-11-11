package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import munit.ScalaCheckSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import scala.concurrent.duration.*

class ReplyTransformerSpec extends ScalaCheckSuite {

  private val everything: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Telegram,
    supportedMedia = MediaKind.values.toSet,
    maxTextLength = 4096,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = true,
    acceptsFileIds = true,
    commandPrefix = "/"
  )

  private val textOnly: PlatformCapabilities = everything.copy(
    platform = Platform.Irc,
    supportedMedia = Set.empty,
    maxTextLength = 400,
    markdown = MarkdownSupport.Plain,
    supportsDelete = false,
    supportsEdit = false,
    supportsButtons = false,
    acceptsFileIds = false,
    commandPrefix = "!"
  )

  private val mp3Link: Uri = uri"https://example.com/a.mp3"

  private val links: ReplyTransformer.MediaLink = {
    case Mp3File("a.mp3", _) => Some(mp3Link)
    case _                   => None
  }

  private val mediaFiles: List[MediaFile] = List(
    Mp3File("a.mp3"),
    GifFile("aGif.mp4"),
    PhotoFile("a.jpg"),
    VideoFile("a.mp4"),
    Document("a.pdf"),
    Sticker("a.sticker"),
    MarkdownFile("a.md")
  )

  private val replyValueGen: Gen[ReplyValue] = Gen.oneOf(
    Gen.alphaNumStr.map(Text(_)),
    Gen.alphaNumStr.map(Markdown(_)),
    Gen.oneOf(mediaFiles)
  )

  property("a platform that supports everything leaves the replies as they are") {
    forAll(Gen.listOf(replyValueGen)) { values =>
      assertEquals(ReplyTransformer.transformAll(everything)(values), values)
    }
  }

  test("a text within the limit is left as it is") {
    assertEquals(ReplyTransformer.transform(textOnly)(Text("hello")), List(Text("hello")))
  }

  test("an empty text is left as it is") {
    assertEquals(ReplyTransformer.transform(textOnly)(Text("")), List(Text("")))
  }

  test("a media the platform cannot send becomes its link") {
    assertEquals(ReplyTransformer.transform(textOnly, links)(Mp3File("a.mp3")), List(Text(mp3Link.renderString)))
  }

  test("a media the platform cannot send and without a link is dropped") {
    assertEquals(ReplyTransformer.transform(textOnly, links)(PhotoFile("a.jpg")), List.empty)
    assertEquals(ReplyTransformer.transform(textOnly)(Mp3File("a.mp3")), List.empty)
  }

  test("a media the platform can send is left as it is, whatever its link") {
    val capabilities = textOnly.copy(supportedMedia = Set(MediaKind.Audio))
    assertEquals(ReplyTransformer.transform(capabilities, links)(Mp3File("a.mp3")), List(Mp3File("a.mp3")))
  }

  test("every media kind is sent as a link by a text only platform") {
    val all: ReplyTransformer.MediaLink = f => Some(Uri.unsafeFromString(s"https://example.com/${f.filename}"))
    val result                          = ReplyTransformer.transformAll(textOnly, all)(mediaFiles)
    assertEquals(result, mediaFiles.map(f => Text(s"https://example.com/${f.filename}")))
  }

  test("markdown becomes plain text on a platform without markdown") {
    assertEquals(
      ReplyTransformer.transform(textOnly)(Markdown("**hi** [a](https://b.c)")),
      List(Text("hi a (https://b.c)"))
    )
  }

  test("markdown is left as it is on a platform with markdown") {
    assertEquals(ReplyTransformer.transform(everything)(Markdown("**hi**")), List(Markdown("**hi**")))
  }

  test("the time to live is dropped on a platform that cannot delete messages") {
    val ttl = Some(5.seconds)
    assertEquals(ReplyTransformer.transform(textOnly)(Text("x", ttl)), List(Text("x")))
    assertEquals(ReplyTransformer.transform(textOnly)(Markdown("x", ttl)), List(Text("x")))
    assertEquals(ReplyTransformer.transform(everything)(Text("x", ttl)), List(Text("x", ttl)))
    assertEquals(ReplyTransformer.transform(everything)(Markdown("x", ttl)), List(Markdown("x", ttl)))
  }

  test("a long text is split in chunks, each one with the time to live when it is supported") {
    val capabilities = everything.copy(maxTextLength = 7)
    val ttl          = Some(5.seconds)
    assertEquals(
      ReplyTransformer.transform(capabilities)(Text("aaa bbb ccc", ttl)),
      List(Text("aaa bbb", ttl), Text("ccc", ttl))
    )
  }

  test("a long markdown is split too") {
    assertEquals(
      ReplyTransformer.transform(everything.copy(maxTextLength = 7))(Markdown("aaa bbb ccc")),
      List(Markdown("aaa bbb"), Markdown("ccc"))
    )
  }

  test("splitText cuts on a line break first, then on a space, then in the middle of a word") {
    assertEquals(ReplyTransformer.splitText("ab cd\nef gh", 8), List("ab cd", "ef gh"))
    assertEquals(ReplyTransformer.splitText("aaa bbb ccc", 7), List("aaa bbb", "ccc"))
    assertEquals(ReplyTransformer.splitText("aaaaaaaaaa", 4), List("aaaa", "aaaa", "aa"))
  }

  test("splitText should not cut a surrogate pair") {
    val emoji = "😀"
    assertEquals(ReplyTransformer.splitText(emoji * 3, 3), List(emoji, emoji, emoji))
  }

  property("splitText gives chunks within the limit and loses no text") {
    forAll(Gen.stringOf(Gen.oneOf(Gen.alphaNumChar, Gen.const(' '), Gen.const('\n'))), Gen.choose(1, 30)) {
      (text, maxLength) =>
        val chunks = ReplyTransformer.splitText(text, maxLength)
        assert(chunks.forall(_.length <= maxLength), s"a chunk is over $maxLength: $chunks")
        assertEquals(chunks.mkString.filterNot(_.isWhitespace), text.filterNot(_.isWhitespace))
    }
  }

  test("the values outside of the core ones are left as they are") {
    case object AdapterValue extends ReplyValue
    assertEquals(ReplyTransformer.transform(textOnly)(AdapterValue), List(AdapterValue))
  }

  test("usableSources drops the sources that are not an url when file ids are not accepted") {
    val sources = List(Left("telegramFileId"), Right(mp3Link))
    assertEquals(ReplyTransformer.usableSources(textOnly)(sources), List(Right(mp3Link)))
    assertEquals(ReplyTransformer.usableSources(everything)(sources), sources)
  }
}
