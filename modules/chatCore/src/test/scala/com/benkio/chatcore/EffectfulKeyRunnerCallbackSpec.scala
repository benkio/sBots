package com.benkio.chatcore

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.time.Instant

class EffectfulKeyRunnerCallbackSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("cb"), botName = SBotInfo.SBotName("CallbackBot"))

  private def run(syntax: CommandSyntax, text: String): IO[Option[(Option[String], CommandSyntax)]] =
    for {
      received <- Ref.of[IO, Option[(Option[String], CommandSyntax)]](None)
      callback: EffectfulCallback[IO] = (msg, commandSyntax) =>
        received.set(Some((msg.text, commandSyntax))).as(Text("done"): ReplyValue)
      _ <- EffectfulKeyRunner.runEffectfulKey[IO](
        effectfulKey = EffectfulKey.Callback("key", sBotInfo),
        msg = Message(
          messageId = None,
          date = Instant.now.getEpochSecond(),
          chatId = TestIds.chatId(1L),
          chatType = "test",
          text = Some(text)
        ),
        dbLayer = DBLayerMock.mock(sBotInfo.botId),
        backgroundJobManager = BackgroundJobManagerMock.mock(),
        effectfulCallbacks = Map("key" -> callback),
        ttl = None,
        commandSyntax = syntax
      )
      result <- received.get
    } yield result

  test("a callback gets the message as it is and the command syntax of the platform") {
    val ircSyntax = CommandSyntax(prefix = "!")
    assertIO(run(ircSyntax, "!key some input"), Some((Some("!key some input"), ircSyntax)))
  }

  test("a callback gets the slash syntax on a slash platform") {
    assertIO(run(CommandSyntax.Slash, "/key some input"), Some((Some("/key some input"), CommandSyntax.Slash)))
  }
}
