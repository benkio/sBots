package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import munit.CatsEffectSuite
import com.benkio.integration.DBFixture

class ITRandomLinkCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "RandomLink Command should return a random show foreach bots if the input is an empty string"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input title matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input description matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input minduration matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input maxduration matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input mindate matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input maxdate matches a show per bot"
  ) { _// fixture
    => ??? }

  databaseFixture.test(
    "RandomLink Command should return a show if the input criteria combination matches a show per bot"
  ) { _// fixture
    => ??? }
}

object ITRandomLinkCommandSpec {

  final case class TestInput(botName: String, randomLinkInput: String, expectedOutput: String)

  val bots: List[TestInput] = List(
    TestInput(botName = "ABarberoBot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "YouTuboAncheI0Bot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "M0sconiBot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "CalandroBot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "XahLeeBot", randomLinkInput = "", expectedOutput = "")
    )
}
