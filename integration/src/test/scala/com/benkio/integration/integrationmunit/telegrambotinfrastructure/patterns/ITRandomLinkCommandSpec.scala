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


    TestInput(botName = "ABarberoBot", randomLinkInput = "001 I GAP", expectedOutput = """2018-10-01 - https://barberopodcast.it/episode/001-i-gap-di-roma-e-lattentato-di-via-rasella-barbero-riserva-festival-della-mente-2017
 #001 I GAP di Roma e l’attentato di Via Rasella - Barbero Riserva (Festival della Mente, 2017)
----------
 Il professor Barbero racconta di cosa erano i GAP, i gruppi di azione patriottica, e di come fu pianificato ed eseguito l’attentato all’esercito tedesco a Roma, in via Rasella, il 24 marzo del ‘44. (in foto: Gruppo di gappisti romani, da "Achtung Banditen!" di Rosario Bentivegna) Festival della Ment..."""),
    TestInput(botName = "ABarberoBot", randomLinkInput = "title=sessuale&title=riserva&title=Festival+del+Medioevo", expectedOutput = """2021-06-06 - https://barberopodcast.it/episode/69-la-vita-sessuale-nel-medioevo-barbero-riserva-festival-del-medioevo-gubbio-2019
 #69 La vita sessuale nel medioevo - Barbero Riserva (Festival del Medioevo, Gubbio 2019)
----------
 Dal Festival del Medioevo di Gubbio (ed. 2019) il professor Barbero ci racconta com’era vissuta la sessualità nel medioevo, sfatando i miti che lo vedono come un periodo represso. Video originale: https://www.youtube.com/watch?v=wBgCDgSauXw Community: https://barberopodcast.it/community Twitter: htt..."""),
    TestInput(botName = "YouTuboAncheI0Bot", randomLinkInput = "1/2 pollo", expectedOutput = """2018-02-17 - https://www.youtube.com/watch?v=s3zI2UGcRu0
 1 kg Tiramisù + 1/2 pollo : circa 3600 kcal !
----------
 """),
    TestInput(botName = "YouTuboAncheI0Bot", randomLinkInput = "title=Insana&title=in+piena+notte", expectedOutput = """2018-03-02 - https://www.youtube.com/watch?v=JafZCx1_HVA
 Insana fame notturna : +1500 kcal in piena notte
----------
 """),
    TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "Timo Tollki", expectedOutput = """2018-06-12 - https://www.youtube.com/watch?v=Nb2yUKUt0sE
 Cocktail Micidiale 09 settembre 2005 (puntata completa) La pazzia di Timo Tollki
----------
 #RichardBenson #CocktailMicidiale #TimoTollki
lo sapevo IOOOOH
lo sapevo iooo che stava male di menteh da annih...
però che sdronzo sto timo tolki...."""),
    TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "title=dissacrazione&title=vai", expectedOutput = """2018-06-11 - https://www.youtube.com/watch?v=2uSxC-_rd4I
 Cocktail Micidiale 04 marzo 2005 (puntata completa) La Dissacrazione Di Steve Vai
----------
 #RichardBenson #CocktailMicidiale #SteveVai
prende due note, e le ripete in continuazione....."""),
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
