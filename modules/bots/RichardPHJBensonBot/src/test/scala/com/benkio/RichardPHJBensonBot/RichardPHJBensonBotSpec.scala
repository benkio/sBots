package com.benkio.RichardPHJBensonBot

import cats.effect.IO
import cats.Show
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot.commandEffectfulCallback
import org.scalacheck.effect.PropF
import org.scalacheck.Gen

import scala.concurrent.duration.Duration

class RichardPHJBensonBotSpec extends BaseBotSpec {

  override val munitIOTimeout = Duration(1, "m")

  val rphjbSBotConfig                       = SBotConfig.fromSBotInfo(RichardPHJBensonBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(rphjbSBotConfig.sBotInfo.botId)

  val richardPHJBensonBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = rphjbSBotConfig)

  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    richardPHJBensonBot.map(_.allCommandRepliesData)
  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    richardPHJBensonBot.map(_.messageRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] = messageRepliesData.map(_.flatMap(_.reply.prettyPrint))

  messageRepliesData
    .map(mrd => {
      exactTriggerReturnExpectedReplyBundle(mrd)
      noOverlappingReplyBundles(mrd)
      inputFileShouldRespondAsExpected(mrd)
    })
    .unsafeRunSync()

  test("messageRepliesData should contain a NewMemberTrigger") {
    val result =
      messageRepliesData.map(
        _.map(_.trigger match {
          case NewMemberTrigger => true
          case _                => false
        })
          .exists(identity(_))
      )

    assertIO(result, true)
  }

  test("messageRepliesData should contain a LeftMemberTrigger") {
    val result =
      messageRepliesData.map(
        _.map(_.trigger match {
          case LeftMemberTrigger => true
          case _                 => false
        })
          .exists(identity(_))
      )

    assertIO(result, true)
  }

  triggerlistCommandTest(
    commandRepliesData = commandRepliesData,
    expectedReply =
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/rphjb_triggers.md"
  )

  test("RichardPHJBensonBot should contain the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 14)
  }

  botJsonsAreValid(rphjbSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "rphjb_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = rphjbSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint)),
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    italianInstructions =
      """
        |---- Instruzioni Per RichardPHJBensonBot ----
        |
        |Per segnalare problemi, scrivere a: https://t.me/Benkio
        |
        |I comandi del bot sono:
        |
        |- '/searchshow': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/searchshow.md#it
        |- '/subscribe': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscribe.md#it
        |- '/unsubscribe': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/unsubscribe.md#it
        |- '/subscriptions': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscriptions.md#it
        |- '/loadcalendar': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/loadcalendar.md#it
        |- '/showcalendar': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/showcalendar.md#it
        |- '/random': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/random.md#it
        |- '/bensonify': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/CommandsDocumentation/bensonify.md#it
        |- '/triggerlist': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggerlist.md#it
        |- '/triggersearch': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggersearch.md#it
        |- '/toptwenty': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/toptwenty.md#it
        |- '/settimeout': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/settimeout.md#it
        |- '/gettimeout': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/gettimeout.md#it
        |
        |Se si vuole disabilitare il bot per un particolare messaggio impedendo
        |che interagisca, è possibile farlo iniziando il messaggio con il
        |carattere: `!`
        |
        |! Messaggio
        |""".stripMargin,
    englishInstructions =
      """
        |---- Instructions for RichardPHJBensonBot ----
        |
        |to report issues, write to: https://t.me/Benkio
        |
        |Bot commands are:
        |
        |- '/searchshow': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/searchshow.md#en
        |- '/subscribe': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscribe.md#en
        |- '/unsubscribe': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/unsubscribe.md#en
        |- '/subscriptions': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscriptions.md#en
        |- '/loadcalendar': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/loadcalendar.md#en
        |- '/showcalendar': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/showcalendar.md#en
        |- '/random': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/random.md#en
        |- '/bensonify': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/CommandsDocumentation/bensonify.md#en
        |- '/triggerlist': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggerlist.md#en
        |- '/triggersearch': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggersearch.md#en
        |- '/toptwenty': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/toptwenty.md#en
        |- '/settimeout': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/settimeout.md#en
        |- '/gettimeout': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/gettimeout.md#en
        |
        |if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
        |character: `!`
        |
        |! Message
        |""".stripMargin
  )

  test("Bensonify command callback should return the expected result") {
    PropF.forAllF(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (input: String) =>
      for {
        // Check that the callback exists with the bensonify key
        callback <- IO.fromOption(commandEffectfulCallback[IO].get(RichardPHJBensonBot.bensonifyKey))(
          Throwable(s"Callback with key '${RichardPHJBensonBot.bensonifyKey}' not found")
        )
        // Create a test message with the command format
        testMessage = Message(
          messageId = 0,
          date = 0,
          chatId = ChatId.telegram(0),
          chatType = "private",
          text = Some(s"/bensonify $input")
        )
        // Call the callback
        callbackResult <- callback(testMessage)
        // Compute expected result
        expectedResult = Bensonify.compute(input)
      } yield {
        // Verify the callback returns the expected result
        assertEquals(callbackResult, Text(expectedResult))
      }
    }
  }
}
