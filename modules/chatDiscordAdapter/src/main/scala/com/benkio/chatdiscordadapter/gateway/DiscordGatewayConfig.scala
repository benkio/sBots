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
  * @param apiBaseUrl
  *   the server to connect to instead of Discord (e.g. `http://localhost:3000`): the REST calls and the gateway go to
  *   it. Only for the tests, with a mock of the Discord API
  */
final case class DiscordGatewayConfig(
    shutdownTimeout: FiniteDuration,
    messageContent: Boolean,
    apiBaseUrl: Option[String] = None
) derives ConfigReader {

  def intents: Set[GatewayIntent] =
    DiscordGateway.defaultIntents ++ Option.when(messageContent)(GatewayIntent.MESSAGE_CONTENT)
}
