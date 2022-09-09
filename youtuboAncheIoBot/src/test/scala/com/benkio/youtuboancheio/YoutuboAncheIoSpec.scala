package com.benkio.youtuboancheio

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.youtuboancheiobot.YoutuboAncheIoBot
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class YoutuboAncheIoBotSpec extends CatsEffectSuite {

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))

  test("triggerlist should return a list of all triggers when called") {
    assertEquals(YoutuboAncheIoBot.commandRepliesData[IO].length, 2)
    assert(
      YoutuboAncheIoBot
        .messageRepliesData[IO]
        .flatMap(
          _.trigger match {
            case TextTrigger(lt @ _*) => lt.map(_.toString)
            case _                    => List.empty[String]
          }
        )
        .forall(s =>
          YoutuboAncheIoBot
            .commandRepliesData[IO]
            .filter(_.trigger.command == "triggerlist")
            .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
            .mkString("\n")
            .contains(s)
        )
    )
  }

  test("triggerlist command should return the warning message if the input message is not a private chat") {
    val nonPrivateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "group"))
    val actual = YoutuboAncheIoBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "triggerlist")
      .flatTraverse(_.text.text(nonPrivateTestMessage))
    assertIO(
      actual,
      List("puoi usare questo comando solo in chat privata")
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
}
