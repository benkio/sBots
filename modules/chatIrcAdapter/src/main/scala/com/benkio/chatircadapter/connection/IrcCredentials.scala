package com.benkio.chatircadapter.connection

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.initialization.TokenReader
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

/** What identifies a bot on IRC. The nick is its name; it has an account on the server (NickServ) only if its password
  * is there, as the document `<botId>_<botName>_irc.token`: without it the bot connects with no identification.
  */
object IrcCredentials {

  /** The nick of a bot: its name restricted to what a nick can be (letters, digits and `[]\`_^{|}-`, not starting with
    * a digit or `-`) and to the 30 characters that most servers accept.
    */
  def nick(sBotInfo: SBotInfo): String = {
    val allowed = sBotInfo.botName.value.filter(c => c.isLetterOrDigit && c < 128 || "[]\\`_^{|}-".contains(c))
    val start   = if allowed.headOption.exists(c => c.isDigit || c == '-') then s"_$allowed" else allowed
    start.take(30)
  }

  def passwordFilename(sBotInfo: SBotInfo): String = s"${sBotInfo.botId}_${sBotInfo.botName}_irc.token"

  /** The password of the account, none if the bot has no document for it. */
  def password[F[_]: Async: LogWriter](sBotInfo: SBotInfo, repository: Repository[F]): Resource[F, Option[String]] =
    TokenReader
      .token(passwordFilename(sBotInfo), repository)
      .map(_.trim)
      .map(_.some)
      .handleErrorWith {
        case _: TokenReader.TokenError.TokenNotFound =>
          Resource
            .eval(LogWriter.info(s"[IrcCredentials] No password for ${sBotInfo.botName}, no identification"))
            .as(None)
        case e => Resource.eval(Async[F].raiseError(e))
      }
}
