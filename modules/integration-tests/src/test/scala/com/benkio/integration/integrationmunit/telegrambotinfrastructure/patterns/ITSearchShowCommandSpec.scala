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
          result.length == 1 && result != List("Nessuna puntata/show contenente '' è stata trovata")
        )(e => result == List(e))
        if !check then println(s"ERROR: $botName - $input - $optExpected - $result")
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
          | Barbero risponde a domande sulla “bibliolitia” nella storia, ovvero la triste pratica di bruciare i libri, spesso promossa da autorità politiche o religiose.
          |
          |Iscriviti al canale per non perderti nessun aggiornamento su “Chiedilo a Barbero” e seguici su:
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
          | Si può insegnare la storia dal presente al passato? Cosa succederebbe se andassimo a ritroso nell’insegnamento dei programmi scolastici?
          |
          |Iscriviti al canale per non perderti nessun aggiornamento su “Chiedilo a Barbero” e seguici su:
          |Spotify: https://open.spotify.com/show/7JLDPffy6du4rAy8xW3hTT
          |Apple Podcast: https://podcasts.apple.com/it/podcast/chiedilo-a-barbero-intesa-sanpaolo-on-air/id1688392438
          |Google Podcast: https://podcasts.google.com/feed/aHR0cHM6Ly9kMTcycTN0b2o3dzFtZC5jbG91ZGZyb250Lm5ldC9yc3MteG1sLWZpbGVzLzhmYjliOGYyLTU5MGItNDhmOS1hNTY2LWE5NWI3OTUwYWY2OC54bWw
          |Intesa Sanpaolo Group: https://group.intesasanpaolo.com/it/sezione-editoriale/intesa-sanpaolo-on-air""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "1/2 pollo",
      expectedOutput = """2018-02-17 - https://www.youtube.com/watch?v=s3zI2UGcRu0
                         | 1 kg Tiramisù + 1/2 pollo : circa 3600 kcal !
                         |----------
                         | """.stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "title=autostrada&title=quasi+fatto&title=addosso",
      expectedOutput = """2019-01-22 - https://www.youtube.com/watch?v=wzXGHazXM1w
                         | Fermo in autostrada per incidente; mi sono quasi fatto la pipì addosso 😢
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
      randomLinkInput = "Kinesis Advantage2 Keyboard Review",
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
    )
  )

  val showByDescription: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "description=mikhail+bulgakov",
      expectedOutput = """2022-09-09 - https://www.youtube.com/watch?v=e65OEEQz8ck
                         | Festival della Mente 2022 - Alessandro Barbero (1/3)
                         |----------
                         | Vite e destini: Mikhail Bulgakov""".stripMargin
    ),
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "description=Mussolini&description=Torino",
      expectedOutput =
        """2021-05-27 - https://www.youtube.com/watch?v=vX_yj69z0tQ
          | Podcast A. Barbero – Monsù Cerruti: Mussolini, il fascismo e il Piemonte – Intesa Sanpaolo On Air
          |----------
          | Il fascismo ebbe a Torino un radicamento più superficiale che altrove. Mussolini lo sapeva e nel corso del ventennio assunse verso la Capitale del Piemonte un atteggiamento ambivalente. A parole rendeva omaggio a quella “splendida città del lavoro”, in realtà, diffidava tanto dei veri padroni di Torino, gli Agnelli, quanto dei loro operai, che lo soprannominavano beffardamente Monsù Cerruti. Finché, durante la repubblica di Salò, Mussolini non nascose più la sua avversione per il Piemonte “centro della Vandea monarchica, reazionaria, bolscevica”.
          |
          |Iscriviti al canale per non perderti nessun aggiornamento su “Alessandro Barbero. La storia, le storie” e seguici su:
          |Spotify https://open.spotify.com/show/3iAPIfy2DGtsy5nfj3eycS?si=X3AU0l8KTF2gefpNigZmjg
          |Apple Podcast https://podcasts.apple.com/it/podcast/alessandro-barbero-la-storia-le-storie-intesa-sanpaolo/id1521869598
          |Google Podcast https://podcasts.google.com/feed/aHR0cHM6Ly9kMTcycTN0b2o3dzFtZC5jbG91ZGZyb250Lm5ldC9yc3MteG1sLWZpbGVzLzBjZWM4MTU4LWNmYWQtNDc0YS1iZDM0LTFmMjY1YjdjNTYxMy54bWw?sa=X&ved=0CAYQrrcFahcKEwjY_4ui-b7wAhUAAAAAHQAAAAAQGA
          |Intesa Sanpaolo Group https://group.intesasanpaolo.com/it/sezione-editoriale/intesa-sanpaolo-on-air""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "description=mio+bellissimo+bengalino",
      expectedOutput =
        """2017-12-12 - https://www.youtube.com/watch?v=o__bw45D5vU
          | È nato un bellissimo bengalino figlio di Gigio ‘sprint’
          |----------
          | Più o meno due settimane fa , usciva per la prima volta dal nido questo mio bellissimo bengalino 🍀 a cui auguro una vita felice e lunga : da me avrà tutte le attenzioni possibili 👍 Anche se questo non lo terró , ma l’ho già promesso ad un mio carissimo cugino ... ed anche lui lo accudirà sempre al 🔝.
          |
          |Buona visione a voi tutti e arrivederci al prossimo video .""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "description=acqua+calabria&description=ketchup+kania",
      expectedOutput =
        """2018-10-01 - https://www.youtube.com/watch?v=UNZebs_Cg0s
          | FISH & CHIPS  Abbondante ma non troppo
          |----------
          | Buongiorno cari followers e buon inizio di settimana con questo mio nuovo video, che dovrebbe piacervi; mi auguro molto.
          |
          |Mi raccomando Condividete, Iscrivetevi, Lasciate il vostro Like, Attivate la campanella, Commentate...
          |
          |Nel video si vedono i seguenti prodotti: acqua Calabria, Coca Cola lattina da 1/2 litro, ketchup Kania, bastoncini di merluzzo Lidl e patatine da ristorante Mc Cain... tutti da me amatissimi 🤩
          |
          |Dunque una buona visione e Grazie per la tua personale visualizzazione 👍
          |
          |Ciaooo da YouTubo Anche Io 🌞""".stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "description=LaBefanaDelMale",
      expectedOutput =
        """2021-05-28 - https://www.youtube.com/watch?v=CcJbvKKfeHE
          | Richard Benson live @ Alpheus | LA BEFANA DEL MALE 6/1/2006 (Ripresa Alternativa) [INEDITO FULL HD]
          |----------
          | #RichardBenson #Live #Alpheus #LaBefanaDelMale #UnPollo
          |
          |Un ringraziamento speciale va a Chiara Zoli che ci ha fornito questo reperto in ALTA QUALITÀ! Ella gestisce anche un blog: www.daftbunziblogger.blogspot.it che tratta principalmente tutto ciò che riguarda la pop culture anni 80-90!
          |
          |NUOVO GRUPPO E CANALE TELEGRAM:
          |
          |GRUPPO: https://t.me/bbensonreloaded
          |
          |CANALE: https://t.me/simposiodelmedallo""".stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "description=brucekulick&description=RichardBenson",
      expectedOutput = """2024-09-06 - https://www.youtube.com/watch?v=bIZXDutgAHI
                         | Richard Benson | Cocktail Micidiale | La cinta di Bruce Kulick (200X)
                         |----------
                         | #RichardBenson #brucekulick #giannineri
                         |
                         |GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
                         |CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
                         |PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook""".stripMargin
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
      randomLinkInput = "description=xah_talk_show_2024-01-26&description=sorting",
      expectedOutput = """2024-01-27 - https://www.youtube.com/watch?v=teoSUATrXOA
                         | Xah Talk Show Ep534 Advent of Code, Day 7, Live Coding in WolframLang
                         |----------
                         | notes at
                         |http://xahlee.info/talk_show/xah_talk_show_2024-01-26.html
                         |
                         |04:09 reading the problem
                         |19:23 start coding
                         |20:58 analize the problem
                         |26:48 analize hand ranking
                         |30:56 WolframLang SortBy
                         |37:43 find repetitions in string
                         |42:26 CharacterCounts
                         |01:00:46 download WolframLang
                         |1:20:04 done function for sorting hand type
                         |1:26:33 emacs pixel scroll
                         |01:39:45 5 min break
                         |01:52:23 function for card ordering
                         |01:59:23 emacs keyboard macro
                         |02:19:31 answer for sample input
                         |02:23:24 answer for user input""".stripMargin
    )
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
      randomLinkInput = "mindate=20240312&title=Episodio+64",
      expectedOutput =
        """2024-07-30 - https://www.youtube.com/watch?v=ABG7zjXVwwE
          | Episodio 64: L'alba dei libri
          |----------
          | Chi è stato il primo a realizzare un libro? Puntata dedicata a uno degli strumenti più rivoluzionari della storia.""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "mindate=20190607",
      expectedOutput =
        """2019-06-08 - https://www.youtube.com/watch?v=MbTXCvrxbTY
          | Capricciosa con Acciughe Alla Pala
          |----------
          | Buongiorno cari Amici followers , ritorno con questo video di Pizza Unboxing — il numero 5– spero vivamente sia di Vs gradimento 🤩
          |Mi raccomando Iscriviti al Canale, lascia il tuo Like e — se proprio devi— un commento ... .. ....
          |Grazie per questa Tua importante visualizzazione 👍""".stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "mindate=20220928&title=amicizia&title=Pino+Scotto",
      expectedOutput = """2023-01-23 - https://www.youtube.com/watch?v=m3N_76dXALg
                         | La grande amicizia tra Pino Scotto e Richard Benson (Rock TV, 23 gennaio 2023)
                         |----------
                         | #RichardBenson #pinoscotto #rocktv
                         |
                         |GRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram
                         |CANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram
                         |PAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook""".stripMargin
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "mindate=20221124&title=Ep534",
      expectedOutput = """2024-01-27 - https://www.youtube.com/watch?v=teoSUATrXOA
                         | Xah Talk Show Ep534 Advent of Code, Day 7, Live Coding in WolframLang
                         |----------
                         | notes at
                         |http://xahlee.info/talk_show/xah_talk_show_2024-01-26.html
                         |
                         |04:09 reading the problem
                         |19:23 start coding
                         |20:58 analize the problem
                         |26:48 analize hand ranking
                         |30:56 WolframLang SortBy
                         |37:43 find repetitions in string
                         |42:26 CharacterCounts
                         |01:00:46 download WolframLang
                         |1:20:04 done function for sorting hand type
                         |1:26:33 emacs pixel scroll
                         |01:39:45 5 min break
                         |01:52:23 function for card ordering
                         |01:59:23 emacs keyboard macro
                         |02:19:31 answer for sample input
                         |02:23:24 answer for user input""".stripMargin
    )
  )

  val showByMaxDate: List[TestInput] = List(
    TestInput(
      botName = "ABarberoBot",
      randomLinkInput = "title=fake+news&maxdate=20191227",
      expectedOutput =
        """2019-09-27 - https://www.youtube.com/watch?v=Oo0v_cgPGpg
          | Tutte le fake news sulle donne nel Medioevo - Alessandro Barbero e Franco Cardini
          |----------
          | Dialogo fra Alessandro Barbero e Franco Cardini al Festival del Medioevo di Gubbio del 2019.
          |
          |🔔 Iscriviti al Canale per non perderti i nuovi video del Festival del Medioevo: https://www.youtube.com/c/FestivaldelMedioevo
          |📌Scarica la App per guardare da casa la diretta del festival e accedere on demand a tutte le lezioni dal 2015 a oggi https://www.festivaldelmedioevo.it/portal/prodotto/app-fdm/
          |💻 Scopri il programma della X edizione: "Secoli di luce. Sulle spalle dei giganti" Gubbio, 25-29 settembre 2024 https://www.festivaldelmedioevo.it/portal/programma-2024/
          |👉 Acquista un biglietto delle serate del Festival del Medioevo https://www.liveticket.it/festivaldelmedioevo""".stripMargin
    ),
    TestInput(
      botName = "YouTuboAncheI0Bot",
      randomLinkInput = "maxdate=20181131&title=Anno+Nuovo",
      expectedOutput = """2017-12-31 - https://www.youtube.com/watch?v=CQi-0VJJSSs
                         | Auguri per un Felice Anno Nuovo : 2018 !
                         |----------
                         | """.stripMargin
    ),
    TestInput(
      botName = "RichardPHJBensonBot",
      randomLinkInput = "maxdate=20191027&title=Non+scaricate+i+nani",
      expectedOutput = """2018-11-27 - https://www.youtube.com/watch?v=G7CoPKpvNSk
                         | Non scaricate i nani (Richard Benson, 2015)
                         |----------
                         | #RichardBenson #Inani #Linfernodeivivi""".stripMargin
    ),
    TestInput(
      botName = "XahLeeBot",
      randomLinkInput = "maxdate=20130704&title=Food+of+a+Genius",
      expectedOutput = """2013-05-04 - https://www.youtube.com/watch?v=GHr-5ktpQys
                         | Xah's Log: Food of a Genius
                         |----------
                         | a documentation of my life.""".stripMargin
    )
  )
}
