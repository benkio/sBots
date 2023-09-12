package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import cats.effect.IO
import cats.implicits._
import munit.CatsEffectSuite
import com.benkio.integration.DBFixture

class ITRandomLinkCommandSpec extends CatsEffectSuite with DBFixture {

  def testBot(botName:String, dbShow: DBShow[IO], input: String, optExpected: Option[String] = None): IO[Boolean] =
    RandomLinkCommand.selectRandomLinkByKeyword[IO](
      keywords = input,
      dbShow = dbShow,
      botName = botName
    ).map(result => {
      val check = optExpected.fold(result.length == 1 && result != List(s"Nessuna puntata/show contenente '' è stata trovata"))(e => result == List(e))
      if (!check) println(s"$botName - $input - $optExpected - $result")
      check
    })

  databaseFixture.test(
    "RandomLink Command should return a random show foreach bots if the input is an empty string"
  ) { fixture =>
    List("ABarberoBot", "YouTuboAncheI0Bot", "RichardPHJBensonBot", "XahLeeBot")
      .traverse(bot =>
        for {
          dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
          result <- testBot(bot, dbShow, "")
        } yield result
      ).map(_.foldLeft(true)(_ && _)).assert
  }

  databaseFixture.test(
    "RandomLink Command should return a show if the input title matches a show per bot"
  ) { fixture =>
    ITRandomLinkCommandSpec.showByTitle
      .traverse(testInput =>
        for {
          dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
          result <- testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        } yield result
      ).map(_.foldLeft(true)(_ && _)).assert
  }

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

  val showByTitle: List[TestInput] = List(


    // TestInput(botName = "ABarberoBot", randomLinkInput = "", expectedOutput = ""),
    // TestInput(botName = "YouTuboAncheI0Bot", randomLinkInput = "", expectedOutput = ""),
    // TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "", expectedOutput = ""),
    TestInput(botName = "XahLeeBot", randomLinkInput = "Lex Fridman", expectedOutput = """2022-11-24 - https://www.youtube.com/watch?v=ohbqrSW-fAc
 Xah Talk Show 2022-11-24 Generative Art, Raytracing, Myers-Briggs, Psychology, Lex Fridman
----------
 notes at http -_xahlee.info_talk_show_xah_talk_show_2022-11-24.html"""),
    TestInput(botName = "XahLeeBot", randomLinkInput = "title=Alice+in+Wonderland", expectedOutput = """2022-11-02 - https://www.youtube.com/watch?v=eu23CKDTydM
 Xah Talk Show 2022-11-01 WolframLang Coding, Count Words in Arabian Nights and Alice in Wonderland
----------
 notes at http -_xahlee.info_talk_show_xah_talk_show_2022-11-01.html"""),
  )
}
