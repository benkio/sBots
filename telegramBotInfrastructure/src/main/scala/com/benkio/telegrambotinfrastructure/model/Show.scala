package com.benkio.telegrambotinfrastructure.model

import org.http4s.Uri

import java.time.LocalDate

final case class Show(
    url: Uri,
    botName: String,
    title: String,
    uploadDate: LocalDate,
    duration: Int,
    description: Option[String]
)
