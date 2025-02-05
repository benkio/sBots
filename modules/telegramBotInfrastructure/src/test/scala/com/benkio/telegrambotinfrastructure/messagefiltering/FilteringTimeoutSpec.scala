package com.benkio.telegrambotinfrastructure.messagefiltering

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeoutData
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

import java.time.Instant
import scala.concurrent.duration.*

class FilteringTimeoutSpec extends CatsEffectSuite {

  test("FilteringTimeout should return true if the timeout is expired") {
    val botName = "botname"
    val chatId  = 0L

    val dbLayer: DBLayer[IO] =
      DBLayerMock.mock(
        botName,
        timeouts = List(
          DBTimeoutData(
            chat_id = chatId,
            bot_name = botName,
            timeout_value = 800.millis.toMillis.toString,
            last_interaction = Instant.now.getEpochSecond.toString
          )
        )
      )
    val msg: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = chatId, `type` = "private")
    )
    val filter = FilteringTimeout.filter[IO](dbLayer, botName)

    assertIO(
      IO.sleep(800.millis) >>
        filter(null, msg),
      true
    )
  }
  test("FilteringTimeout should return false if the timeout is not expired") {
    val botName = "botname"
    val chatId  = 0L

    val dbLayer: DBLayer[IO] =
      DBLayerMock.mock(
        botName,
        timeouts = List(
          DBTimeoutData(
            chat_id = chatId,
            bot_name = botName,
            timeout_value = 30.seconds.toMillis.toString,
            last_interaction = Instant.now.getEpochSecond.toString
          )
        )
      )
    val msg: Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = chatId, `type` = "private")
    )
    val filter = FilteringTimeout.filter[IO](dbLayer, botName)

    assertIO(filter(null, msg), false)
  }
}
