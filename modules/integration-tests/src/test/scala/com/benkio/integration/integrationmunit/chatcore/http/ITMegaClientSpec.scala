package com.benkio.integration.integrationmunit.chatcore.http

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*

import java.nio.file.Files
import java.nio.file.Path

class ITMegaClientSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test("fetch should return the expected urls content in a file if the urls is valid") { fixture =>
    val input = List(
      uri"https://mega.nz/file/ebhDGaoY#GXRQWztlo_BpSrh3ly8Gc2O8NK1F7s1hEK3VeGFYkkY" -> "mega_test_file"
    )

    val result = for {
      megaClient <- fixture.megaClientResource
      files      <- input.parTraverse { case (url, filename) => megaClient.fetchFile(filename, url) }
      fileBytes       = files.map((path: Path) => Files.readAllBytes(path))
      isNotHtml       = fileBytes.forall(bytes => !String(bytes.take(64)).toLowerCase.contains("<!doctype html"))
      hasData         = fileBytes.forall(bytes => bytes.length > 1000)
      hasMp3Id3Header = fileBytes.forall(bytes =>
        bytes.length >= 3 && bytes(0) == 'I'.toByte && bytes(1) == 'D'.toByte && bytes(2) == '3'.toByte
      )
    } yield isNotHtml && hasData && hasMp3Id3Header

    result.use(IO.pure).assert
  }

  databaseFixture.test("getEncryptedFileContent should return an array of bytes over a valid request") { fixture =>
    val megaUrl  = uri"https://mega.nz/file/ebhDGaoY#GXRQWztlo_BpSrh3ly8Gc2O8NK1F7s1hEK3VeGFYkkY"
    val fileName = "mega_test_file_get_encrypted"

    val result = for {
      megaClient <- fixture.megaClientResource
      file       <- megaClient.fetchFile(fileName, megaUrl)
      bytes = Files.readAllBytes(file)
    } yield bytes.nonEmpty

    result.use(IO.pure).assert
  }
}
