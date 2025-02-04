package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SearchShowCommand
import cats.effect.IO
import cats.implicits.*
import munit.CatsEffectSuite
import com.benkio.integration.DBFixture

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
          result.length == 1 && result != List(s"Nessuna puntata/show contenente '' è stata trovata")
        )(e => result == List(e))
        if (!check) println(s"ERROR: $botName - $input - $optExpected - $result")
        check
      })

  databaseFixture.test(
    "SearchShow Command should return a random show foreach bots if the input is an empty string"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check <- List("ABarberoBot", "YouTuboAncheI0Bot", "RichardPHJBensonBot", "XahLeeBot")
        .traverse(bot => testBot(bot, dbShow, ""))
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }

  databaseFixture.test(
    "SearchShow Command should return a show if the input title matches a show per bot"
  ) { fixture =>
    val result = for {
      dbShow <- fixture.resourceDBLayer.map(_.dbShow).use(IO.pure(_))
      check <- ITSearchShowCommandSpec.showByTitle
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
      check <- ITSearchShowCommandSpec.showByDescription
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
      check <- ITSearchShowCommandSpec.showByDescription
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
      check <- ITSearchShowCommandSpec.showByDescription
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
      check <- ITSearchShowCommandSpec.showByMinDate
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
      check <- ITSearchShowCommandSpec.showByMaxDate
        .traverse(testInput =>
          testBot(testInput.botName, dbShow, testInput.randomLinkInput, testInput.expectedOutput.some)
        )
    } yield check.foldLeft(true)(_ && _)

    result.assert
  }
}

object ITSearchShowCommandSpec {

  final case class TestInput(botName: String, randomLinkInput: String, expectedOutput: String)

