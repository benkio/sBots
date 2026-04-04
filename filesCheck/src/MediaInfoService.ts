import { exec as mediainfoExec } from 'mediainfo-parser';
import { Context, Data, Effect, Layer } from 'effect';

type MediaTrack = {
  readonly type?: string;
  readonly _type?: string;
};

export type MediaInfo = {
  media: {
    track: ReadonlyArray<MediaTrack>;
  };
};

class MediaInfoParserError extends Data.TaggedError('MediaInfoError')<{
  readonly message: string;
}> {}

export class MediaInfoParserService extends Context.Tag(
  'MediaInfoParserService'
)<
  MediaInfoParserService,
  {
    readonly exec: (
      file: string
    ) => Effect.Effect<MediaInfo, MediaInfoParserError>;
  }
>() {}

const mediaInfoParserService = Effect.sync(() => {
  return {
    exec: (file: string) =>
      Effect.async<MediaInfo, MediaInfoParserError>((resume) => {
        mediainfoExec(file, (err: unknown, result: unknown) => {
          if (err) {
            resume(
              Effect.fail(
                new MediaInfoParserError({
                  message: `[MediaInfoParser] 🚫 Error occurred when getting mediaInfo from ${file} with ${String(
                    err
                  )}`,
                })
              )
            );
          } else {
            resume(Effect.succeed(result as MediaInfo));
          }
        });
      }),
  };
});

export const mediaInfoParserServiceLayer = Layer.effect(
  MediaInfoParserService,
  mediaInfoParserService
);

export class MediaInfoService extends Context.Tag('MediaInfoService')<
  MediaInfoService,
  {
    checkAudioTrackMissing: (filePath: string) => Effect.Effect<boolean, never>;
    checkAudioVideoTrackExists: (
      filePath: string
    ) => Effect.Effect<boolean, never>;
  }
>() {}

export const mediaInfoService = Effect.gen(function* () {
  const mediaInfoParserService = yield* MediaInfoParserService;
  return {
    checkAudioTrackMissing: (filePath: string) =>
      mediaInfoParserService.exec(filePath).pipe(
        Effect.map((mediaInfo) =>
          mediaInfo.media.track.some(
            (tr) => tr.type === 'Audio' || tr._type === 'Audio'
          )
        ),
        Effect.catchAll((error) =>
          Effect.logError(
            `[MediaInfoService] 🚫 ${filePath} checkAudioTrackMissing Error: ${error}`
          ).pipe(Effect.as(false))
        )
      ),
    checkAudioVideoTrackExists: (filePath: string) =>
      mediaInfoParserService.exec(filePath).pipe(
        Effect.map((mediaInfo) => {
          const tracks = mediaInfo.media.track.map(
            (tr) => tr.type || tr._type || ''
          );
          return tracks.includes('Audio') && tracks.includes('Video');
        }),
        Effect.catchAll((error) =>
          Effect.logError(
            `[MediaInfoService] 🚫 ${filePath} checkAudioVideoTrackExists Error: ${error}`
          ).pipe(Effect.as(false))
        )
      ),
  };
});

export const mediaInfoServiceLayer = Layer.effect(
  MediaInfoService,
  mediaInfoService
).pipe(Layer.provide(mediaInfoParserServiceLayer));

export const checkAudioTrackMissing = (filePath: string) =>
  Effect.runPromise(
    Effect.gen(function* () {
      const mediaInfoService = yield* MediaInfoService;
      return yield* mediaInfoService.checkAudioTrackMissing(filePath);
    }).pipe(Effect.provide(mediaInfoServiceLayer))
  );

export const checkAudioVideoTrackExists = (filePath: string) =>
  Effect.runPromise(
    Effect.gen(function* () {
      const mediaInfoService = yield* MediaInfoService;
      return yield* mediaInfoService.checkAudioVideoTrackExists(filePath);
    }).pipe(Effect.provide(mediaInfoServiceLayer))
  );
