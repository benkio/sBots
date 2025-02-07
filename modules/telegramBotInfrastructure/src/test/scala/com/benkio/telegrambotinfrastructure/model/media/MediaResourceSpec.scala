package com.benkio.telegrambotinfrastructure.model.media

import munit.*
import telegramium.bots.InputLinkFile
import telegramium.bots.InputPartFile

import java.io.File

class MediaResourceSpec extends FunSuite {
  test("toTelegramApi should return the expected1 Telegram Type") {
    val file      = File(".")
    val actual1   = MediaResource.MediaResourceFile(file).toTelegramApi
    val expected1 = InputPartFile(file)
    assertEquals(actual1, expected1)
    val ifile     = "ifile"
    val actual2   = MediaResource.MediaResourceIFile(ifile).toTelegramApi
    val expected2 = InputLinkFile(ifile)
    assertEquals(actual2, expected2)
  }
  test("getMediaResourceFile should extrac the file or return None") {
    val file      = File(".")
    val actual1   = MediaResource.MediaResourceFile(file).getMediaResourceFile
    val expected1 = Some(file)
    assertEquals(actual1, expected1)
    val ifile   = "ifile"
    val actual2 = MediaResource.MediaResourceIFile(ifile).getMediaResourceFile
    assertEquals(actual2, None)
  }
}
