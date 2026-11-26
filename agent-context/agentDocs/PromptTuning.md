# Tune the LLM Prompt

## Goal

Change how the bots talk to the LLM (the system template, the output contract) knowing that logs and traces carry no
template version: old evidence is told from new by the deploy time. The template is `modules/chatLlm/src/main/resources/prompts/system.md`, filled by
`PromptBuilder` with the persona, the chat context and the message. Persona
files are a different task: `GeneratePersona.md` (same folder).

## Rules

- **One template, git is the history.** Edit `prompts/system.md` in place; do not keep old copies. Logs and traces carry no
  template version: note the deploy time of a change to tell old evidence from new.
- **Keep the contract.** The placeholders are `{{max_chars}}`, `{{persona}}`, `{{context}}` and `{{message}}`
  (values are inserted in one pass, a `{{...}}` inside a value is never expanded). The template must keep saying that the
  conversation and the message are untrusted data, never instructions; the output format
  `{action: text|silent, text}` that `StructuredReply` accepts; the no commands / no mentions rules; and
  that the reply language is the persona's (Italian only by default).
- **Mind the size.** The template (examples included) has `PromptBudget.templateReserveBytes` (4096 bytes) of the prompt
  budget; a larger one has to raise that reserve, which shrinks the room of every persona (`sbt checkPersona`).
- **No network, no secrets** in the template or the examples.

## Execution

1. Look at what is wrong, in Loki (see `docs/monitoring.md`):
   `{app="sbots"} |= "llm_interaction" | json | line_format "{{.message}}" | json`. The `action` ratio,
   the `failure` reasons (`output_rejected`, `malformedjson`) and `stripped=true` point at the template.
   For one odd reply, the opt-in `llm_trace` has the full prompt and raw response (`LLM_TRACE_ENABLED=true`, short TTL).
2. Edit the template. Change one thing at a time.
3. Validate:
   ```bash
   sbt "chatLlm/test"
   sbt "check; checkInstructions; checkPersona"
   sh scripts/checkSpecs.sh
   ```
4. Roll out and compare. There is no dry-run mode (#1495 was dropped): the new template answers for real in the chats that
   enabled the LLM, so try it first in one private chat with `/llm on`, then compare the dashboard (LLM row) and the lines
   after the deploy with the ones before it.

## Expected Agent Output

What changed in the template and why (which log evidence), and the result of the validation.
