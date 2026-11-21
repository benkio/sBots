package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class AutoTimeoutSetSpec extends CatsEffectSuite {

  test("set stores the logarithmic timeout of the member count") {
    val botId  = SBotId("bot")
    val chatId = TestIds.chatId(1L)
    val db     = DBLayerMock.mock(botId)

    for {
      _      <- AutoTimeout.set(db.dbTimeout, chatId, botId, memberCount = 10)
      stored <- db.dbTimeout.getOrDefault(chatId, botId)
    } yield {
      assertEquals(stored.platform, chatId.platform.id)
      assertEquals(stored.chat_id, chatId.id)
      assertEquals(stored.bot_id, botId.value)
      assertEquals(stored.timeout_value, AutoTimeout.forMembers(10).toMillis.toString)
    }
  }

  test("set stores the minimum timeout for a chat with no members") {
    val botId  = SBotId("bot")
    val chatId = TestIds.chatId(2L)
    val db     = DBLayerMock.mock(botId)

    for {
      _      <- AutoTimeout.set(db.dbTimeout, chatId, botId, memberCount = 0)
      stored <- db.dbTimeout.getOrDefault(chatId, botId)
    } yield assertEquals(stored.timeout_value, AutoTimeout.minimum.toMillis.toString)
  }
}
