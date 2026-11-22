# /triggersearch

## IT

### Descrizione

Controlla se il testo inserito corrisponde a uno dei trigger automatici del bot.

### Come usarlo

- Sintassi: `/triggersearch <testo>`
- Il testo puo essere una parola o una frase.

### Esempi

- `/triggersearch ciao`
- `/triggersearch bella domanda`

### Note

- I trigger possono essere testuali o regex.
- Se non c'e una corrispondenza esatta, il bot cerca il trigger piu vicino (correzione automatica solo se l'affidabilita e alta); altrimenti propone fino a 5 suggerimenti numerati come pulsanti (una sola pagina, senza paginazione).
- Premendo il pulsante di un suggerimento si ottiene la stessa risposta di `/triggersearch <trigger>`; su Discord i suggerimenti sono righe numerate con pulsanti, sulle piattaforme senza pulsanti una sola riga di testo.
- Se non ci sono corrispondenze, il bot lo segnala esplicitamente.

## EN

### Description

Check whether the provided text matches one of the bot automatic triggers.

### How to use

- Syntax: `/triggersearch <text>`
- Text can be a single keyword or a full sentence.

### Examples

- `/triggersearch hello`
- `/triggersearch nice question`

### Notes

- Triggers may be plain text or regex-based.
- If there is no exact match, the bot looks for the closest trigger (auto-correction only when confidence is high); otherwise it suggests up to 5 numbered triggers as buttons on a single page (no pagination).
- Pressing a suggestion button gives the same reply as `/triggersearch <trigger>`; on Discord the suggestions are numbered lines with buttons, on platforms without buttons a single text line.
- If there is no match, the bot explicitly reports it.
