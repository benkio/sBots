package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import scala.concurrent.duration.Duration

class GenerateCalendarSpec extends CatsEffectSuite {

  override val munitIOTimeout = Duration(1, "m")

  private val dtStamp = Instant.parse("2026-11-05T10:15:30Z")

  private def withShowFile[A](content: String)(f: Path => IO[A]): IO[A] =
    IO.blocking(Files.createTempFile("shows", ".json")).flatMap { path =>
      IO.blocking(Files.writeString(path, content, StandardCharsets.UTF_8)) *> f(path)
        .guarantee(IO.blocking(Files.deleteIfExists(path)).void)
    }

  private def showJson(id: String, title: String, date: String, duration: Int, live: Boolean, srt: String): String =
    s"""{ "show_id": "$id", "bot_id": "test", "show_title": "$title", "show_upload_date": "$date",
       |  "show_duration": $duration, "show_description": "desc $id", "show_is_live": $live,
       |  "show_origin_automatic_caption": null, "show_origin_automatic_caption_srt": "$srt" }""".stripMargin

  test("loadShows decodes the shows file, tolerating non decodable captions") {
    val json = s"[${showJson(id = "a", title = "Live 23 Novembre 1999", date = "2020-05-17T11:46:11.000Z", duration = 3600, live = false, srt = "{}")}]"
    withShowFile(json)(GenerateCalendar.loadShows).map { shows =>
      assertEquals(shows.map(_.id), List("a"))
      assertEquals(shows.flatMap(_.originAutomaticCaptionSrt), List.empty)
    }
  }

  test("dtStamp is the start of the newest upload day, or the epoch without shows") {
    val json = List(
      showJson(id = "a", title = "Plain", date = "2020-05-17T11:46:11.000Z", duration = 3600, live = false, srt = "[]"),
      showJson(id = "b", title = "Plain", date = "2022-01-02T23:59:59.000Z", duration = 3600, live = false, srt = "[]")
    ).mkString("[", ",", "]")
    withShowFile(json)(GenerateCalendar.loadShows).map { shows =>
      assertEquals(GenerateCalendar.dtStamp(shows), Instant.parse("2022-01-02T00:00:00Z"))
      assertEquals(GenerateCalendar.dtStamp(List.empty), Instant.EPOCH)
    }
  }

  test("generateCalendar keeps the candidates only") {
    val json = List(
      showJson(id = "vod", title = "Plain", date = "2020-05-17T11:46:11.000Z", duration = 3600, live = false, srt = "[]"),
      showJson(id = "live", title = "Plain live", date = "2020-05-17T11:46:11.000Z", duration = 3600, live = true, srt = "[]"),
      showJson(id = "short", title = "Short", date = "2021-05-17T11:46:11.000Z", duration = 30, live = false, srt = "[]")
    ).mkString("[", ",", "]")
    withShowFile(json)(GenerateCalendar.loadShows).map { shows =>
      val ics = GenerateCalendar.generateCalendar(shows, dtStamp)
      assert(ics.contains("UID:vod@sbots"))
      assert(!ics.contains("UID:live@sbots"))
      assert(!ics.contains("UID:short@sbots"))
      assert(ics.contains("RRULE:FREQ=YEARLY"))
    }
  }

  test("generateCalendarFile writes the ics next to the given path") {
    val json = s"[${showJson(id = "a", title = "Plain", date = "2020-05-17T11:46:11.000Z", duration = 3600, live = false, srt = "{}")}]"
    withShowFile(json) { showPath =>
      val calendarPath = showPath.resolveSibling(showPath.getFileName.toString + ".ics")
      GenerateCalendar
        .generateCalendarFile(showPath, calendarPath)
        .map(_ => Files.readString(calendarPath, StandardCharsets.UTF_8))
        .guarantee(IO.blocking(Files.deleteIfExists(calendarPath)).void)
        .map { content =>
          assert(content.startsWith("BEGIN:VCALENDAR\r\n"))
          assert(content.contains("DTSTART;VALUE=DATE:20200517"))
        }
    }
  }

  test("GenerateCalendar.run should create the calendar of every bot") {
    val calendarFiles: List[Path] = BotRegistry.toList(BotRegistry.value).map(_.sBotConfig.calendarFilePath)
    GenerateCalendar
      .run(List.empty)
      .map { exitCode =>
        assertEquals(exitCode, ExitCode.Success)
        calendarFiles.foreach { file =>
          assert(Files.exists(file), s"$file should exist")
          assert(Files.readString(file, StandardCharsets.UTF_8).startsWith("BEGIN:VCALENDAR"))
        }
      }
  }
}
