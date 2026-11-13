package com.benkio.chatircadapter

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.connection.FakeIrcServer
import com.benkio.chatircadapter.connection.IrcConfig
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*

import scala.concurrent.duration.*

/** A whole bot on IRC, with the real library against a fake server: the messages reach the engine, the replies come
  * back to the channel and to the nick, and the bot quits when it is stopped.
  */
class IrcBotSpec extends CatsEffectSuite {

  override val munitIOTimeout: FiniteDuration = 30.seconds

  private val sBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  private val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = sBotInfo,
    triggerFilename = "eng_triggers.md",
    triggerListUri = uri"https://example.com/eng_triggers.md",
    token = "eng.token",
    listJsonFilename = "eng_list.json",
    repliesJsonFilename = "eng_replies.json",
    commandsJsonFilename = "eng_commands.json",
    showFilename = "eng_shows.json"
  )

  private val core: BotCoreSetup[IO] = BotCoreSetup[IO](
    jsonDataRepository = JsonDataRepository[IO](),
    dbLayer = DBLayerMock.mock(
      sBotInfo.botId,
      messageReplies = List(ReplyBundleMessage.textToText("ciao")("hello")),
      commandReplies = List(
        ReplyBundleCommand(
          trigger = CommandTrigger("hi"),
          reply = TextReply.fromList("command reply")(true),
          instruction = CommandInstructionData.NoInstructions
        )
      )
    ),
    repository = new RepositoryMock()
  )

  private def config(port: Int): IrcConfig = IrcConfig(
    host = "127.0.0.1",
    port = port,
    tls = false,
    channels = List("#sbots"),
    readyTimeout = 15.seconds,
    shutdownTimeout = 5.seconds,
    reconnectBaseDelay = 100.millis,
    reconnectMaxDelay = 500.millis,
    messageDelay = 50.millis,
    rateLimitMessages = 100,
    rateLimitWindow = 10.seconds
  )

  private def eventually(check: => Boolean, tries: Int = 100): IO[Boolean] =
    if check then IO.pure(true)
    else if tries == 0 then IO.pure(false)
    else IO.sleep(100.millis) >> eventually(check, tries - 1)

  test("a trigger in the channel and a command in a private message are answered, the bot quits when it stops") {
    FakeIrcServer.resource.use(server =>
      for {
        bot <- IrcBot
          .runWith[IO](core, sBotConfig, None, Map.empty, config(server.port), Notifier.noop[IO])
          .start
        _        <- eventually(server.joined.nonEmpty)
        _        <- server.push(":alice!a@host PRIVMSG #sbots :ciao a tutti")
        _        <- server.push(":bob!b@host PRIVMSG EngineBot :!hi")
        answered <- eventually(
          server.linesReceived.exists(_.startsWith("PRIVMSG bob")) && server.linesReceived.exists(
            _.startsWith("PRIVMSG #sbots")
          )
        )
        _       <- bot.cancel
        quitted <- eventually(server.linesReceived.exists(_.startsWith("QUIT")))
      } yield {
        assert(answered, server.linesReceived.toString)
        assert(server.linesReceived.contains("PRIVMSG #sbots :hello"), server.linesReceived.toString)
        assert(server.linesReceived.contains("PRIVMSG bob :command reply"), server.linesReceived.toString)
        assert(server.linesReceived.exists(_.startsWith("NICK EngineBot")), server.linesReceived.toString)
        assert(quitted, server.linesReceived.toString)
      }
    )
  }
}
