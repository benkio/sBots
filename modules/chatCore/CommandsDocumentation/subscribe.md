# /subscribe

## IT

### Descrizione

Crea una sottoscrizione periodica per la chat corrente. A ogni esecuzione pianificata il bot invia un contenuto casuale.

### Come usarlo

- Sintassi: `/subscribe <cron>`
- Formato cron: 6 campi `secondi minuti ore giorno-del-mese mese giorno-della-settimana`
- Regola importante: tra `giorno-del-mese` e `giorno-della-settimana` uno dei due deve essere `?`

### Esempi

- Ogni minuto: `/subscribe 0 * * ? * *`
- Ogni giorno alle 09:30: `/subscribe 0 30 9 * * ?`
- Ogni lunedi alle 18:00: `/subscribe 0 0 18 ? * MON`

### Note

- Dopo la creazione, il bot risponde con ID sottoscrizione e prossima esecuzione.
- Usa `/subscriptions` per vedere le sottoscrizioni attive.
- Usa `/unsubscribe` per rimuoverle.

## EN

### Description

Create a periodic subscription for the current chat. On each scheduled execution, the bot sends a random content.

### How to use

- Syntax: `/subscribe <cron>`
- Cron format: 6 fields `seconds minutes hours day-of-month month day-of-week`
- Important rule: between `day-of-month` and `day-of-week`, one must be `?`

### Examples

- Every minute: `/subscribe 0 * * ? * *`
- Every day at 09:30: `/subscribe 0 30 9 * * ?`
- Every Monday at 18:00: `/subscribe 0 0 18 ? * MON`

### Notes

- After creation, the bot returns subscription ID and next execution time.
- Use `/subscriptions` to inspect active subscriptions.
- Use `/unsubscribe` to remove them.
