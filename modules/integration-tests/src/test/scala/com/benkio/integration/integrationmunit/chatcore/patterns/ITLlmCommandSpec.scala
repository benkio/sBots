package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBLlmSettings
import com.benkio.chatcore.ComputeReply
import com.benkio.chatcore.TestIds
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import doobie.implicits.*
import munit.CatsEffectSuite

class ITLlmCommandSpec extends CatsEffectSuite with DBFixture {

  private val sBotConfig = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  private val sBotInfo   = sBotConfig.sBotInfo
  private val botId      = sBotInfo.botId
  private val chatId     = TestIds.chatId(1L)
  private val otherChat  = TestIds.chatId(2L)
  private val otherBot   = SBotInfo.SBotId("otherbot")

  private def msg(text: String): Message =
    Message(messageId = None, date = 0L, chatId = chatId, chatType = "private", text = Some(text))

  /** The same effectful reply the fixed command produces, run through the normal effectful dispatch. */
  private def run(dbLayer: DBLayer[IO], text: String): IO[String] =
    ComputeReply
      .runEffectfulReply[IO](
        reply = EffectfulReply(EffectfulKey.Llm(sBotInfo)),
        msg = msg(text),
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        dbLayer = dbLayer,
        effectfulCallbacks = Map.empty,
        ttl = sBotConfig.messageTimeToLive,
        commandSyntax = CommandSyntax.Slash
      )
      .map(_.show)

  databaseFixture.test("/llm uses the real settings port and persists only this chat and bot") { fixture =>
    val result = for {
      dbLayer <- Resource.eval(
        DBLayer.make[IO](
          transactor = fixture.transactor,
          botId = botId
        )
      )

      on              <- Resource.eval(run(dbLayer, "/llm on"))
      storedOn        <- Resource.eval(DBLlmSettings.statusQuery(chatId, botId).option.transact(fixture.transactor))
      status          <- Resource.eval(run(dbLayer, "/llm status"))
      otherChatStatus <- Resource.eval(
        DBLlmSettings.statusQuery(otherChat, botId).option.transact(fixture.transactor)
      )
      otherBotStatus <- Resource.eval(
        DBLlmSettings.statusQuery(chatId, otherBot).option.transact(fixture.transactor)
      )

      off       <- Resource.eval(run(dbLayer, "/llm off"))
      storedOff <- Resource.eval(DBLlmSettings.statusQuery(chatId, botId).option.transact(fixture.transactor))
    } yield {
      assertEquals(on, "LLM replies enabled for this chat")
      assertEquals(storedOn, Some(true))
      assertEquals(status, "LLM replies are enabled for this chat")
      assertEquals(otherChatStatus, None)
      assertEquals(otherBotStatus, None)
      assertEquals(off, "LLM replies disabled for this chat")
      assertEquals(storedOff, Some(false))
    }
    result.use_
  }
}
