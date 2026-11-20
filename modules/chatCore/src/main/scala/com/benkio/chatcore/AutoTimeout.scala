package com.benkio.chatcore

import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.repository.db.DBTimeout
import com.benkio.chatcore.repository.db.DBTimeoutData
import log.effect.LogWriter

import java.time.Instant
import scala.concurrent.duration.*

/** The reply timeout a chat gets when the bot is added to it, from the number of its members. It is logarithmic, so a
  * big chat is slowed more than a small one but never in proportion to its size: 2 members -> 5s, 10 -> 17s, 100 ->
  * 33s, 1000 -> 50s, 10000 -> 66s. `/settimeout` changes it afterwards.
  */
object AutoTimeout {

  val perDoubling: FiniteDuration = 5.seconds
  val minimum: FiniteDuration     = 1.second

  def forMembers(memberCount: Int): FiniteDuration = {
    val log2 = if memberCount < 1 then 0d else math.log(memberCount.toDouble) / math.log(2d)
    (perDoubling.toSeconds * log2).round.seconds.max(minimum)
  }

  /** Replaces the timeout of the pair, if any: the bot was removed from the chat in between. The member count is a
    * snapshot, the interaction clock starts now.
    */
  def set[F[_]: Monad](dbTimeout: DBTimeout[F], chatId: ChatId, botId: SBotId, memberCount: Int)(using
      log: LogWriter[F]
  ): F[Unit] = {
    val value = forMembers(memberCount)
    log.info(s"[AutoTimeout] $chatId - $botId: $memberCount members, timeout $value") *>
      dbTimeout.setTimeout(DBTimeoutData(Timeout(chatId, botId, value, Instant.now())))
  }
}
