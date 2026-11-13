package com.benkio.main.runner

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import com.benkio.main.Logger.given
import com.benkio.main.MainSetup
import log.effect.LogWriter
import munit.CatsEffectSuite

class RunnerSpec extends CatsEffectSuite {

  private def bot(name: String): SBotInfo =
    SBotInfo(botId = SBotInfo.SBotId(name.toLowerCase), botName = SBotInfo.SBotName(name))

  private val onTelegram       = BotRegistryEntry[IO](sBotInfo = bot("OnTelegram"))
  private val onTelegramAndIrc =
    BotRegistryEntry[IO](sBotInfo = bot("OnBoth"), platforms = List(Platform.Telegram, Platform.Irc))
  private val registry: BotRegistry[IO] = BotRegistry(List(onTelegram, onTelegramAndIrc))

  /** A runtime that records the bots it is started with and that its work ran. */
  private final class FakeRuntime(
      override val platform: Platform,
      started: Ref[IO, List[(Platform, List[String])]],
      ran: Ref[IO, List[Platform]]
  ) extends PlatformRuntime {
    override def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
        LogWriter[IO]
    ): Resource[IO, IO[Unit]] =
      Resource
        .eval(started.update(_ :+ (platform -> entries.map(_.sBotInfo.botName.value))))
        .as(ran.update(_ :+ platform))
  }

  private def withMainSetup[A](use: MainSetup[IO] => IO[A]): IO[A] = MainSetup[IO]().use(use)

  test("a bot runs on Telegram unless it is bound to other platforms") {
    assertEquals(onTelegram.platforms, List(Platform.Telegram))
    assertEquals(BotRegistry.toList(registry).map(_.sBotInfo.botName.value), List("OnTelegram", "OnBoth"))
  }

  test("the registry tells the bots that run on a platform") {
    import com.benkio.main.entriesOn
    assertEquals(registry.entriesOn(Platform.Telegram).map(_.sBotInfo.botName.value), List("OnTelegram", "OnBoth"))
    assertEquals(registry.entriesOn(Platform.Irc).map(_.sBotInfo.botName.value), List("OnBoth"))
    assertEquals(registry.entriesOn(Platform.Slack), List.empty)
  }

  test("every runtime is started with the bots that run on its platform, and the work of all of them is run") {
    for {
      started <- Ref.of[IO, List[(Platform, List[String])]](List.empty)
      ran     <- Ref.of[IO, List[Platform]](List.empty)
      _       <- withMainSetup(mainSetup =>
        Runner
          .start(
            registry,
            List(new FakeRuntime(Platform.Telegram, started, ran), new FakeRuntime(Platform.Irc, started, ran)),
            mainSetup
          )
          .use(identity)
      )
      startedRuntimes <- started.get
      ranRuntimes     <- ran.get
    } yield {
      assertEquals(
        startedRuntimes.toMap,
        Map(Platform.Telegram -> List("OnTelegram", "OnBoth"), Platform.Irc -> List("OnBoth"))
      )
      assertEquals(ranRuntimes.toSet, Set(Platform.Telegram, Platform.Irc))
    }
  }

  test("a bot on a platform with no runtime is an error, before anything is started") {
    for {
      started <- Ref.of[IO, List[(Platform, List[String])]](List.empty)
      ran     <- Ref.of[IO, List[Platform]](List.empty)
      result  <- withMainSetup(mainSetup =>
        Runner.start(registry, List(new FakeRuntime(Platform.Telegram, started, ran)), mainSetup).use(identity).attempt
      )
      startedRuntimes <- started.get
    } yield {
      assertEquals(result.left.map(_.getMessage), Left("The bot OnBoth runs on irc, which has no runtime to run it"))
      assertEquals(startedRuntimes, List.empty)
    }
  }

  test("the Telegram runtimes serve the Telegram platform") {
    assertEquals(TelegramWebhookPlatformRuntime.platform, Platform.Telegram)
    assertEquals(TelegramPollingPlatformRuntime.platform, Platform.Telegram)
  }

  test("every bot of the real registry runs on Telegram, only Richard is also on Discord") {
    import com.benkio.main.entriesOn
    val registry = BotRegistry.value
    assertEquals(
      registry.entriesOn(Platform.Telegram).size,
      BotRegistry.toList(registry).size
    )
    assertEquals(registry.entriesOn(Platform.Discord).map(_.sBotInfo.botId.value), List("rphjb"))
  }

  test("select keeps the bots with the given ids or names") {
    import com.benkio.main.select
    def names(selected: Either[String, BotRegistry[IO]]): Either[String, List[String]] =
      selected.map(r => BotRegistry.toList(r).map(_.sBotInfo.botName.value))
    assertEquals(names(registry.select(List("onboth"))), Right(List("OnBoth")))
    assertEquals(names(registry.select(List("OnTelegram"))), Right(List("OnTelegram")))
    assertEquals(names(registry.select(List("OnBoth", "ontelegram"))), Right(List("OnTelegram", "OnBoth")))
  }

  test("select names the unknown bots and the available ids") {
    import com.benkio.main.select
    assertEquals(
      registry.select(List("onboth", "nope")).void,
      Left("Unknown bots: nope. Available: ontelegram, onboth")
    )
  }

  test("every bot of the real registry can be selected by its id and by its name") {
    import com.benkio.main.select
    BotRegistry.toList(BotRegistry.value).foreach { entry =>
      val byId   = BotRegistry.value.select(List(entry.sBotInfo.botId.value)).map(BotRegistry.toList)
      val byName = BotRegistry.value.select(List(entry.sBotInfo.botName.value)).map(BotRegistry.toList)
      assertEquals(byId.map(_.map(_.sBotInfo)), Right(List(entry.sBotInfo)))
      assertEquals(byName.map(_.map(_.sBotInfo)), Right(List(entry.sBotInfo)))
    }
  }

  test("the runtimes of a mode are the Telegram one of the mode and the ones of the platforms with a connection") {
    for {
      started <- Ref.of[IO, List[(Platform, List[String])]](List.empty)
      ran     <- Ref.of[IO, List[Platform]](List.empty)
    } yield {
      val irc = new FakeRuntime(Platform.Irc, started, ran)
      assertEquals(
        Runner.runtimes(Runner.RunMode.Webhook, List(irc)),
        List(TelegramWebhookPlatformRuntime, irc)
      )
      assertEquals(
        Runner.runtimes(Runner.RunMode.Polling, List(irc)),
        List(TelegramPollingPlatformRuntime, irc)
      )
    }
  }

  test("Discord is the platform with a connection, a mode runs its Telegram runtime and the Discord one") {
    assertEquals(Runner.connectionRuntimes, List(DiscordPlatformRuntime))
    assertEquals(
      Runner.runtimes(Runner.RunMode.Webhook),
      List(TelegramWebhookPlatformRuntime, DiscordPlatformRuntime)
    )
    assertEquals(
      Runner.runtimes(Runner.RunMode.Polling),
      List(TelegramPollingPlatformRuntime, DiscordPlatformRuntime)
    )
  }

  test("a runtime whose platform has no bots is not started") {
    for {
      started <- Ref.of[IO, List[(Platform, List[String])]](List.empty)
      ran     <- Ref.of[IO, List[Platform]](List.empty)
      _       <- withMainSetup(mainSetup =>
        Runner
          .start(
            registry,
            List(
              new FakeRuntime(Platform.Telegram, started, ran),
              new FakeRuntime(Platform.Irc, started, ran),
              new FakeRuntime(Platform.Slack, started, ran)
            ),
            mainSetup
          )
          .use(identity)
      )
      startedRuntimes <- started.get
      ranRuntimes     <- ran.get
    } yield {
      assertEquals(startedRuntimes.map(_._1).toSet, Set(Platform.Telegram, Platform.Irc))
      assertEquals(ranRuntimes.toSet, Set(Platform.Telegram, Platform.Irc))
    }
  }

  test("a bot that fails on Discord, e.g. without its token, does not fail the runtime of the other platforms") {
    val withoutToken = BotRegistryEntry[IO](
      sBotInfo = bot("NoDiscordToken"),
      platforms = List(Platform.Discord)
    )
    withMainSetup(mainSetup => DiscordPlatformRuntime.start(List(withoutToken), mainSetup).use(identity))
  }
}
