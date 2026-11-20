package com.benkio.integration.integrationmunit.irc

import cats.effect.std.Dispatcher
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatircadapter.connection.IrcConnection
import com.benkio.chatircadapter.connection.IrcCredentials
import com.benkio.chatircadapter.connection.KittehConnection
import com.benkio.chatircadapter.IrcBot
import com.benkio.integration.DBFixture
import com.benkio.integration.integrationmunit.PlatformITSupport
import com.benkio.integration.integrationmunit.PlatformITSupport.*
import com.benkio.integration.Logger.given
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import munit.CatsEffectSuite
import munit.TestOptions
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/** The bots on a local IRC server (the ergo image, started with testcontainers): every bot of the registry joins the
  * channel (one after the other), answers a message that triggers one of its markdown files and the `!instructions`
  * command, and quits. A second client, the tester, writes to the channel and reads the answers.
  *
  * It needs Docker.
  */
class ITIrcSpec extends CatsEffectSuite with DBFixture {

  override val munitIOTimeout: FiniteDuration = 30.minutes

  private val ircPort: Int                                  = 6667
  private val channel: String                               = "#sBotsTesting"
  private val ergoImage: String                             = "ghcr.io/ergochat/ergo:stable"
  private def ircConfig(host: String, port: Int): IrcConfig = IrcConfig(
    host = host,
    port = port,
    tls = false,
    channels = List(channel),
    readyTimeout = 30.seconds,
    shutdownTimeout = 10.seconds,
    reconnectBaseDelay = 1.second,
    reconnectMaxDelay = 10.seconds,
    messageDelay = 700.millis,
    rateLimitMessages = 100,
    rateLimitWindow = 10.seconds
  )

  /** The local IRC server: the config to connect to it. */
  private val ergo = ResourceSuiteLocalFixture(
    "ergo",
    container(ergoImage, ircPort)(Wait.forLogMessage(s".*now listening on :$ircPort.*", 1))
      .map(c => ircConfig(c.getHost, c.getMappedPort(ircPort)))
  )

  /** What the tester read in the channel. */
  private final case class Seen(sender: String, text: String)

  /** The listener of the tester. */
  private final class Collector(dispatcher: Dispatcher[IO], seen: Ref[IO, List[Seen]]) {
    @Handler
    def onChannelMessage(event: ChannelMessageEvent): Unit =
      dispatcher.unsafeRunAndForget(seen.update(_ :+ Seen(event.getActor.getNick, event.getMessage)))
  }

  private final case class Tester(connection: KittehConnection[IO], seen: Ref[IO, List[Seen]]) {
    def say(text: String): IO[Unit] = IO.blocking(connection.client.sendMessage(channel, text))
    def nicknames: IO[List[String]] =
      IO(connection.client.getChannel(channel).toScala.toList.flatMap(_.getNicknames.asScala))
  }

  private val tester = ResourceSuiteLocalFixture(
    "irc-tester",
    for {
      dispatcher <- Dispatcher.sequential[IO]
      seen       <- Resource.eval(Ref.of[IO, List[Seen]](List.empty))
      nick = s"sBotsTester${scala.util.Random.nextInt(100000)}"
      config     <- Resource.eval(IO(ergo()))
      connection <- IrcConnection.resource[IO](nick, None, config, List(new Collector(dispatcher, seen)))
      tester = Tester(connection, seen)
      _ <- Resource.eval(until("the tester joins the channel", answerTimeout)(tester.nicknames.map(_.contains(nick))))
    } yield tester
  )

  override def munitFixtures: Seq[munit.AnyFixture[?]] = List(ergo, tester)

  private def answer(t: Tester, nick: String, send: IO[Unit]): IO[List[Seen]] =
    t.seen.set(List.empty) >> PlatformITSupport.answer(
      nick = nick,
      answers = t.seen.get.map(_.filter(_.sender.startsWith(nick))),
      send = send
    )

  private def botTest(entry: BotRegistryEntry[IO]): Unit =
    databaseFixture.test(
      TestOptions(s"${entry.sBotInfo.botName.value} answers its markdown file and !instructions on IRC")
    ) { fixture =>
      val sBotConfig = SBotConfig.fromSBotInfo(entry.sBotInfo)
      val nick       = IrcCredentials.nick(entry.sBotInfo)
      val t          = tester()
      core(fixture, sBotConfig)
        .use { core =>
          val chat = ChatId(Platform.Irc, channel.toLowerCase)
          for {
            // an earlier run could have left the bot waiting for its timeout in this chat
            _   <- core.dbLayer.dbTimeout.removeTimeout(chat, entry.sBotInfo.botId)
            bot <- IrcBot
              .runWith[IO](core, sBotConfig, None, entry.commandEffectfulCallback, ergo(), Notifier.logging[IO])
              .start
            result <- (for {
              _        <- until(s"$nick joins $channel", answerTimeout)(t.nicknames.map(_.exists(_.startsWith(nick))))
              markdown <- markdownTrigger(core.dbLayer.dbMessageReplies.messageReplies)
                .traverse(trigger => answer(t = t, nick = nick, send = t.say(trigger)))
              instructions <- answer(t = t, nick = nick, send = t.say("!instructions"))
            } yield (markdown, instructions)).guarantee(bot.cancel >> IO.sleep(2.seconds))
          } yield {
            // a set with a markdown file always gives the text of the file, never the link of one of the media
            result._1.foreach { markdown =>
              assert(markdown.exists(_.text.trim.nonEmpty), markdown.toString)
              assert(!markdown.exists(answer => bareLink.matches(answer.text.trim)), markdown.toString)
            }
            assert(result._2.exists(_.text.trim.nonEmpty), result._2.toString)
          }
        }
    }

  BotRegistry.toList(BotRegistry.value).foreach(botTest)
}
