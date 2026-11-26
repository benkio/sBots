package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import com.benkio.chatllm.llm.LlmConfig
import com.benkio.chatllm.llm.LlmContextConfig
import com.benkio.chatllm.prompt.PromptBudget
import pureconfig.ConfigSource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** Tooling for issue #1490, reduced by #1594. It does not change runtime behavior: it checks that the curated persona
  * files stay within the prompt budget and have the expected sections. Personas are long-form research documents (up to
  * roughly 8k words).
  */
object PersonaTool extends IOApp {

  final case class BotFiles(id: String, persona: Path)

  private val PersonaHardMaxBytes = 64 * 1024

  /** The persona must fit the prompt budget of the configured input limit next to a full context window. */
  private lazy val personaMaxBytes: Int = {
    val llm            = ConfigSource.default.at("main.llm")
    val maxInputTokens = llm.at("max-input-tokens").load[Int].getOrElse(LlmConfig.defaultMaxInputTokens)
    val contextTokens  = llm.at("context.max-tokens").load[Int].getOrElse(LlmContextConfig.defaultMaxTokens)
    math.min(PersonaHardMaxBytes, PromptBudget.personaMaxBytes(maxInputTokens, contextTokens))
  }

  /** Accept both the compact four-heading form and long, numbered research personas. Matching is done on level-2
    * headings only, so `### Esempio` cannot satisfy the examples requirement.
    */
  private val PersonaSectionPatterns = List(
    "description"  -> """(?iu)^##\s+(?:\d+\.\s+)?(?:descrizione|identità|identita|profilo|identity|description).*""".r,
    "speech style" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:stile|voce|linguaggio|style|voice|language|communication).*""".r,
    "few-shot examples" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:esempi|esempio|conversazioni|dialoghi|examples?|dialogue|conversation).*""".r,
    "rules" -> """(?iu)^##\s+(?:\d+\.\s+)?(?:regole|accuratezza|rules?|accuracy).*""".r
  )

  override def run(args: List[String]): IO[ExitCode] =
    BotRegistry.toList(BotRegistry.value).map(botFiles).traverse_(checkBot).as(ExitCode.Success)

  private def botFiles(entry: BotRegistryEntry[IO]): BotFiles = {
    val config = entry.sBotConfig
    val id     = config.sBotInfo.botId.value
    BotFiles(id = id, persona = config.repliesJsonFilePath.getParent.resolve(s"${id}_persona.md"))
  }

  private def validate(files: BotFiles): List[String] = {
    val errors = List.newBuilder[String]

    if !Files.exists(files.persona) then errors += s"missing ${files.persona.getFileName}"
    else {
      val bytes   = Files.readAllBytes(files.persona)
      val content = new String(bytes, StandardCharsets.UTF_8)
      val lines   = content.linesIterator.toList
      if bytes.isEmpty then errors += "persona is empty"
      if bytes.length > personaMaxBytes
      then errors += s"persona too large (${bytes.length} bytes, the prompt budget allows $personaMaxBytes)"
      PersonaSectionPatterns.foreach { case (section, pattern) =>
        if !lines.exists(pattern.matches) then errors += s"persona missing a '$section' section"
      }
      if lines.exists(line => line.trim.matches("(?i)(assistant|system)\\s*:.*"))
      then errors += "persona contains a role label"
      if content.contains("/llm") then errors += "persona contains a command"
    }

    errors.result()
  }

  private def checkBot(files: BotFiles): IO[Unit] =
    IO.blocking(validate(files)).flatMap { errors =>
      if errors.nonEmpty then IO.raiseError(
        new RuntimeException(s"[PersonaTool] ${files.id}: ${errors.mkString("; ")}")
      )
      else IO.println(s"[PersonaTool] OK: ${files.id}")
    }
}
