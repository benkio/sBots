import { ByteSize, Effect, type PlatformError } from 'effect';
import type { Bot } from './Config';
import type { FileService } from './FileService';
import type { MediaList, MediaListItem } from './MediaListSchema';

interface Id3TagServiceLike {
  fixMp3ArtistId3Tag: (
    file: string,
    initialArtist: string
  ) => Effect.Effect<void, unknown>;
}

interface MediaInfoServiceLike {
  checkAudioTrackMissing(filePath: string): Effect.Effect<boolean, never>;
  checkAudioVideoTrackExists(filePath: string): Effect.Effect<boolean, never>;
}

interface Matches {
  check: (f: string) => Effect.Effect<boolean>;
  logic: (f: string) => Effect.Effect<void, unknown>;
}

const sizeThresholdMB = 10;
const markdownSizeThresholdMB = 1;
const markdownV2ReservedChars = new Set([
  '_',
  '*',
  '[',
  ']',
  '(',
  ')',
  '~',
  '`',
  '>',
  '#',
  '+',
  '-',
  '=',
  '|',
  '{',
  '}',
  '.',
  '!',
  '\\',
]);

export const hasInvalidTelegramMarkdownV2 = (content: string): boolean => {
  for (let i = 0; i < content.length; i += 1) {
    const char = content[i];
    if (char === '\\') {
      i += 1;
      continue;
    }
    if (markdownV2ReservedChars.has(char)) {
      return true;
    }
  }
  return false;
};

const isMediaListItem = (
  mediaList: MediaList,
  filePath: string,
  fileService: FileService['Service']
): boolean =>
  mediaList.some(
    (mediaListItem: MediaListItem) =>
      fileService.basename(filePath) === mediaListItem.filename
  );

const containinedInMediaListLogic =
  (_bot: Bot, mediaList: MediaList, fileService: FileService['Service']) =>
  (f: string): Effect.Effect<void, never> =>
    isMediaListItem(mediaList, f, fileService)
      ? Effect.void
      : Effect.logError(
          `[filesCheck] 🚫 ${fileService.basename(f)} is not contained in the bot media list`
        );

const isLessThenMB = (
  f: string,
  sizeThresholdMB: number,
  fileService: FileService['Service']
): Effect.Effect<boolean, PlatformError.PlatformError> =>
  fileService
    .stat(f)
    .pipe(
      Effect.map((stat) => stat.size < ByteSize.megabytes(sizeThresholdMB))
    );

const mp3Logic =
  (
    bot: Bot,
    mediaList: MediaList,
    id3TagService: Id3TagServiceLike,
    fileService: FileService['Service']
  ) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList, fileService);
      yield* guard(f);
      yield* id3TagService.fixMp3ArtistId3Tag(f, bot.artist);
      yield* Effect.logInfo(`[filesCheck] ✓ ${fileService.basename(f)}`);
    });

const gifLogic =
  (
    bot: Bot,
    mediaList: MediaList,
    mediaInfoService: MediaInfoServiceLike,
    fileService: FileService['Service']
  ) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList, fileService);
      yield* guard(f);
      const hasAudioTrack = yield* mediaInfoService.checkAudioTrackMissing(f);

      yield* hasAudioTrack
        ? Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} contains audio track`
          )
        : Effect.void;

      const sizeCheck = yield* isLessThenMB(f, sizeThresholdMB, fileService);
      yield* sizeCheck
        ? Effect.logInfo(`[filesCheck] ✓ ${fileService.basename(f)}`)
        : Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} is Bigger than ${sizeThresholdMB}MB`
          );
    });