  val showByTitle: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "bruciare i libri",
      expectedOutput =
        """2023-05-18 - https://www.youtube.com/watch?v=x0cvonF0GYU
          | Chiedilo a Barbero - Bruciare i libri - Intesa Sanpaolo On Air
          |----------
          | Barbero risponde a domande sulla ￢ﾀﾜbibliolitia￢ﾀﾝ nella storia, ovvero la triste pratica di bruciare i libri, spesso promossa da autoritￃﾠ politiche o religiose.
          |
          |Iscriviti al canale per non perderti nessun aggiornamento su ￢ﾀﾜChiedilo a Barbero￢ﾀﾝ e seguici su:
          |Spotify: https://open.spotify.com/show/7JLDPffy6du4rAy8xW3hTT
          |Apple Podcast: https://podcasts.apple.com/it/podcast/chiedilo-a-barbero-intesa-sanpaolo-on-air/id1688392438
          |Google Podcast: https://podcasts.google.com/feed/aHR0cHM6Ly9kMTcycTN0b2o3dzFtZC5jbG91ZGZyb250Lm5ldC9yc3MteG1sLWZpbGVzLzhmYjliOGYyLTU5MGItNDhmOS1hNTY2LWE5NWI3OTUwYWY2OC54bWw
          |Intesa Sanpaolo Group: https://group.intesasanpaolo.com/it/sezione-editoriale/intesa-sanpaolo-on-air""".stripMargin
    ),
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "title=storia&title=al+contrario&title=on+air",
      expectedOutput =
        """2023-05-23 - https://www.youtube.com/watch?v=m3eF0adEZe0
          | Chiedilo a Barbero - La storia al contrario - Intesa Sanpaolo On Air
          |----------
          | Si puￃﾲ insegnare la storia dal presente al passato? Cosa succederebbe se andassimo a ritroso nell￢ﾀﾙinsegnamento dei programmi scolastici? 
          |
          |Iscriviti al canale per non perderti nessun aggiornamento su ￢ﾀﾜChiedilo a Barbero￢ﾀﾝ e seguici su:
          |Spotify: https://open.spotify.com/show/7JLDPffy6du4rAy8xW3hTT
          |Apple Podcast: https://podcasts.apple.com/it/podcast/chiedilo-a-barbero-intesa-sanpaolo-on-air/id1688392438
          |Google Podcast: https://podcasts.google.com/feed/aHR0cHM6Ly9kMTcycTN0b2o3dzFtZC5jbG91ZGZyb250Lm5ldC9yc3MteG1sLWZpbGVzLzhmYjliOGYyLTU5MGItNDhmOS1hNTY2LWE5NWI3OTUwYWY2OC54bWw
          |Intesa Sanpaolo Group: https://group.intesasanpaolo.com/it/sezione-editoriale/intesa-sanpaolo-on-air""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "1/2 pollo",
      expectedOutput = """2018-02-17 - https://www.youtube.com/watch?v=s3zI2UGcRu0
                         | 1 kg Tiramisￃﾹ + 1/2 pollo : circa 3600 kcal !
                         |----------
                         | """.stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "title=autostrada&title=quasi+fatto&title=addosso",
      expectedOutput = """2019-01-22 - https://www.youtube.com/watch?v=wzXGHazXM1w
                         | Fermo in autostrada per incidente; mi sono quasi fatto la pipￃﾬ addosso ￰ﾟﾘﾢ
                         |----------
                         | """.stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "inghilterra elisabettiana",
      expectedOutput =
        """2025-01-22 - https://www.youtube.com/watch?v=yJHfzSNjsC0
          | Richard Benson | Ottava Nota | L'Inghilterra elisabettiana degli ex-Marillion (22/01/1997) [INEDITA]
          |----------
          | Si ringrazia Renzo Di Pietro, che ha messo a disposizione per le Brigate Benson il suo prezioso archivio di nastri di Ottava Nota. 
          |
          |GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
          |CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
          |PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook
          |
          |#richardbenson #ottavanota""".stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "title=squallore&title=steve+vai",
      expectedOutput =
        """2024-10-02 - https://www.youtube.com/watch?v=S3dDXbpjQVs
          | Richard Benson | Ottava Nota | Lo squallore che regna in Steve Vai (25 Settembre 1996) [INEDITA]
          |----------
          | Si ringrazia Renzo Di Pietro, che ha messo a disposizione per le Brigate Benson il suo prezioso archivio di nastri di Ottava Nota. 
          |
          |GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
          |CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
          |PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook
          |#richardbenson #ottavanota""".stripMargin
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "Kinesis Advantage",
      expectedOutput = """2018-05-30 - https://www.youtube.com/watch?v=FR6Ujuo6seY
                         | Xah Kinesis Advantage2 Keyboard Review
                         |----------
                         | For detail and where to buy, see http://xahlee.info/kbd/keyboard_Kinesis.html""".stripMargin
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "title=realtime&title=elisp",
      expectedOutput =
        """2018-09-19 - https://www.youtube.com/watch?v=1JgNEpiZ_wo
          | emacs realtime. mwe log command, editing elisp
          |----------
          | Real world use of emacs. First 20 min is running mwe log commands package. From 20:00 to end, modifying a emacs lisp code that creates ATOM/RSS entry. Elisp code shows beginning at 25:30.""".stripMargin
    ),
  )

  val showByDescription: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "description=Michail+Bulgakov",
      expectedOutput = """2020-04-19 - https://barberopodcast.it/episode/095-il-maestro-e-margherita-di-bulgakov-extrabarbero-scandicci-cultura-2017
 #095 Il Maestro e Margherita di Bulgakov - Extrabarbero (Scandicci Cultura, 2017)
----------
 Da Il Libro della Vita (Scandicci Cultura) il professor Barbero racconta “Il Maestro e Margherita” di Michail Bulgakov. Video originale: https://www.youtube.com/watch?v=nwZ1i2I15ps Twitter: https://twitter.com/barberopodcast Facebook: https://facebook.com/barberopodcast Instagram: https://instagram...."""
    ),
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "description=Liceo&description=Sassuolo",
      expectedOutput = """2021-10-24 - https://barberopodcast.it/episode/142-dante-e-la-sua-opera-1000vocix100canti-sassuolo-2021
 #142 Dante e la sua opera (1000VociX100Canti, Sassuolo 2021)
----------
 Il prof. Barbero risponde alle domande preparate dagli studenti del Liceo Formiggini di Sassuolo nell’ambito del progetto 1000VociX100Canti, organizzato dall’Istituto e dalla compagnia teatrale H.O.T. Minds di Sassuolo. Progetto 1000VociX100Canti: https://www.instagram.com/1000vocix100canti/ Video o..."""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "description=grazie+a+tutti",
      expectedOutput = """2018-11-23 - https://www.youtube.com/watch?v=l4W7-lCCif0
 Unboxing www.norcineriacoccia.it   Pt 2^
----------
 Shop online:

www.norcineriacoccia.it


Se anche tu desideri i prodotti che ho ricevuto io , guarda il sito indicato sopra ; scegli e compra!

Grazie a Tutti 🤩"""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "description=acqua+calabria&description=ketchup+kania",
      expectedOutput = """2018-10-01 - https://www.youtube.com/watch?v=UNZebs_Cg0s
 FISH & CHIPS  Abbondante ma non troppo
----------
 Buongiorno cari followers e buon inizio di settimana con questo mio nuovo video, che dovrebbe piacervi; mi auguro molto.

Mi raccomando Condividete, Iscrivetevi, Lasciate il vostro Like, Attivate la campanella, Commentate...

Nel video si vedono i seguenti prodotti: acqua Calabria, Coca Cola lattina da 1/2 litro, ketchup Kania, bastoncini di merluzzo Lidl e patatine da ristorante Mc Cain... tutti da me amatissimi 🤩

Dunque una buona visione e Grazie per la tua personale visualizzazione 👍

Ciaooo da YouTubo Anche Io 🌞"""
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "description=hardtodie",
      expectedOutput = """2022-05-11 - https://www.youtube.com/watch?v=CWK8yoOO934
 Richard Benson | RicHARDtoDIE (Blob, 10 maggio 2022)
----------
 #RichardBenson #Blob #HardToDie

GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook"""
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "description=carpi&description=covo+del+medallo",
      expectedOutput = """2018-06-11 - https://www.youtube.com/watch?v=nZOhrQ4jZzI
 Cocktail Micidiale 04 febbraio 2005 (puntata completa) Andrea Carpi
----------
 #RichardBenson #CocktailMicidiale #GianniNeri
Andrea Carpi, vieni qui, vieni ad affrontare me, nel vero covo del medallo e del simposio."""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "description=undo+hell",
      expectedOutput = """2019-01-05 - https://www.youtube.com/watch?v=JFdQtbEcwzE
 emacs talk show. workflow. command log mode, working with raw html
----------
 randomish topic discussed:

command log mode
add html nav bar
categorization problem
15:50 undo hell
using xah-fly-keys.el and xah-html-mode.el"""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "description=david&description=suzuki",
      expectedOutput = """2019-09-29 - https://www.youtube.com/watch?v=L_Q7_F83DVY
 Open Source, Richard Stallman, Recycling, Global Warming, Democracy Dies in Darkness. 2019-09-29
----------
 topics talked -• lisp  〈SAIL Keyboard〉 [ http -_xahlee.info_kbd_sail_keyboard.html ] • biggest flamewar online  〈The One True History of Meow〉 [ http -_xahlee.info_Netiquette_dir_meow_wars.html ] •  〈Richard Stallman Resigned from FSF, 2019-09-16〉 [ http -_ergoemacs.org_misc_rms_resign.html ] •  〈Richard Stallman Speech Requirement (2011)〉 [ http -_ergoemacs.org_misc_rms_speech_requirement.html ] • Why Utopian Communities Fail 2018-03-08 By Ewan Morrison• The Washington Post “democracy dies in darkness”• PBS• search YouTube - Penn and Teller Recycling• David Suzuki s daughter, Severn Cullis-Suzuki s speech to the UN in 1992 on climate change sounds an awful lot like Greta Thunberg s in 2019.  https -_twitter.com_CalebJHull_status_1177221680999124992"""
    ),
  )

  val showByMinDuration: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "minduration=7830",
      expectedOutput = """2020-03-15 - https://barberopodcast.it/episode/90-quid-deinde-fit-costruire-il-futuro-della-storia-barberotalk-logos-roma-2019
 #90 Quid deinde fit? Costruire il futuro della Storia - BarberoTalk (LOGOS, Roma 2019)
----------
 Da LOGOS, il festival del CSOA Ex-Snia di Roma, un dialogo con il professor Barbero dal titolo “Quid Deinde Fit?”, “Che succede dopo?”. Audio gentilmente fornito dall'organizzazione di Logos: https://www.logosfest.org/ Twitter: https://twitter.com/barberopodcast Facebook: https://facebook.com/barber..."""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "minduration=4890",
      expectedOutput = """2018-04-12 - https://www.youtube.com/watch?v=UhCGo2xWCfs
 La Cina ai tempi della dinastia dei Chou
----------
 Grazie per le prime migliaia ... ... ... no, dico migliaia! ... ... ... di visualizzazioni .

Mi devo scusare se, nella prima parte , l'audio non è in perfetta sincronia con il muoversi delle labbra, non è colpa mia : questo video sono riuscito a caricarlo solo dopo molte ore, dalle 8.40 di stamattina a dopo le 18.00 di stasera ! Motivi di linea che riguardano la mia città e il punto dove vivo io , nonostante sia attrezzato con le migliori promozioni. Ma , forse ne dovrò provare un'altra!

Ho citato in quanto mi piacciono sinceramente l' Unical , l'Università di Arcavacata di Rende, nota molto più universalmente come Università della Calabria, che io adoro, benchè non mi sia riuscito di raggiungere l'obiettivo della Laurea; e l'Università Telematica Pegaso, che mi piace molto perchè ti permette di fare anche un solo esame e, dopo la promozione, di ottenerne la certificazione.

Ho nominato inoltre e fatto vedere fisicamente il barattolo del Sale Dietetico Cuore, prodotto che uso poco ma con cui ci stiamo trovando bene a casa.

Infine , ho rivolto la mia stima e i miei cari saluti a Valentino Rossi.

Non posso dimenticare, il mio breve ma sincero pensiero intorno alla figura dello YouTuber Nicolò Balini, ovvero Human Safari . complimenti!

Ok, detto tutto, non mi resta che augurarVi un a buona visione.

YouTubo Anche Io ."""
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "minduration=15420",
      expectedOutput = """2018-12-09 - https://www.youtube.com/watch?v=iidXYBvVKg0
 Ottava Nota su Rete Sole (Richard Benson, 2012)
----------
 #RichardBenson #OttavaNota #ReteSole"""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "minduration=8850",
      expectedOutput = """2021-04-22 - https://www.youtube.com/watch?v=xbPRjqlcD_4
 Xah Talk Show 2021-04-21 JavaScript+SVG Live Coding, Interactive Bigram Chart
----------
 • Part 1  〈2021-04-10 Bigrams Count〉 [ http -_xahlee.info_talk_show_xah_talk_show_2021-04-10.html ] • Part 2  〈2021-04-16 Creating Bar Chart〉 [ http -_xahlee.info_talk_show_xah_talk_show_2021-04-16.html ] • Part 3  〈2021-04-21 Bigram Bar Chart〉 [ http -_xahlee.info_talk_show_xah_talk_show_2021-04-21.html ]"""
    )
  )

  val showByMaxDuration: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "maxduration=30",
      expectedOutput = """2019-03-21 - https://barberopodcast.it/episode/38-episodio-rimosso-festival-della-comunicazione
 #38 Episodio Rimosso (Festival della Comunicazione)
----------
 Contenuto rimosso dietro richiesta dell'ente organizzatore del Festival della Comunicazione. Video originario: https://www.youtube.com/watch?v=Fko3NKb6VLc"""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "maxduration=30",
      expectedOutput = """2018-01-30 - https://www.youtube.com/watch?v=0hZxyxbXQnU
 Il risveglio dei bengalini diamantini
----------
 """
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "maxduration=5",
      expectedOutput = """2018-12-14 - https://www.youtube.com/watch?v=fwXin-RaBgQ
 Richard Benson ci saluta (2018)
----------
 #RichardBenson #Saluti #Fans"""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "maxduration=50",
      expectedOutput = """2010-07-25 - https://www.youtube.com/watch?v=X5ukM_9Q63I
 Second Life Thriller Dance
----------
 Second Life Thriller Dance

Dancing in Second Life to the tune of Michael Jackson's Thriller. The dancers are members of Vanguard and Chthonic Syndicate. Recorded on 2010-07."""
    )
  )

  val showByMinDate: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "mindate=20240312",
      expectedOutput = """2024-03-13 - https://open.spotify.com/episode/2232NCQbI48hW68RCYrtJK
 Episodio 44: Canzoni e storia Prima parte (Puntata speciale live al Conservatorio di Milano)
----------
 Dal conservatorio Giuseppe Verdi di Milano, Barbero risponde a molte domande con l’aiuto del pubblico, in chiave “musicale”."""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "mindate=20190607",
      expectedOutput = """2019-06-08 - https://www.youtube.com/watch?v=MbTXCvrxbTY
 Capricciosa con Acciughe Alla Pala
----------
 Buongiorno cari Amici followers , ritorno con questo video di Pizza Unboxing — il numero 5– spero vivamente sia di Vs gradimento 🤩
Mi raccomando Iscriviti al Canale, lascia il tuo Like e — se proprio devi— un commento ... .. ....
Grazie per questa Tua importante visualizzazione 👍"""
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "mindate=20220928",
      expectedOutput = """2022-09-29 - https://www.youtube.com/watch?v=cQSdK7sOh38
 Lino Vairetti (degli Osanna) saluta le Brigate Benson
----------
 #RichardBenson #LinoVairetti #Osanna #BrigateBenson

GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook"""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "mindate=20221124",
      expectedOutput = """2022-11-25 - https://www.youtube.com/watch?v=B-PkmWMbFfY
 xah talk show 2022-11-25 emacs lisp coding live session
----------
 """
    ),
  )

  val showByMaxDate: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "title=terroristi&maxdate=20181002",
      expectedOutput = """2018-10-01 - https://barberopodcast.it/episode/003-il-rapimento-di-aldo-moro-una-rete-di-terroristi-barbero-riserva-festival-della-mente-2018
 #003 Il rapimento di Aldo Moro - Una rete di terroristi - Barbero Riserva (Festival della Mente, 2018)
----------
 Il 16 maggio 1978 le Brigate Rosse rapirono, a Roma, l’allora presidente della DC Aldo Moro. Nella settimana dell’anniversario ascoltiamo il professor Barbero raccontare come l’operazione fu organizzata ed eseguita dalle Brigate Rosse, la più importante rete di terroristi che abbia mai agito in Ital..."""
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "maxdate=20170819",
      expectedOutput = """2017-08-18 - https://www.youtube.com/watch?v=2Q66egvMkyU
 Canto sprint del mio piccolo diamantino bengalino ... in compagnia con gli altri miei uccellini .
----------
 Piccola clip che osserva i miei bengalini e i due canarini che dividono con loro la gabbia , andando d'amore d'accordo : rispettano gli spazi altrui e basta un niente per capirsi .
Viene poi ripreso più particolarmente il bengalino piccolino che canta , ancora un po' acerbo , peró lo fà con continuità : è bello sentirlo e vederlo mentre canta ...

Mi raccomando , Iscriviti al mio canale e , se gradisci , lascia puntualmente il tuo like 👍

Grazie. E al prossimo video : buona visione !"""
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "maxdate=20180611&title=gilbert",
      expectedOutput = """2018-06-10 - https://www.youtube.com/watch?v=8lNe8xmtgc8
 Cocktail Micidiale 21 gennaio 2005 (puntata completa) il segreto di Brian May, Paul Gilbert
----------
 #RichardBenson #CocktailMicidiale #PaulGilbert
arriva il peggio del peggio"""
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "maxdate=20080301",
      expectedOutput = """2008-02-29 - https://www.youtube.com/watch?v=OAPwh_w5JD4
 "色，戒" clip: 天涯歌
----------
 The 天涯歌 song clip from movie "色，戒" (Lust,Caution)."""
    ),
  )
}
