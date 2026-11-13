package com.benkio.chatdiscordadapter.gateway

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.initialization.TokenReader
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

/** Every bot has its own Discord application, so its own token, kept like the Telegram one as a document of the bot. */
object DiscordToken {

  /** The name of the token file of a bot: `<botId>_<botName>_discord.token`. */
  def filename(sBotInfo: SBotInfo): String = s"${sBotInfo.botId}_${sBotInfo.botName}_discord.token"

  def read[F[_]: Async: LogWriter](sBotInfo: SBotInfo, repository: Repository[F]): Resource[F, String] =
    TokenReader.token(filename(sBotInfo), repository).map(_.trim)
}
