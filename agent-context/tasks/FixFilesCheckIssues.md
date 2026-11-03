# Fix filesCheck Issues

## Goal

Run the `filesCheck` project, and fix every issue it reports on the bot media
files (stored in the synced Dropbox/Mega folders) so that files and
`<bot_id>_list.json` stay aligned. Produce a report for the developer to review.

Use this task when the developer asks to "fix filesCheck errors", "sanitize the
bot files", or similar. Related tasks: `FixFileName.md` (renames),
`DeleteFiles.md` (removals).

## Ground Rules

- **Human-needed => skip and report.** Anything that needs a judgment call,
  a missing credential, a remote login, or that cannot be verified: do NOT
  guess. Leave the file untouched and list it in the report under
  "Needs human review".
- **Ask only for input that is mandatory** (currently: the missing mp3 title).
  Batch all questions into one message when possible.
- **Cloud sync is the agent's duty.** Any change to a file in the Mega/Dropbox
  folder (new, replaced, renamed) is only done when the agent has also made sure
  the file is **fully uploaded** and that `<bot_id>_list.json` stores the
  **correct, verified share link** for it. A fix without verified upload + link
  is incomplete and must be reported as such (never as done). See E10.
- **Never overwrite originals without a backup.** Before any in-place
  transformation (`ffmpeg`, tag writes), copy the original to
  `~/sBots-filesCheck-backup/<bot_id>/<filename>` (create dir if missing). Keep
  the backup until the developer reviews the report.
- **Links must stay aligned with `<bot_id>_list.json`**: after any change that
  modifies a file's content or name, verify the `filename -> sources` pair
  (see "List alignment" below).
- Do not edit `filesCheck/src` to silence a check. Fix the data, not the checker
  (unless the developer asks for checker changes).
- Tools expected on PATH: `ffmpeg`, `ffprobe`, `mediainfo`, `id3v2`/`eyeD3`, `maestral` (Dropbox), MEGAcmd `mega-*` (Mega).
  If a tool is missing, report it and skip the dependent fixes.

## How filesCheck works (reference)

Entry: `filesCheck/src/index.ts`. Per bot in `filesCheck/src/Config.ts`
(`id`, `artist`, `filePath` relative to `$HOME`, `jsonFilePath`):

1. Lists the files in `$HOME/<filePath>` (non-recursive).
2. Loads `<bot_id>_list.json` (`filename`, `sources[]`, optional `mime`/`kinds`).
3. For each file, the **first** matching check in `Logic.ts#buildFileLogic` runs.
   No match => `Not Processed` warning.

Log level is `Warn`: only warnings/errors are printed (`✓` lines are hidden).

Note on a naming quirk: `MediaInfoService.checkAudioTrackMissing` returns
`true` when an audio track **exists** (inverted name); `gifLogic` relies on it.

## Run

```
cd filesCheck && npm install   # only if node_modules is missing
npm run start 2>&1 | tee /tmp/filesCheck-output.txt
```

Group the output by bot and by message type, then handle each case below.
After fixing, re-run until clean or only "Needs human review" items remain.

## Error Cases and Fix Steps

Filename patterns (from `Logic.ts`), with `<id>` = bot id, name `[A-Za-z0-9]+`:
`<id>_Name.mp3`, `<id>_Name.mp4` (video), `<id>_NameGif.mp4` (gif),
`<id>_Name.md`; `.jpg` and `.token` are ignored; `application.conf` and
directories are ignored.

### E1. `<file> is too long (max 64): N`

Cause: basename longer than 64 chars. Note this check runs first and stops
other checks for that file.

Steps:
1. Propose a shorter name preserving meaning and the `<id>_` prefix and
   extension suffix (`Gif.mp4`, `.mp4`, `.mp3`, `.md`). Siblings sharing the
   base name must be renamed together.
2. **The new name is a judgment call => ask the developer** (batch). If no
   answer, skip and report.
3. Apply via `FixFileName.md` (renames storage file, list.json, replies json,
   triggers md, inputTest.txt, regenerates/verifies source URL).

### E2. `<file> is not contained in the bot media list`

Cause: file exists on disk but there is no `filename` entry in
`<bot_id>_list.json`.

