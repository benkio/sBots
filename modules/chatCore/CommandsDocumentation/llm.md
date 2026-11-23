# /llm

## IT

### Descrizione

Abilita o disabilita le risposte generate dal LLM per la chat corrente e questo bot. Una chat senza impostazione è
disattivata: l'LLM non risponde finché non si esegue `/llm on`. Stato e contesto sono separati per chat e bot.

### Come usarlo

- Attiva: `/llm on`
- Disattiva: `/llm off`
- Mostra lo stato: `/llm status`
- Cancella il contesto ricordato: `/llm forget`
- Senza argomento o con un argomento non riconosciuto: mostra l'uso del comando

`forget` rimuove solo i messaggi ricordati: lo stato attivo/disattivo resta invariato. Su IRC il prefisso è `!`, per
esempio `!llm status`.

### Esempi

- Attivare le risposte LLM: `/llm on`
- Verificare che siano attive: `/llm status`
- Ricominciare una nuova conversazione senza cambiare l'impostazione: `/llm forget`

## EN

### Description

Enables or disables the LLM replies for the current chat and this bot. A chat with no setting is off: the LLM never
answers until `/llm on` is run. The switch and the remembered context are scoped to the chat and the bot.

### How to use

- Enable: `/llm on`
- Disable: `/llm off`
- Show the current state: `/llm status`
- Delete the remembered context: `/llm forget`
- With no argument or an unknown argument: shows the command usage

`forget` only drops the remembered messages: the on/off switch is left unchanged. On IRC the prefix is `!`, for example
`!llm status`.

### Examples

- Turn LLM replies on: `/llm on`
- Check that they are on: `/llm status`
- Start a fresh conversation without changing the setting: `/llm forget`
