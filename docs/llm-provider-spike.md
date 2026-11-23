# LLM provider spike: Gemini free tier vs OpenRouter free models

Findings for issue #1483 (epic #1499). Status: **desk research only, nothing measured**. No API key was available, so
the quota, latency, Italian quality and structured-output checks are still open (see "Still to measure").
Sources were read on 2026-11-23; limits change often, so re-check before relying on a number.

## Gemini API free tier

- **Limits apply per project, not per API key**; the daily quota resets at midnight Pacific time. Exceeding any one of
  RPM, TPM or RPD returns a rate-limit error. Experimental and preview models have stricter limits.
- **Numbers per model are not recorded here.** The documentation page fetched did not give a usable free-tier table
  (the automated summary was inconsistent), so read the live values for the chosen model in the AI Studio rate-limit
  page for the project.
- **Data use (unpaid services)**, from the Gemini API additional terms:
  - Prompts, system instructions, cached content, files and responses are used to improve Google products and models.
  - Human reviewers may read and annotate inputs and outputs (disconnected from the account, key and project first).
  - Retention period for unpaid prompts is not stated.
  - "Do not submit sensitive, confidential, or personal information to the Unpaid Services."
  - Users must be 18+, and an API client must not be directed at or likely to be accessed by under-18s.
  - **In the EEA, Switzerland and the UK the paid-service terms apply, and an API client offered to users there may use
    only paid services.**

## OpenRouter free models

- Free model ids end in `:free`: **20 requests per minute**; per day **50** requests with fewer than 10 credits bought,
  **1,000** with at least 10 credits. A 429 can come from OpenRouter or from the upstream provider.
- A negative credit balance can break even free models.
- Data: logging/training is controlled by account settings, separately for paid and free models; opting out of training
  stops routing to providers that train. Each provider has its own retention policy (zero, safety-only, or longer and
  possibly training). The free-model policies themselves are not described on the page read.

## Implications for the bots

- The Gemini free-tier terms are the blocker to settle first: chat messages are personal data of third parties, humans
  may read them, and the terms forbid minors-facing clients and EEA/UK/CH users on the free tier. The bots are public
  Telegram/Discord/IRC bots, so the operator cannot guarantee either condition. The privacy note (#1498) must say so.
- The free daily caps are small for a bot in several chats, so the rate limiter (#1486) must count requests per
  provider and fail soft (no LLM reply) instead of erroring.
- OpenRouter at 50/day without credits is only a fallback; with 10 credits bought it is 1,000/day at no per-token cost.

## Provisional recommendation

**Conditional go, not a final one.** Keep Gemini free tier as the first provider and OpenRouter `:free` as fallback, as
the epic planned, but only if the owner accepts the data-use terms above (no guarantee on minors or EEA/UK/CH users).
If that is not acceptable, the alternative is a paid Gemini tier (paid terms apply) or OpenRouter with a provider that
does not train. The model is not chosen: it needs the measurements below.

## Still to measure (needs a key)

- Free-tier RPM/RPD of the candidate Gemini Flash-class model, and what happens at the limit.
- Latency (p50/p95) against the reply timeout of the bots.
- Quality of Italian replies in each bot persona.
- Structured-output (JSON schema) reliability over a few hundred calls, for Gemini and one OpenRouter `:free` model.
