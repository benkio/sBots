package com.benkio.chatllm.llm

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop

class LlmLawsSpec extends ScalaCheckSuite {

  private val prompt = "ciao bot"

  private val providerGen: Gen[LlmProvider] = Gen.oneOf(LlmProvider.values.toList)

  private val responseGen: Gen[ProviderResponse] =
    Gen.oneOf(
      Gen.alphaStr.map(text => ProviderResponse.success(LlmReply(text))),
      Gen.const(ProviderResponse.rateLimited),
      Gen.const(ProviderResponse.serverError),
      Gen.const(ProviderResponse.timeout),
      Gen.const(ProviderResponse.rejected),
      Gen.const(ProviderResponse.malformedJson),
      Gen.const(ProviderResponse(ProviderResult.Success, None))
    )

  private val responsesGen: Gen[List[(LlmProvider, ProviderResponse)]] =
    Gen.listOfN(20, Gen.zip(providerGen, responseGen))

  property("completion laws hold for any response sequence") {
    Prop.forAll(responsesGen) { responses =>
      val started = Completion.started(prompt, LlmProvider.Gemini)
      val finals  = responses.scanLeft(started) { case (completion, (provider, response)) =>
        Completion.acceptResponse(completion, provider, response)
      }

      finals.forall { completion =>
        val providers         = completion.attempts.map(_.provider)
        val uniqueAttempts    = providers.distinct.size == providers.size
        val replyOnlyAnswered = completion.reply.isEmpty || completion.status == CompletionStatus.Answered
        val atMostTwoAttempts = completion.attempts.size <= 2
        uniqueAttempts && replyOnlyAnswered && atMostTwoAttempts
      }
    }
  }

  property("terminal completions ignore further responses") {
    Prop.forAll(responsesGen) { responses =>
      val started  = Completion.started(prompt, LlmProvider.Gemini)
      val terminal =
        responses
          .scanLeft(started) { case (completion, (provider, response)) =>
            Completion.acceptResponse(completion, provider, response)
          }
          .find(_.status != CompletionStatus.Pending)
      terminal.forall { t =>
        responses.forall { case (provider, response) =>
          Completion.acceptResponse(t, provider, response) == t
        }
      }
    }
  }

  property("no more than one fallback attempt is ever created") {
    Prop.forAll(responsesGen) { responses =>
      val started = Completion.started(prompt, LlmProvider.Gemini)
      val finals  = responses.scanLeft(started) { case (completion, (provider, response)) =>
        Completion.acceptResponse(completion, provider, response)
      }
      finals.forall(_.attempts.count(_.provider == started.fallbackProvider) <= 1)
    }
  }
}
