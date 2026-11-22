# /subscribe

## IT

### Descrizione

Crea una sottoscrizione periodica per la chat corrente. A ogni esecuzione pianificata il bot invia uno show (uno casuale, oppure quello indicato) oppure, con il tipo `trigger`, risponde a un testo come se la chat lo avesse scritto.

### Come usarlo

- Sintassi: `/subscribe <cron> [show [target]]` oppure `/subscribe <cron> trigger [testo]`
- Formato cron: 6 campi `secondi minuti ore giorno-del-mese mese giorno-della-settimana`
- Regola importante: tra `giorno-del-mese` e `giorno-della-settimana` uno dei due deve essere `?`
- Tipo: `show` (se omesso) oppure `trigger`
- Target (opzionale, solo per `show`): l'ID di uno show oppure una ricerca, come per `/searchshow`. Senza target viene inviato uno show casuale.
- Testo (opzionale, `trigger` solo): a ogni esecuzione il bot risponde come se la chat avesse scritto `<testo>`: viene inviata la risposta del trigger corrispondente, come per un messaggio normale. Il testo deve corrispondere a un trigger del bot, come per un messaggio normale (vedi `/triggerlist` e `/triggersearch`): altrimenti il comando viene rifiutato e non viene pianificato nulla. Senza testo viene inviata una risposta casuale del bot (se il bot non ne ha, il comando viene rifiutato). Il bot non risponde al messaggio (non esiste) e non applica il timeout della chat.

### Esempi

- Ogni minuto, show casuale: `/subscribe 0 * * ? * *`
- Ogni giorno alle 09:30, show casuale: `/subscribe 0 30 9 * * ?`
- Ogni lunedi alle 18:00: `/subscribe 0 0 18 ? * MON`
- Ogni anno l'11 settembre alle 09:30, la risposta al trigger "torri gemelle": `/subscribe 0 30 9 11 9 ? trigger torri gemelle`
- Ogni giorno alle 09:30, uno show che contiene "pino scotto": `/subscribe 0 30 9 * * ? show pino scotto`
- Ogni giorno alle 09:30, uno show preciso: `/subscribe 0 30 9 * * ? show <ID show>`
- Ogni giorno alle 09:30, la risposta al trigger "buongiorno": `/subscribe 0 30 9 * * ? trigger buongiorno`
- Ogni giorno alle 09:30, una risposta casuale del bot: `/subscribe 0 30 9 * * ? trigger`

### Procedura guidata (Telegram e Discord)

- `/subscribe` senza argomenti avvia la procedura guidata: il bot mostra un messaggio con pulsanti che viene aggiornato a ogni passaggio.
- Passaggi: tipo (`show` o `trigger`) → pianificazione (preset oppure un cron di 6 campi scritto direttamente) → target (testo libero oppure `random`) → conferma.
- `back` torna al passaggio precedente e cancella la scelta fatta; non è disponibile al primo passaggio. `cancel` è disponibile sempre e interrompe tutto.
- Ogni risposta o rifiuto fa ripartire il tempo di inattività: la bozza scade dopo 10 minuti, senza altri messaggi. Il primo messaggio indica il limite.
- Esiste una sola bozza per chat, bot e utente: un altro `/subscribe` senza argomenti la sostituisce. Utenti diversi nella stessa chat hanno bozze separate.
- Solo chi ha avviato la procedura può usare i pulsanti: gli altri ricevono una risposta privata e non modificano la bozza.
- Se troppe procedure sono aperte, non viene eliminata nessuna bozza: la richiesta viene rifiutata con il suggerimento di usare il comando in una riga oppure di riprovare più tardi.
- Un cron scritto a mano non valido chiude la procedura. Se la conferma fallisce, la bozza viene chiusa e il messaggio spiega il motivo e mostra il comando in una riga già pronto.
- La conferma crea la sottoscrizione esattamente come il comando in una riga, con lo stesso ID e la stessa prossima esecuzione.
- Su IRC non esiste la procedura guidata: `/subscribe` senza argomenti mostra il solito errore di utilizzo.

