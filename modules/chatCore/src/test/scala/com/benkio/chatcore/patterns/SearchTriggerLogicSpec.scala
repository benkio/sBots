package com.benkio.chatcore.patterns

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.gif
import com.benkio.chatcore.model.reply.mp3
import com.benkio.chatcore.model.reply.vid
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.patterns.CommandPatterns.TriggerSearchCommand
import munit.*

import java.time.Instant

class SearchTriggerLogicSpec extends CatsEffectSuite {
  val sBotInfo: SBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("TestBot"),
    botId = SBotInfo.SBotId("test")
  )
  val expectedSearchTriggerResponse: List[((List[ReplyBundleMessage], String), String)] = List(
    (
      List(
        ReplyBundleMessage.textToMedia(
          RegexTextTriggerValue("fro(ci|sh)o([ -]fro(ci|sh)o)+".r, 13)
        )(
          mp3"rphjb_NudoFrocio.mp3",
          vid"rphjb_FrocioFrocio.mp4"
        )
      ),
      "frocio frocio"
    ) -> """--------------------------------------------------
           |rphjb_NudoFrocio.mp3      | fro(ci|sh)o([ -]fro(ci|sh)o)+
           |rphjb_FrocioFrocio.mp4    |
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "una vergogna"
        )(
          mp3"rphjb_Vergogna.mp3",
          vid"rphjb_Vergogna.mp4",
          gif"rphjb_VergognaGif.mp4",
          gif"rphjb_Vergogna2Gif.mp4"
        )
      ),
      "una vergogna!"
    ) -> """--------------------------------------------------
           |rphjb_Vergogna.mp3        | una vergogna
           |rphjb_Vergogna.mp4        |
           |rphjb_VergognaGif.mp4     |
           |rphjb_Vergogna2Gif.mp4    |
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "ostia"
        )(
          mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
          mp3"mos_AntonioFossoCarteColla.mp3"
        )
      ),
      "ostia!!!"
    ) -> """--------------------------------------------------
           |mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3 | ostia
           |mos_AntonioFossoCarteColla.mp3 |
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "ciclismo"
        )(
          mp3"mos_CiclismoAllieviDio.mp3",
          mp3"mos_CiclismoGianniBugnoRitardo.mp3"
        )
      ),
      "ciclismo"
    ) -> """--------------------------------------------------
           |mos_CiclismoAllieviDio.mp3 | ciclismo
           |mos_CiclismoGianniBugnoRitardo.mp3 |
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "francesismo"
        )(
          mp3"ytai_Francesismo.mp3"
        )
      ),
      "un francesismo"
    ) -> """--------------------------------------------------
           |ytai_Francesismo.mp3      | francesismo
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "miele"
        )(
          gif"ytai_ForteProfumoMieleGif.mp4",
          gif"ytai_AppiccicaticcioMieleGif.mp4"
        )
      ),
      "splendido miele"
    ) -> """--------------------------------------------------
           |ytai_ForteProfumoMieleGif.mp4 | miele
           |ytai_AppiccicaticcioMieleGif.mp4 |
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "italiani",
          "arrendetevi"
        )(
          mp3"abar_Taliani.mp3"
        )
      ),
      "italiani!!!"
    ) -> """--------------------------------------------------
           |abar_Taliani.mp3          | italiani
           |                          | arrendetevi
           |--------------------------------------------------
           |""".stripMargin,
    (
      List(
        ReplyBundleMessage.textToMedia(
          "spranga"
        )(
          gif"abar_SprangaGif.mp4",
          vid"abar_ParoleLongobarde.mp4"
        )
      ),
      "ti tiro una sprangata"
    ) -> """--------------------------------------------------
           |abar_SprangaGif.mp4       | spranga
           |abar_ParoleLongobarde.mp4 |
           |--------------------------------------------------
           |""".stripMargin
  )

  test("searchTriggerLogic should return the expected result when a trigger is found") {
    expectedSearchTriggerResponse.traverse_ { case ((mdr, query), expectedResponse) =>
      val msg = ModelMessage(
        messageId = 0,
        date = Instant.now.getEpochSecond(),
        chatId = ChatId(0L),
        chatType = "test",
        text = Some(s"/triggersearch $query")
      )
      TriggerSearchCommand
        .searchTriggerLogic[IO](mdr, msg, Some("!"), sBotInfo, ttl = None)
        .map(result => {
          assertEquals(result.length, 1)
          assertEquals(result.head.value, expectedResponse)
        })
    }
  }
}
