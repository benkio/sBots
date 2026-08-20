package com.benkio.XahLeeBot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.Parallel
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.BaseBotSpec
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotPolling

class XahLeeBotSpec extends BaseBotSpec {

  val xahSBotConfig                         = SBot.buildSBotConfig(XahLeeBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(XahLeeBot.sBotInfo.botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(getResourceByKindHandler =
    (_, inputBotId) =>
      IO.raiseUnless(inputBotId == XahLeeBot.sBotInfo.botId)(
        Throwable(s"[XahLeeBotSpec] getResourceByKindHandler called with unexpected botId: $inputBotId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource)))
  )

  val xahLeeBot: IO[SBotPolling[IO]] = for {
    botSetup <- buildTestBotSetup(
      repository = repositoryMock,
      dbLayer = emptyDBLayer,
      sBotConfig = xahSBotConfig,
      ttl = xahSBotConfig.messageTimeToLive
    )
    messageRepliesData <- botSetup.jsonDataRepository.loadData[ReplyBundleMessage](
      xahSBotConfig.repliesJsonFilename
    )
    commandRepliesData <- botSetup.jsonDataRepository.loadData[ReplyBundleCommand](
      xahSBotConfig.commandsJsonFilename
    )
  } yield new SBotPolling[IO](botSetup, messageRepliesData, commandRepliesData)(using
    Parallel[IO],
    botSetup.api,
    Async[IO],
    log
  )

  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    xahLeeBot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    xahLeeBot.map(_.messageRepliesData.flatMap(_.reply.prettyPrint))

  botJsonsAreValid(xahSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    italianInstructions =
      """
        |---- Instruzioni Per XahLeeBot ----
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
        |- '/alanmackenzie': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/alanmackenzie.md#it
        |- '/ass': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/ass.md#it
        |- '/ccpp': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/ccpp.md#it
        |- '/crap': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/crap.md#it
        |- '/emacs': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/emacs.md#it
        |- '/extra': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/extra.md#it
        |- '/fak': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/fak.md#it
        |- '/fakhead': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/fakhead.md#it
        |- '/google': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/google.md#it
        |- '/idiocy': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/idiocy.md#it
        |- '/idiots': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/idiots.md#it
        |- '/laugh': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/laugh.md#it
        |- '/linux': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/linux.md#it
        |- '/millennial': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/millennial.md#it
        |- '/opensource': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/opensource.md#it
        |- '/opera': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/opera.md#it
        |- '/python': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/python.md#it
        |- '/rantcompilation': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/rantcompilation.md#it
        |- '/richardstallman': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/richardstallman.md#it
        |- '/sucks': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/sucks.md#it
        |- '/unix': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/unix.md#it
        |- '/wtf': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/wtf.md#it
        |- '/zoomer': Documentazione: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/zoomer.md#it
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
        |---- Instructions for XahLeeBot ----
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
        |- '/alanmackenzie': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/alanmackenzie.md#en
        |- '/ass': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/ass.md#en
        |- '/ccpp': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/ccpp.md#en
        |- '/crap': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/crap.md#en
        |- '/emacs': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/emacs.md#en
        |- '/extra': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/extra.md#en
        |- '/fak': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/fak.md#en
        |- '/fakhead': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/fakhead.md#en
        |- '/google': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/google.md#en
        |- '/idiocy': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/idiocy.md#en
        |- '/idiots': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/idiots.md#en
        |- '/laugh': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/laugh.md#en
        |- '/linux': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/linux.md#en
        |- '/millennial': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/millennial.md#en
        |- '/opensource': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/opensource.md#en
        |- '/opera': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/opera.md#en
        |- '/python': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/python.md#en
        |- '/rantcompilation': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/rantcompilation.md#en
        |- '/richardstallman': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/richardstallman.md#en
        |- '/sucks': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/sucks.md#en
        |- '/unix': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/unix.md#en
        |- '/wtf': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/wtf.md#en
        |- '/zoomer': Documentation: https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/CommandsDocumentation/zoomer.md#en
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

}
