package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.repository.db.LlmSettingsDB
import doobie.util.transactor.Transactor
import log.effect.LogWriter
import org.llm4s.config.Llm4sSettings
import org.llm4s.http.HttpClientOptions
import org.llm4s.http.Llm4sHttpClient

/** The implementations of the chatCore LLM ports. Only the `/llm` settings exist for now: the per-chat switch is stored
  * and read from the bot's DB. The LLM replies are being rebuilt on top of LLM4s: the clients are loaded at startup,
  * but the responder does not answer yet.
  */
object ChatLlm {

  /** The settings port for one bot: the table is scoped by platform, chat and bot. */
  def dbSettings[F[_]: Async](transactor: Transactor[F], botId: SBotId)(using
      log: LogWriter[F]
  ): LlmSettings[F] =
    LlmSettingsDB[F](transactor, botId)

  def wiring[F[_]: Async](using log: LogWriter[F]): LlmWiring[F] =
    LlmWiring(settings = dbSettings[F], responder = LlmResponder.noop[F])

  /** Loads the settings and one client per configured provider once, when the application starts. The HTTP transport
    * and the clients are owned by this Resource and closed on shutdown. A provider without credentials is reported and
    * skipped: the bots still start, and the reply path remains a noop until the LLM work lands.
    */
  def startupWiring[F[_]: Async](using log: LogWriter[F]): Resource[F, LlmWiring[F]] =
    for {
      settings <- Resource.eval(LlmLoader.load[F])
      network  <- Resource.eval(
        Async[F].fromEither(settings.httpNetwork().leftMap(LLMLoadingError.fromLLMError(_)))
      )
      http <- Resource.make(
        Async[F].delay(Llm4sHttpClient.create(HttpClientOptions(network = network)))
      )(http => Async[F].delay(http.close()))
      _ <- loadClients[F](settings, http)
      _ <- Resource.eval(log.info("[ChatLlm] llm4s startup wiring ready"))
      // TODO: pass the clients to the right place and build a fallback client. check cookbook
    } yield wiring[F]

  private def loadClients[F[_]: Async](settings: Llm4sSettings, http: Llm4sHttpClient)(using
      log: LogWriter[F]
  ): Resource[F, Unit] =
    for {
      configs <- Resource.eval(
        Async[F].fromEither(settings.providerConfigs().leftMap(LLMLoadingError.fromLLMError(_)))
      )
      defaultProvider <- Resource.eval(
        Async[F].fromEither(settings.defaultProviderName().leftMap(LLMLoadingError.fromLLMError(_)))
      )
      _ <- configs.toList.traverse { case (providerName, config) =>
        config.fold(
          err => Resource.eval(log.warn(s"[ChatLlm] skipping provider $providerName: ${err.message}")),
          _ =>
            if providerName == defaultProvider then Resource.eval(Async[F].unit)
            else LlmLoader.loadClient[F](settings, http, Some(providerName)).void
        )
      }
    } yield ()
}
