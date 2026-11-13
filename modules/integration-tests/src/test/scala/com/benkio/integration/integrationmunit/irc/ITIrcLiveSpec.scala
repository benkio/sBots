package com.benkio.integration.integrationmunit.irc

import cats.effect.std.Dispatcher
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.http.HttpClients
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatircadapter.connection.IrcConnection
import com.benkio.chatircadapter.connection.IrcCredentials
import com.benkio.chatircadapter.connection.KittehConnection
import com.benkio.chatircadapter.IrcBot
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import munit.CatsEffectSuite
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent

import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

/** The bots on a real IRC network: every bot of the registry joins the channel (one after the other, to stay under the
  * limits of connections per address of the network), answers a message that triggers one of its markdown files and the
  * `!instructions` command, and quits. A second client, the tester, writes to the channel and reads the answers.
  *
  * It needs the network: it is skipped when the environment variable `IRC_LIVE_TEST` is `false`.
  */
class ITIrcLiveSpec extends CatsEffectSuite with DBFixture {

  override val munitIOTimeout: FiniteDuration = 30.minutes

  /** `##` is the prefix that Libera.Chat asks for the channels that do not belong to a registered project. */
  private val channel: String = "##sBotsTesting"

  private val ircConfig: IrcConfig = IrcConfig(
    host = "irc.libera.chat",
    port = 6697,
    tls = true,
    channels = List(channel),
    readyTimeout = 90.seconds,
    shutdownTimeout = 10.seconds,
    reconnectBaseDelay = 2.seconds,
    reconnectMaxDelay = 30.seconds,
    messageDelay = 700.millis,
    rateLimitMessages = 100,
    rateLimitWindow = 10.seconds
  )

  private val answerTimeout: FiniteDuration = 90.seconds

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
      connection <- IrcConnection.resource[IO](nick, None, ircConfig, List(new Collector(dispatcher, seen)))
      tester = Tester(connection, seen)
      _ <- Resource.eval(until("the tester joins the channel", 60.seconds)(tester.nicknames.map(_.contains(nick))))
    } yield tester
  )

  override def munitFixtures: Seq[munit.AnyFixture[?]] = List(tester)

  private def until(what: String, timeout: FiniteDuration)(check: IO[Boolean]): IO[Unit] = {
    def go: IO[Unit] = check.flatMap(ok => if ok then IO.unit else IO.sleep(500.millis) >> go)
    go.timeoutTo(timeout, IO.raiseError(new AssertionError(s"Timeout after $timeout: $what")))
  }

  private def core(
      fixture: com.benkio.integration.DBFixtureResources,
      sBotConfig: SBotConfig
  ): Resource[IO, BotCoreSetup[IO]] =
    for {
      base    <- fixture.resourceDBLayer
      dbLayer <- Resource.eval(DBLayer.loadReplies[IO](base, JsonDataRepository[IO](), sBotConfig))
      dropbox <- fixture.dropboxClientResource
      mega    <- fixture.megaClientResource
    } yield BotCoreSetup(
      jsonDataRepository = JsonDataRepository[IO](),
      dbLayer = dbLayer,
      repository =
        DBRepository.dbResources[IO](dbLayer.dbMedia, HttpClients(dropboxClient = dropbox, megaClient = mega))
    )

  /** The text that triggers a reply made only of markdown files (or of some, if no reply is made only of them), when
    * the bot has one that a text can trigger.
    */
  private def markdownTrigger(replies: List[ReplyBundleMessage]): Option[String] = {
    val withMarkdown = for {
      bundle <- replies
      files  <- bundle.reply match {
        case media: MediaReply => Some(media.mediaFiles)
        case _                 => None
      }
      if files.exists(_.isInstanceOf[MarkdownFile])
      strings <- bundle.trigger match {
        case TextTrigger(values*) => Some(values.collect { case StringTextTriggerValue(s) => s }.toList)
        case _                    => None
      }
      if strings.nonEmpty
    } yield (files.forall(_.isInstanceOf[MarkdownFile]), strings.mkString(" "))
    withMarkdown.sortBy(!_._1).headOption.map(_._2)
  }

  private def answer(t: Tester, nick: String, send: IO[Unit]): IO[List[Seen]] =
    for {
      _       <- t.seen.set(List.empty)
      _       <- send
      _       <- until(s"$nick answers", answerTimeout)(t.seen.get.map(_.exists(_.sender.startsWith(nick))))
      answers <- t.seen.get.map(_.filter(_.sender.startsWith(nick)))
    } yield answers

  private def botTest(entry: BotRegistryEntry[IO]): Unit =
    databaseFixture.test(s"${entry.sBotInfo.botName.value} answers its markdown file and !instructions on IRC") {
      fixture =>
        assume(!sys.env.get("IRC_LIVE_TEST").contains("false"), "IRC_LIVE_TEST=false")
        val sBotConfig = SBotConfig.fromSBotInfo(entry.sBotInfo)
        val nick       = IrcCredentials.nick(entry.sBotInfo)
        val t          = tester()
        core(fixture, sBotConfig).use { core =>
          val chat = ChatId(Platform.Irc, channel.toLowerCase)
          for {
            // an earlier run could have left the bot waiting for its timeout in this chat
            _   <- core.dbLayer.dbTimeout.removeTimeout(chat, entry.sBotInfo.botId)
            bot <- IrcBot
              .runWith[IO](core, sBotConfig, None, entry.commandEffectfulCallback, ircConfig, Notifier.logging[IO])
              .start
            result <- (for {
              _        <- until(s"$nick joins $channel", answerTimeout)(t.nicknames.map(_.exists(_.startsWith(nick))))
              markdown <- markdownTrigger(core.dbLayer.dbMessageReplies.messageReplies)
                .traverse(trigger => answer(t = t, nick = nick, send = t.say(trigger)))
              instructions <- answer(t = t, nick = nick, send = t.say("!instructions"))
            } yield (markdown, instructions)).guarantee(bot.cancel >> IO.sleep(2.seconds))
          } yield {
            result._1.foreach(markdown => assert(markdown.exists(_.text.trim.nonEmpty), markdown.toString))
            assert(result._2.exists(_.text.trim.nonEmpty), result._2.toString)
          }
        }
    }

  BotRegistry.toList(BotRegistry.value).foreach(botTest)
}
