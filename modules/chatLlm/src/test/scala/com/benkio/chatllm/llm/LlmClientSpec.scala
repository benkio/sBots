package com.benkio.chatllm.llm

import cats.effect.IO
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatllm.observability.LlmInteraction
import com.benkio.chatllm.ChatLlm
import io.circe.Json
import io.circe.Printer
import munit.CatsEffectSuite
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import pureconfig.ConfigSource

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.*

/** The behaviour of the provider-agnostic client, exercised through stubbed HTTP responses: no network, dummy keys.
  */
class LlmClientSpec extends CatsEffectSuite {

  private val prompt         = "ciao bot"
  private val geminiHost     = "gemini.example"
  private val openRouterHost = "openrouter.example"

  private def structured(text: String): Json =
    Json.obj("action" -> Json.fromString("text"), "text" -> Json.fromString(text))

  private def geminiOutput(raw: String): Json =
    Json.obj(
      "steps" -> Json.arr(
        Json.obj(
          "type"    -> Json.fromString("model_output"),
          "content" -> Json.arr(Json.obj("type" -> Json.fromString("text"), "text" -> Json.fromString(raw)))
        )
      )
    )

  private def geminiSuccess(text: String): Json =
    Json.obj(
      "steps" -> Json.arr(
        Json.obj("type" -> Json.fromString("thought")),
        Json.obj(
          "type"    -> Json.fromString("model_output"),
          "content" -> Json.arr(
            Json.obj(
              "type" -> Json.fromString("text"),
              "text" -> Json.fromString(Printer.noSpaces.print(structured(text)))
            )
          )
        )
      )
    )

  private def openRouterSuccess(text: String): Json =
    Json.obj(
      "choices" -> Json.arr(
        Json.obj(
          "message" -> Json.obj(
            "content" -> Json.fromString(
              Printer.noSpaces.print(structured(text))
            )
          )
        )
      )
    )

  private val configText =
    s"""gemini { endpoint = "https://$geminiHost/generate", api-key = "test-gemini-key" }
       |openrouter { endpoint = "https://$openRouterHost/chat", api-key = "test-openrouter-key" }
       |context.author-salt = test-only-salt""".stripMargin

  private def config(primary: String, extra: String = ""): LlmConfig =
    ConfigSource
      .string(s"$primary$extra$configText")
      .load[LlmConfig]
      .fold(
        err => throw new RuntimeException(err.prettyPrint()),
        identity
      )

  private def primary(provider: LlmProvider): String = provider match {
    case LlmProvider.Gemini     => "gemini"
    case LlmProvider.OpenRouter => "openrouter"
  }

  private def stub(
      gemini: (Status, Json),
      openRouter: (Status, Json),
      hosts: ListBuffer[String],
      bodies: ListBuffer[String]
  ): Client[IO] =
    Client.fromHttpApp(HttpApp[IO] { request =>
      hosts += request.uri.host.map(_.value).getOrElse("")
      request.as[String].flatMap { body =>
        bodies += body
        val (status, json) = request.uri.host.map(_.value).getOrElse("") match {
          case `geminiHost`     => gemini
          case `openRouterHost` => openRouter
          case _                => (Status.NotFound, Json.obj())
        }
        IO.pure(Response[IO](status).withEntity(json))
      }
    })

  private def completeWith(
      chosen: LlmProvider,
      gemini: (Status, Json),
      openRouter: (Status, Json),
      hosts: ListBuffer[String],
      bodies: ListBuffer[String]
  ): IO[Completion] =
    LlmClients
      .build[IO](
        stub(gemini = gemini, openRouter = openRouter, hosts = hosts, bodies = bodies),
        config("", s"primary-provider = ${primary(chosen)}\n")
      )
      .flatMap(_.complete(prompt))

  test("the provider and result enums keep their alternatives distinct") {
    // obligation: enum-comparable.LlmProvider
    // obligation: enum-comparable.ProviderResult
    assertEquals(LlmProvider.values.toSet, Set(LlmProvider.Gemini, LlmProvider.OpenRouter))
    assertEquals(
      ProviderResult.values.toSet,
      Set(
        ProviderResult.Success,
        ProviderResult.RateLimited,
        ProviderResult.ServerError,
        ProviderResult.Timeout,
        ProviderResult.Rejected,
        ProviderResult.MalformedJson
      )
    )
  }

  test("a reply is a value with a text field and structural equality") {
    // obligation: value-equality.LlmReply
    // obligation: entity-fields.LlmReply
    assertEquals(LlmReply("ciao", LlmAction.Text), LlmReply("ciao", LlmAction.Text))
    assertNotEquals(LlmReply("ciao", LlmAction.Text), LlmReply("bua", LlmAction.Text))
    assertEquals(LlmReply("ciao", LlmAction.Text).text, "ciao")
  }

  test("the primary provider is Gemini by default and the fallback is always the other provider") {
    // obligation: config-default.primary_provider
    // obligation: derived.Completion.fallback_provider
    assertEquals(LlmConfig.primary(config("")), Right(LlmProvider.Gemini))
    assertEquals(LlmConfig.primary(config("primary-provider = openrouter\n")), Right(LlmProvider.OpenRouter))
    assertEquals(Completion.started(prompt, LlmProvider.Gemini).fallbackProvider, LlmProvider.OpenRouter)
    assertEquals(Completion.started(prompt, LlmProvider.OpenRouter).fallbackProvider, LlmProvider.Gemini)
  }

  test("a completion starts with the primary attempt, no reply and no result yet") {
    // obligation: entity-fields.Completion
    // obligation: entity-relationship.Completion.attempts
    // obligation: entity-optional.Completion.reply
    // obligation: entity-fields.ProviderAttempt
    // obligation: entity-optional.ProviderAttempt.result
    val started = Completion.started(prompt, LlmProvider.Gemini)
    assertEquals(started.prompt, prompt)
    assertEquals(started.primaryProvider, LlmProvider.Gemini)
    assertEquals(started.status, CompletionStatus.Pending)
    assertEquals(started.reply, None)
    assertEquals(started.attempts, List(ProviderAttempt(LlmProvider.Gemini, None)))
  }

