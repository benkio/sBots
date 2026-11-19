package com.benkio.RichardPHJBensonBot

import cats.effect.IO
import cats.Show
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot.commandEffectfulCallback
import org.scalacheck.effect.PropF
import org.scalacheck.Gen

import scala.concurrent.duration.Duration

class RichardPHJBensonBotSpec extends BaseBotSpec {

  override val munitIOTimeout = Duration(1, "m")

  val rphjbSBotConfig           = SBotConfig.fromSBotInfo(RichardPHJBensonBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(rphjbSBotConfig.sBotInfo.botId)

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
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/rphjb_instructions.md"
  )

  test("Bensonify command callback should follow the command syntax of the platform") {
    val ircSyntax = CommandSyntax(prefix = "!")
    for {
      callback <- IO.fromOption(commandEffectfulCallback[IO].get(RichardPHJBensonBot.bensonifyKey))(
        Throwable(s"Callback with key '${RichardPHJBensonBot.bensonifyKey}' not found")
      )
      message = Message(
        messageId = None,
        date = 0,
        chatId = TestIds.chatId(0),
        chatType = "private",
        text = Some("!bensonify ciao")
      )
      result <- callback(message, ircSyntax)
    } yield assertEquals(result, Text(Bensonify.compute("ciao")))
  }

  test("Bensonify command callback should return the expected result") {
    PropF.forAllF(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (input: String) =>
      for {
        // Check that the callback exists with the bensonify key
        callback <- IO.fromOption(commandEffectfulCallback[IO].get(RichardPHJBensonBot.bensonifyKey))(
          Throwable(s"Callback with key '${RichardPHJBensonBot.bensonifyKey}' not found")
        )
        // Create a test message with the command format
        testMessage = Message(
          messageId = None,
          date = 0,
          chatId = TestIds.chatId(0),
          chatType = "private",
          text = Some(s"/bensonify $input")
        )
        // Call the callback
        callbackResult <- callback(testMessage, CommandSyntax.Slash)
        // Compute expected result
        expectedResult = Bensonify.compute(input)
      } yield {
        // Verify the callback returns the expected result
        assertEquals(callbackResult, Text(expectedResult))
      }
    }
  }
}
