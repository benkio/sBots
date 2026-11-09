# Generate Calendar

## Goal

Regenerate the `<id>_calendar.ics` file of every bot (yearly events built from the shows JSON).

## Command

```bash
sbt generateCalendar
```

## Notes

- Output: `modules/bots/<BotName>/<id>_calendar.ics`, committed in the repo.
- The file is the one linked by `/showcalendar`; the daily `/loadcalendar` subscription does not read it, it
  computes the shows of the day with `ShowCalendar.forDay` at fire time.
- Regenerating without new shows leaves the files untouched (the `DTSTAMP` is derived from the data).
- Run it after adding/updating shows (it is also part of the `sbt fix` alias).
