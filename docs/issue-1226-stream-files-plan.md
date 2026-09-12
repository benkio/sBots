# Issue #1226 — Stream Files / Heap Memory Management

## Root cause

`IFile` in telegramium is sealed with only two cases: `InputPartFile(java.io.File)` and
`InputLinkFile(String)` — there is no streaming variant, so a physical temp file on disk is
**always** required to hand media to Telegram. That is not the problem:

- The **sending** side already streams: telegramium's http4s backend uploads via
  `org.http4s.multipart.Part.fileData(name, path, ..., Files)`, which reads the file from disk
  in bounded fs2 chunks (verified in `telegramium-high` `Http4sUtils$.class`).
- The real heap blow-up is on the **loading** side, before the file ever reaches telegramium:
  - `Repository.getResourceByteArray` — `inputStream.readAllBytes()`  (whole classpath/JAR file in heap)
  - `DropboxClient.fetchFile` — `response.body.compile.toList`  (whole body as a `List[Byte]` in heap)
  - `MegaClient` — `_body.compile.to(Array)` **plus** `cipher.doFinal` on the whole array
    (encrypted + decrypted copies in heap simultaneously)
  - All funneled into `Repository.toTempFile` which does `Files.write(path, content)`.

A large file briefly occupies several full-size copies in heap. Goal: stream every source into
the temp file chunk-by-chunk, keeping only ~64 KB chunks in memory.

## Direction

Full streaming migration: remove the `Array[Byte]`-based helpers
(`getResourceByteArray` / `toTempFile(Array)`) and migrate all call sites to fs2 streams.

fs2 is already a dependency of `chatCore` (`fs2-core`, `fs2-io` 3.14.0). The APIs used exist
and were verified against 3.14.0:
- `fs2.io.file.Files[F].writeAll(path): Pipe[F, Byte, Nothing]`
- `fs2.io.readInputStream(acquire: F[InputStream], chunkSize: Int, closeAfterUse: Boolean): Stream[F, Byte]`

## Changes

### 1. `modules/chatCore/.../repository/Repository.scala`
- **Replace** `toTempFile(fileName, content: Array[Byte])` with
  `def toTempFile[F[_]: Async](fileName: String, content: Stream[F, Byte]): Resource[F, Path]`:
  keep the current `Files.createTempFile(name, ext)` + `Files.deleteIfExists` `Resource.make`
  lifecycle, but write via `content.through(files.writeAll(path)).compile.drain`.
- **Replace** `getResourceByteArray` with
  `def getResourceStream(resourceName): Resource[F, Either[RepositoryError, Stream[F, Byte]]]`:
  same `getResourceAsStream("/$resourceName")` / `new FileInputStream(resourceName)` opening,
  wrapped with `fs2.io.readInputStream(Async[F].pure(is), chunkSize, closeAfterUse = false)`
  instead of `readAllBytes()`.
- **Add** `def streamToString[F[_]: Async](stream: Stream[F, Byte]): F[String]`
  = `stream.through(fs2.text.utf8.decode).compile.string` (needed by JSON + token decoders).
- Rename error case `NoResourcesFoundByteArray` → `NoResourcesFoundStream` (only referenced inside this file).
- `fileToString(Path)` stays (small config files on disk; returning a String inherently materializes it).

### 2. `modules/chatCore/.../repository/ResourcesRepository.scala`
- `getResourcesByKind`, JAR branch (lines 75–87): `getResourceByteArray` → `getResourceStream`,
  and `toTempFile(name, Array)` → `toTempFile(name, Stream)`. Keep the `Either` shape:
  `streamEither.traverse(stream => Repository.toTempFile(name, stream).map(Resource.pure))`,
  then the existing `_.sequence`. The local-FS branch already yields `Path`s — untouched.
- `getResourceFile` (lines 107–118): same swap for its loaded-resource path.

