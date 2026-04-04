import * as NodeID3 from 'node-id3';
import * as path from 'node:path';
import { Context, Data, Effect, Layer, Option } from 'effect';

class Id3NodeError extends Data.TaggedError('Id3NodeError')<{
  readonly message: string;
}> {}

export class Id3NodeService extends Context.Tag('Id3NodeService')<
  Id3NodeService,
  {
    read: (file: string) => Effect.Effect<NodeID3.Tags, Id3NodeError>;
    write: (
      tags: NodeID3.Tags,
      file: string
    ) => Effect.Effect<void, Id3NodeError>;
  }
>() {}

export const Id3NodeServiceLayer = Layer.succeed(Id3NodeService, {
  read: (file) =>
    Effect.try({
      try: () => NodeID3.read(file),
      catch: (error) =>
        new Id3NodeError({ message: `ID3 read failed: ${String(error)}` }),
    }).pipe(
      Effect.flatMap((tags) =>
        tags
          ? Effect.succeed(tags)
          : Effect.fail(
              new Id3NodeError({
                message: `ID3 read failed: tags not found for ${file}`,
              })
            )
      )
    ),
  write: (tags, file) =>
    Effect.try({
      try: () => NodeID3.update(tags, file),
      catch: (error) =>
        new Id3NodeError({ message: `ID3 update failed: ${String(error)}` }),
    }).pipe(
      Effect.flatMap((result) =>
        result
          ? Effect.void
          : Effect.fail(
              new Id3NodeError({
                message: 'ID3 update failed: operation failed',
              })
            )
      )
    ),
});

export class Id3TagService extends Context.Tag('Id3TagService')<
  Id3TagService,
  {
    checkArtist: (
      initialArtist: string,
      artistTag: Option.Option<string>,
      file: string
    ) => Effect.Effect<void, Id3NodeError>;
    checkTitle: (
      file: string,
      title: Option.Option<string>
    ) => Effect.Effect<void, Id3NodeError>;
    fixMp3ArtistId3Tag: (
      file: string,
      initialArtist: string
    ) => Effect.Effect<void, Id3NodeError>;
  }
>() {}

export const id3TagService = Effect.gen(function* () {
  const id3NodeService = yield* Id3NodeService;

  const checkArtistFun = (
    initialArtist: string,
    artistTag: Option.Option<string>,
    file: string
  ) =>
    Option.match(
      Option.filter(artistTag, (artist) => artist === initialArtist),
      {
        onSome: (okArtist) =>
          Effect.logTrace(
            `[id3TagService] ⚠️ ${path.basename(file)} not updated: ${okArtist}(file) === ${initialArtist}(wanted)`
          ),
        onNone: () =>
          Effect.gen(function* () {
            yield* Effect.logInfo(
              `[id3TagService] Update file ${path.basename(file)} with artist ${initialArtist}`
            );
            yield* id3NodeService
              .write({ artist: initialArtist }, file)
              .pipe(
                Effect.catchAll((error) =>
                  Effect.logError(
                    `[id3TagService] Tag not updated: operation failed: file ${path.basename(file)} - error: ${String(error)}`
                  )
                )
              );
          }),
      }
    );

  const checkTitleFun = (file: string, title: Option.Option<string>) =>
    Option.match(title, {
      onSome: () => Effect.void,
      onNone: () =>
        Effect.logError(
          `[id3TagService] 🚫 ${path.basename(file)} doesn't contain a title`
        ),
    });

  return {
    checkArtist: (
      initialArtist: string,
      artistTag: Option.Option<string>,
      file: string
    ) => checkArtistFun(initialArtist, artistTag, file),
    checkTitle: (file: string, title: Option.Option<string>) =>
      checkTitleFun(file, title),
    fixMp3ArtistId3Tag: (file: string, initialArtist: string) =>
      Effect.gen(function* () {
        const tags = yield* id3NodeService.read(file);
        yield* checkArtistFun(
          initialArtist,
          Option.fromNullable(tags.artist),
          file
        );
        yield* checkTitleFun(file, Option.fromNullable(tags.title));
        yield* Effect.logTrace(`[id3TagService] ✓ ${path.basename(file)}`);
      }),
  };
});

const fixMp3ArtistId3TagEffect = (file: string, initialArtist: string) =>
  Effect.gen(function* () {
    const id3TagService = yield* Id3TagService;
    return yield* id3TagService.fixMp3ArtistId3Tag(file, initialArtist);
  }).pipe(Effect.provide(Id3TagServiceLayer));

export const Id3TagServiceLayer = Layer.effect(
  Id3TagService,
  id3TagService
).pipe(Layer.provide(Id3NodeServiceLayer));

export const fixMp3ArtistId3Tag = (file: string, initialArtist: string) =>
  Effect.runPromise(fixMp3ArtistId3TagEffect(file, initialArtist));
