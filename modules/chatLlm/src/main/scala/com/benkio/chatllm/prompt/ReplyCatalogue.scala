package com.benkio.chatllm.prompt

import com.benkio.chatcore.model.reply.ReplyBundleMessage
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Decoder
import io.circe.Json

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** One known reply of a bot as the prompt shows it: a stable id and a one-line Italian summary (#1490). */
final case class CatalogueEntry(id: String, summary: String)

object CatalogueEntry {
  given Decoder[CatalogueEntry] = Decoder.forProduct2("id", "summary")(CatalogueEntry.apply)
}

/** The reply catalogue of a bot and the id of a reply bundle. The id is the one `PersonaTool` writes in the catalogue
  * files: SHA-256 of the matcher and the sorted triggers, so the engine's `ReplyBundleMessage` of a candidate and the
  * catalogue entry the model picked identify the same reply.
  */
object ReplyCatalogue {

  def parse(content: String): Either[String, List[CatalogueEntry]] =
    decode[List[CatalogueEntry]](content).left.map(_.getMessage).flatMap { entries =>
      val duplicated = entries.groupBy(_.id).collect { case (id, es) if es.size > 1 => id }.toList.sorted
      Either.cond(duplicated.isEmpty, entries, s"Duplicate catalogue ids: ${duplicated.mkString(", ")}")
    }

  def idOf(bundle: ReplyBundleMessage): String = idOf(bundle.asJson)

  private def idOf(bundle: Json, triggers: List[String]): String = hash(matcherOf(bundle), triggers)

  /** The id of a bundle the model may reuse: only a bundle with at least one text trigger has a meaningful signature,
    * the others would all hash to the same id.
    */
  def reusableId(bundle: ReplyBundleMessage): Option[String] = {
    val json     = bundle.asJson
    val triggers = triggersOf(json)
    Option.when(triggers.nonEmpty)(idOf(json, triggers))
  }

  def idOf(bundle: Json): String = idOf(bundle, triggersOf(bundle))

  /** The matcher and the sorted triggers: the same signature of agent-context/tools/conflict-resolution. */
  private def hash(matcher: String, triggers: List[String]): String = {
    val signature = Json.arr(Json.fromString(matcher), Json.fromValues(triggers.sorted.map(Json.fromString))).noSpaces
    val digest    = MessageDigest.getInstance("SHA-256").digest(signature.getBytes(StandardCharsets.UTF_8))
    "r" + digest.take(6).map(byte => f"${byte & 0xff}%02x").mkString
  }

  private def matcherOf(bundle: Json): String =
    bundle.hcursor.get[String]("matcher").toOption.getOrElse("ContainsOnce")

  private def triggersOf(bundle: Json): List[String] =
    bundle.hcursor
      .downField("trigger")
      .downField("TextTrigger")
      .downField("triggers")
      .as[List[Json]]
      .toOption
      .getOrElse(Nil)
      .flatMap { value =>
        value.hcursor
          .get[String]("StringTextTriggerValue")
          .toOption
          .orElse(value.hcursor.downField("RegexTextTriggerValue").get[String]("trigger").toOption)
      }
}
