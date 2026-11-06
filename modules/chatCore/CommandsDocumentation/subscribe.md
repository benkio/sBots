# /subscribe

## IT

### Descrizione

Crea una sottoscrizione periodica per la chat corrente. A ogni esecuzione pianificata il bot invia uno show: uno casuale, oppure quello indicato.

### Come usarlo

- Sintassi: `/subscribe <cron> [show [target]]`
- Formato cron: 6 campi `secondi minuti ore giorno-del-mese mese giorno-della-settimana`
- Regola importante: tra `giorno-del-mese` e `giorno-della-settimana` uno dei due deve essere `?`
- Tipo: per ora l'unico tipo supportato è `show` (se omesso vale `show`)
- Target (opzionale, solo per `show`): l'ID di uno show oppure una ricerca, come per `/searchshow`. Senza target viene inviato uno show casuale.

### Esempi

- Ogni minuto, show casuale: `/subscribe 0 * * ? * *`
- Ogni giorno alle 09:30, show casuale: `/subscribe 0 30 9 * * ?`
- Ogni lunedi alle 18:00: `/subscribe 0 0 18 ? * MON`
- Ogni giorno alle 09:30, uno show che contiene "pino scotto": `/subscribe 0 30 9 * * ? show pino scotto`
- Ogni giorno alle 09:30, uno show preciso: `/subscribe 0 30 9 * * ? show <ID show>`

### Limiti

- Intervallo minimo tra due esecuzioni: 1 minuto. Un cron che scatta più spesso viene rifiutato.
- Massimo 50 sottoscrizioni per chat.
- Il target può essere lungo al massimo 200 caratteri.

### Note

- Dopo la creazione, il bot risponde con ID sottoscrizione e prossima esecuzione.
- Se l'ID indicato come target non è uno show del bot, viene usato come testo di ricerca.
- Usa `/subscriptions` per vedere le sottoscrizioni attive.
- Usa `/unsubscribe` per rimuoverle.

## EN

### Description

Create a periodic subscription for the current chat. On each scheduled execution the bot sends a show: a random one, or the one you chose.

### How to use

- Syntax: `/subscribe <cron> [show [target]]`
- Cron format: 6 fields `seconds minutes hours day-of-month month day-of-week`
- Important rule: between `day-of-month` and `day-of-week`, one must be `?`
- Type: for now the only supported type is `show` (it is `show` when omitted)
- Target (optional, `show` only): a show ID or a search query, as for `/searchshow`. Without a target a random show is sent.

### Examples

- Every minute, random show: `/subscribe 0 * * ? * *`
- Every day at 09:30, random show: `/subscribe 0 30 9 * * ?`
- Every Monday at 18:00: `/subscribe 0 0 18 ? * MON`
- Every day at 09:30, a show matching "pino scotto": `/subscribe 0 30 9 * * ? show pino scotto`
- Every day at 09:30, one specific show: `/subscribe 0 30 9 * * ? show <show ID>`

### Limits

- Minimum interval between two executions: 1 minute. A cron that fires more often is rejected.
- At most 50 subscriptions per chat.
- The target can be at most 200 characters long.

### Notes

- After creation, the bot returns subscription ID and next execution time.
- If the ID given as target is not a show of the bot, it is used as a search text.
- Use `/subscriptions` to inspect active subscriptions.
- Use `/unsubscribe` to remove them.
