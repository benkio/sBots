package com.benkio.chatcore.llm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter

import java.nio.charset.StandardCharsets

/** The character a bot plays when the LLM replies: the text of its persona file, `<botId>_persona.md` in the resources
  * of the bot module. chatCore reads it and hands it to the LLM engine, which owns what to do with it.
  */
final case class Persona(text: String)

object Persona {

  final case class EmptyPersona(filename: String) extends Throwable(s"[Persona] The persona file $filename is empty")

  /** Reads the persona file from the resources. A missing or empty file is an error: the bot would play no character.
    */
  def load[F[_]: Async: LogWriter](filename: String): F[Persona] =
    Repository
      .getResourceByteArray[F](filename)
      .use(Async[F].fromEither(_))
      .map(bytes => String(bytes, StandardCharsets.UTF_8))
      .flatMap(text => Async[F].raiseWhen(text.trim.isEmpty)(EmptyPersona(filename)).as(Persona(text)))
}
