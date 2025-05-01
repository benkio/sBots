package com.benkio.telegrambotinfrastructure.dataentry
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.MimeType
import munit.*
import org.http4s.Uri

class DataEntrySpec extends CatsEffectSuite {
  test("parseInput should successfully parse a valid input") {
    val input = List(
      "https://www.dropbox.com/scl/fi/kdebzm75zf9qobugzbf3v/rphjb_5DitaRivolta.mp3?rlkey=9sr3dhbbt0ntqh29280sjmyqo&dl=1",
      "https://www.dropbox.com/scl/fi/x65f1r5qvxl27090yu8il/rphjb_5DitaRivolta.mp4?rlkey=imii04m83xnn27zm5qf08s350&dl=1",
      "https://www.dropbox.com/scl/fi/1uo9npnlupdk5jhykd8zt/rphjb_5DitaRivoltaGif.mp4?rlkey=2nzrxid175oik5m5fva1z54kr&dl=1",
      "https://www.dropbox.com/scl/fi/o6unllv17028yx059mdpl/rphjb_AdolfHitlerGif.mp4?rlkey=750bdh4zud1j1eier6q7pr7rh&dl=1"
    )
    val actual = DataEntry.parseInput(input)
    val expected = List(
      MediaFileSource(
        filename = "rphjb_5DitaRivolta.mp3",
        kinds = List.empty,
        mime = MimeType.MPEG,
        sources = List(
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/kdebzm75zf9qobugzbf3v/rphjb_5DitaRivolta.mp3?rlkey=9sr3dhbbt0ntqh29280sjmyqo&dl=1"
            )
          )
        )
      ),
      MediaFileSource(
        filename = "rphjb_5DitaRivolta.mp4",
        kinds = List.empty,
        mime = MimeType.MP4,
        sources = List(
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/x65f1r5qvxl27090yu8il/rphjb_5DitaRivolta.mp4?rlkey=imii04m83xnn27zm5qf08s350&dl=1"
            )
          )
        )
      ),
      MediaFileSource(
        filename = "rphjb_5DitaRivoltaGif.mp4",
        kinds = List.empty,
        mime = MimeType.GIF,
        sources = List(
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/1uo9npnlupdk5jhykd8zt/rphjb_5DitaRivoltaGif.mp4?rlkey=2nzrxid175oik5m5fva1z54kr&dl=1"
            )
          )
        )
      ),
      MediaFileSource(
        filename = "rphjb_AdolfHitlerGif.mp4",
        kinds = List.empty,
        mime = MimeType.GIF,
        sources = List(
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/o6unllv17028yx059mdpl/rphjb_AdolfHitlerGif.mp4?rlkey=750bdh4zud1j1eier6q7pr7rh&dl=1"
            )
          )
        )
      )
    )
    assertIO(actual, expected)
  }
}
