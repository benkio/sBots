package com.benkio.chatcore.http

import munit.FunSuite
import org.http4s.syntax.literals.*

class HttpClientsSpec extends FunSuite {

  test("expectedClient should select supported clients only") {
    val megaUri    = uri"https://mega.nz/robots.txt"
    val dropboxUri = uri"https://www.dropbox.com/s/syd0ivnsyq1r5pk/file.mp4?dl=1"
    val otherUri   = uri"https://example.com/file.mp4"

    assertEquals(HttpClients.expectedClient(megaUri), HttpClients.ExpectedClient.Mega)
    assertEquals(HttpClients.expectedClient(dropboxUri), HttpClients.ExpectedClient.Dropbox)
    interceptMessage[IllegalArgumentException](
      s"Not supported client for uri: $otherUri"
    ) {
      HttpClients.expectedClient(otherUri)
    }
  }
}
