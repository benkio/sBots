# /subscriptions

## IT

### Descrizione

Mostra le sottoscrizioni della chat corrente e il loro stato.

### Come usarlo

- Sintassi: `/subscriptions`

### Cosa mostra

Una tabella con una riga per ogni sottoscrizione della chat corrente di questo bot:

- Tipo: `show`, `trigger` o `calendar`
- Query: il target della sottoscrizione. Se è vuoto è _random_ (uno show o una risposta casuale). Il calendario non ha query (`-`)
- Prossimo trigger: data e ora della prossima esecuzione, nell'ora del server. `⚠️ not scheduled` indica una sottoscrizione salvata ma non schedulata in memoria
- Cron: l'espressione cron
- ID: l'ID della sottoscrizione, da usare con `/unsubscribe <uuid>`

Se esistono sottoscrizioni schedulate in memoria ma non più salvate, vengono elencate sotto la tabella.

## EN

### Description

Show subscriptions for the current chat and their status.

### How to use

- Syntax: `/subscriptions`

### What it shows

A table with a row for each subscription of the current chat of this bot:

- Type: `show`, `trigger` or `calendar`
- Query: the target of the subscription. When empty it is _random_ (a random show or reply). The calendar has no query (`-`)
- Next trigger: date and time of the next execution, in server time. `⚠️ not scheduled` flags a subscription that is stored but not scheduled in memory
- Cron: the cron expression
- ID: the subscription ID, to use with `/unsubscribe <uuid>`

Subscriptions that are scheduled in memory but no longer stored are listed below the table.
