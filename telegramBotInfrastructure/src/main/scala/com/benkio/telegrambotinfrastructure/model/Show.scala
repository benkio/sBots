package com.benkio.telegrambotinfrastructure.model

import java.time.LocalDate

final case class Show(
    id: String,
    botName: String,
    title: String,
    uploadDate: LocalDate,
    duration: Int,
    description: Option[String]
)
