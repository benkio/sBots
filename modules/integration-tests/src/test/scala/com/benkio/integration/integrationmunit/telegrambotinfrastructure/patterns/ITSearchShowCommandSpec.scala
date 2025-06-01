package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import munit.CatsEffectSuite

class ITSearchShowCommandSpec extends CatsEffectSuite with DBFixture {

  def testBot(botName: String, dbShow: DBShow[IO], input: String, optExpected: Option[String] = None): IO[Boolean] =
    SearchShowCommand
      .selectLinkByKeyword[IO](
        keywords = input,
        dbShow = dbShow,
        botName = botName
      )
      .map(result => {
        val check = optExpected.fold(
          result != "Nessuna puntata/show contenente '' è stata trovata"
        )(e => result == e)
        if !check then println(s"ERROR: $botName - $input - $optExpected - $result")
        check
      })

  databaseFixture.test(
    "SearchShow Command should return a random show foreach bots if the input is an empty string"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- List("ABarberoBot", "YouTuboAncheI0Bot", "RichardPHJBensonBot", "XahLeeBot")
        .traverse(bot => testBot(bot, dbShow, ""))
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input title matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByTitle
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input description matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByDescription
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input minduration matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByMinDuration
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input maxduration matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByMaxDuration
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input mindate matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByMinDate
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input maxdate matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check  <- ITSearchShowCommandSpec.showByMaxDate
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }
}

object ITSearchShowCommandSpec {

  final case class TestInput(botName: String, randomLinkInput: String, expectedOutput: String)

  val expectedTestShowOutput: String = """2025-01-01 - https://www.youtube.com/watch?v=test
                                         | Test Show Title
                                         |----------
                                         | Test Show Description""".stripMargin

  val showByTitle: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "test show title",
      expectedOutput = expectedTestShowOutput
    )
  )

  val showByDescription: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "description=test+show",
      expectedOutput = expectedTestShowOutput
    )
  )

  val showByMinDuration: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "minduration=0",
      expectedOutput = expectedTestShowOutput
    )
  )

  val showByMaxDuration: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "maxduration=50",
      expectedOutput = expectedTestShowOutput
    )
  )

  val showByMinDate: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "mindate=20241212",
      expectedOutput = expectedTestShowOutput
    )
  )

  val showByMaxDate: List[TestInput] = List(
    TestInput(
      botName = "TestBot",
      randomLinkInput = "maxdate=20250202",
      expectedOutput = expectedTestShowOutput
    )
  )
}
