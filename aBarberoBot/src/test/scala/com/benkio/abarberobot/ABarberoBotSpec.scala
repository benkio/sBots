package com.benkio.abarberobot

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class ABarberoBotSpec extends CatsEffectSuite {

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))

  test("triggerlist should return a list of all triggers when called") {
    val triggerlist: String = ABarberoBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
      .mkString("\n")
    assertEquals(ABarberoBot.commandRepliesData[IO].length, 2)
    assertEquals(
      triggerlist,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/myTelegramBot/blob/master/aBarberoBot/abar_triggers.txt"
    )
  }

  test("the `abar_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/abar_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = ABarberoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files => assert(botFile.forall(filename => files.contains(filename)))
    )

  }

  test("the `abar_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/abar_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = ABarberoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      ABarberoBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

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

}
