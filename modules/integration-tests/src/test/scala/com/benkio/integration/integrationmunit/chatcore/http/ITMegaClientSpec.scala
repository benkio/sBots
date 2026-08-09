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
      uri"https://mega.nz/file/LDxATYLQ#cAdB5jxfJov_BFr-bu548G7cXtLgEsdih4e69PmJ6OI" -> "mega_test_file"
    )

    val result = for {
      megaClient <- fixture.megaClientResource
      files      <- input.parTraverse { case (url, filename) => megaClient.fetchFile(filename, url) }
      fileBytes = files.map((path: Path) => Files.readAllBytes(path))
      isNotHtml = fileBytes.forall(bytes => !String(bytes.take(64)).toLowerCase.contains("<!doctype html"))
      hasData   = fileBytes.forall(bytes => bytes.length > 1000)
      _ = {
        val outputPath = Path.of("/Users/benkio/temp/testFile.mp3")
        Files.createDirectories(outputPath.getParent)
        fileBytes.headOption.foreach(bytes => Files.write(outputPath, bytes))
      }
    } yield isNotHtml && hasData

    result.use(IO.pure).assert
  }
}
