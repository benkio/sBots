package com.benkio.chatircadapter.connection

import munit.FunSuite
import pureconfig.ConfigSource

import scala.concurrent.duration.*

class IrcConfigSpec extends FunSuite {

  private val config = IrcConfig(
    host = "irc.example.com",
    port = 6697,
    tls = true,
    channels = List("#sbots"),
    readyTimeout = 60.seconds,
    shutdownTimeout = 10.seconds,
    reconnectBaseDelay = 2.seconds,
    reconnectMaxDelay = 60.seconds,
    messageDelay = 50.millis,
    rateLimitMessages = 5,
    rateLimitWindow = 10.seconds
  )

  test("the reconnection delay doubles with the failed attempts, up to the maximum") {
    assertEquals(
      (0 to 7).toList.map(config.reconnectDelay),
      List(2.seconds, 4.seconds, 8.seconds, 16.seconds, 32.seconds, 60.seconds, 60.seconds, 60.seconds)
    )
  }

  test("a huge number of attempts is the maximum, not an overflow") {
    assertEquals(config.reconnectDelay(10_000), 60.seconds)
    assertEquals(config.reconnectDelay(-1), 2.seconds)
  }

  test("the config is read") {
    val common =
      """host = "irc.example.com"
        |port = 6697
        |tls = true
        |ready-timeout = 60 seconds
        |shutdown-timeout = 10 seconds
        |reconnect-base-delay = 2 seconds
        |reconnect-max-delay = 60 seconds
        |message-delay = 1200 milliseconds
        |rate-limit-messages = 5
        |rate-limit-window = 10 seconds
        |""".stripMargin
    assertEquals(
      ConfigSource.string(common + """channels = ["#a", "#b"]""").load[IrcConfig].map(_.channels),
      Right(List("#a", "#b"))
    )
  }
}
