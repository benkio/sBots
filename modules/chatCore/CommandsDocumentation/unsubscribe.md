# /unsubscribe

## IT

### Descrizione

Rimuove sottoscrizioni della chat corrente.

### Come usarlo

- Rimuovi una sottoscrizione specifica: `/unsubscribe <uuid>`
- Rimuovi tutte le sottoscrizioni di un tipo: `/unsubscribe <tipo>` dove `<tipo>` è `show`, `trigger` o `calendar`
- Rimuovi tutte le sottoscrizioni della chat: `/unsubscribe`

### Esempi

- Singola sottoscrizione: `/unsubscribe b674cce0-9684-4d31-8cc7-9e2a41ea0878`
- Tutte le sottoscrizioni `show`: `/unsubscribe show`
- Tutte le sottoscrizioni: `/unsubscribe`

### Note

- L'ID di una sottoscrizione si trova nella risposta di `/subscribe` e in `/subscriptions`.
- Se non esiste nessuna sottoscrizione del tipo indicato, il comando non fa nulla.
- Il comando agisce solo sulle sottoscrizioni di questo bot nella chat corrente: le sottoscrizioni degli altri bot della stessa chat non vengono toccate.

## EN

### Description

Remove subscriptions for the current chat.

### How to use

- Remove a specific subscription: `/unsubscribe <uuid>`
- Remove all the subscriptions of a type: `/unsubscribe <type>` where `<type>` is `show`, `trigger` or `calendar`
- Remove all subscriptions for this chat: `/unsubscribe`

### Examples

- Single subscription: `/unsubscribe b674cce0-9684-4d31-8cc7-9e2a41ea0878`
- All `show` subscriptions: `/unsubscribe show`
- All subscriptions: `/unsubscribe`

### Notes

- The ID of a subscription is in the reply of `/subscribe` and in `/subscriptions`.
- If there is no subscription of the given type, the command does nothing.
- The command only affects the subscriptions of this bot in the current chat: the subscriptions of the other bots of the same chat are left untouched.