### Limiti

- Intervallo minimo tra due esecuzioni: 1 minuto. Un cron che scatta più spesso viene rifiutato.
- Massimo 50 sottoscrizioni per chat.
- Il target (o il testo del trigger) può essere lungo al massimo 200 caratteri.

### Note

- Dopo la creazione, il bot risponde con ID sottoscrizione e prossima esecuzione.
- Se l'ID indicato come target non è uno show del bot, viene usato come testo di ricerca.
- Usa `/subscriptions` per vedere le sottoscrizioni attive.
- Usa `/unsubscribe` per rimuoverle.

## EN

### Description

Create a periodic subscription for the current chat. On each scheduled execution the bot sends a show (a random one, or the one you chose) or, with the `trigger` type, replies to a text as if the chat had written it.

### How to use

- Syntax: `/subscribe <cron> [show [target]]` or `/subscribe <cron> trigger [text]`
- Cron format: 6 fields `seconds minutes hours day-of-month month day-of-week`
- Important rule: between `day-of-month` and `day-of-week`, one must be `?`
- Type: `show` (when omitted) or `trigger`
- Target (optional, `show` only): a show ID or a search query, as for `/searchshow`. Without a target a random show is sent.
- Text (optional, `trigger` only): on each execution the bot replies as if the chat had written `<text>`: the reply of the matching trigger is sent, as for a normal message. The text must match a trigger of the bot, as for a normal message (see `/triggerlist` and `/triggersearch`): otherwise the command is rejected and nothing is scheduled. Without text a random reply of the bot is sent (if the bot has none, the command is rejected). The bot does not reply to the message (there is none) and does not apply the chat timeout.

### Examples

- Every minute, random show: `/subscribe 0 * * ? * *`
- Every day at 09:30, random show: `/subscribe 0 30 9 * * ?`
- Every Monday at 18:00: `/subscribe 0 0 18 ? * MON`
- Every year on September 11 at 09:30, the reply to the trigger "torri gemelle": `/subscribe 0 30 9 11 9 ? trigger torri gemelle`
- Every day at 09:30, a show matching "pino scotto": `/subscribe 0 30 9 * * ? show pino scotto`
- Every day at 09:30, one specific show: `/subscribe 0 30 9 * * ? show <show ID>`
- Every day at 09:30, the reply to the trigger "buongiorno": `/subscribe 0 30 9 * * ? trigger buongiorno`
- Every day at 09:30, a random reply of the bot: `/subscribe 0 30 9 * * ? trigger`

### Guided setup (Telegram and Discord)

- Bare `/subscribe` starts a guided setup: the bot shows a message with buttons that is edited in place at every step.
- Steps: type (`show` or `trigger`) → schedule (a preset or a typed 6-field cron) → target (free text or `random`) → confirm.
- `back` goes to the previous step and clears the choice made there; it is not offered at the first step. `cancel` is always available and stops the setup.
- Every answer or rejection restarts the idle timer: a draft expires after 10 minutes, silently. The first prompt states the time limit.
- There is one draft per chat, bot and user: another bare `/subscribe` replaces it. Different users in the same chat have separate drafts.
- Only who started the setup can use its buttons: anyone else gets a private answer and cannot change the draft.
- When too many setups are open, no draft is evicted: the new one is declined, with the one-line command suggested and a hint to wait and retry.
- An invalid typed cron ends the setup. If confirmation fails, the draft is dropped and the reply explains why and shows the ready-to-run one-line command.
- Confirmation creates the subscription exactly as the one-line command does, with the same ID and next occurrence.
- On IRC there is no guided setup: bare `/subscribe` keeps the usual usage error.

### Limits

- Minimum interval between two executions: 1 minute. A cron that fires more often is rejected.
- At most 50 subscriptions per chat.
- The target (or the trigger text) can be at most 200 characters long.

### Notes

- After creation, the bot returns subscription ID and next execution time.
- If the ID given as target is not a show of the bot, it is used as a search text.
- Use `/subscriptions` to inspect active subscriptions.
- Use `/unsubscribe` to remove them.
