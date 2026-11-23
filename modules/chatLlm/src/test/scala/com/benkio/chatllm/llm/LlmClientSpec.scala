package com.benkio.chatllm.llm

import cats.effect.IO
import com.benkio.chatcore.mocks.NotifierMock
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

  private def structured(text: String): Json = Json.obj("text" -> Json.fromString(text))

  private def geminiSuccess(text: String): Json =
    Json.obj(
      "candidates" -> Json.arr(
        Json.obj(
          "content" -> Json.obj(
            "parts" -> Json.arr(
              Json.obj(
                "text" -> Json.fromString(
                  Printer.noSpaces.print(structured(text))
                )
              )
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
    s"""gemini { endpoint = "https://$geminiHost/generate", model = "gemini-test", api-key = "test-gemini-key" }
       |openrouter { endpoint = "https://$openRouterHost/chat", model = "openrouter-test", api-key = "test-openrouter-key" }
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
      .build[IO](stub(gemini, openRouter, hosts, bodies), config("", s"primary-provider = ${primary(chosen)}\n"))
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
    assertEquals(LlmReply("ciao"), LlmReply("ciao"))
    assertNotEquals(LlmReply("ciao"), LlmReply("bua"))
    assertEquals(LlmReply("ciao").text, "ciao")
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
      _          <- IO(assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini)))
      _          <- IO(
        assertEquals(
          ChatLlm
            .responder[IO]
            .respond(
              com.benkio.chatcore.model.Message(
                messageId = None,
                date = 0L,
                chatId = com.benkio.chatcore.model.ChatId(com.benkio.chatcore.model.Platform.Telegram, "1"),
                chatType = "private"
              ),
              List.empty
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
      LlmProvider.Gemini,
      (Status.Ok, geminiSuccess("ciao")),
      (Status.Ok, openRouterSuccess("bua")),
      hosts,
      bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.reply, Some(LlmReply("ciao")))
        assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini))
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.Success))
        assertEquals(hosts.toList, List(geminiHost))
        assert(bodies.head.contains("responseSchema"), "the Gemini request did not ask for structured JSON")
        assert(bodies.head.contains(prompt), "the Gemini request did not carry the prompt")
      }
  }

  test("switching the config to OpenRouter makes OpenRouter the primary provider") {
    // obligation: surface-provides.LlmClientSurface
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      LlmProvider.OpenRouter,
      (Status.Ok, geminiSuccess("ciao")),
      (Status.Ok, openRouterSuccess("bua")),
      hosts,
      bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Answered)
        assertEquals(completion.reply, Some(LlmReply("bua")))
        assertEquals(completion.attempts.map(_.provider), List(LlmProvider.OpenRouter))
        assertEquals(hosts.toList, List(openRouterHost))
        assert(bodies.head.contains("response_format"), "the OpenRouter request did not ask for structured JSON")
      }
  }

  test("a 429 from Gemini falls back to OpenRouter") {
    // obligation: rule-success.RetryableProviderFailureFallsBack
    // obligation: rule-entity-creation.RetryableProviderFailureFallsBack.1
    // obligation: rule-failure.ProviderReturnsStructuredReply.2
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      LlmProvider.Gemini,
      (Status.TooManyRequests, Json.obj()),
      (Status.Ok, openRouterSuccess("bua")),
      hosts,
      bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua")))
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
      LlmProvider.Gemini,
      (Status.InternalServerError, Json.obj()),
      (Status.Ok, openRouterSuccess("bua")),
      hosts,
      bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua")))
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
      LlmProvider.Gemini,
      (Status.TooManyRequests, Json.obj()),
      (Status.ServiceUnavailable, Json.obj()),
      hosts,
      bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Failed)
      assertEquals(completion.reply, None)
      assertEquals(completion.attempts.map(_.provider), List(LlmProvider.Gemini, LlmProvider.OpenRouter))
      assertEquals(hosts.toList, List(geminiHost, openRouterHost))
    }
  }

  test("a rejected request fails without a fallback") {
    // obligation: rule-success.NonRetryableProviderFailureFails
    // obligation: rule-failure.NonRetryableProviderFailureFails.2
    // obligation: rule-failure.RetryableProviderFailureFallsBack.2
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted.2
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      LlmProvider.Gemini,
      (Status.BadRequest, Json.obj()),
      (Status.Ok, openRouterSuccess("bua")),
      hosts,
      bodies
    )
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Failed)
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.Rejected))
        assertEquals(hosts.toList, List(geminiHost))
      }
  }

  test("a malformed structured reply fails without a fallback") {
    // obligation: rule-success.MalformedProviderReplyFails
    // obligation: rule-failure.MalformedProviderReplyFails.2
    // obligation: rule-failure.ProviderReturnsStructuredReply.3
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(LlmProvider.Gemini, (Status.Ok, Json.obj()), (Status.Ok, openRouterSuccess("bua")), hosts, bodies)
      .map { completion =>
        assertEquals(completion.status, CompletionStatus.Failed)
        assertEquals(completion.reply, None)
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson))
        assertEquals(hosts.toList, List(geminiHost))
      }
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
      Some(LlmReply("ciao")),
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
      ProviderResponse.success(LlmReply("ciao"))
    )
    val failed = Completion.acceptResponse(started, LlmProvider.Gemini, ProviderResponse.malformedJson)

    assertEquals(answered.status, CompletionStatus.Answered)
    assertEquals(failed.status, CompletionStatus.Failed)
    assertEquals(
      Completion.acceptResponse(answered, LlmProvider.Gemini, ProviderResponse.success(LlmReply("bua"))),
      answered
    )
    assertEquals(
      Completion.acceptResponse(failed, LlmProvider.Gemini, ProviderResponse.success(LlmReply("bua"))),
      failed
    )
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
        assertEquals(completion.attempts.flatMap(_.result), List(ProviderResult.MalformedJson))
        assertEquals(hosts.toList, List(geminiHost))
    }
  }

  test("a 429 from OpenRouter primary falls back to Gemini") {
    val hosts  = ListBuffer.empty[String]
    val bodies = ListBuffer.empty[String]
    completeWith(
      LlmProvider.OpenRouter,
      (Status.Ok, geminiSuccess("bua")),
      (Status.TooManyRequests, Json.obj()),
      hosts,
      bodies
    ).map { completion =>
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("bua")))
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
        assert(bodies.toList.head._2.contains("\"maxOutputTokens\":123"))
        assert(bodies.toList.last._2.contains("\"max_tokens\":123"))
      }
  }

  test("a fallback attempt consumes the fallback provider's quota, not the primary's") {
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.TooManyRequests, Json.obj()),
      openRouter = (Status.Ok, openRouterSuccess("ok")),
      hosts,
      ListBuffer.empty[String]
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
      hosts,
      ListBuffer.empty[String]
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
      hosts,
      ListBuffer.empty[String]
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
      // this is a failure, not a silent exhaustion: no ops notice
      assertEquals(infos, Nil)
    }
  }

  test("a retryable failure from a fallback-first completion fails with the single fallback attempt recorded") {
    // obligation: rule-failure.CompletionFailsAfterFallbackExhausted
    val hosts  = ListBuffer.empty[String]
    val client = stub(
      gemini = (Status.Ok, geminiSuccess("ciao")),
      openRouter = (Status.TooManyRequests, Json.obj()),
      hosts,
      ListBuffer.empty[String]
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
    val cfg = config("primary-provider = gemini\ntimeout = 10 milliseconds\n")
    for {
      notifier   <- NotifierMock.recording
      llm        <- LlmClients.build[IO](client, cfg, notifier)
      completion <- llm.complete(prompt)
      errors     <- notifier.errors.get
    } yield {
      assertEquals(completion.status, CompletionStatus.Answered)
      assertEquals(completion.reply, Some(LlmReply("ok")))
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
          IO.sleep(200.millis).as(Response[IO](Status.Ok).withEntity(openRouterSuccess("too late")))
        case _ =>
          IO.pure(Response[IO](Status.NotFound).withEntity(Json.obj()))
      }
    })
    val cfg = config("primary-provider = gemini\ntimeout = 10 milliseconds\n")
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
           |gemini { endpoint = "https://$geminiHost/generate", model = "gemini-test", api-key = "g-key" }
           |openrouter { endpoint = "https://$openRouterHost/chat", model = "openrouter-test", api-key = "or-key" }
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
}