Steps:
1. Check for near-miss entries (typo/case/renamed sibling) in list.json:
   `grep -i "<base name>" modules/bots/<Bot>/<id>_list.json`.
2. If a near-miss entry exists => this is likely a rename mismatch: **human
   review** (do not auto-rename either side), report both names.
3. Otherwise the file is new/unreferenced. Make sure it is uploaded, create the
   share link following E10 (steps 2–4), then add:
   ```json
   {
     "filename" : "<name>",
     "sources" : [ "<url with dl=1 for Dropbox>" ],
     "mime" : "<only for gif: image/gif; md: text/markdown>"
   }
   ```
   keeping the file's existing ordering conventions (entries are grouped by base
   name). If the link cannot be created/verified, report it as incomplete.
4. Never delete the file as a "fix". Deletion goes through `DeleteFiles.md`
   and only on explicit developer request.

### E3. mp3: `doesn't contain a title`

Cause: ID3 tag `title` missing (artist is auto-fixed by the tool itself — it
writes `bot.artist`; see E4).

Steps:
1. Collect all mp3s of the bot lacking a title.
2. **Ask the developer for the title of each** (mandatory input; do not invent
   it from the filename). Offer a suggestion derived from the filename (split
   CamelCase) as a default they can accept.
3. Backup the file, then write the tag:
   `id3v2 -t "<title>" <file>` (or `eyeD3 -t "<title>" <file>`). Keep the artist
   tag as `bot.artist` from `Config.ts`.
4. Verify: `id3v2 -l <file>` shows TIT2 and TPE1.
5. File content changed => run "List alignment" (E10) for this file.

If no answer is given, skip and list in the report.

### E4. mp3: tags unreadable / `ID3 read failed: tags not found`

Cause: mp3 has no ID3 tag block at all (or it is corrupted). Note the checker
fails the file with an error rather than creating tags.

Steps:
1. Backup the file.
2. Create a tag block with artist = `bot.artist` and ask for the title as in E3.
   `id3v2 -a "<artist>" -t "<title>" <file>`
3. If the tag still can't be read afterwards, the audio may be corrupt:
   `ffprobe <file>`. If ffprobe fails => **human review** (do not re-encode
   blindly).
4. Run E10.

### E5. mp3: `Tag not updated: operation failed` (artist fix failed)

Steps:
1. Check file is writable (permissions, cloud sync lock, file not 0 bytes).
2. Retry once with `id3v2 -a "<artist>" <file>`.
3. If still failing => **human review**.

### E6. video (`.mp4`) `doesn't contain both audio and video tracks`

Cause: mediainfo shows missing audio and/or video track, or mediainfo itself
failed (an error line `checkAudioVideoTrackExists Error` precedes it).

Steps:
1. `mediainfo <file>` / `ffprobe -hide_banner <file>` to see actual tracks.
2. If mediainfo errored only (file plays fine in ffprobe with both streams):
   treat as tooling issue, report, no change.
3. If a track is truly missing, the content cannot be reconstructed
   => **human review** (maybe the file should be a Gif `<Name>Gif.mp4`, or the
   source needs replacing). Do not rename or re-encode on your own.

### E7. gif (`<Name>Gif.mp4`) `contains audio track`

Cause: gif files must be video-only.

Steps:
1. Backup, then strip audio without re-encoding the video:
   `ffmpeg -i <in> -c:v copy -an <tmp>.mp4 && mv <tmp>.mp4 <in>`
2. Verify with `mediainfo` that only a Video track remains and the file plays
   (`ffprobe`).
3. Re-check the size (E8) since stripping usually reduces it.
4. Run E10.

### E8. `is Bigger than 10MB` (video `.mp4` and Gif `.mp4`)

Cause: size >= 10 MB (`ByteSize.megabytes(10)` = 10,000,000 bytes, decimal; verified in effect).
Target: strictly below that, keeping the highest possible quality. Aim for
≤ ~9.5 MB for safety margin.

