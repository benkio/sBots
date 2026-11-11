package com.benkio.main

/*
 * Modifications to this file must be done carefully. The new-bot process
 * (CompleteBotRegistration.sc) appends a BotRegistryEntry for each new bot.
 * When editing, keep the same structure (imports and registry list) so adding
 * future bots continues to work. See docs/adding-a-bot.md.
 */

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.Alessandro0rlandoBot.Alessandro0rlandoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.PinoScottoBot.PinoScottoBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.VittorioSgarbiBot.VittorioSgarbiBot
import com.benkio.XahLeeBot.XahLeeBot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot

/** A bot of the registry and the platforms it runs on, by default Telegram only. Adding a platform to a bot is adding
  * it to `platforms`, as long as the platform has a [[runner.PlatformRuntime]].
  */
final case class BotRegistryEntry[F[_]](
    sBotInfo: SBotInfo,
    commandEffectfulCallback: Map[String, Message => F[ReplyValue]] = Map.empty,
    platforms: List[Platform] = List(Platform.Telegram)
)

extension (botRegistryEntry: BotRegistryEntry[IO]) {
  def sBotConfig: SBotConfig = SBotConfig.fromSBotInfo(botRegistryEntry.sBotInfo)
}

opaque type BotRegistry[F[_]] = List[BotRegistryEntry[F]]

extension (botRegistry: BotRegistry[IO]) {

  /** The bots that run on the platform. */
  def entriesOn(platform: Platform): List[BotRegistryEntry[IO]] =
    BotRegistry.toList(botRegistry).filter(_.platforms.contains(platform))
}

object BotRegistry {
  def apply[F[_]](entries: List[BotRegistryEntry[F]]): BotRegistry[F] = entries
  def toList[F[_]](r: BotRegistry[F]): List[BotRegistryEntry[F]]      = r

  val value: BotRegistry[IO] = BotRegistry(
    List(
      BotRegistryEntry[IO](sBotInfo = ABarberoBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = CalandroBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = M0sconiBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = XahLeeBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = YouTuboAncheI0Bot.sBotInfo),
      BotRegistryEntry[IO](
        sBotInfo = RichardPHJBensonBot.sBotInfo,
        commandEffectfulCallback = RichardPHJBensonBot.commandEffectfulCallback[IO]
      ),
      BotRegistryEntry[IO](sBotInfo = PinoScottoBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = Alessandro0rlandoBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = VittorioSgarbiBot.sBotInfo)
    )
  )
}