### 3. `modules/chatCore/.../http/DropboxClient.scala`
- Replace `response.body.compile.toList` + `toTempFile(filename, content.toArray)` (lines 51–57)
  with `toTempFile(filename, response.body)` (pipes the fs2 body straight to disk; bounded chunks).
- Preserve the "empty response → error" contract (spec asserts `UnexpectedDropboxResponse`):
  after the write, `Files.size(path) == 0` → `Async[F].raiseWhen(size == 0L)(UnexpectedDropboxResponse(response))`.
- Log the file size (from `Files.size`) instead of `content.length`.
- The `httpClient.run(req)` resource stays open across the drain, so the connection is valid.

### 4. `modules/chatCore/.../http/MegaClient.scala`
- **Encrypted download as stream**: `getEncryptedFileContent` (currently `F[Array[Byte]]` via
  `compile.to(Array)`) → produce `Stream[F, Byte]` using `httpClient.stream(Request(GET, uri)).flatMap(_.body)`
  (keeps the connection open for the stream lifetime — `httpClient.get(...)` returning a body
  stream would break the connection pool). Keep the `firstSuccessful` fallback across encrypted
  URIs as a `Stream`-level `handleErrorWith` chain.
- **Streaming decryption**: replace `decryptFileContent(Array[Byte])` with a chunked pass — build
  **one** AES/CTR `Cipher` (NoPadding; CTR is a stream cipher, state carries across chunks),
  then `encrypted.mapChunks(chunk => Chunk.array(cipher.update(chunk.toArray))) ++
  Stream.eval(Async[F].delay(Chunk.array(cipher.doFinal())))`.
- `fetchFile` signature unchanged: `fetchFile(filename, url): Resource[F, Path]` — `HttpClients`,
  `DBRepository`, and integration specs (`ITMegaClientSpec`) are untouched.

### 5. `modules/chatCore/.../repository/JsonDataRepository.scala`
- `loadData` (lines 32–52): `getResourceByteArray` + `String(bytes, UTF_8)` → `getResourceStream`
  + `streamToString`. Error folding unchanged.

### 6. Token / config loaders — no change
- `TokenReader` and `BotSetup` already read the temp `Path` produced by `getResourceFile` via
  `fileToString`, so they're independent of the array removal.

### 7. Untouched
- `MediaResourceConversions.toTelegramApi` (`InputPartFile(p.toFile())`), `TelegramReply.telegramFileReplyPattern`,
  `ISBot` webhook cert, `HttpClients`, `DBRepository` — all unchanged; the send side already streams.

## Tests to update
- `RepositorySpec`:
  - `toTempFile` test → build the content as `fs2.Stream` (e.g. from the random bytes via
    `Stream.chunk(Chunk.array(...))`); assertions unchanged.
  - Add: `getResourceStream` on a known classpath resource yields the same bytes as the source;
    temp file is cleaned up after `use`.
- `BotSetupSpec` line 37: `Repository.toTempFile[IO](tokenFilename, expectedFileContent)` →
  pass `Stream.chunk(Chunk.array(expectedFileContent))`.
- `DropboxClientSpec`: unchanged (redirect + empty-response behavior preserved).
- Integration specs calling `fetchFile` / `getResourceFile`: unchanged public APIs.

## Verification
- `sbt chatCore/test chatTelegramAdapter/test` (bot specs + integration unaffected).
- Manual sanity: send a PhotoFile/VideoFile reply; confirm temp file created/deleted and heap
  peak no longer scales with file size (observe with `-Xmx` cap + `jcmd GC.heap_info`, or
  `-verbose:gc`).

## Alternatives considered (rejected)
- **`InputLinkFile(url)` passthrough** — zero local memory/file, but requires a publicly reachable
  HTTPS URL; Dropbox/Mega links are signed/expiring and Dropbox uses non-standard redirects.
  Only viable for self-hosted files; not general.
- **"Real" streaming to Telegram** — impossible: the Bot API multipart contract requires a named
  payload and telegramium only exposes `java.io.File`. Streaming-to-disk gives the memory win.
- **Bumping heap** — does not address the issue's stated goal.