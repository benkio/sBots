package com.benkio.chatcore.model.show

import com.benkio.chatcore.model.SBotInfo.SBotId

import java.time.LocalDate
import scala.collection.immutable.Vector

object ShowTestData {
  def show(
      id: String = "abc",
      title: String = "A title",
      uploadDate: LocalDate = LocalDate.of(2020, 5, 17),
      duration: Int = 3600,
      description: Option[String] = Some("A description"),
      isLive: Boolean = false
  ): Show = Show(
    id = id,
    botId = SBotId("test"),
    title = title,
    uploadDate = uploadDate,
    duration = duration,
    description = description,
    isLive = isLive,
    originAutomaticCaption = None,
    originAutomaticCaptionSrt = Vector.empty,
    timestamp = None
  )
}
