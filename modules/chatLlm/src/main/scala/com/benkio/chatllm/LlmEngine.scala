package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.llm.LlmDependencies
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.LlmEngine as CoreLlmEngine
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import log.effect.LogWriter
import org.llm4s.http.HttpClientOptions
import org.llm4s.http.Llm4sHttpClient

/** The chatLlm implementation of the LLM entry point of the bot engine. The llm4s settings and clients are loaded once
  * at startup; every bot gets its engine from the `build` of the resulting [[LlmDependencies]], backed by the active
  * [[LlmResponder]].
  */
object LlmEngine {

  /** Loads the shared clients and offers the `build` the bots call once their own `DBLayer` is ready. */
  def dependencies[F[_]: Async](
      config: => Config = ConfigFactory.load()
  )(using log: LogWriter[F]): Resource[F, LlmDependencies[F]] =
    for {
      _        <- Resource.eval(log.info("[LlmEngine] llm4s startup wiring Started"))
      settings <- Resource.eval(LlmLoader.loadSettings[F](config))
      http     <- Resource.make(
        Async[F].delay(Llm4sHttpClient.create(HttpClientOptions.default))
      )(http => Async[F].delay(http.close()))
      llmClients   <- LlmLoader.loadClients[F](settings, http)
      systemPrompt <- Resource.eval(SystemPrompt.load[F])
      responder = LlmResponder[F](llmClients, systemPrompt)
      _ <- Resource.eval(log.info("[LlmEngine] llm4s startup wiring ready"))
    } yield LlmDependencies[F](
      // TODO(#1499): the `DBLayer` is ignored; build the per-bot context from it here before the LLM replies go live.
      (_, persona) => Resource.pure(LlmEngineImpl[F](responder, persona))
    )

  private[chatllm] final case class LlmEngineImpl[F[_]](responder: LlmResponder[F], persona: Persona)
      extends CoreLlmEngine[F] {
    override def reply(msg: Message): F[Option[Text]] = responder.respond(persona, msg)
  }
}
