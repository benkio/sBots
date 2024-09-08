package com.benkio.telegrambotinfrastructure.patterns

import java.time.Instant
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand

import munit.*
import cats.effect.*
import cats.syntax.all.*

import telegramium.bots.Chat
import com.benkio.telegrambotinfrastructure.model.*

import telegramium.bots.Message

class CommandPatternsSpec extends CatsEffectSuite {

  val expectedTriggerResponse: List[((List[ReplyBundleMessage[IO]], String), String)] = List(
    (
      List(
        ReplyBundleMessage.textToMedia[IO]("fro(ci|sh)o([ -]fro(ci|sh)o)+".r.tr(5))(
          mp3"rphjb_NudoFrocio.mp3",
          vid"rphjb_FrocioFrocio.mp4"
        )
      ),
      "frocio frocio"
    ) -> """--------------------------------------------------
rphjb_NudoFrocio.mp3      | fro(ci|sh)o([ -]fro(ci|sh)o)+
rphjb_FrocioFrocio.mp4    | 
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "una vergogna"
        )(
          mp3"rphjb_Vergogna.mp3",
          vid"rphjb_Vergogna.mp4",
          gif"rphjb_VergognaGif.mp4",
          gif"rphjb_Vergogna2.mp4"
        )
      ),
      "una vergogna!"
    ) -> """--------------------------------------------------
rphjb_Vergogna.mp3        | una vergogna
rphjb_Vergogna.mp4        | 
rphjb_VergognaGif.mp4     | 
rphjb_Vergogna2.mp4       | 
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "ostia",
        )(
          mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
          mp3"mos_AntonioFossoCarteColla.mp3"
        )
      ),
      "ostia!!!"
    ) -> """--------------------------------------------------
mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3 | ostia
mos_AntonioFossoCarteColla.mp3 | 
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "ciclismo"
        )(
          mp3"mos_CiclismoAllieviDio.mp3",
          mp3"mos_CiclismoGianniBugnoRitardo.mp3",
        )
      ),
      "ciclismo"
    ) -> """--------------------------------------------------
mos_CiclismoAllieviDio.mp3 | ciclismo
mos_CiclismoGianniBugnoRitardo.mp3 | 
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "francesismo"
        )(
          mp3"ytai_Francesismo.mp3"
        )
      ),
      "un francesismo"
    ) -> """--------------------------------------------------
ytai_Francesismo.mp3      | francesismo
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "miele",
        )(
          gif"ytai_ForteProfumoMiele.mp4",
          gif"ytai_AppiccicaticcioMiele.mp4"
        )
      ),
      "splendido miele"
    ) -> """--------------------------------------------------
ytai_ForteProfumoMiele.mp4 | miele
ytai_AppiccicaticcioMiele.mp4 | 
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "italiani",
          "arrendetevi"
        )(
          mp3"abar_Taliani.mp3"
        )
      ),
      "italiani!!!"
    ) -> """--------------------------------------------------
abar_Taliani.mp3          | italiani
                          | arrendetevi
--------------------------------------------------
""",
    (
      List(
        ReplyBundleMessage.textToMedia[IO](
          "spranga"
        )(
          gif"abar_Spranga.gif",
          vid"abar_ParoleLongobarde.mp4"
        )
      ),
      "ti tiro una sprangata"
    ) -> """--------------------------------------------------
abar_Spranga.gif          | spranga
abar_ParoleLongobarde.mp4 | 
--------------------------------------------------
"""
  )

  test("searchTrigger should return the expected result when a trigger is found") {
    expectedTriggerResponse.traverse_ { case ((mdr, query), expectedResponse) =>
      val msg = Message(
        messageId = 0,
        date = Instant.now.getEpochSecond().toInt,
        chat = Chat(id = 0, `type` = "test"),
        text = Some(s"/testCommand $query")
      )
      TriggerSearchCommand
        .searchTrigger[IO](mdr, msg, Some("!"))
        .apply(query)
        .map(result => {
          assertEquals(result.length, 1)
          assertEquals(result.head, expectedResponse)
        })
    }
  }

}
