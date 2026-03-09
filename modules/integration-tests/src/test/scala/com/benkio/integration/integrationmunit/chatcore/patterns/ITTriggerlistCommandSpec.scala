package com.benkio.integration.integrationmunit.chatcore.patterns

import com.benkio.chatcore.patterns.CommandPatterns.TriggerListCommand
import munit.FunSuite
import org.http4s.Uri

class ITTriggerlistCommandSpec extends FunSuite {

  val testUriValue: String = "https://github.com/benkio/sBots"

  val testUri: Uri = Uri.unsafeFromString(testUriValue)

  test("Triggerlist command should return the input uri") {
    assert(TriggerListCommand.triggerListLogic(testUri).value.contains(testUriValue))
  }
}
