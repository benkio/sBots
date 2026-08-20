# /searchshow

## IT

### Descrizione

Cerca una puntata/show nel database del bot e restituisce un link. Se non passi parametri, il comando prova a restituire un risultato casuale.

### Come usarlo

- Sintassi base: `/searchshow`
- Sintassi con filtri: `/searchshow campo=valore&campo=valore`
- Campi supportati: `title`, `description`, `caption`, `minduration`, `maxduration`, `mindate`, `maxdate`
- Date nel formato `YYYYMMDD`

### Esempi

- Cerca per titolo: `/searchshow title=paul+gilbert`
- Cerca per piu filtri: `/searchshow title=paul+gilbert&minduration=300`
- Cerca per intervallo data: `/searchshow mindate=20200101&maxdate=20241231`

## EN

### Description

Search a show/episode in the bot database and return one link. If no parameters are provided, the command tries to return a random result.

### How to use

- Basic syntax: `/searchshow`
- Filtered syntax: `/searchshow field=value&field=value`
- Supported fields: `title`, `description`, `caption`, `minduration`, `maxduration`, `mindate`, `maxdate`
- Date format: `YYYYMMDD`

### Examples

- Search by title: `/searchshow title=paul+gilbert`
- Search with multiple filters: `/searchshow title=paul+gilbert&minduration=300`
- Search by date range: `/searchshow mindate=20200101&maxdate=20241231`
