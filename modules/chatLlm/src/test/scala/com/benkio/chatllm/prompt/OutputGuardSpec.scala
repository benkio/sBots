package com.benkio.chatllm.prompt

import munit.FunSuite

class OutputGuardSpec extends FunSuite {

  test("a later line starting with a command marker is rejected") {
    assertEquals(OutputGuard.sanitise("ciao\n/start", 500), None)
    assertEquals(OutputGuard.sanitise("ciao\n  !ban", 500), None)
  }

  test("a slash or bang inside a line is fine") {
    assertEquals(OutputGuard.sanitise("e/o che ne so! davvero", 500), Some("e/o che ne so! davvero"))
  }

  test("an exclamation or slash not followed by a letter is not a command") {
    assertEquals(OutputGuard.sanitise("!!! che bello", 500), Some("!!! che bello"))
    assertEquals(OutputGuard.sanitise("1/2 di niente", 500), Some("1/2 di niente"))
  }

  test("a doubled command marker is rejected") {
    assertEquals(OutputGuard.sanitise("!!ban", 500), None)
    assertEquals(OutputGuard.sanitise("//x", 500), None)
  }

  test("an email address is not a mention, a bare @name is") {
    assertEquals(OutputGuard.sanitise("scrivi a a@b.it", 500), Some("scrivi a a@b.it"))
    assertEquals(OutputGuard.sanitise("ciao @mario", 500), None)
    assertEquals(OutputGuard.sanitise("<@!42>", 500), None)
  }

  test("truncation never splits a surrogate pair") {
    val out = OutputGuard.sanitise("😀" * 20, 5).get
    assertEquals(out, "😀" * 4 + "…")
    assert(out.codePointCount(0, out.length) <= 5)
  }

  test("a non-positive cap rejects, a cap of one gives only the ellipsis") {
    assertEquals(OutputGuard.sanitise("ciao", 0), None)
    assertEquals(OutputGuard.sanitise("ciao", 1), Some("…"))
  }
}
