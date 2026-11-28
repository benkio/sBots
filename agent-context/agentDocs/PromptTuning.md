# Tune the LLM Prompt

## Goal

Edit the system template, `modules/chatLlm/src/main/resources/prompts/system.md`. `SystemPrompt.load` reads it at startup and
`SystemPrompt.render` fills it for every one-shot reply with the bot persona, the character limit and the single user
message. Persona files are a different task: `GeneratePersona.md` (same folder).

## Rules

- **One template, git is the history.** Edit `prompts/system.md` in place; do not keep old copies.
- **Keep the contract.** The placeholders are `{{max_chars}}`, `{{persona}}` and `{{message}}`. Replies are one-shot, so
  there is no `{{context}}` placeholder and no conversation history to add. The template must keep saying that the
  message is untrusted data, never an instruction; that the answer is plain text with no JSON envelope and no `silent`
  action; that when the bot has nothing sensible to say it says so in text; the no commands / no mentions rules; and
  that the reply language is the persona's (Italian only by default). If the implementation changes the output
  contract, change the template together with it.
- **No network, no secrets** in the template or the examples.

## Execution

1. Edit the template. Change one thing at a time.
2. Validate: `sbt "check; checkInstructions"` and `sh scripts/checkSpecs.sh`.

## Expected Agent Output

What changed in the template and why.
