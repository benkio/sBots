package com.benkio.youtuboancheio

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.youtuboancheiobot.YoutuboAncheIoBot
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class YoutuboAncheIoBotSpec extends CatsEffectSuite {

  implicit val noAction: Action[Reply, IO] = (_: Reply) => (_: Message) => IO.pure(List.empty)
  implicit val log: LogWriter[IO]          = consoleLogUpToLevel(LogLevels.Info)
  private val privateTestMessage           = Message(0, date = 0, chat = Chat(0, `type` = "private"))
  val emptyDBLayer                         = DBLayerMock.mock()
  val emptyBackgroundJobManager = BackgroundJobManager[IO](
    emptyDBLayer.dbSubscription,
    ResourceAccess.fromResources[IO],
    ""
  ).unsafeRunSync()

  test("triggerlist should return the link to the trigger txt file") {
    val triggerlistUrl = YoutuboAncheIoBot
      .commandRepliesData[IO](
        resourceAccess = ResourceAccess.fromResources[IO],
        dbLayer = emptyDBLayer,
        backgroundJobManager = emptyBackgroundJobManager,
        linkSources = ""
      )
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
      .mkString("")
    assertEquals(
      YoutuboAncheIoBot
        .commandRepliesData[IO](
          resourceAccess = ResourceAccess.fromResources[IO],
          backgroundJobManager = emptyBackgroundJobManager,
          dbLayer = emptyDBLayer,
          linkSources = ""
        )
        .length,
      8
    )
    assertEquals(
      triggerlistUrl,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/myTelegramBot/blob/master/youtuboAncheIoBot/ytai_triggers.txt"
    )

  }

  test("the `ytai_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/ytai_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = YoutuboAncheIoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files => assert(botFile.forall(filename => files.contains(filename)))
    )

  }

  test("the `ytai_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/ytai_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = YoutuboAncheIoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      YoutuboAncheIoBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.foreach { mediaFileString =>
      assert(triggerContent.contains(mediaFileString))
    }
    botTriggersFiles.foreach { triggerString =>
      {
        val result = triggerContent.contains(triggerString)
        if (!result) {
          println(s"triggerString: " + triggerString)
          println(s"content: " + triggerContent)
        }
        assert(result)
      }
    }
  }

  test("instructions command should return the expected message") {
    val actual = YoutuboAncheIoBot
      .commandRepliesData[IO](
        resourceAccess = ResourceAccess.fromResources[IO],
        backgroundJobManager = emptyBackgroundJobManager,
        dbLayer = emptyDBLayer,
        linkSources = ""
      )
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.text(privateTestMessage))
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per YoutuboAncheIoBot ----

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/randomshow': Restituisce un link di uno show/video riguardante il personaggio del bot
- '/randomshowkeyword 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato
- '/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii
- '/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo sito come riferimento: https://crontab.guru. Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for YoutuboAncheIoBot ----

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/randomshow': Return the link of one show/video about the bot's character
- '/randomshowkeyword 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword
- '/topTwentyTriggers': Return a list of files and theirs send frequency
- '/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following site: https://crontab.guru. Beware the underlying library require to specify the seconds as well as reported in the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': UnSubscribe the current chat from random shows

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
      )
    )
  }
}
