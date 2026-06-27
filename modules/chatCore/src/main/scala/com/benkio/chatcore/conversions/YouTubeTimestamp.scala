package com.benkio.chatcore.conversions

import scala.concurrent.duration.FiniteDuration

object YouTubeTimestamp {

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
}
