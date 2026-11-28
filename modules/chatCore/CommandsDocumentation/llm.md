# /llm

## IT

### Descrizione

Abilita o disabilita le risposte generate dal LLM per la chat corrente e questo bot. Una chat senza impostazione è
disattivata: l'LLM non risponde finché non si esegue `/llm on`. Le risposte sono a colpo singolo: il bot non ricorda la
conversazione e non invia lo storico al provider. L'impostazione è separata per chat e bot.

### Come usarlo

- Attiva: `/llm on`
- Disattiva: `/llm off`
- Mostra lo stato: `/llm status`
- Senza argomento o con un argomento non riconosciuto: mostra l'uso del comando

Su IRC il prefisso è `!`, per esempio `!llm status`.

### Esempi

- Attivare le risposte LLM: `/llm on`
- Verificare che siano attive: `/llm status`
- Disattivarle: `/llm off`

## EN

### Description

Enables or disables the LLM replies for the current chat and this bot. A chat with no setting is off: the LLM never
answers until `/llm on` is run. Replies are one-shot: the bot does not remember the conversation and does not send any
history to the provider. The switch is scoped to the chat and the bot.

### How to use

- Enable: `/llm on`
- Disable: `/llm off`
- Show the current state: `/llm status`
- With no argument or an unknown argument: shows the command usage

On IRC the prefix is `!`, for example `!llm status`.

### Examples

- Turn LLM replies on: `/llm on`
- Check that they are on: `/llm status`
- Turn them off: `/llm off`
