package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.http.HttpClients
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.Logger.given
import com.benkio.main.BotRegistryEntry
import com.benkio.main.sBotConfig
import munit.CatsEffectSuite

import java.time.Instant
import scala.concurrent.duration.*

/** Shared performance test harness for a single registered bot. Each bot spec defines its own explicit, switchable
  * [[perfCase]] and runs this perf test. Uses the default per-request processing timeout (3s) as threshold: if
  * handling hits/crosses timeout (request dropped/no reply delivered) or exceeds 3s, the test fails.
  */
trait BaseBotPerformanceSpec extends CatsEffectSuite with DBFixture {

  override val munitIOTimeout: FiniteDuration = 2.minutes

  /** Explicit, switchable performance scenario for a bot. */
  final case class PerfCase(
      inputText: String,
      description: String,
      expectedReplyMinCount: Int = 1
  )

  /** The bot under test. */
  def botEntry: BotRegistryEntry[IO]

  /** Explicit, switchable performance scenario for this bot. */
  def perfCase: PerfCase

  protected val perfThreshold: FiniteDuration = 3.seconds

  private val testCapabilities: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Telegram,
    supportedMedia =
      Set(MediaKind.Audio, MediaKind.Video, MediaKind.Gif, MediaKind.Photo, MediaKind.Sticker, MediaKind.Document),
    maxTextLength = 4096,
    markdown = MarkdownSupport.Native,
    supportsDelete = false,
    supportsEdit = false,
    supportsButtons = false,
    acceptsFileIds = true,
    commandSyntax = CommandSyntax.Slash
  )

  /** Override when the bot's SBotConfig needs custom path resolution (e.g. special layouts). */
  protected def configFor(entry: BotRegistryEntry[IO]): SBotConfig = entry.sBotConfig

  private def buildCore(
      fixture: DBFixtureResources,
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
        DBRepository.dbResources[IO](dbLayer.dbMedia, HttpClients(dropboxClient = dropbox, megaClient = mega)),
      sBotConfig = sBotConfig
    )

  private def message(text: String): Message = Message(
    messageId = Some(MessageId("perf-1")),
    date = Instant.now.getEpochSecond,
    chatId = ChatId(Platform.Telegram, "1"),
    chatType = "private",
    text = Some(text)
  )

  /** Registers this bot's performance test. Called from concrete specs to avoid trait-init order issues with
    * subclass-defined [[botEntry]]/[[perfCase]].
    */
  protected def registerPerformanceTest(): Unit = {
    val entry = botEntry
    val pc    = perfCase
    val testName =
      s"${entry.sBotInfo.botName.value} (${entry.sBotInfo.botId.value}) performance: ${pc.description} [input='${pc.inputText}']"

    databaseFixture.test(testName) { fixture =>
      val baseConfig = configFor(entry)
      val sBotConfig = baseConfig.copy(requestProcessingTimeout = Some(perfThreshold))
      buildCore(fixture, sBotConfig).use { core =>
        for {
          adapter <- FakePlatformAdapter(capabilities = testCapabilities)
          engine = BotEngine[IO](core.sBotConfig, core.dbLayer)
          start  <- IO.monotonic
          _      <- engine.onMessage(message(pc.inputText))(using adapter)
          end    <- IO.monotonic
          sent   <- adapter.sent.get
          elapsed = end - start
          _ <- IO.println(
            s"[ITBotPerformance] bot=${entry.sBotInfo.botId.value} input='${pc.inputText}' elapsed=${elapsed.toMillis}ms replies=${sent.size}"
          )
          _ <- IO(
            assert(
              sent.size >= pc.expectedReplyMinCount,
              s"Expected >= ${pc.expectedReplyMinCount} reply/replies but got ${sent.size} (request may have timed out/dropped at ${perfThreshold})"
            )
          )
          _ <- IO(
            assert(
              elapsed <= perfThreshold,
              s"Elapsed ${elapsed.toMillis}ms exceeded threshold ${perfThreshold.toMillis}ms"
            )
          )
        } yield ()
      }
    }
  }
}
