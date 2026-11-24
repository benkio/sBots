package com.benkio.chatllm.observability

import cats.syntax.all.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatllm.llm.Completion
import com.benkio.chatllm.llm.CompletionStatus
import com.benkio.chatllm.llm.LlmProvider
import com.benkio.chatllm.llm.LlmReply
import com.benkio.chatllm.llm.ProviderResult
import com.benkio.chatllm.prompt.LlmDecision

import java.security.MessageDigest

/** What the responder did with one completion, in the words of the dashboard (#1493). */
enum ReplyAction(val label: String) {
  case Text   extends ReplyAction("text")
  case Reuse  extends ReplyAction("reuse")
  case Silent extends ReplyAction("silent")
}

/** One interaction of a bot with the LLM, ready to be logged: no chat content, only what is needed to count, time and
  * explain it. `chatHash` is salted, so a log reader cannot recover the chat id.
  *
  * Token counts are estimates (four characters per token, like the input limit): the providers do not report usage
  * through the client boundary.
  */
final case class LlmInteraction(
    chatHash: String,
    bot: String,
    model: String,
    promptVersion: String,
    promptTokens: Int,
    responseTokens: Int,
    latencyMs: Long,
    status: String,
    action: ReplyAction,
    reusedReplyId: Option[String],
    failure: Option[String],
    attempts: List[String],
    fallback: Boolean,
    parsed: Boolean,
    stripped: Boolean,
    localRejection: Option[String],
    providerErrors: List[String] = Nil,
    providerErrorMessage: Option[String] = None,
    geminiMs: Option[Long] = None,
    openRouterMs: Option[Long] = None,
    // A `text` reply recovered from an object cut by the provider's output-token limit, still guarded before sending.
    truncated: Boolean = false
)

object LlmInteraction {

  /** A stable anonymous label of a chat: the salted SHA-256 prefix, the same technique as the anonymous author label.
    */
  def chatHash(chatId: ChatId, salt: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(s"$salt:${chatId.platform.id}:${chatId.id}".getBytes("UTF-8"))
      .take(8)
      .map("%02x".format(_))
      .mkString

  def estimateTokens(text: String): Int = math.ceil(text.length.toDouble / 4).toInt

  /** The action the responder takes for the decision, and the reply id of a reuse that was really offered. */
  def actionOf(decision: LlmDecision, reusable: Boolean): (ReplyAction, Option[String]) =
    decision match {
      case LlmDecision.Text(_)                       => (ReplyAction.Text, None)
      case LlmDecision.Reuse(id) if reusable         => (ReplyAction.Reuse, Some(id))
      case LlmDecision.Reuse(_) | LlmDecision.Silent => (ReplyAction.Silent, None)
    }

  /** Why no reply was sent, when it is a failure rather than the model choosing silence. */
  def failureOf(completion: Completion, decision: LlmDecision, reuseOffered: Boolean): Option[String] =
    completion.status match {
      case CompletionStatus.Silent  => Some("quota_exhausted")
      case CompletionStatus.Pending => Some("pending")
      case CompletionStatus.Failed  =>
        // A provider can report success with no structured reply: that is a failure of the output contract, not a
        // successful attempt, so it must not appear in the dashboard as `failure=success`.
        completion.localRejection
          .map(_.label)
          .orElse(
            completion.attempts
              .flatMap(_.result)
              .filter(_ != ProviderResult.Success)
              .lastOption
              .map(r => r.toString.toLowerCase)
          )
          .orElse(
            completion.attempts
              .find(_.result.contains(ProviderResult.Success))
              .as("success_without_reply")
          )
          .orElse(Some("failed"))
      case CompletionStatus.Answered =>
        (completion.reply.map(_.action), decision) match {
          case (Some(com.benkio.chatllm.llm.LlmAction.Text), LlmDecision.Silent)  => Some("output_rejected")
          case (Some(com.benkio.chatllm.llm.LlmAction.Reuse), LlmDecision.Silent) => Some("reuse_without_id")
          case (Some(com.benkio.chatllm.llm.LlmAction.Reuse), LlmDecision.Reuse(_)) if !reuseOffered =>
            Some("reuse_not_offered")
          case _ => None
        }
    }

  /** The attempts as `provider=result`, in order; a provider asked but never answered is `none`. */
  def attemptLabels(completion: Completion): List[String] =
    completion.attempts.map(a => s"${a.provider.toString.toLowerCase}:${a.result.fold("none")(_.toString.toLowerCase)}")

  /** The HTTP failures of the attempts as `provider:status`, e.g. `gemini:404`; the messages stay in the log line. */
  def providerErrors(completion: Completion): List[String] =
    completion.attempts.flatMap(a => a.failure.map(f => s"${a.provider.toString.toLowerCase}:${f.httpStatus}"))

  /** The first provider error message (capped), the one that explains why the fallback was needed. */
  def providerErrorMessage(completion: Completion): Option[String] =
    completion.attempts.flatMap(_.failure).flatMap(_.message).headOption

  /** How long the call to `provider` took, when it was asked. */
  def providerLatencyMs(completion: Completion, provider: LlmProvider): Option[Long] =
    completion.attempts.find(_.provider == provider).flatMap(_.latencyMs)

  /** The fallback was used when the completion did not start and end with its configured primary alone. */
  def usedFallback(completion: Completion): Boolean =
    completion.attempts.exists(_.provider != completion.primaryProvider)

  /** The provider the evidence belongs to: the one that produced the raw text, else the one that answered, else the
    * last one asked.
    */
  def responsibleProvider(completion: Completion): Option[LlmProvider] =
    completion.raw.map(_.provider).orElse(answeringProvider(completion))

  /** The model the provider reported for the raw text, when it did. */
  def servedModel(completion: Completion): Option[String] = completion.raw.flatMap(_.model)

  def answeringProvider(completion: Completion): Option[LlmProvider] =
    completion.attempts
      .find(_.result.contains(ProviderResult.Success))
      .orElse(completion.attempts.lastOption)
      .map(_.provider)

  def replyText(reply: Option[LlmReply]): Int = reply.fold(0)(r => estimateTokens(r.text))
}
