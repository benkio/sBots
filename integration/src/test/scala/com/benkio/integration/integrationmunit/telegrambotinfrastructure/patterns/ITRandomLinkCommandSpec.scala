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
      if (!check) println(s"ERROR: $botName - $input - $optExpected - $result")
      check
    })

  databaseFixture.test(
    "RandomLink Command should return a random show foreach bots if the input is an empty string"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check <- List("ABarberoBot", "YouTuboAncheI0Bot", "RichardPHJBensonBot", "XahLeeBot")
      .traverse(bot => testBot(bot, dbShow, ""))
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "RandomLink Command should return a show if the input title matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check <- ITRandomLinkCommandSpec.showByTitle
      .traverse(testInput => testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
      )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "RandomLink Command should return a show if the input description matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check <- ITRandomLinkCommandSpec.showByDescription
      .traverse(testInput => testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
      )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

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

  val showByDescription: List[TestInput] = List(
    TestInput(botName = "ABarberoBot",         randomLinkInput = "description=Michail+Bulgakov", expectedOutput = """2020-04-19 - https://barberopodcast.it/episode/095-il-maestro-e-margherita-di-bulgakov-extrabarbero-scandicci-cultura-2017
 #095 Il Maestro e Margherita di Bulgakov - Extrabarbero (Scandicci Cultura, 2017)
----------
 Da Il Libro della Vita (Scandicci Cultura) il professor Barbero racconta “Il Maestro e Margherita” di Michail Bulgakov. Video originale: https://www.youtube.com/watch?v=nwZ1i2I15ps Twitter: https://twitter.com/barberopodcast Facebook: https://facebook.com/barberopodcast Instagram: https://instagram...."""),
    TestInput(botName = "ABarberoBot",         randomLinkInput = "description=Liceo&description=Sassuolo", expectedOutput = """2021-10-24 - https://barberopodcast.it/episode/142-dante-e-la-sua-opera-1000vocix100canti-sassuolo-2021
 #142 Dante e la sua opera (1000VociX100Canti, Sassuolo 2021)
----------
 Il prof. Barbero risponde alle domande preparate dagli studenti del Liceo Formiggini di Sassuolo nell’ambito del progetto 1000VociX100Canti, organizzato dall’Istituto e dalla compagnia teatrale H.O.T. Minds di Sassuolo. Progetto 1000VociX100Canti: https://www.instagram.com/1000vocix100canti/ Video o..."""),
    TestInput(botName = "YoutuboAncheI0Bot",   randomLinkInput = "description=grazie+a+tutti", expectedOutput = """2018-11-23 - https://www.youtube.com/watch?v=l4W7-lCCif0
 Unboxing www.norcineriacoccia.it   Pt 2^
----------
 Shop online:

www.norcineriacoccia.it


Se anche tu desideri i prodotti che ho ricevuto io , guarda il sito indicato sopra ; scegli e compra!

Grazie a Tutti 🤩"""),
    TestInput(botName = "YoutuboAncheI0Bot",   randomLinkInput = "description=acqua+calabria&description=ketchup+kania", expectedOutput = """2018-10-01 - https://www.youtube.com/watch?v=UNZebs_Cg0s
 FISH & CHIPS  Abbondante ma non troppo
----------
 Buongiorno cari followers e buon inizio di settimana con questo mio nuovo video, che dovrebbe piacervi; mi auguro molto.

Mi raccomando Condividete, Iscrivetevi, Lasciate il vostro Like, Attivate la campanella, Commentate...

Nel video si vedono i seguenti prodotti: acqua Calabria, Coca Cola lattina da 1/2 litro, ketchup Kania, bastoncini di merluzzo Lidl e patatine da ristorante Mc Cain... tutti da me amatissimi 🤩

Dunque una buona visione e Grazie per la tua personale visualizzazione 👍

Ciaooo da YouTubo Anche Io 🌞"""),
    TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "description=hardtodie", expectedOutput = """2022-05-11 - https://www.youtube.com/watch?v=CWK8yoOO934
 Richard Benson | RicHARDtoDIE (Blob, 10 maggio 2022)
----------
 #RichardBenson #Blob #HardToDie

GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook"""),
    TestInput(botName = "RichardPHJBensonBot", randomLinkInput = "description=carpi&description=simposio", expectedOutput = """2018-06-11 - https://www.youtube.com/watch?v=nZOhrQ4jZzI
 Cocktail Micidiale 04 febbraio 2005 (puntata completa) Andrea Carpi
----------
 #RichardBenson #CocktailMicidiale #GianniNeri
Andrea Carpi, vieni qui, vieni ad affrontare me, nel vero covo del medallo e del simposio."""),
    TestInput(botName = "XahLeeBot",           randomLinkInput = "description=undo+hell", expectedOutput = """2019-01-05 - https://www.youtube.com/watch?v=JFdQtbEcwzE
 emacs talk show. workflow. command log mode, working with raw html
----------
 randomish topic discussed:

command log mode
add html nav bar
categorization problem
15:50 undo hell
using xah-fly-keys.el and xah-html-mode.el"""),
    TestInput(botName = "XahLeeBot",           randomLinkInput = "description=david&description=suzuki", expectedOutput = """2019-09-29 - https://www.youtube.com/watch?v=L_Q7_F83DVY
 Open Source, Richard Stallman, Recycling, Global Warming, Democracy Dies in Darkness. 2019-09-29
----------
 topics talked -• lisp  〈SAIL Keyboard〉 [ http -_xahlee.info_kbd_sail_keyboard.html ] • biggest flamewar online  〈The One True History of Meow〉 [ http -_xahlee.info_Netiquette_dir_meow_wars.html ] •  〈Richard Stallman Resigned from FSF, 2019-09-16〉 [ http -_ergoemacs.org_misc_rms_resign.html ] •  〈Richard Stallman Speech Requirement (2011)〉 [ http -_ergoemacs.org_misc_rms_speech_requirement.html ] • Why Utopian Communities Fail 2018-03-08 By Ewan Morrison• The Washington Post “democracy dies in darkness”• PBS• search YouTube - Penn and Teller Recycling• David Suzuki s daughter, Severn Cullis-Suzuki s speech to the UN in 1992 on climate change sounds an awful lot like Greta Thunberg s in 2019.  https -_twitter.com_CalebJHull_status_1177221680999124992"""),
  )

}
