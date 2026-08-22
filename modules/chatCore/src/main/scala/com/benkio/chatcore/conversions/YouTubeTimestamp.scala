package com.benkio.chatcore.conversions

import scala.concurrent.duration.FiniteDuration

object YouTubeTimestamp {
  private val TimestampRegex = """^(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$""".r

  def finiteDurationToYoutubeTimestamp(timestamp: FiniteDuration): String = {
    val totalSeconds = timestamp.toSeconds
    val hours        = totalSeconds / 3600
    val minutes      = (totalSeconds % 3600) / 60
    val seconds      = totalSeconds  % 60

    val hoursPart   = if hours > 0 then s"${hours}h" else ""
    val minutesPart = if minutes > 0 then s"${minutes}m" else ""
    val secondsPart =
      if seconds > 0 || (hours == 0 && minutes == 0) then s"${seconds}s" else ""

    s"$hoursPart$minutesPart$secondsPart"
  }

  def youtubeTimestampToFiniteDuration(timestamp: String): Option[FiniteDuration] =
    timestamp match {
      case TimestampRegex(hours, minutes, seconds) =>
        val parsedHours   = Option(hours).flatMap(_.toLongOption).getOrElse(0L)
        val parsedMinutes = Option(minutes).flatMap(_.toLongOption).getOrElse(0L)
        val parsedSeconds = Option(seconds).flatMap(_.toLongOption).getOrElse(0L)
        Option.when(hours != null || minutes != null || seconds != null) {
          FiniteDuration(parsedHours * 3600 + parsedMinutes * 60 + parsedSeconds, "seconds")
        }
      case _ => None
    }
}
