package com.benkio.integration.integrationmunit.discord

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Notifier
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import com.benkio.chatdiscordadapter.DiscordBot
import com.benkio.integration.integrationmunit.PlatformITSupport.*
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import io.circe.parser.parse
import io.circe.Json
import munit.CatsEffectSuite
import munit.TestOptions
import org.testcontainers.containers.wait.strategy.Wait

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.*

/** The bots on a local Discord API mock (the fauxcord image, started with testcontainers): every bot of the registry
  * connects to its gateway (one after the other), answers a message that triggers one of its markdown files and the
  * `/instructions` command, and disconnects. A tester, a user that is not a bot, writes to the channel through the
  * control API of the mock, and the answers are read from it.
  *
  * It needs Docker.
  */
class ITDiscordSpec extends CatsEffectSuite with DBFixture {

  override val munitIOTimeout: FiniteDuration = 30.minutes

  private val mockPort: Int       = 3000
  private val mockImage: String   = "ghcr.io/tomacheese/fauxcord:latest"
  private val guildId: String     = "1876543210987654321"
  private val channelId: String   = "1234567890123456789"
  private val testerId: String    = "3333333333333333333"
  private val botIds: AtomicLong  = AtomicLong(4000000000000000000L)
  private val http: HttpClient    = HttpClient.newHttpClient()
  private val startupDelay        = 3.seconds
  private val commandInstructions = "/instructions"

  /** The mock: the address of it. */
  private final class Mock(val baseUrl: String) {

    private def call(request: HttpRequest.Builder): IO[Json] =
      IO.blocking(http.send(request.build(), HttpResponse.BodyHandlers.ofString())).flatMap { response =>
        if response.statusCode() / 100 == 2 then IO.fromEither(parse(response.body().ifEmpty("{}")))
        else IO.raiseError(new AssertionError(s"${response.statusCode()} ${response.body()}"))
      }

    private def post(path: String, body: Json): IO[Json] =
      call(
        HttpRequest
          .newBuilder(URI.create(baseUrl + path))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body.noSpaces))
      )

    def registerBot(token: String, id: String, name: String): IO[Unit] =
      post(
        "/_test/setup",
        Json.obj(
          "token"  -> Json.fromString(s"Bot $token"),
          "user"   -> Json.obj("id" -> Json.fromString(id), "username" -> Json.fromString(name)),
          "guilds" -> Json.arr(
            Json.obj(
              "id"       -> Json.fromString(guildId),
              "name"     -> Json.fromString("sBotsTesting"),
              "channels" -> Json.arr(
                Json.obj("id" -> Json.fromString(channelId), "name" -> Json.fromString("general"), "type" -> Json.fromInt(0))
              )
            )
          )
        )
      ).void

    def registerTester: IO[Unit] =
      post("/_test/users", Json.obj("id" -> Json.fromString(testerId), "username" -> Json.fromString("tester"))).void

    def say(text: String): IO[Unit] =
      post(
        s"/_test/channels/$channelId/messages",
        Json.obj("content" -> Json.fromString(text), "author" -> Json.obj("id" -> Json.fromString(testerId)))
      ).void

    /** What the bot with the token said in the channel, in order. */
    def answers(token: String): IO[List[String]] =
      call(HttpRequest.newBuilder(URI.create(s"$baseUrl/_test/messages/$channelId")).GET()).map { json =>
        json.hcursor
          .downField("messages")
          .as[List[Json]]
          .getOrElse(List.empty)
          .filter(_.hcursor.get[String]("author_token").contains(s"Bot $token"))
          .flatMap(_.hcursor.get[String]("content").toOption)
      }
  }

  private val mock = ResourceSuiteLocalFixture(
    "fauxcord",
    container(mockImage, mockPort)(Wait.forHttp("/_mock/health").forPort(mockPort))
      .map(c => new Mock(s"http://${c.getHost}:${c.getMappedPort(mockPort)}"))
      .evalTap(_.registerTester)
  )

  override def munitFixtures: Seq[munit.AnyFixture[?]] = List(mock)

  extension (s: String) private def ifEmpty(default: String): String = if s.isEmpty then default else s

  private def botTest(entry: BotRegistryEntry[IO]): Unit =
    databaseFixture.test(
      TestOptions(s"${entry.sBotInfo.botName.value} answers its markdown file and $commandInstructions on Discord")
    ) { fixture =>
      val sBotConfig = SBotConfig.fromSBotInfo(entry.sBotInfo)
      val m          = mock()
      val token      = s"token-${entry.sBotInfo.botId.value}"
      val gateway    = DiscordGatewayConfig(
        shutdownTimeout = 10.seconds,
        messageContent = true,
        apiBaseUrl = Some(m.baseUrl)
      )
      core(fixture, sBotConfig)
        .use { core =>
          val chat = ChatId(Platform.Discord, channelId)
          // the answers of the bot to the messages after the call
          def answered(send: IO[Unit]): IO[List[String]] =
            m.answers(token).flatMap { before =>
              answer(
                nick = entry.sBotInfo.botName.value,
                answers = m.answers(token).map(_.drop(before.length)),
                send = send
              )
            }
          for {
            _ <- m.registerBot(token, botIds.incrementAndGet().toString, entry.sBotInfo.botName.value)
            // an earlier run could have left the bot waiting for its timeout in this chat
            _   <- core.dbLayer.dbTimeout.removeTimeout(chat, entry.sBotInfo.botId)
            bot <- DiscordBot
              .runWith[IO](core, token, entry.commandEffectfulCallback, gateway, Notifier.logging[IO])
              .start
            result <- (for {
              _        <- IO.sleep(startupDelay)
              markdown <- markdownTrigger(core.dbLayer.dbMessageReplies.messageReplies)
                .traverse(trigger => answered(m.say(trigger)))
              instructions <- answered(m.say(commandInstructions))
            } yield (markdown, instructions)).guarantee(bot.cancel >> IO.sleep(2.seconds))
          } yield {
            // a set with a markdown file always gives the text of the file, never the link of one of the media
            result._1.foreach { markdown =>
              assert(markdown.exists(_.trim.nonEmpty), markdown.toString)
              assert(!markdown.exists(text => bareLink.matches(text.trim)), markdown.toString)
            }
            assert(result._2.exists(_.trim.nonEmpty), result._2.toString)
          }
        }
    }

  BotRegistry.toList(BotRegistry.value).foreach(botTest)
}
