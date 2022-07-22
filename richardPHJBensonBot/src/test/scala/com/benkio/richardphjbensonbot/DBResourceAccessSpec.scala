package com.benkio.richardphjbensonbot

import munit._

import java.nio.file.Files
import scala.util.Random

class DBResourceAccessSpec extends FunSuite {

  val random = new Random()

  test("toTempFile should create a temporary file with the expected content and name") {
    val (inputFileName, inputContent) = ("test.txt", random.nextBytes(100))

    val obtained = DBResourceAccess.toTempFile(inputFileName, inputContent)
    assert(obtained.getName().startsWith("test"))
    assert(obtained.getName().endsWith(".txt"))
    assertEquals(Files.readAllBytes(obtained.toPath).toSeq, inputContent.toSeq)
  }
}
