package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import com.benkio.chatcore.model.show.IcsCalendar
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowCalendar
import com.benkio.chatcore.repository.db.DBShowData
import io.circe.parser.decode

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneOffset

object GenerateCalendar extends IOApp {

  // Captions are not needed for the calendar (and the stored SRT column is not always decodable).
  def loadShows(showFilePath: Path): IO[List[Show]] =
    for {
      content <- IO.blocking(Files.readString(showFilePath, StandardCharsets.UTF_8))
      dbShows <- IO.fromEither(decode[List[DBShowData]](content))
      shows   <- dbShows.traverse(dbShow => Show[IO](dbShow.copy(show_origin_automatic_caption_srt = "")))
    } yield shows

  // Derived from the data (not the clock) so that regenerating without new shows leaves the files untouched.
  def dtStamp(shows: List[Show]): Instant =
    shows.map(_.uploadDate).maxOption.fold(Instant.EPOCH)(_.atStartOfDay(ZoneOffset.UTC).toInstant)

  def generateCalendar(shows: List[Show], dtStamp: Instant): String =
    IcsCalendar.render(ShowCalendar.orderedCandidates(shows), dtStamp)

  def writeCalendarFile(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  def generateCalendarFile(showFilePath: Path, calendarFilePath: Path): IO[Unit] =
    for {
      _     <- IO.println(s"[GenerateCalendar] Generate calendar file: $calendarFilePath")
      shows <- loadShows(showFilePath)
      _     <- writeCalendarFile(calendarFilePath, generateCalendar(shows, dtStamp(shows)))
      _     <- IO.println(s"[GenerateCalendar] Generate $calendarFilePath done")
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    BotRegistry
      .toList(BotRegistry.value)
      .traverse_((botRegistryEntry: BotRegistryEntry[IO]) =>
        generateCalendarFile(
          showFilePath = botRegistryEntry.sBotConfig.showFilePath,
          calendarFilePath = botRegistryEntry.sBotConfig.calendarFilePath
        )
      )
      .as(ExitCode.Success)

}
