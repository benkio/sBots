package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.resources.db.DBTimeout
import telegramium.bots.Message

object PostComputationPatterns {

  def timeoutPostComputation[F[_]](dbTimeout: DBTimeout[F], botName: String): Message => F[Unit] = m =>
  dbTimeout.logLastInteraction(m.chat.id, botName)
}
