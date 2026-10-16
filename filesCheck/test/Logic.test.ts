import * as assert from 'node:assert';
import { test } from 'node:test';
import { Effect } from 'effect';
import { buildFileLogic, hasInvalidTelegramMarkdownV2 } from '../src/Logic';
import type { Bot } from '../src/Config';
import type { MediaList } from '../src/MediaListSchema';
import type { FileService } from '../src/FileService';
import type { FileSystem } from 'effect';

const bot: Bot = {
  id: 'bot',
  artist: 'artist',
  filePath: '/unused',
  jsonFilePath: '/unused',
};

const id3TagService = {
  fixMp3ArtistId3Tag: () => Effect.void,
};

const mediaInfoService = {
  checkAudioTrackMissing: () => Effect.succeed(false),
  checkAudioVideoTrackExists: () => Effect.succeed(true),
};

const runFirstMatchingLogic = async (
  checks: ReturnType<typeof buildFileLogic>,
  filePath: string
): Promise<boolean> => {
  for (const checkEntry of checks) {
    const isMatch = await Effect.runPromise(checkEntry.check(filePath));
    if (isMatch) {
      await Effect.runPromise(checkEntry.logic(filePath));
      return true;
    }
  }
  return false;
};

const buildMockFileService = (
  statImpl: (targetPath: string) => Effect.Effect<FileSystem.File.Info, never>
): FileService['Service'] => ({
  getFilePaths: () => Effect.succeed([]),
  getFile: () => Effect.succeed(''),
  buildFromHomeDirectory: () => Effect.succeed(''),
  buildFromProjectDirectory: () => Effect.succeed(''),
  stat: statImpl,
  extname: (inputPath: string) => inputPath.slice(inputPath.lastIndexOf('.')),
  basename: (inputPath: string) => inputPath.split('/').at(-1) ?? inputPath,
});

void test('buildFileLogic markdown rule should check file size', async () => {
  let statCalls = 0;
  let getFileCalls = 0;

  const fileService = {
    ...buildMockFileService(() => {
      statCalls += 1;
      return Effect.succeed({
        type: 'File',
        size: 2_000_000,
      } as unknown as FileSystem.File.Info);
    }),
    getFile: () => {
      getFileCalls += 1;
      return Effect.succeed('Unescaped markdown. Needs escaping!');
    },
  } satisfies FileService['Service'];

  const mediaList: MediaList = [
    {
      filename: 'bot_readme.md',
      sources: ['https://example.com/bot_readme.md'],
    },
  ];

  const checks = buildFileLogic(
    bot,
    id3TagService,
    mediaInfoService,
    fileService,
    mediaList
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/bot_readme.md');
  assert.strictEqual(matched, true);
  assert.strictEqual(statCalls, 1);
  assert.strictEqual(getFileCalls, 1);
});

void test('hasInvalidTelegramMarkdownV2 should detect invalid content', () => {
  assert.strictEqual(hasInvalidTelegramMarkdownV2('Plain text'), false);
  assert.strictEqual(hasInvalidTelegramMarkdownV2('Ends with dot.'), true);
  assert.strictEqual(
    hasInvalidTelegramMarkdownV2('Escaped dot\\. and escaped exclamation\\!'),
    false
  );
});

void test('buildFileLogic mp3 rule should invoke id3 fix logic', async () => {
  let id3Calls = 0;
  let statCalls = 0;

  const fileService = buildMockFileService(() => {
    statCalls += 1;
    return Effect.succeed({
      type: 'File',
      size: 100,
    } as unknown as FileSystem.File.Info);
  });

  const checks = buildFileLogic(
    bot,
    {
      fixMp3ArtistId3Tag: () => {
        id3Calls += 1;
        return Effect.void;
      },
    },
    mediaInfoService,
    fileService,
    [
      {
        filename: 'bot_song.mp3',
        sources: ['https://example.com/bot_song.mp3'],
      },
    ]
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/bot_song.mp3');
  assert.strictEqual(matched, true);
  assert.strictEqual(id3Calls, 1);
  assert.strictEqual(statCalls, 0);
});

void test('buildFileLogic gif rule should invoke mediaInfo and size checks', async () => {
  let audioTrackChecks = 0;
  let statCalls = 0;

  const checks = buildFileLogic(
    bot,
    id3TagService,
    {
      checkAudioTrackMissing: () => {
        audioTrackChecks += 1;
        return Effect.succeed(false);
      },
      checkAudioVideoTrackExists: mediaInfoService.checkAudioVideoTrackExists,
    },
    buildMockFileService(() => {
      statCalls += 1;
      return Effect.succeed({
        type: 'File',
        size: 100,
      } as unknown as FileSystem.File.Info);
    }),
    [
      {
        filename: 'bot_animGif.mp4',
        sources: ['https://example.com/bot_animGif.mp4'],
      },
    ]
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/bot_animGif.mp4');
  assert.strictEqual(matched, true);
  assert.strictEqual(audioTrackChecks, 1);
  assert.strictEqual(statCalls, 1);
});

void test('buildFileLogic video rule should invoke audio+video and size checks', async () => {
  let audioVideoChecks = 0;
  let statCalls = 0;

  const checks = buildFileLogic(
    bot,
    id3TagService,
    {
      checkAudioTrackMissing: mediaInfoService.checkAudioTrackMissing,
      checkAudioVideoTrackExists: () => {
        audioVideoChecks += 1;
        return Effect.succeed(true);
      },
    },
    buildMockFileService(() => {
      statCalls += 1;
      return Effect.succeed({
        type: 'File',
        size: 100,
      } as unknown as FileSystem.File.Info);
    }),
    [
      {
        filename: 'bot_video.mp4',
        sources: ['https://example.com/bot_video.mp4'],
      },
    ]
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/bot_video.mp4');
  assert.strictEqual(matched, true);
  assert.strictEqual(audioVideoChecks, 1);
  assert.strictEqual(statCalls, 1);
});

void test('buildFileLogic .gif extension should match unsupported gif rule', async () => {
  let statCalls = 0;

  const checks = buildFileLogic(
    bot,
    id3TagService,
    mediaInfoService,
    buildMockFileService(() => {
      statCalls += 1;
      return Effect.succeed({
        type: 'File',
        size: 100,
      } as unknown as FileSystem.File.Info);
    }),
    []
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/not_supported.gif');
  assert.strictEqual(matched, true);
  assert.strictEqual(statCalls, 0);
});

void test('buildFileLogic should match ignore rule for directories', async () => {
  let statCalls = 0;

  const checks = buildFileLogic(
    bot,
    id3TagService,
    mediaInfoService,
    buildMockFileService(() => {
      statCalls += 1;
      return Effect.succeed({
        type: 'Directory',
      } as unknown as FileSystem.File.Info);
    }),
    []
  );

  const matched = await runFirstMatchingLogic(checks, '/tmp/some-directory');
  assert.strictEqual(matched, true);
  assert.strictEqual(statCalls, 2);
});
