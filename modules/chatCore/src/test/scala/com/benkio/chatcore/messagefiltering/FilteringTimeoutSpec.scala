package com.benkio.chatcore.messagefiltering

import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chatcore.TestIds
import munit.*

import java.time.Instant
import scala.concurrent.duration.*

class FilteringTimeoutSpec extends CatsEffectSuite {

  test("FilteringTimeout should return true if the timeout is expired") {
    val botId  = SBotId("botid")
    val chatId = 0L

    val dbLayer: DBLayer[IO] =
      DBLayerMock.mock(
        botId,
        timeouts = List(
          DBTimeoutData(
            platform = "telegram",
            chat_id = chatId.toString,
            bot_id = botId.value,
            timeout_value = 800.millis.toMillis.toString,
            last_interaction = Instant.now.getEpochSecond.toString
          )
        )
      )
    val msg: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(chatId),
      chatType = "private"
    )
    val filter = FilteringTimeout.filter[IO](dbLayer, botId)

    assertIO(
      IO.sleep(800.millis) >>
        filter(null, msg),
      true
    )
  }
  test("FilteringTimeout should return false if the timeout is not expired") {
    val botId  = SBotId("botid")
    val chatId = 0L

    val dbLayer: DBLayer[IO] =
      DBLayerMock.mock(
        botId,
        timeouts = List(
          DBTimeoutData(
            platform = "telegram",
            chat_id = chatId.toString,
            bot_id = botId.value,
            timeout_value = 30.seconds.toMillis.toString,
            last_interaction = Instant.now.getEpochSecond.toString
          )
        )
      )
    val msg: Message = Message(
      messageId = None,
      date = 0L,
      chatId = TestIds.chatId(chatId),
      chatType = "private"
    )
    val filter = FilteringTimeout.filter[IO](dbLayer, botId)

    assertIO(filter(null, msg), false)
  }
}
