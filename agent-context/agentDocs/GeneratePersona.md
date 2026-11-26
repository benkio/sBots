# Generate Persona

## Goal

Create and review the LLM persona files for the 9 bots (issue #1490; the reply catalogue was removed by #1594). These files are read by the prompt builder in **chatLlm**; they are repo data, not runtime logic.

## Files

For every bot in `BotsRegistry` (the 9 modules under `modules/bots/`, excluding `_template`):

- `modules/bots/<Bot>/src/main/resources/<id>_persona.md`

`<id>` is the short bot id (`abar`, `orl`, `cala`, `mos`, `pino`, `rphjb`, `sgar`, `xah`, `ytai`).

## Sources

Use, in this order:

1. `<id>_replies.json` — the authoritative list of replies, a source of examples for the persona (the LLM never sees it at runtime).
2. `<id>_triggers.md` and `<id>_instructions.md` — human-readable trigger/command names.
3. `showCaptions/*.srt` — speech rhythm and recurring phrases (`it`, except `xah` = `en`).
4. `<id>_shows.json` — show descriptions for tone and topics.
5. `README.org` and optional Wikipedia text when available locally. Do **not** fetch network content as part of this task.

Calandro (`cala`) and M0sconi (`mos`) have **no SRT captions** and empty show catalogues: build their persona from description, instructions, commands and replies only.

## Persona format

`<id>_persona.md` is a long-form research persona in the bot's language (Italian for all bots except `xah`, whose researched persona is English). It may use numbered level-2 sections (e.g. `## 1. IDENTITÀ`, `## 3. STILE COMUNICATIVO`, `## 9. ESEMPI DI CONVERSAZIONE`, `## 3. COMMUNICATION STYLE`, `## 11. EXAMPLES`). It must contain, as `##` headings:

- a **description/identity** section — who the character is and how they talk;
- a **speech style** section — concrete voice and rhythm rules;
- a **few-shot examples** section — examples preferably taken from real triggers/replies;
- a **rules** section — accuracy, behaviour, reply language, no commands, no mentions/usernames, stay silent when there is nothing sensible to say.

The compact four-heading form (`## Descrizione`, `## Stile`, `## Esempi`, `## Regole`) is still accepted. Long personas are allowed, within the prompt budget: `sbt checkPersona` fails above `min(64 KiB, PromptBudget.personaMaxBytes)`, which is **40 960 bytes** with the default `LLM_MAX_INPUT_TOKENS` (18432) and `LLM_CONTEXT_MAX_TOKENS` (2048) (see "Sizing the prompt, the persona and the timeout together" in the README). The persona must also fit the smaller context window of the fallback model. Do not add role labels such as `assistant:` or `system:`, and do not put the `/llm` command in the persona.

## Execution

1. Read the sources above for the bot you are updating.
2. Write or refine `<id>_persona.md` by hand. This is the human-review part: make the voice recognisable, not generic.
3. Validate:
   ```bash
   sbt checkPersona
   ```

## Validation

- `sbt checkPersona` — structural check for every bot (also in `sbt verify` and CI).
- `sbt "check; checkInstructions; checkPersona"` — full read-only pre-PR check.
- `sh scripts/checkSpecs.sh` — Allium specs (no spec change is expected for this data-only task).

## Expected Agent Output

Report which persona files were created or refined, how Calandro/M0sconi were handled without captions, and the result of `sbt checkPersona`.
