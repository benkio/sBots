package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.ComputeReply
import com.benkio.chatcore.TestIds
import com.benkio.chatllm.repository.db.LlmSettingsDB
import com.benkio.chatllm.ChatLlm
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import doobie.implicits.*
import doobie.Transactor
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
          botId = botId,
          llmWiring = Some(ChatLlm.wiring[IO])
        )
      )
      _ <- Resource.eval(insertContext(xa = fixture.transactor, chatId = chatId, botId = botId, content = "to forget"))
      _ <- Resource.eval(
        insertContext(xa = fixture.transactor, chatId = otherChat, botId = otherBot, content = "to keep")
      )

      on              <- Resource.eval(run(dbLayer, "/llm on"))
      storedOn        <- Resource.eval(LlmSettingsDB.statusQuery(chatId, botId).option.transact(fixture.transactor))
      status          <- Resource.eval(run(dbLayer, "/llm status"))
      otherChatStatus <- Resource.eval(
        LlmSettingsDB.statusQuery(otherChat, botId).option.transact(fixture.transactor)
      )
      otherBotStatus <- Resource.eval(
        LlmSettingsDB.statusQuery(chatId, otherBot).option.transact(fixture.transactor)
      )

      off               <- Resource.eval(run(dbLayer, "/llm off"))
      storedOff         <- Resource.eval(LlmSettingsDB.statusQuery(chatId, botId).option.transact(fixture.transactor))
      forget            <- Resource.eval(run(dbLayer, "/llm forget"))
      context           <- Resource.eval(contexts(xa = fixture.transactor, chatId = chatId, botId = botId))
      otherContext      <- Resource.eval(contexts(xa = fixture.transactor, chatId = otherChat, botId = otherBot))
      storedAfterForget <- Resource.eval(
        LlmSettingsDB.statusQuery(chatId, botId).option.transact(fixture.transactor)
      )
    } yield {
      assertEquals(on, "LLM replies enabled for this chat")
      assertEquals(storedOn, Some(true))
      assertEquals(status, "LLM replies are enabled for this chat")
      assertEquals(otherChatStatus, None)
      assertEquals(otherBotStatus, None)
      assertEquals(off, "LLM replies disabled for this chat")
      assertEquals(storedOff, Some(false))
      assertEquals(forget, "LLM context forgotten for this chat")
      assertEquals(context, Nil)
      assertEquals(otherContext, List("to keep"))
      assertEquals(storedAfterForget, Some(false))
    }
    result.use_
  }

  private def insertContext(xa: Transactor[IO], chatId: ChatId, botId: SBotId, content: String): IO[Unit] =
    sql"""INSERT INTO llm_context (platform, chat_id, bot_id, role, content, created_at)
          VALUES (${chatId.platform.id}, ${chatId.id}, ${botId.value}, 'user', $content, 1)""".update.run
      .transact(xa)
      .void

  private def contexts(xa: Transactor[IO], chatId: ChatId, botId: SBotId): IO[List[String]] =
    sql"""SELECT content FROM llm_context
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"""
      .query[String]
      .to[List]
      .transact(xa)
}
