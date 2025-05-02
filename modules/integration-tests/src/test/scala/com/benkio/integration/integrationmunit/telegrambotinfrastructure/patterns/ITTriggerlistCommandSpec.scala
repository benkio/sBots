package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerListCommand
import munit.FunSuite
import org.http4s.Uri

class ITTriggerlistCommandSpec extends FunSuite {

  val testUriValue: String = "https://github.com/benkio/sBots"

  val testUri: Uri = Uri.unsafeFromString(testUriValue)

  test("Triggerlist command should return the input uri") {
    assert(TriggerListCommand.triggerListLogic(testUri).contains(testUriValue))
  }
}
