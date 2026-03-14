package com.benkio.chatcore.model

import munit.FunSuite

class MimeTypeSpec extends FunSuite {

  test("mimeTypeOrDefault should parse explicit known mime type") {
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("file.txt", Some("audio/mpeg")),
      MimeType.MPEG
    )
  }

  test("mimeTypeOrDefault should infer mime type from media extension") {
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("photo.jpg", None),
      MimeType.JPEG
    )
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("animation.gif.mp4", None),
      MimeType.GIF
    )
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("video.mp4", None),
      MimeType.MP4
    )
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("audio.mp3", None),
      MimeType.MPEG
    )
  }

  test("mimeTypeOrDefault should fallback to DOC when unrecognized") {
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("unknown.abc", Some("invalid/type")),
      MimeType.DOC
    )
    assertEquals(
      MimeTypeOps.mimeTypeOrDefault("unknown", None),
      MimeType.DOC
    )
  }
}
