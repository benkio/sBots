package com.benkio.chatdiscordadapter.gateway

import net.dv8tion.jda.api.requests.GatewayIntent
import pureconfig.ConfigReader

import scala.concurrent.duration.FiniteDuration

/** What is Discord specific in the configuration (`main.discord`).
  *
  * @param shutdownTimeout
  *   how long the connection is given to close before it is closed without waiting for the pending requests
  * @param messageContent
  *   whether the bot asks for the privileged message content intent: it has to be enabled in the Discord developer
  *   portal too, or the connection is refused
  */
final case class DiscordGatewayConfig(
    shutdownTimeout: FiniteDuration,
    messageContent: Boolean
) derives ConfigReader {

  def intents: Set[GatewayIntent] =
    DiscordGateway.defaultIntents ++ Option.when(messageContent)(GatewayIntent.MESSAGE_CONTENT)
}