const videoLogic =
  (
    bot: Bot,
    mediaList: MediaList,
    mediaInfoService: MediaInfoServiceLike,
    fileService: FileService['Service']
  ) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList, fileService);
      yield* guard(f);
      const hasBothAudioAndVideo =
        yield* mediaInfoService.checkAudioVideoTrackExists(f);

      yield* !hasBothAudioAndVideo
        ? Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} doesn't contain both audio and video tracks`
          )
        : Effect.void;

      const sizeCheck = yield* isLessThenMB(f, sizeThresholdMB, fileService);
      yield* sizeCheck
        ? Effect.logInfo(`[filesCheck] ✓ ${fileService.basename(f)}`)
        : Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} is Bigger than ${sizeThresholdMB}MB`
          );
    });

const markdownLogic =
  (bot: Bot, mediaList: MediaList, fileService: FileService['Service']) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList, fileService);
      yield* guard(f);

      const markdownContent = yield* fileService.getFile(f);
      const hasInvalidMarkdown =
        hasInvalidTelegramMarkdownV2(markdownContent);
      yield* hasInvalidMarkdown
        ? Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} contains unescaped Telegram MarkdownV2 special chars`
          )
        : Effect.void;

      const sizeCheck = yield* isLessThenMB(
        f,
        markdownSizeThresholdMB,
        fileService
      );
      yield* sizeCheck
        ? Effect.logInfo(`[filesCheck] ✓ ${fileService.basename(f)}`)
        : Effect.logError(
            `[filesCheck] 🚫  ${fileService.basename(f)} is Bigger than ${markdownSizeThresholdMB}MB`
          );
    });

export const buildFileLogic = (
  bot: Bot,
  id3TagService: Id3TagServiceLike,
  mediaInfoService: MediaInfoServiceLike,
  fileService: FileService['Service'],
  mediaList: MediaList
): Matches[] => [
  {
    check: (f: string) => Effect.succeed(fileService.basename(f).length > 64),
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫 ${fileService.basename(f)} is too long (max 64): ${fileService.basename(f).length}`
      ),
  },
  {
    check: (f: string) => {
      const mp3Regex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp3$`);
      return Effect.succeed(mp3Regex.test(fileService.basename(f)));
    },
    logic: mp3Logic(bot, mediaList, id3TagService, fileService),
  },
  {
    check: (f: string) => {
      const gifRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+Gif.mp4$`);
      return Effect.succeed(gifRegex.test(fileService.basename(f)));
    },
    logic: gifLogic(bot, mediaList, mediaInfoService, fileService),
  },
  {
    check: (f: string) => {
      const videoRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp4$`);
      return Effect.succeed(videoRegex.test(fileService.basename(f)));
    },
    logic: videoLogic(bot, mediaList, mediaInfoService, fileService),
  },
  {
    check: (f: string) => {
      const markdownRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.md$`);
      return Effect.succeed(markdownRegex.test(fileService.basename(f)));
    },
    logic: markdownLogic(bot, mediaList, fileService),
  },
  {
    check: (f: string) => Effect.succeed(fileService.extname(f) === '.gif'),
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫  ${fileService.basename(f)} Gif is not supported`
      ),
  },
  {
    check: (f: string) =>
      fileService.stat(f).pipe(
        Effect.map((stat) => {
          const generalFilename = new RegExp(
            `^${bot.id}_[A-Za-z0-9]+.[A-Za-z0-9]+$`
          );
          return (
            stat.type === 'File' &&
            ['.token', '.jpg'].find((ext) => fileService.extname(f) === ext) ===
              undefined &&
            !generalFilename.test(fileService.basename(f))
          );
        }),
        Effect.catch(() => Effect.succeed(false))
      ),
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫 ${fileService.basename(f)} file doesn't comply to expected filename or not supported`
      ),
  },
  {
    check: (f: string) =>
      fileService.stat(f).pipe(
        Effect.map(
          (stat) =>
            stat.type === 'Directory' ||
            fileService.basename(f) === 'application.conf'
        ),
        Effect.catch(() => Effect.succeed(false))
      ),
    logic: (f: string) =>
      Effect.logWarning(`[filesCheck] ⚠️ Ignore ${fileService.basename(f)}`),
  },
];

export const unprocessedLogic = (
  bot: Bot,
  fileService: FileService['Service']
): {
  logic: (file: string) => Effect.Effect<void, unknown>;
} => ({
  logic: (file: string) =>
    Effect.logWarning(
      `[filesCheck] ⚠️  Not Processed ${fileService.basename(file)} for ${bot.artist}`
    ),
});
