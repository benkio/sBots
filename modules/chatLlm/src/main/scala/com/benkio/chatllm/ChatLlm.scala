package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.llm.LlmEngine
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.Repository
import com.benkio.chatllm.llm.LlmClientApi
import com.benkio.chatllm.llm.LlmConfig
import com.benkio.chatllm.llm.LlmProvider
import com.benkio.chatllm.llm.OpenRouterClient
import com.benkio.chatllm.observability.LlmObserver
import com.benkio.chatllm.observability.LlmTraceStore
import com.benkio.chatllm.prompt.ChatLlmResponder
import com.benkio.chatllm.prompt.PromptBudget
import com.benkio.chatllm.prompt.PromptBuilder
import com.benkio.chatllm.prompt.ReplyCatalogue
import com.benkio.chatllm.repository.db.LlmContextStore
import com.benkio.chatllm.repository.db.LlmSettingsDB
import doobie.util.transactor.Transactor
import log.effect.LogWriter

import java.nio.charset.StandardCharsets

/** The LLM support of the bots: provider clients, rate limiting, prompt building and the implementation of the
  * `LlmResponder` / `LlmSettings` ports of chatCore. Only `main` depends on this module.
  *
  * `settings` is still the `noop` port: the feature is unconfigured. `dbSettings` is wired by `main` behind the `/llm`
  * command (#1488); `contextStore` is the real doobie-backed context, ready for the engine routing of #1489.
  */
object ChatLlm {

  def responder[F[_]: Applicative]: LlmResponder[F] = LlmResponder.noop[F]
  def settings[F[_]: Applicative]: LlmSettings[F]   = LlmSettings.noop[F]

  /** The settings port for one bot: the table is scoped by platform, chat and bot. */
  def dbSettings[F[_]: Async](transactor: Transactor[F], botId: SBotId)(using
      log: LogWriter[F]
  ): LlmSettings[F] =
    LlmSettingsDB[F](transactor, botId)

  /** The model configured for each provider, for the logs: the free router unless OpenRouter has a model set. The model
    * that really served a request, when the provider reports it, is preferred by the observer.
    */
  def modelOf(config: LlmConfig)(provider: LlmProvider): Option[String] =
    provider match {
      case LlmProvider.Gemini     => config.gemini.model
      case LlmProvider.OpenRouter => Some(OpenRouterClient.modelFor(config.openrouter))
    }

  /** The reply cap the prompt states and the output guard enforces. */
  val defaultMaxReplyChars: Int = 500

  /** The LLM for the bots: the `/llm` settings and, per bot, the responder with its persona, catalogue and context. A
    * bot gets [[LlmEngine.noop]] (and the reason is logged) when the API keys of both providers are missing, or its
    * persona or reply catalogue is missing or unusable: nothing breaks, the bot just never answers with the LLM.
    */
  def wiring[F[_]: Async](config: LlmConfig, client: LlmClientApi[F])(using log: LogWriter[F]): LlmWiring[F] =
    LlmWiring(
      settings = dbSettings[F],
      engine = (transactor, botId) =>
        if !LlmConfig.credentialsComplete(config) then log
          .info(s"[ChatLlm] $botId: no API keys for both providers, the LLM replies are off") *>
          LlmEngine.noop[F].pure[F]
        else
          loadResponder[F](config = config, client = client, transactor = transactor, botId = botId)
            .map {
              case Right(responder) => LlmEngine(responder, config.timeout)
              case Left(_)          => LlmEngine.noop[F]
            }
            .flatTap {
              case engine if engine.responder.answers =>
                log.info(
                  s"[ChatLlm] $botId: the LLM replies are available (timeout=${config.timeout}, provider-timeout=${config.providerTimeout})"
                )
              case _ => Async[F].unit
            }
    )

  private def loadResponder[F[_]: Async](
      config: LlmConfig,
      client: LlmClientApi[F],
      transactor: Transactor[F],
      botId: SBotId
  )(using log: LogWriter[F]): F[Either[String, LlmResponder[F]]] = {
    def resource(name: String): F[Either[String, String]] =
      Repository
        .getResourceByteArray[F](name)
        .use(_.map(bytes => new String(bytes, StandardCharsets.UTF_8)).leftMap(_.toString).pure[F])
    val result: F[Either[String, LlmResponder[F]]] = for {
      persona   <- resource(s"${botId.value}_persona.md")
      catalogue <- resource(s"${botId.value}_reply_catalogue.json")
    } yield
      for {
        personaText <- persona
        catalogueJs <- catalogue
        entries     <- ReplyCatalogue.parse(catalogueJs)
        maxBytes = PromptBudget.personaMaxBytes(config.maxInputTokens, config.context.maxTokens)
        _ <- Either.cond(
          personaText.getBytes(StandardCharsets.UTF_8).length <= maxBytes,
          (),
          s"the persona is over the prompt budget ($maxBytes bytes)"
        )
        builder <- PromptBuilder(defaultMaxReplyChars)
      } yield new ChatLlmResponder[F](
        client = client,
        context = LlmContext[F](transactor, botId, config.context),
        builder = builder,
        botId = botId,
        persona = personaText,
        catalogue = entries,
        observer = Some(
          LlmObserver[F](
            traces = LlmTraceStore[F](transactor),
            traceEnabled = config.trace.enabled,
            salt = config.context.authorSalt.value,
            modelOf = modelOf(config)
          )
        )
      )
    result.flatTap {
      case Left(problem) => log.warn(s"[ChatLlm] $botId: the LLM replies are off: $problem")
      case Right(_)      => Async[F].unit
    }
  }

  def contextStore[F[_]: Async](transactor: Transactor[F]): LlmContextStore[F] =
    LlmContextStore[F](transactor)
}