  test("the client boundary offers a completion operation and the provider boundary accepts responses") {
    // obligation: contract-signature.LlmClient.complete
    // obligation: surface-actor.LlmClientSurface
    // obligation: surface-provides.LlmClientSurface
    // obligation: surface-actor.LlmProviderBoundary
    // obligation: surface-provides.LlmProviderBoundary
    // obligation: entity-fields.LlmResponder
    val providerClient: LlmProviderClientApi[IO] = new LlmProviderClientApi[IO] {
      override val provider: LlmProvider                         = LlmProvider.Gemini
      override def generate(value: String): IO[ProviderResponse] = IO.pure(ProviderResponse.malformedJson)
    }
    for {
      limiter <- LlmRateLimiter[IO](LlmConfig.quotaFor(config("")))
      client: LlmClientApi[IO] = FallbackLlmClient(providerClient, providerClient, LlmConfig.defaultTimeout, limiter)
      completion <- client.complete(prompt)
      _          <- IO(assertEquals(completion.status, CompletionStatus.Failed))
      _ <- IO(assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter)))
      _ <- IO(
        assertEquals(
          ChatLlm
            .responder[IO]
            .respond(
              com.benkio.chatcore.model.Message(
                messageId = None,
                date = 0L,
                chatId = com.benkio.chatcore.model.ChatId(com.benkio.chatcore.model.Platform.Telegram, "1"),
                chatType = "private"
              )
            )
            .unsafeRunSync(),
          None
        )
      )
    } yield ()
  }

  test("Gemini answers with the structured JSON reply and asks for a JSON schema") {
    // obligation: rule-success.LlmClientUsesConfiguredProvider
    // obligation: rule-entity-creation.LlmClientUsesConfiguredProvider.1
    // obligation: rule-success.ProviderReturnsStructuredReply
    // obligation: transition-edge.Completion.pending.answered
    // obligation: invariant.ReplyOnlyWhenAnswered
    // obligation: invariant.OneAttemptPerProvider
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted.3
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.reply, Some(LlmReply("ciao", LlmAction.Text)))
        assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini))
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.Success))
        assertEquals(hosts.toList, List(geminiHost))
        assert(bodies.head.contains("response_format"), "the Gemini request did not ask for structured JSON")
        assert(bodies.head.contains(""""store":false"""), "the Gemini request must not store the interaction")
        assert(!bodies.head.contains("previous_interaction_id"), "the Gemini request must be stateless")
        assert(bodies.head.contains(prompt), "the Gemini request did not carry the prompt")
      }
  }

  test("switching the config to OpenRouter makes OpenRouter the primary provider") {
    // obligation: surface-provides.LlmClientSurface
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.reply, Some(LlmReply("bua", LlmAction.Text)))
        assertEquals(completion.attempts.map(_.provider), List(LlmProvider.OpenRouter))
        assertEquals(hosts.toList, List(openRouterHost))
        val request = io.circe.parser.parse(bodies.head).toOption.get.hcursor
        val format  = request.downField("response_format")
        assertEquals(format.get[String]("type"), Right("json_schema"))
        assertEquals(format.downField("json_schema").get[String]("name"), Right("bot_reply"))
        assertEquals(format.downField("json_schema").get[Boolean]("strict"), Right(true))
        assertEquals(
          OpenRouterClient.responseSchema.hcursor.get[List[String]]("required"),
          Right(List("action", "text"))
        )
        assertEquals(
          OpenRouterClient.responseSchema.hcursor.downField("properties").keys.map(_.toSet),
          Some(Set("action", "text"))
        )
        assertEquals(OpenRouterClient.responseSchema.hcursor.get[Boolean]("additionalProperties"), Right(false))
        assertEquals(request.downField("provider").get[Boolean]("require_parameters"), Right(true))
      }
  }

  test("a 429 from Gemini falls back to OpenRouter") {
    // obligation: rule-success.RetryableProviderFailureFallsBack
    // obligation: rule-entity-creation.RetryableProviderFailureFallsBack.1
    // obligation: rule-failure.ProviderReturnsStructuredReply.2
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.TooManyRequests, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua", LlmAction.Text)))
      assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter))
      assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.RateLimited, ProviderResult.Success))
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
    }
  }

  test("a 5xx from Gemini falls back to OpenRouter") {
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted.3
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.InternalServerError, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua", LlmAction.Text)))
      assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.ServerError, ProviderResult.Success))
    }
  }

  test("a 429 from the fallback fails the completion without a third attempt") {
    // obligation: rule-success.CompletionFailsAfterFallbackExhausted
    // obligation: transition-edge.Completion.pending.failed
    // obligation: rule-failure.RetryableProviderFailureFallsBack.4
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.TooManyRequests, Json.obj()),
      openRouter = (Status.ServiceUnavailable, Json.obj()),
      hosts = hosts,
      bodies = bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Failed)
      assertEquals(completion.reply, None)
      assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter))
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
    }
  }

  test("a rejected request from the primary falls back to the other provider") {
    // obligation: rule-success.NonRetryableProviderFailureFails
    // obligation: rule-failure.NonRetryableProviderFailureFails.2
    // obligation: rule-failure.RetryableProviderFailureFallsBack.2
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted.2
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.BadRequest, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.Rejected, ProviderResult.Success))
        assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      }
  }

  test("each provider attempt records how long its call took") {
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.ServiceUnavailable, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = ListBuffer.empty[String],
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter))
        assert(completion.attempts.forall(_.latencyMs.exists(_ >= 0L)))
        assert(LlmInteraction.providerLatencyMs(completion, LlmProvider.OpenRouter).isDefined)
      }
  }

  test("OpenRouter: a reply without text keeps the finish reason, the null content and the error of the choice") {
    val body = Json.obj(
      "model"   -> Json.fromString("vendor/reasoner:free"),
      "choices" -> Json.arr(
        Json.obj(
          "finish_reason" -> Json.fromString("length"),
          "message"       -> Json.obj("role" -> Json.fromString("assistant"), "content" -> Json.Null),
          "error"         -> Json.obj("code" -> Json.fromInt(502), "message" -> Json.fromString("upstream cut"))
        )
      )
    )
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.Ok, body),
      hosts = ListBuffer.empty[String],
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        assertEquals(completion.attempts.flatMap(_.result).headOption, Some(ProviderResult.MalformedJson))
        val message = completion.attempts.flatMap(_.failure).flatMap(_.message).headOption
        assertEquals(
          message,
          Some("no model text: finish_reason=length content=null choice_error=502 upstream cut")
        )
      }
  }

  test("OpenRouter diagnosis: documented fields only, nothing for a normal reply") {
    val ok = Json.obj(
      "choices" -> Json.arr(
        Json.obj(
          "finish_reason" -> Json.fromString("stop"),
          "message"       -> Json.obj("content" -> Json.fromString("{}"))
        )
      )
    )
    assertEquals(OpenRouterClient.diagnose(ok), Some("finish_reason=stop"))
    assertEquals(OpenRouterClient.diagnose(Json.obj()), None)
    val blank = Json.obj(
      "error"   -> Json.obj("message" -> Json.fromString("provider down")),
      "choices" -> Json.arr(Json.obj("message" -> Json.obj("content" -> Json.fromString("  "))))
    )
    assertEquals(OpenRouterClient.diagnose(blank), Some("content=empty error=provider down"))
  }

  test("a rejected request keeps the HTTP status and the capped error message of the provider") {
    val message = "This model models/gemini-2.5-flash is no longer available to new users."
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (
        Status.NotFound,
        Json.obj("error" -> Json.obj("code" -> Json.fromInt(404), "message" -> Json.fromString(message + ("x" * 500))))
      ),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = ListBuffer.empty[String],
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        val failure = completion.attempts.flatMap(_.failure).headOption
        assertEquals(failure.map(_.httpStatus), Some(404))
        assert(failure.flatMap(_.message).exists(_.startsWith(message)))
        assertEquals(failure.flatMap(_.message).map(_.length), Some(ProviderHttp.MaxErrorChars))
        assertEquals(LlmInteraction.providerErrors(completion), List("gemini:404"))
      }
  }

  test("a malformed structured reply from the primary falls back to the other provider") {
    // obligation: rule-success.MalformedProviderReplyFails
    // obligation: rule-failure.MalformedProviderReplyFails.2
    // obligation: rule-failure.ProviderReturnsStructuredReply.3
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.Ok, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson, ProviderResult.Success))
        assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      }
  }

  test("an OpenRouter reply breaking the strict schema (reuse, replyId) is malformed and falls back, then silence") {
    val old = Json.obj(
      "choices" -> Json.arr(
        Json.obj(
          "message" -> Json.obj(
            "content" -> Json.fromString("""{"action":"reuse","replyId":"abc","text":""}""")
          )
        )
      )
    )
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.Ok, old),
      hosts = ListBuffer.empty[String],
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson, ProviderResult.Success))
        assertEquals(completion.reply, Some(LlmReply("ciao", LlmAction.Text)))
      }
  }

  test("an OpenRouter schema violation with no fallback answer ends in a failed completion") {
    val hosts = ListBuffer.empty[String]
    val old   = Json.obj(
      "choices" -> Json.arr(
        Json.obj("message" -> Json.obj("content" -> Json.fromString("""{"action":"reuse","replyId":"abc"}""")))
      )
    )
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, Json.obj()),
      openRouter = (Status.Ok, old),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        assertEquals(hosts.toList, List(openRouterHost, geminiHost))
        assertEquals(completion.reply, None)
        assertEquals(
          completion.attempts.flatMap(_.result),
          List(ProviderResult.MalformedJson, ProviderResult.MalformedJson)
        )
      }
  }

  test("OpenRouter 404 (no endpoint supports the required parameters) is a rejection, then the fallback answers") {
    val hosts = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (
        Status.NotFound,
        Json.obj("error" -> Json.obj("code" -> Json.fromInt(404), "message" -> Json.fromString("No endpoints found")))
      ),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
      .map { completion =>
        assertEquals(hosts.toList, List(openRouterHost, geminiHost))
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.Rejected, ProviderResult.Success))
        assertEquals(completion.reply, Some(LlmReply("ciao", LlmAction.Text)))
      }
  }

  test("both providers ask for the same properties; only OpenRouter (strict mode) requires `text` too") {
    val gemini = GeminiClient.responseSchema.hcursor
    val router = OpenRouterClient.responseSchema.hcursor
    assertEquals(gemini.downField("properties").as[Json], router.downField("properties").as[Json])
    assertEquals(gemini.get[List[String]]("required"), Right(List("action")))
    assertEquals(router.get[List[String]]("required"), Right(List("action", "text")))
  }

  test("responses for a non-pending completion, from another provider or without a reply are rejected") {
    // obligation: rule-failure.ProviderReturnsStructuredReply.1
    // obligation: rule-failure.MalformedProviderReplyFails.1
    // obligation: rule-failure.NonRetryableProviderFailureFails.1
    // obligation: rule-failure.RetryableProviderFailureFallsBack.1
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted.1
    // obligation: rule-failure.RetryableProviderFailureFallsBack.3
    // obligation: rule-success.SuccessWithoutReplyFails
    // obligation: rule-failure.SuccessWithoutReplyFails.1
    // obligation: rule-failure.SuccessWithoutReplyFails.2
    // obligation: rule-failure.SuccessWithoutReplyFails.3
    val started  = Completion.started(prompt, LlmProvider.Gemini)
    val answered = Completion(
      prompt,
      LlmProvider.Gemini,
      CompletionStatus.Answered,
      Some(LlmReply("ciao", LlmAction.Text)),
      List(ProviderAttempt(LlmProvider.Gemini, Some(ProviderResult.Success)))
    )
    assertEquals(Completion.acceptResponse(answered, LlmProvider.Gemini, ProviderResponse.malformedJson), answered)
    assertEquals(
      Completion.acceptResponse(started, LlmProvider.OpenRouter, ProviderResponse.rateLimited),
      started
    )
    val withoutReply =
      Completion.acceptResponse(started, LlmProvider.Gemini, ProviderResponse(ProviderResult.Success, None))
    assertEquals(withoutReply.status, CompletionStatus.Failed)
    assertEquals(withoutReply.reply, None)
  }

  test("answered and failed completions are terminal and reject further transitions") {
    // obligation: transition-terminal.Completion.status
    // obligation: transition-rejected.Completion.status
    val started  = Completion.started(prompt, LlmProvider.Gemini)
    val answered = Completion.acceptResponse(
      started,
      LlmProvider.Gemini,
      ProviderResponse.success(LlmReply("ciao", LlmAction.Text))
    )
    val failed = Completion.acceptResponse(
      Completion.acceptResponse(started, LlmProvider.Gemini, ProviderResponse.malformedJson),
      LlmProvider.OpenRouter,
      ProviderResponse.malformedJson
    )

    assertEquals(answered.status, CompletionStatus.Answered)
    assertEquals(failed.status, CompletionStatus.Failed)
    assertEquals(
      Completion
        .acceptResponse(answered, LlmProvider.Gemini, ProviderResponse.success(LlmReply("bua", LlmAction.Text))),
      answered
    )
    assertEquals(
      Completion.acceptResponse(failed, LlmProvider.Gemini, ProviderResponse.success(LlmReply("bua", LlmAction.Text))),
      failed
    )
  }

  test("structured reply: fenced or surrounded JSON is accepted, anything else is not") {
    val expected = Right(LlmReply("ciao", LlmAction.Text))
    assertEquals(StructuredReply.parse("```json\n{\"action\":\"text\",\"text\":\"ciao\"}\n```"), expected)
    assertEquals(StructuredReply.parse("Ecco: {\"action\":\"text\",\"text\":\"ciao\"} spero vada bene"), expected)
    assertEquals(
      StructuredReply.parseDetailed("{\"action\":\"silent\"}").map(_.repair),
      Right(StructuredReply.Repair.AsIs)
    )
    assertEquals(
      StructuredReply.parseDetailed("Ecco: {\"action\":\"silent\"} ok").map(_.repair),
      Right(StructuredReply.Repair.Stripped)
    )
    assert(StructuredReply.parse("nessun json qui").isLeft)
    assert(StructuredReply.parse("```json\n{\"action\":\"shout\"}\n```").isLeft)
  }

  test("structured reply: an object cut by the output-token limit is malformed, never recovered") {
    val cut = "{\"action\":\"text\",\"text\":\"Cari amici, questa è una risposta lunga che il modello non ha chiuso"
    assert(StructuredReply.parse(cut).isLeft)
    assert(StructuredReply.parse("{\"action\":\"text\",\"text\":\"una \\\"citazione\\\" e un backslash \\").isLeft)
    assert(StructuredReply.parse("{\"action\":\"text\",\"text\":\"ciao\"").isLeft)
    assert(StructuredReply.parse("testo senza json").isLeft)
  }

  test("a cut Gemini reply is a malformed failure with its raw text kept, then the fallback answers") {
    val raw    = "{\"action\":\"text\",\"text\":\"Ciao ragazzi, parliamo di musica"
    val body   = geminiOutput(raw).deepMerge(Json.obj("status" -> Json.fromString("incomplete")))
    val client = Client.fromHttpApp(
      HttpApp[IO] { request =>
        if request.uri.host.map(_.value).contains(geminiHost) then IO.pure(Response[IO](Status.Ok).withEntity(body))
        else IO.pure(Response[IO](Status.Ok).withEntity(openRouterSuccess("bua")))
      }
    )
    LlmClients.build[IO](client, config("", "primary-provider = gemini\n")).flatMap(_.complete(prompt)).map {
      completion =>
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson, ProviderResult.Success))
        assertEquals(completion.reply.map(_.text), Some("bua"))
    }
  }

  test("Gemini and OpenRouter usage is read from the response, absent counts stay absent") {
    val gemini = Json.obj(
      "usage" -> Json.obj(
        "total_input_tokens"   -> Json.fromInt(100),
        "total_output_tokens"  -> Json.fromInt(7),
        "total_thought_tokens" -> Json.fromInt(3),
        "total_cached_tokens"  -> Json.fromInt(64)
      )
    )
    val openRouter = Json.obj(
      "usage" -> Json.obj(
        "prompt_tokens"         -> Json.fromInt(90),
        "completion_tokens"     -> Json.fromInt(5),
        "prompt_tokens_details" -> Json.obj("cached_tokens" -> Json.fromInt(32))
      )
    )
    assertEquals(GeminiClient.usageOf(gemini), TokenUsage.of(Some(100), Some(7 + 3), Some(64)))
    assertEquals(OpenRouterClient.usageOf(openRouter), TokenUsage.of(Some(90), Some(5), Some(32)))
    assertEquals(
      OpenRouterClient.usageOf(Json.obj("usage" -> Json.obj("prompt_tokens" -> Json.fromInt(9)))),
      TokenUsage.of(Some(9), None, None)
    )
    assertEquals(GeminiClient.usageOf(Json.obj()), None)
    assertEquals(OpenRouterClient.usageOf(Json.obj()), None)
  }

  private def withGeminiUsage(body: Json, prompt: Int, candidates: Int, cached: Int): Json =
    body.deepMerge(
      Json.obj(
        "usage" -> Json.obj(
          "total_input_tokens"  -> Json.fromInt(prompt),
          "total_output_tokens" -> Json.fromInt(candidates),
          "total_cached_tokens" -> Json.fromInt(cached)
        )
      )
    )

  private def withOpenRouterUsage(body: Json, prompt: Int, completion: Int): Json =
    body.deepMerge(
      Json.obj(
        "usage" -> Json.obj("prompt_tokens" -> Json.fromInt(prompt), "completion_tokens" -> Json.fromInt(completion))
      )
    )

  test("usage of a stubbed 200 reaches the attempt for a parsed reply, and is absent when not reported") {
    val gemini = withGeminiUsage(body = geminiSuccess("ciao"), prompt = 100, candidates = 7, cached = 64)
    for {
      reported <- completeWith(
        chosen = LlmProvider.Gemini,
        gemini = (Status.Ok, gemini),
        openRouter = (Status.Ok, openRouterSuccess("x")),
        hosts = ListBuffer.empty,
        bodies = ListBuffer.empty
      )
      plain <- completeWith(
        chosen = LlmProvider.Gemini,
        gemini = (Status.Ok, geminiSuccess("ciao")),
        openRouter = (Status.Ok, openRouterSuccess("x")),
        hosts = ListBuffer.empty,
        bodies = ListBuffer.empty
      )
    } yield {
      assertEquals(reported.attempts.map(_.usage), List(TokenUsage.of(Some(100), Some(7), Some(64))))
      assertEquals(LlmInteraction.reportedUsage(reported), TokenUsage.of(Some(100), Some(7), Some(64)))
      assertEquals(plain.attempts.map(_.usage), List(None))
      assertEquals(LlmInteraction.reportedUsage(plain), None)
    }
  }

  test("a primary that bills tokens but returns unreadable output still counts, on top of the fallback's usage") {
    // Gemini: MAX_TOKENS with no text (unreadable) but usage; OpenRouter answers with its own usage
    val unreadable = withGeminiUsage(
      body = Json.obj("status" -> Json.fromString("incomplete")),
      prompt = 100,
      candidates = 1024,
      cached = 50
    )
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.Ok, unreadable),
      openRouter = (Status.Ok, withOpenRouterUsage(body = openRouterSuccess("bua"), prompt = 90, completion = 5)),
      hosts = ListBuffer.empty,
      bodies = ListBuffer.empty
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson, ProviderResult.Success))
      assertEquals(
        completion.attempts.map(_.usage),
        List(
          TokenUsage.of(Some(100), Some(1024), Some(50)),
          TokenUsage.of(Some(90), Some(5), None)
        )
      )
      assertEquals(LlmInteraction.reportedUsage(completion), TokenUsage.of(Some(190), Some(1029), Some(50)))
    }
  }

  test("a malformed structured reply keeps the usage the provider reported for it") {
    val malformed = withGeminiUsage(
      body = geminiOutput("not json"),
      prompt = 10,
      candidates = 2,
      cached = 0
    )
    completeWith(
      chosen = LlmProvider.Gemini,
      gemini = (Status.Ok, malformed),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = ListBuffer.empty,
      bodies = ListBuffer.empty
    ).map(completion =>
      assertEquals(completion.attempts.headOption.flatMap(_.usage), TokenUsage.of(Some(10), Some(2), Some(0)))
    )
  }

  test("acceptResponse records the response usage on the attempt of its provider") {
    val usage    = TokenUsage.of(Some(5), Some(1), None)
    val response = ProviderResponse.success(LlmReply("ciao", LlmAction.Text)).copy(usage = usage)
    val accepted = Completion.acceptResponse(
      Completion.started("p", LlmProvider.Gemini),
      LlmProvider.Gemini,
      response
    )
    assertEquals(accepted.attempts.map(_.usage), List(usage))
  }

  test("Gemini's finish reason and token count diagnose a response without usable text") {
    val body = Json.obj(
      "status" -> Json.fromString("incomplete"),
      "usage"  -> Json.obj("total_output_tokens" -> Json.fromInt(1024))
    )
    assertEquals(GeminiClient.diagnose(body), Some("status=incomplete output_tokens=1024"))
    assertEquals(GeminiClient.diagnose(Json.obj()), None)
  }

  test("a non-JSON 200 response is treated as malformed JSON") {
    val hosts  = ListBuffer.empty[String]
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      hosts += request.uri.host.map(_.value).getOrElse("")
      IO.pure(Response[IO](Status.Ok).withEntity("not json"))
    })
    LlmClients.build[IO](client, config("", "primary-provider = gemini\n")).flatMap(_.complete(prompt)).map {
      completion =>
        assertEquals(completion.status, CompletionStatus.Failed)
        assertEquals(
          completion.attempts.flatMap(_.result),
          List(ProviderResult.MalformedJson, ProviderResult.MalformedJson)
        )
        // the full raw body survives for the malformed-reply error line, beyond the old 500-character cap
        assertEquals(completion.raw.map(_.text), Some("not json"))
        assertEquals(hosts.toList, List(geminiHost, openRouterHost))
    }
  }

  test("the model OpenRouter reports as the one that served the request is kept with the raw text") {
    val body   = openRouterSuccess("ciao").deepMerge(Json.obj("model" -> Json.fromString("vendor/picked:free")))
    val client = Client.fromHttpApp(HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok).withEntity(body))))
    LlmClients.build[IO](client, config("", "primary-provider = openrouter\n")).flatMap(_.complete(prompt)).map {
      completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.raw.flatMap(_.model), Some("vendor/picked:free"))
        assertEquals(completion.raw.map(_.provider), Some(LlmProvider.OpenRouter))
    }
  }

  test("the model of Gemini's response is kept, and a very long reported model is capped") {
    val gemini = geminiSuccess("ciao").deepMerge(Json.obj("model" -> Json.fromString("gemini-2.5-flash")))
    val long   = openRouterSuccess("ciao").deepMerge(Json.obj("model" -> Json.fromString("m" * 500)))
    def answer(body: Json, primary: String) =
      LlmClients
        .build[IO](
          Client.fromHttpApp(HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok).withEntity(body)))),
          config("", s"primary-provider = $primary\n")
        )
        .flatMap(_.complete(prompt))
    for {
      g <- answer(gemini, "gemini")
      o <- answer(long, "openrouter")
    } yield {
      assertEquals(g.raw.flatMap(_.model), Some("gemini-2.5-flash"))
      assertEquals(o.raw.flatMap(_.model).map(_.length), Some(ProviderHttp.MaxModelChars))
    }
  }

  test("a 429 from OpenRouter primary falls back to Gemini") {
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      chosen = LlmProvider.OpenRouter,
      gemini = (Status.Ok, geminiSuccess("bua")),
      openRouter = (Status.TooManyRequests, Json.obj()),
      hosts = hosts,
      bodies = bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua", LlmAction.Text)))
      assertEquals(completion.attempts.map(_.provider), List(LlmProvider.OpenRouter, LlmProvider.Gemini))
      assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.RateLimited, ProviderResult.Success))
    }
  }

  test("provider requests carry the configured output token limit") {
    val bodies = ListBuffer.empty[(String, String)]
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      request.as[String].flatMap { body =>
        val host = request.uri.host.map(_.value).getOrElse("")
        bodies += ((host, body))
        val response = host match {
          case `geminiHost`     => (Status.TooManyRequests, Json.obj())
          case `openRouterHost` => (Status.Ok, openRouterSuccess("ok"))
          case _                => (Status.NotFound, Json.obj())
        }
        IO.pure(Response[IO](response._1).withEntity(response._2))
      }
    })
    LlmClients
      .build[IO](client, config("primary-provider = gemini\nmax-output-tokens = 123\n"))
      .flatMap(_.complete(prompt))
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(bodies.map(_._1).toList, List(geminiHost, openRouterHost))
        assert(bodies.toList.head._2.contains("\"max_output_tokens\":123"))
        assert(bodies.toList.last._2.contains("\"max_tokens\":123"))
        // no model configured: the free router
        assert(bodies.toList.last._2.contains("\"model\":\"openrouter/free\""))
      }
  }

  test("a fallback attempt consumes the fallback provider's quota, not the primary's") {
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.TooManyRequests, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("ok")),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
    val cfg = config(
      "primary-provider = gemini\ngemini.quota.max-per-minute = 1\nopenrouter.quota.max-per-minute = 2\n"
    )
    for {
      notifier <- NotifierMock.recording
      llm      <- LlmClients.build[IO](client, cfg, notifier)
      first    <- llm.complete(prompt)
      second   <- llm.complete(prompt)
      infos    <- notifier.infos.get
    } yield {
      // first completion: primary + fallback, each admitted by its own quota
      assertEquals(first.status, CompletionStatus.Answered)
      assertEquals(first.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter))
      // second completion: the primary is exhausted, so the fallback (still has budget) is asked first
      assertEquals(second.status, CompletionStatus.Answered)
      assertEquals(second.attempts.map(_.provider), List(LlmProvider.OpenRouter))
      assertEquals(hosts.toList, List(geminiHost, openRouterHost, openRouterHost))
      assertEquals(infos, Nil)
    }
  }

  test("both providers exhausted stays silent, makes no call and notifies ops once") {
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.Ok, openRouterSuccess("bua")),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
    val cfg = config(
      "primary-provider = gemini\ngemini.quota.max-per-minute = 1\nopenrouter.quota.max-per-minute = 1\n"
    )
    for {
      notifier <- NotifierMock.recording
      llm      <- LlmClients.build[IO](client, cfg, notifier)
      first    <- llm.complete(prompt)
      second   <- llm.complete(prompt)
      third    <- llm.complete(prompt)
      fourth   <- llm.complete(prompt)
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Answered)  // gemini's one call
      assertEquals(second.status, CompletionStatus.Answered) // gemini exhausted -> openrouter's one call
      assertEquals(third.status, CompletionStatus.Silent)
      assertEquals(third.attempts, Nil)
      assertEquals(fourth.status, CompletionStatus.Silent)
      // no extra HTTP call for either exhausted completion
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      assertEquals(infos.size, 1)
    }
  }

  test("a retryable primary failure with an exhausted fallback quota fails without a fallback call") {
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.TooManyRequests, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("ok")),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
    val cfg = config(
      "primary-provider = gemini\ngemini.quota.max-per-minute = 2\nopenrouter.quota.max-per-minute = 1\n"
    )
    for {
      notifier <- NotifierMock.recording
      llm      <- LlmClients.build[IO](client, cfg, notifier)
      first    <- llm.complete(prompt) // gemini 429 -> openrouter answers, using its only slot
      second   <- llm.complete(prompt) // gemini 429 again, but the fallback has no quota left
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Answered)
      assertEquals(second.status, CompletionStatus.Failed)
      // only the call that really happened is recorded; the pending fallback attempt is dropped
      assertEquals(
        second.attempts.map(a => (a.provider, a.result)),
        List((LlmProvider.Gemini, Some(ProviderResult.RateLimited)))
      )
      assertEquals(hosts.toList, List(geminiHost, openRouterHost, geminiHost))
      // the fallback's exhausted quota is still a no-reply the operators must see, even though the completion is Failed
      assertEquals(infos.size, 1)
      assert(infos.head.toString.contains("quota exhausted"))
    }
  }

  test("a retryable failure from a fallback-first completion fails with the single fallback attempt recorded") {
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.TooManyRequests, Json.obj()),
      hosts = hosts,
      bodies = ListBuffer.empty[String]
    )
    val cfg = config("primary-provider = gemini\ngemini.quota.max-per-minute = 1\n")
    for {
      notifier <- NotifierMock.recording
      llm      <- LlmClients.build[IO](client, cfg, notifier)
      first    <- llm.complete(prompt) // gemini uses its only slot
      second   <- llm.complete(prompt) // gemini exhausted -> fallback asked first, and 429s
      infos    <- notifier.infos.get
    } yield {
      assertEquals(first.status, CompletionStatus.Answered)
      assertEquals(second.status, CompletionStatus.Failed)
      assertEquals(second.reply, None)
      assertEquals(
        second.attempts.map(a => (a.provider, a.result)),
        List((LlmProvider.OpenRouter, Some(ProviderResult.RateLimited)))
      )
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      assertEquals(infos, Nil)
    }
  }

  test("a primary timeout falls back to a successful reply") {
    // obligation: rule-success.RetryableProviderFailureFallsBack
    // obligation: rule-success.ProviderReturnsStructuredReply
    val hosts  = ListBuffer.empty[String]
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      val host = request.uri.host.map(_.value).getOrElse("")
      hosts += host
      host match {
        case `geminiHost` =>
          IO.sleep(200.millis).as(Response[IO](Status.TooManyRequests).withEntity(Json.obj()))
        case `openRouterHost` =>
          IO.pure(Response[IO](Status.Ok).withEntity(openRouterSuccess("ok")))
        case _ =>
          IO.pure(Response[IO](Status.NotFound).withEntity(Json.obj()))
      }
    })
    val cfg = config("primary-provider = gemini\nprovider-timeout = 10 milliseconds\ntimeout = 5 seconds\n")
    for {
      notifier   <- NotifierMock.recording
      llm        <- LlmClients.build[IO](client, cfg, notifier)
      completion <- llm.complete(prompt)
      errors     <- notifier.errors.get
    } yield {
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("ok", LlmAction.Text)))
      assertEquals(
        completion.attempts.map(attempt => (attempt.provider, attempt.result)),
        List(
          (LlmProvider.Gemini, Some(ProviderResult.Timeout)),
          (LlmProvider.OpenRouter, Some(ProviderResult.Success))
        )
      )
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      assertEquals(errors, Nil)
    }
  }

  test("a hanging primary leaves the rest of the answer budget to the fallback (#1587)") {
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      request.uri.host.map(_.value).getOrElse("") match {
        case `geminiHost`     => IO.never
        case `openRouterHost` =>
          IO.sleep(10.millis).as(Response[IO](Status.Ok).withEntity(openRouterSuccess("ok")))
        case _ => IO.pure(Response[IO](Status.NotFound).withEntity(Json.obj()))
      }
    })
    val cfg = config("primary-provider = gemini\nprovider-timeout = 50 milliseconds\ntimeout = 5 seconds\n")
    for {
      llm        <- LlmClients.build[IO](client, cfg)
      completion <- llm.complete(prompt).timeout(cfg.timeout)
    } yield {
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(
        completion.attempts.map(a => (a.provider, a.result)),
        List((LlmProvider.Gemini, Some(ProviderResult.Timeout)), (LlmProvider.OpenRouter, Some(ProviderResult.Success)))
      )
    }
  }

  test("the whole-answer timeout cancels the call when both providers hang (#1587)") {
    val client = Client.fromHttpApp(HttpApp[IO](_ => IO.never))
    val cfg    = config("primary-provider = gemini\nprovider-timeout = 200 milliseconds\ntimeout = 300 milliseconds\n")
    for {
      llm    <- LlmClients.build[IO](client, cfg)
      result <- llm.complete(prompt).timeout(cfg.timeout).attempt
    } yield assert(clue(result).left.exists(_.isInstanceOf[java.util.concurrent.TimeoutException]))
  }

  test("a timeout after a primary 429 fails with both attempts recorded") {
    // obligation: rule-success.RetryableProviderFailureFallsBack
    // obligation: rule-success.CompletionFailsAfterFallbackExhausted
    val hosts  = ListBuffer.empty[String]
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      val host = request.uri.host.map(_.value).getOrElse("")
      hosts += host
      host match {
        case `geminiHost` =>
          IO.pure(Response[IO](Status.TooManyRequests).withEntity(Json.obj()))
        case `openRouterHost` =>
          IO.sleep(1.second).as(Response[IO](Status.Ok).withEntity(openRouterSuccess("too late")))
        case _ =>
          IO.pure(Response[IO](Status.NotFound).withEntity(Json.obj()))
      }
    })
    // wide enough for the 429 (its error body is read and parsed) to finish on a slow runner, far below the fallback
    val cfg = config("primary-provider = gemini\nprovider-timeout = 250 milliseconds\ntimeout = 5 seconds\n")
    for {
      notifier   <- NotifierMock.recording
      llm        <- LlmClients.build[IO](client, cfg, notifier)
      completion <- llm.complete(prompt)
      errors     <- notifier.errors.get
    } yield {
      assertEquals(completion.status, CompletionStatus.Failed)
      assertEquals(completion.reply, None)
      assertEquals(
        completion.attempts.map(attempt => (attempt.provider, attempt.result)),
        List(
          (LlmProvider.Gemini, Some(ProviderResult.RateLimited)),
          (LlmProvider.OpenRouter, Some(ProviderResult.Timeout))
        )
      )
      // the fallback was really called; a timeout is a recorded result, not an unreported error
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
      assertEquals(errors, Nil)
    }
  }

  test("provider requests include the configured auth headers") {
    import org.typelevel.ci.CIString
    val geminiHeaders     = ListBuffer.empty[Option[String]]
    val openRouterHeaders = ListBuffer.empty[Option[String]]
    val client            = Client.fromHttpApp(HttpApp[IO] { request =>
      request.as[String].flatMap { _ =>
        val host = request.uri.host.map(_.value).getOrElse("")
        host match {
          case `geminiHost` =>
            geminiHeaders += request.headers.get(CIString("x-goog-api-key")).map(_.head.value)
            IO.pure(Response[IO](Status.TooManyRequests).withEntity(Json.obj()))
          case `openRouterHost` =>
            openRouterHeaders += request.headers.get(CIString("Authorization")).map(_.head.value)
            IO.pure(Response[IO](Status.Ok).withEntity(openRouterSuccess("ok")))
          case _ => IO.pure(Response[IO](Status.NotFound).withEntity(Json.obj()))
        }
      }
    })
    val cfg = ConfigSource
      .string(
        s"""primary-provider = gemini
           |gemini { endpoint = "https://$geminiHost/generate", api-key = "g-key" }
           |openrouter { endpoint = "https://$openRouterHost/chat", api-key = "or-key" }
           |context.author-salt = test-only-salt""".stripMargin
      )
      .load[LlmConfig]
      .fold(err => throw new RuntimeException(err.prettyPrint()), identity)
    LlmClients.build[IO](client, cfg).flatMap(_.complete(prompt)).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(geminiHeaders.toList, List(Some("g-key")))
      assertEquals(openRouterHeaders.toList, List(Some("Bearer or-key")))
    }
  }

  test("structured reply: action is required, text is an optional string, reuse is rejected") {
    assertEquals(StructuredReply.parse("""{"action":"silent"}"""), Right(LlmReply("", LlmAction.Silent)))
    assert(StructuredReply.parse("""{"action":"reuse","replyId":"r1"}""").isLeft)
    assertEquals(
      StructuredReply.parse("""{"action":" Text ","text":"ciao"}"""),
      Right(LlmReply("ciao", LlmAction.Text))
    )
    assert(StructuredReply.parse("""{"text":"ciao"}""").isLeft)
    assert(StructuredReply.parse("""{"action":"shout","text":"x"}""").isLeft)
    assert(StructuredReply.parse("""{"action":"text","text":5}""").isLeft)
    assert(StructuredReply.parse("[1]").isLeft)
  }

  test("the Gemini schema asks for action (text or silent) and text, and requires only action") {
    val schema = GeminiClient.responseSchema.hcursor
    assertEquals(schema.downField("properties").keys.map(_.toSet), Some(Set("text", "action")))
    assertEquals(
      schema.downField("properties").downField("action").get[List[String]]("enum"),
      Right(List("text", "silent"))
    )
    assertEquals(schema.get[List[String]]("required"), Right(List("action")))
  }

  test("Gemini text is read from the model_output steps only: thoughts and non-text items give nothing, text joins") {
    def text(s: String)      = Json.obj("type" -> Json.fromString("text"), "text" -> Json.fromString(s))
    def output(items: Json*) =
      Json.obj("type" -> Json.fromString("model_output"), "content" -> Json.arr(items*))
    def steps(ss: Json*) = Json.obj("steps" -> Json.arr(ss*))
    assertEquals(GeminiClient.textOf(steps(Json.obj("type" -> Json.fromString("thought")))), None)
    assertEquals(GeminiClient.textOf(steps(output(Json.obj("type" -> Json.fromString("image"))))), None)
    assertEquals(GeminiClient.textOf(steps(output(text("a"), text("b")), output(text("c")))), Some("abc"))
    assertEquals(GeminiClient.textOf(Json.obj()), None)
  }

  // The example Interaction of Google's API reference (aistudio.google.com/api-reference/interactions-api), ids and
  // empty fields trimmed: an independent source for the response shape, unlike the fixtures built in this spec.
  private val documentedInteraction: Json = io.circe.parser
    .parse("""{
             |  "object": "interaction",
             |  "id": "v1_example",
             |  "model": "gemini-3.8-flash",
             |  "status": "completed",
             |  "steps": [
             |    {"type": "model_output", "content": [
             |      {"type": "text", "text": "Hello! I'm doing well, functioning as expected. Thank you for asking! How are you doing today?"}
             |    ]}
             |  ],
             |  "usage": {
             |    "total_cached_tokens": 0, "total_input_tokens": 7, "total_output_tokens": 23,
             |    "total_thought_tokens": 49, "total_tokens": 79, "total_tool_use_tokens": 0
             |  }
             |}""".stripMargin)
    .fold(throw _, identity)

  test("the documented Interaction response is read: text, usage with thoughts as output, status") {
    assertEquals(
      GeminiClient.textOf(documentedInteraction),
      Some("Hello! I'm doing well, functioning as expected. Thank you for asking! How are you doing today?")
    )
    assertEquals(GeminiClient.usageOf(documentedInteraction), TokenUsage.of(Some(7), Some(23 + 49), Some(0)))
    assertEquals(GeminiClient.diagnose(documentedInteraction), Some("status=completed output_tokens=23"))
    assertEquals(documentedInteraction.hcursor.get[String]("model"), Right("gemini-3.8-flash"))
  }

  test("the Gemini model is the configured one, and the default when it is empty or blank") {
    def conn(model: Option[String]) = ProviderConnection(endpoint = "https://g.example", model = model)
    assertEquals(GeminiClient.modelFor(conn(None)), "gemini-flash-lite-latest")
    assertEquals(GeminiClient.modelFor(conn(Some("  "))), "gemini-flash-lite-latest")
    assertEquals(GeminiClient.modelFor(conn(Some(" gemini-x "))), "gemini-x")
  }

  test("the OpenRouter model is the configured one, and the free router when it is empty or blank") {
    def conn(model: Option[String]) = ProviderConnection(endpoint = "https://o.example", model = model)
    assertEquals(OpenRouterClient.modelFor(conn(None)), "openrouter/free")
    assertEquals(OpenRouterClient.modelFor(conn(Some(""))), "openrouter/free")
    assertEquals(OpenRouterClient.modelFor(conn(Some("   "))), "openrouter/free")
    assertEquals(OpenRouterClient.modelFor(conn(Some(" vendor/model:free "))), "vendor/model:free")
  }

  test("a configured model that is not free is reported at startup, the free ones are not") {
    def warn(model: Option[String]) =
      OpenRouterClient.paidModelWarning(ProviderConnection(endpoint = "https://o.example", model = model))
    assertEquals(warn(None), None)
    assertEquals(warn(Some("")), None)
    assertEquals(warn(Some("openrouter/free")), None)
    assertEquals(warn(Some("vendor/model:free")), None)
    assert(warn(Some("google/gemini-2.5-flash")).exists(_.contains("google/gemini-2.5-flash")))
    assert(warn(Some("vendor/model:free-ish")).isDefined)
  }
}
