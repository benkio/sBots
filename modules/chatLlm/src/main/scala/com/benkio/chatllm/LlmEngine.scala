package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.llm.LlmDependencies
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.LlmEngine as CoreLlmEngine
import log.effect.LogWriter
import org.llm4s.http.HttpClientOptions
import org.llm4s.http.Llm4sHttpClient

/** The chatLlm implementation of the LLM entry point of the bot engine. The llm4s settings and clients are loaded once
  * at startup; every bot gets its engine from the `build` of the resulting [[LlmDependencies]]. Until the LLM replies
  * land the responder never answers.
  */
object LlmEngine {

  /** Loads the shared clients and offers the `build` the bots call once their own `DBLayer` is ready. */
  def dependencies[F[_]: Async](using log: LogWriter[F]): Resource[F, LlmDependencies[F]] =
    for {
      _        <- Resource.eval(log.info("[LlmEngine] llm4s startup wiring Started"))
      settings <- Resource.eval(LlmLoader.loadSettings[F])
      http     <- Resource.make(
        Async[F].delay(Llm4sHttpClient.create(HttpClientOptions.default))
      )(http => Async[F].delay(http.close()))
      llmClients <- LlmLoader.loadClients[F](settings, http)
      responder = LlmResponder[F](llmClients)
      _ <- Resource.eval(log.info("[LlmEngine] llm4s startup wiring ready"))
    } yield LlmDependencies[F](_ => Resource.pure(LlmEngineImpl[F](responder)))

  private final case class LlmEngineImpl[F[_]](responder: LlmResponder[F]) extends CoreLlmEngine[F] {
    override def reply(msg: Message): F[Option[Text]] = responder.respond(msg)
  }
}
