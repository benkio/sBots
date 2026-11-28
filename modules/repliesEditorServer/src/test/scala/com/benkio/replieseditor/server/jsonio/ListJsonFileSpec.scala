package com.benkio.replieseditor.server.jsonio

import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ListJsonFileSpec extends CatsEffectSuite {

  private def tempDir(): Path =
    Files.createTempDirectory("list-json-file-test-").toAbsolutePath.normalize()

  test("readFilenamesSorted returns sorted list") {
    val dir  = tempDir()
    val path = dir.resolve("list.json")
    val raw  =
      """[
        |  { "filename": "cala_z.jpg", "sources": ["https://example.com/cala_z.jpg"] },
        |  { "filename": "cala_a.jpg", "sources": ["https://example.com/cala_a.jpg"] }
        |]""".stripMargin
    Files.writeString(path, raw, StandardCharsets.UTF_8)

    ListJsonFile.readFilenamesSorted(path).map { xs =>
      assertEquals(xs, List("cala_a.jpg", "cala_z.jpg"))
    }
  }
}
