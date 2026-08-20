package com.benkio.RichardPHJBensonBot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.syntax.all.*
import cats.Parallel
import cats.Show
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.BaseBotSpec
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot.commandEffectfulCallback
import org.scalacheck.effect.PropF
import org.scalacheck.Gen

import scala.concurrent.duration.Duration

class RichardPHJBensonBotSpec extends BaseBotSpec {

  override val munitIOTimeout = Duration(1, "m")

  val rphjbSBotConfig                       = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(rphjbSBotConfig.sBotInfo.botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (_, inputBotId) =>
      IO.raiseUnless(inputBotId == rphjbSBotConfig.sBotInfo.botId)(
        Throwable(s"[RichardPHJBensonBotSpec] getResourceByKindHandler called with unexpected botId: $inputBotId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource))),
    getResourceFileHandler = (mediaFile: MediaFile) =>
      mediaFile match {
        case Document(v, _) if v == rphjbSBotConfig.repliesJsonFilename =>
          ResourcesRepository.fromResources[IO]().getResourceFile(mediaFile).use(IO.pure)
        case Document(v, _) if v == rphjbSBotConfig.commandsJsonFilename =>
          ResourcesRepository.fromResources[IO]().getResourceFile(mediaFile).use(IO.pure)
        case _ => Left(RepositoryError.NoResourcesFoundFile(mediaFile)).pure[IO]
      }
  )

  val richardPHJBensonBot: IO[SBotPolling[IO]] = for {
    sBotSetup <- buildTestBotSetup(
      repository = repositoryMock,
      dbLayer = emptyDBLayer,
      sBotConfig = rphjbSBotConfig,
      ttl = None
    )
    messageRepliesData <- sBotSetup.jsonDataRepository.loadData[ReplyBundleMessage](
      rphjbSBotConfig.repliesJsonFilename
    )
    commandRepliesData <- sBotSetup.jsonDataRepository.loadData[ReplyBundleCommand](
      rphjbSBotConfig.commandsJsonFilename
    )
  } yield new SBotPolling[IO](
    sBotSetup = sBotSetup,
    messageRepliesData = messageRepliesData,
    commandRepliesData = commandRepliesData,
    commandEffectfulCallback = commandEffectfulCallback[IO]
  )(using
    Parallel[IO],
    sBotSetup.api,
    Async[IO],
    log
  )

  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    richardPHJBensonBot.map(_.allCommandRepliesData)
  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    richardPHJBensonBot.map(_.messageRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] = messageRepliesData.map(_.flatMap(_.reply.prettyPrint))

  messageRepliesData
    .map(mrd => {
      exactTriggerReturnExpectedReplyBundle(mrd)
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
    assertIO(commandRepliesData.map(_.length), 12)
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
        bot <- richardPHJBensonBot
        // Check that the callback exists with the bensonify key
        callback <- IO.fromOption(bot.commandEffectfulCallback.get(RichardPHJBensonBot.bensonifyKey))(
          Throwable(s"Callback with key '${RichardPHJBensonBot.bensonifyKey}' not found")
        )
        // Create a test message with the command format
        testMessage = Message(
          messageId = 0,
          date = 0,
          chatId = ChatId(0),
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
