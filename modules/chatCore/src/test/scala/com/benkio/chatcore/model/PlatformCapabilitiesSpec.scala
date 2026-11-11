package com.benkio.chatcore.model

import com.benkio.chatcore.model.reply.*
import munit.FunSuite

class PlatformCapabilitiesSpec extends FunSuite {

  private val textOnly: PlatformCapabilities = PlatformCapabilities(
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

  test("MediaKind.of should give a kind to every media file") {
    val kinds = List[MediaFile](
      Mp3File("a.mp3"),
      GifFile("aGif.mp4"),
      PhotoFile("a.jpg"),
      VideoFile("a.mp4"),
      Document("a.pdf"),
      Sticker("a.sticker"),
      MarkdownFile("a.md")
    ).map(MediaKind.of)
    assertEquals(kinds, MediaKind.values.toList)
  }

  test("supports should check the kind of the media file") {
    val capabilities = textOnly.copy(supportedMedia = Set(MediaKind.Photo))
    assert(capabilities.supports(PhotoFile("a.png")))
    assert(!capabilities.supports(Mp3File("a.mp3")))
    assert(!textOnly.supports(PhotoFile("a.png")))
  }

  test("the maximum text length must be positive") {
    intercept[IllegalArgumentException](textOnly.copy(maxTextLength = 0))
  }
}
