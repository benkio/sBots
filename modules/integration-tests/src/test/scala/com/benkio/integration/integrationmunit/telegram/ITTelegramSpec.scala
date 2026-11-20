package com.benkio.integration.integrationmunit.telegram

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.integration.integrationmunit.PlatformITSupport.*
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import io.circe.parser.parse
import io.circe.Json
import munit.CatsEffectSuite
import munit.TestOptions
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import telegramium.bots.high.Api

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.nio.file.Files
import scala.concurrent.duration.*

/** The bots on a local Telegram Bot API mock (the telegram-mock-ai image, started with testcontainers): every bot of
  * the registry polls its updates (one after the other), answers a message that triggers one of its markdown files and
  * the `/instructions` command, and stops. A tester user writes to a group chat through the admin API of the mock, and
  * the answers are read from it.
  *
  * It needs Docker.
  */
class ITTelegramSpec extends CatsEffectSuite with DBFixture {

  override val munitIOTimeout: FiniteDuration = 30.minutes

  private val botPort: Int        = 8081
  private val adminPort: Int      = 8082
  private val mockImage: String   = "ghcr.io/skrashevich/telegram-mock-ai:latest"
  private val http: HttpClient    = HttpClient.newHttpClient()
  private val commandInstructions = "/instructions"

  /** The admin API listens only on the loopback by default, and the LLM that makes the mock answer is turned off. */
  private val mockConfig: String =
    s"""llm:
       |  enabled: false
       |admin:
       |  enabled: true
       |  host: "0.0.0.0"
       |  port: $adminPort
       |""".stripMargin

  /** The mock: the addresses of the Bot API and of the admin API. */
  private final class Mock(val botApiUrl: String, adminUrl: String) {

    private def call(request: HttpRequest.Builder): IO[Json] =
      IO.blocking(http.send(request.build(), HttpResponse.BodyHandlers.ofString())).flatMap { response =>
        if response.statusCode() / 100 == 2 then IO.fromEither(parse(response.body()))
        else IO.raiseError(new AssertionError(s"${response.statusCode()} ${response.body()}"))
      }

    private def post(path: String, body: Json): IO[Json] =
      call(
        HttpRequest
          .newBuilder(URI.create(adminUrl + path))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body.noSpaces))
      )

    def createUser(name: String): IO[Long] =
      post("/api/users", Json.obj("first_name" -> Json.fromString(name))).flatMap(id(_))

    def createGroup(title: String): IO[Long] =
      post("/api/chats", Json.obj("type" -> Json.fromString("group"), "title" -> Json.fromString(title))).flatMap(id(_))

    private def id(json: Json): IO[Long] = IO.fromEither(json.hcursor.get[Long]("id"))

    def say(chat: Long, user: Long, text: String): IO[Unit] =
      post(
        s"/api/chats/$chat/messages",
        Json.obj("user_id" -> Json.fromLong(user), "text" -> Json.fromString(text))
      ).void

    /** The mock knows a bot from its first call, and gives the updates only to the bots it knows when they arrive. */
    def knows(token: String): IO[Boolean] =
      call(HttpRequest.newBuilder(URI.create(s"$adminUrl/api/bots")).GET()).map(
        _.asArray.toList.flatten.exists(_.hcursor.get[String]("token").contains(token))
      )

    /** What the bots said in the chat, in order. */
    def answers(chat: Long): IO[List[String]] =
      call(HttpRequest.newBuilder(URI.create(s"$adminUrl/api/chats/$chat/messages?from=bot")).GET()).map(
        _.asArray.toList.flatten.flatMap(_.hcursor.get[String]("text").toOption)
      )
  }

  private val mock = ResourceSuiteLocalFixture(
    "telegram-mock",
    container(mockImage, botPort, adminPort)(
      Wait.forHttp("/api/health").forPort(adminPort),
      container => {
        container.withCopyFileToContainer(MountableFile.forHostPath(writeConfig(), 420), "/config.yaml")
        container.withCommand("--config", "/config.yaml")
        ()
      }
    ).map(c =>
      new Mock(
        botApiUrl = s"http://${c.getHost}:${c.getMappedPort(botPort)}",
        adminUrl = s"http://${c.getHost}:${c.getMappedPort(adminPort)}"
      )
    )
  )

  private def writeConfig(): java.nio.file.Path = {
    val file = Files.createTempFile("telegram-mock", ".yaml")
    file.toFile.deleteOnExit()
    Files.writeString(file, mockConfig)
  }

  override def munitFixtures: Seq[munit.AnyFixture[?]] = List(mock)

  private def botTest(entry: BotRegistryEntry[IO]): Unit =
    databaseFixture.test(
      TestOptions(s"${entry.sBotInfo.botName.value} answers its markdown file and $commandInstructions on Telegram")
    ) { fixture =>
      val sBotConfig = SBotConfig.fromSBotInfo(entry.sBotInfo)
      val m          = mock()
      // the mock accepts any token, the bot reads its id from the part before the colon
      val token = s"123456:token${entry.sBotInfo.botId.value}"
      BotSetupFixture
        .botSetupResource(fixture, sBotConfig, apiBaseUrl = m.botApiUrl, token = token)
        .use { setup =>
          given Api[IO] = setup.api
          for {
            user <- m.createUser("tester")
            chat <- m.createGroup(s"sBotsTesting ${entry.sBotInfo.botName.value}")
            chatId = ChatId(Platform.Telegram, chat.toString)
            // the answers of the bot to the messages after the call
            answered = (send: IO[Unit]) =>
              m.answers(chat).flatMap { before =>
                answer(
                  nick = entry.sBotInfo.botName.value,
                  answers = m.answers(chat).map(_.drop(before.length)),
                  send = send
                )
              }
            // an earlier run could have left the bot waiting for its timeout in this chat
            _      <- setup.dbLayer.dbTimeout.removeTimeout(chatId, entry.sBotInfo.botId)
            bot    <- new SBotPolling[IO](setup, entry.commandEffectfulCallback).start().start
            result <- (for {
              _        <- until(s"the mock knows ${entry.sBotInfo.botName.value}", answerTimeout)(m.knows(token))
              markdown <- markdownTrigger(setup.dbLayer.dbMessageReplies.messageReplies, onlyMarkdown = true)
                .traverse(trigger => answered(m.say(chat = chat, user = user, text = trigger)))
              instructions <- answered(m.say(chat = chat, user = user, text = commandInstructions))
            } yield (markdown, instructions)).guarantee(bot.cancel)
          } yield {
            // the trigger gives only markdown files (the mock has no file uploads): the text of the file, not a link
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
