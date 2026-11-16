package com.benkio.integration.integrationmunit.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.TestIds
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite

import java.time.Instant

class ITDBProcessedMessageSpec extends CatsEffectSuite with DBFixture {

  val botId: SBotId = SBotId("botid")

  databaseFixture.test("DBProcessedMessage.markIfNew is true only the first time for the same message") { fixture =>
    val resourceAssert = for {
      db     <- fixture.resourceDBLayer.map(_.dbProcessedMessage)
      first  <- Resource.eval(db.markIfNew(TestIds.chatId(51L), botId, MessageId("1")))
      second <- Resource.eval(db.markIfNew(TestIds.chatId(51L), botId, MessageId("1")))
    } yield (first, second)
    resourceAssert.use { case (first, second) => IO(assertEquals((first, second), (true, false))) }
  }

  databaseFixture.test(
    "DBProcessedMessage.markIfNew keeps apart different messages, chats, bots and platforms"
  ) { fixture =>
    val chat  = TestIds.chatId(52L)
    val other = ChatId(Platform.Irc, chat.id)
    val resourceAssert = for {
      db <- fixture.resourceDBLayer.map(_.dbProcessedMessage)
      results <- Resource.eval(
        List(
          (chat, botId, MessageId("1")),
          (chat, botId, MessageId("2")),
          (TestIds.chatId(53L), botId, MessageId("1")),
          (chat, SBotId("otherbot"), MessageId("1")),
          (other, botId, MessageId("1"))
        ).traverse { case (c, b, m) => db.markIfNew(c, b, m) }
      )
    } yield results
    resourceAssert.use(results => IO(assertEquals(results, List.fill(5)(true))))
  }

  databaseFixture.test("DBProcessedMessage.deleteOlderThan forgets only the old messages") { fixture =>
    val resourceAssert = for {
      db       <- fixture.resourceDBLayer.map(_.dbProcessedMessage)
      _        <- Resource.eval(db.markIfNew(TestIds.chatId(55L), botId, MessageId("1")))
      none     <- Resource.eval(db.deleteOlderThan(Instant.now.getEpochSecond - 3600))
      all      <- Resource.eval(db.deleteOlderThan(Instant.now.getEpochSecond + 3600))
      again    <- Resource.eval(db.markIfNew(TestIds.chatId(55L), botId, MessageId("1")))
    } yield (none, all, again)
    resourceAssert.use { case (none, all, again) => IO(assertEquals((none, all, again), (0, 1, true))) }
  }
}