Steps:
1. Backup the original.
2. Read duration/streams: `ffprobe -v error -show_entries format=duration:stream=codec_type,codec_name,width,height,r_frame_rate -of json <file>`.
3. Try the least destructive option first and stop at the first that fits:
   1. **Remux only** (drops container overhead/metadata):
      `ffmpeg -i <in> -c copy -map_metadata -1 -movflags +faststart <tmp>.mp4`
   2. **Constrained-size re-encode (two-pass, size-targeted bitrate)**:
      - total_kbps = `(9.3 * 8000) / duration_seconds`; for videos reserve audio
        at 96–128 kbps (AAC), the rest to video; for gifs (no audio) all to video.
      - ```
        ffmpeg -y -i <in> -c:v libx264 -preset slow -b:v <vkbps>k -pass 1 -an -f null /dev/null
        ffmpeg -i <in> -c:v libx264 -preset slow -b:v <vkbps>k -pass 2 \
          -c:a aac -b:a 128k -movflags +faststart <tmp>.mp4      # gif: use -an
        ```
      - Keep the original resolution and frame rate; keep `-pix_fmt yuv420p`
        (Telegram compatibility).
   3. If the bitrate comes out unreasonably low (video < ~300 kbps at the
      original resolution) lower the resolution stepwise (e.g. 1080 -> 720 ->
      480 via `-vf scale=-2:<h>`), never below 480p, and only as far as needed.
   4. If still not under the limit at 480p => **human review** (suggest
      trimming the clip).
4. Verify: new size < 10 MB, `ffprobe` OK, audio/video tracks as expected for
   the file kind (video: both; gif: video only).
5. Replace the original with the new file (same name), keep the backup.
6. Record original size, new size, method and resolution/bitrate changes in the
   report.
7. Run E10 (content changed).

### E9. `.md` `is Bigger than 1MB`

Cause: markdown file above 1 MB.

Steps: **human review** (content decision). Report the file and size.

### E10. Upload + link alignment (agent duty, after ANY change in the cloud folder)

Requirement: every media file on disk is fully uploaded to its provider and has
exactly one `<id>_list.json` entry whose `sources` URL serves that very file.
Provider is inferred from the bot's `filePath` in `Config.ts` (`/Mega/...` or
`/Dropbox/...`) and from existing URLs (`mega.nz` / `dropbox.com`).

1. **Wait for the upload/sync to finish** (never link a half-synced file):
   - Mega: `mega-transfers` / `mega-sync` (MEGAcmd) must show no pending
     transfer for the file; confirm remote presence and size with
     `mega-ls -l /sBots/<Bot>/src/main/resources/<file>` (adapt to the remote
     path) equal to the local size.
   - Dropbox (Maestral): `maestral status` must show `Up to date` with 0 sync
     errors, `maestral filestatus <local file>` must return `up to date`, and
     `maestral ls -l /sBots/<Bot>/src/main/resources/<file>` must show the same
     size as local. If the daemon is stopped: `maestral start`.
   - If sync cannot be confirmed within a reasonable time => report as
     **not uploaded**; do not touch list.json.
2. **Get the link**:
   - Same-name in-place replacement:
     - Dropbox: the existing shared link stays valid; keep it (verify in step 3).
     - Mega: the old link may point to a stale node. Check the new node
       (`mega-export -a -f <remote path>` prints/creates the public link; use
       `mega-export <remote path>` to see an existing one). If the link differs
       from the stored one, use the new one.
   - New file or renamed file: create a new link.
     - Mega: `mega-export -a -f <remote path>`.
     - Dropbox (Maestral): first `maestral sharelink list <dropbox path>`; if a
       link exists reuse it, otherwise
       `maestral sharelink create /sBots/<Bot>/src/main/resources/<file>`.
       Dropbox path = local path relative to `~/Dropbox`. Store the URL in the
       existing list.json format: `...?rlkey=...&dl=1` (replace a trailing
       `dl=0` with `dl=1`; direct download).
     - If a tool is unavailable or not authenticated (`mega-whoami`,
       `maestral status`), ask the developer once to fix it (or to provide the
       share link). Without it, report as **link missing**.
   - Never reuse an old URL that points to an old path/name.
3. **Verify the link** serves the intended file:
   - `curl -sIL "<url>"` => HTTP 200, `content-length` equal to the local size
     (Dropbox `dl=1`), content type plausible. For Mega, `mega-ls`/`mega-export`
     of the link target or `mega-get` into a temp dir and compare size/hash with
     the local file.
   - Provider matches storage (no Mega link for a Dropbox bot and vice versa).
