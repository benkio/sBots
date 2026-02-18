package com.benkio.RichardPHJBensonBot

import cats.syntax.all.*
import cats.ApplicativeThrow
import com.benkio.telegrambotinfrastructure.model.reply.toText
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.SBot
import telegramium.bots.Message

object RichardPHJBensonBot {

  val sBotInfo: SBotInfo = SBotInfo(SBotId("rphjb"), SBotName("RichardPHJBensonBot"))

  val bensonifyKey: String                                                                    = "bensonify"
  def commandEffectfulCallback[F[_]: ApplicativeThrow]: Map[String, Message => F[List[Text]]] =
    Map(
      (
        bensonifyKey,
        (msg: Message) =>
          CommandPatterns.handleCommandWithInput[F](
            msg = msg,
            command = RichardPHJBensonBot.bensonifyKey,
            sBotInfo = sBotInfo,
            computation = t => List(Bensonify.compute(t)).toText.pure[F],
            defaultReply = "E PARLAAAAAAA!!!!",
            ttl = SBot.buildSBotConfig(sBotInfo).messageTimeToLive
          )
      )
    )
}
