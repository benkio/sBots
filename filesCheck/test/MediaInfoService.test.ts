import * as assert from 'node:assert';
import { test } from 'node:test';
import { Effect, Layer } from 'effect';
import {
  MediaInfoParserService,
  MediaInfoService,
  mediaInfoService,
} from '../src/MediaInfoService';

const runWithMockedParser = (
  file: string,
  parser: (
    filePath: string
  ) => Effect.Effect<{ media: { track: { type?: string; _type?: string }[] } }>
) => {
  const mockParserLayer = Layer.succeed(MediaInfoParserService, {
    exec: parser,
  });
  const mockedMediaInfoServiceLayer = Layer.effect(
    MediaInfoService,
    mediaInfoService
  ).pipe(Layer.provide(mockParserLayer));

  return Effect.gen(function* () {
    const mediaInfoService = yield* MediaInfoService;
    return yield* mediaInfoService.checkAudioTrackMissing(file);
  }).pipe(Effect.provide(mockedMediaInfoServiceLayer));
};

void test('checkAudioTrackMissing is true when parser provides an audio track', async () => {
  const check = runWithMockedParser('/tmp/audio.mp3', () =>
    Effect.succeed({
      media: {
        track: [{ type: 'Audio' }],
      },
    })
  );
  const hasAudio = await Effect.runPromise(check);
  assert.strictEqual(hasAudio, true);
});

void test('checkAudioTrackMissing is false when parser provides no audio track', async () => {
  const check = runWithMockedParser('/tmp/video.mp4', () =>
    Effect.succeed({
      media: {
        track: [{ type: 'Video' }],
      },
    })
  );
  const hasAudio = await Effect.runPromise(check);
  assert.strictEqual(hasAudio, false);
});

void test('checkAudioTrackMissing is false when parser fails', async () => {
  const mockParser = () => Effect.fail(new Error('parse error') as never);
  const check = runWithMockedParser('/tmp/fail.mp3', mockParser);
  const hasAudio = await Effect.runPromise(check);
  assert.strictEqual(hasAudio, false);
});

void test('checkAudioVideoTrackExists is true when parser provides both tracks', async () => {
  const check = Effect.gen(function* () {
    const mediaInfoService = yield* MediaInfoService;
    return yield* mediaInfoService.checkAudioVideoTrackExists('/tmp/both.mp4');
  }).pipe(
    Effect.provide(
      Layer.effect(MediaInfoService, mediaInfoService).pipe(
        Layer.provide(
          Layer.succeed(MediaInfoParserService, {
            exec: () =>
              Effect.succeed({
                media: {
                  track: [{ type: 'Audio' }, { type: 'Video' }],
                },
              }),
          })
        )
      )
    )
  );

  const hasBoth = await Effect.runPromise(check);
  assert.strictEqual(hasBoth, true);
});

void test('checkAudioVideoTrackExists is false when parser misses one track', async () => {
  const check = Effect.gen(function* () {
    const mediaInfoService = yield* MediaInfoService;
    return yield* mediaInfoService.checkAudioVideoTrackExists(
      '/tmp/audio-only.mp4'
    );
  }).pipe(
    Effect.provide(
      Layer.effect(MediaInfoService, mediaInfoService).pipe(
        Layer.provide(
          Layer.succeed(MediaInfoParserService, {
            exec: () =>
              Effect.succeed({
                media: {
                  track: [{ type: 'Audio' }],
                },
              }),
          })
        )
      )
    )
  );

  const hasBoth = await Effect.runPromise(check);
  assert.strictEqual(hasBoth, false);
});