4. **Write list.json**: exactly one entry per `filename`; set/replace its
   `sources` with the verified URL (single URL); keep `mime`/`kinds` as is
   (gif: `image/gif`, md: `text/markdown`); keep the file's JSON formatting
   (2-space indent, ` : ` separators) and order of entries.
5. Check duplicates/orphans: no other entry uses the same `filename` or URL;
   no leftover entry for an old name after a rename.
6. Run `sbt "fix"` and the affected bot test suite (list.json is a resource
   read by the bots), and re-run `npm run start` in `filesCheck/`.
7. Record in the report, per file: provider, upload verified (size), old URL,
   new URL, verification result. If any step could not be completed, mark the
   file **INCOMPLETE** (what is missing) in "Needs human review".
8. Optional global scan (the checker doesn't do it): list.json entries whose
   file is missing on disk, and entries with empty/duplicated `sources` or
   `filename`. Report these as findings; deleting entries or missing files is
   **human review**.

### E11. `Gif is not supported`

Cause: a `.gif` file exists. The bot expects an mp4 gif named `<id>_NameGif.mp4`.

Steps:
1. Convert, silent and Telegram friendly:
   `ffmpeg -i <name>.gif -movflags +faststart -pix_fmt yuv420p -vf "scale=trunc(iw/2)*2:trunc(ih/2)*2" -an <id>_NameGif.mp4`
   (choose the `NameGif` name from the .gif basename; if the name is ambiguous or
   invalid, ask).
2. Size check (E8), then upload + link + list.json entry (E10).
3. Keep the original `.gif` (move to backup dir); removing it from the cloud
   folder is a developer decision => mention in the report.

### E12. `file doesn't comply to expected filename or not supported`

Cause: a regular file whose name doesn't match
`^<id>_[A-Za-z0-9]+.[A-Za-z0-9]+$` and isn't `.token`/`.jpg`
(wrong prefix, spaces, underscores, dashes, accents, etc.).

Steps:
1. Propose a compliant name (`<id>_` prefix, alphanumeric only, supported
   extension).
2. Renaming needs developer confirmation => ask (batch), else skip and report.
3. Apply via `FixFileName.md`.
4. Unsupported extensions (e.g. `.wav`, `.txt`, `.DS_Store`): do not delete; if
   it is OS junk (`.DS_Store`) just report as ignorable, otherwise **human review**.

### E13. `Not Processed <file> for <artist>`

Cause: file didn't match any check (e.g. a name with extension patterns not
covered such as `.mp3` with invalid chars where the general regex also fails,
or a `.jpg`/`.token`-like file). Treat like E12 and report; mostly ignore.

### E14. Runtime failures (`Error occurred:`)

- `$HOME` dir for the bot missing (cloud folder not synced/mounted): stop,
  report, ask the developer to sync. Do not create the folder.
- list.json missing/invalid (schema decode error): **human review**.
- `mediainfo` binary missing: install is the developer's decision; skip E6/E7
  and report.

## Validation

- `cd filesCheck && npm run start` finishes with no errors for the handled
  cases (remaining ones must all appear in the report's "Needs human review").
- `cd filesCheck && npm run test` if any checker code was touched (should not be).
- Every changed file passed E10 (upload verified, link verified, list.json
  updated) or is explicitly marked INCOMPLETE in the report.
- `git diff --stat` shows changes only in `modules/bots/*/<id>_list.json`
  (and files touched by renames via `FixFileName.md`). Cloud-folder file changes
  are outside git: list them in the report.
- If list.json changed: `sbt "fix"` and the affected bot test suite.

## Report (mandatory)

Write `agent-context/reports/filesCheck-<YYYY-MM-DD>.md` (create the folder if
missing; if the repo ignores it, still create it and say so). Contents:

1. **Summary**: bots processed, issues found per error case (E1..E14), fixed,
   skipped.
2. **Actions taken**, one row per file:
   `bot | file | issue | action/command | before -> after (size, tags, tracks) | backup path | list.json aligned (yes / link not verified)`
3. **Needs human review**: file, reason, what the developer must decide or
   provide (title, new name, share link, trimmed clip, ...).
4. **Questions asked / answers received** (e.g. mp3 titles).
5. **Residual output** of the final `npm run start`.
6. **Backups location** and reminder to delete them after review.

Finish by returning a short version of the report in chat with the report path.
