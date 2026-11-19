package com.benkio.main.runner

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import com.benkio.main.BotRegistryEntry
import munit.CatsEffectSuite

class PlatformRuntimeSpec extends CatsEffectSuite {

  private def entry(name: String): BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = SBotInfo(SBotInfo.SBotId(name.toLowerCase), SBotInfo.SBotName(name)))

  test("a bot that fails is reported to the health check and the other bots keep running") {
    for {
      reasons <- Ref.of[IO, List[String]](List.empty)
      ran     <- Ref.of[IO, List[String]](List.empty)
      onFailure = (reason: String) => reasons.update(_ :+ reason)
      _ <- List("Broken", "Fine").parTraverse_(name =>
        PlatformRuntime.isolated(entry(name), "Discord", onFailure)(
          if name == "Broken" then IO.raiseError(new RuntimeException("no token")) else ran.update(_ :+ name)
        )
      )
      reported <- reasons.get
      finished <- ran.get
    } yield {
      assertEquals(finished, List("Fine"))
      assertEquals(reported.size, 1)
      assert(reported.head.contains("Broken stopped on Discord"), reported.head)
      assert(reported.head.contains("no token"), reported.head)
    }
  }

  test("a bot that runs fine is not reported") {
    for {
      reasons  <- Ref.of[IO, List[String]](List.empty)
      _        <- PlatformRuntime.isolated(entry("Fine"), "IRC", reason => reasons.update(_ :+ reason))(IO.unit)
      reported <- reasons.get
    } yield assertEquals(reported, List.empty)
  }
}
