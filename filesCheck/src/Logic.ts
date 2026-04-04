import * as fs from 'node:fs';
import * as path from 'node:path';
import { Effect } from 'effect';
import type { Bot } from './Config';
import type { MediaList, MediaListItem } from './MediaListSchema';

type Id3TagServiceLike = {
  fixMp3ArtistId3Tag: (
    file: string,
    initialArtist: string
  ) => Effect.Effect<void, unknown>;
};

type MediaInfoServiceLike = {
  checkAudioTrackMissing(filePath: string): Effect.Effect<boolean, never>;
  checkAudioVideoTrackExists(filePath: string): Effect.Effect<boolean, never>;
};

type Matches = {
  check: (f: string) => Boolean;
  logic: (f: string) => Effect.Effect<void, unknown>;
};

const isMediaListItem = (mediaList: MediaList, filePath: string): boolean =>
  mediaList.some(
    (mediaListItem: MediaListItem) =>
      path.basename(filePath) === mediaListItem.filename
  );

const containinedInMediaListLogic =
  (_bot: Bot, mediaList: MediaList) =>
  (f: string): Effect.Effect<void, never> =>
    isMediaListItem(mediaList, f)
      ? Effect.void
      : Effect.logError(
          `[filesCheck] 🚫 ${path.basename(f)} is not contained in the bot media list`
        );

const mp3Logic =
  (bot: Bot, mediaList: MediaList, id3TagService: Id3TagServiceLike) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList);
      yield* guard(f);
      yield* id3TagService.fixMp3ArtistId3Tag(f, bot.artist);
    });

const gifLogic =
  (bot: Bot, mediaList: MediaList, mediaInfoService: MediaInfoServiceLike) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList);
      yield* guard(f);
      const hasAudioTrack = yield* mediaInfoService.checkAudioTrackMissing(f);

      yield* hasAudioTrack
        ? Effect.logError(
            `[filesCheck] 🚫  ${path.basename(f)} contains audio track`
          )
        : Effect.logInfo(`[filesCheck] ✓ ${path.basename(f)}`);
    });

const videoLogic =
  (bot: Bot, mediaList: MediaList, mediaInfoService: MediaInfoServiceLike) =>
  (f: string) =>
    Effect.gen(function* () {
      const guard = containinedInMediaListLogic(bot, mediaList);
      yield* guard(f);
      const hasBothAudioAndVideo =
        yield* mediaInfoService.checkAudioVideoTrackExists(f);

      yield* !hasBothAudioAndVideo
        ? Effect.logError(
            `[filesCheck] 🚫  ${path.basename(f)} doesn't contain both audio and video tracks`
          )
        : Effect.logInfo(`[filesCheck] ✓ ${path.basename(f)}`);
    });

export const buildFileLogic = (
  bot: Bot,
  id3TagService: Id3TagServiceLike,
  mediaInfoService: MediaInfoServiceLike,
  mediaList: MediaList
): Matches[] => [
  {
    check: (f: string) => path.basename(f).length > 64,
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫 ${path.basename(f)} is too long (max 64): ${path.basename(f).length}`
      ),
  },
  {
    check: (f: string) => {
      const mp3Regex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp3$`);
      return mp3Regex.test(path.basename(f));
    },
    logic: mp3Logic(bot, mediaList, id3TagService),
  },
  {
    check: (f: string) => {
      const gifRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+Gif.mp4$`);
      return gifRegex.test(path.basename(f));
    },
    logic: gifLogic(bot, mediaList, mediaInfoService),
  },
  {
    check: (f: string) => {
      const videoRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp4$`);
      return videoRegex.test(path.basename(f));
    },
    logic: videoLogic(bot, mediaList, mediaInfoService),
  },
  {
    check: (f: string) => path.extname(f) === '.gif',
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫  ${path.basename(f)} Gif is not supported`
      ),
  },
  {
    check: (f: string) => {
      const stats = fs.statSync(f);
      const generalFilename = new RegExp(
        `^${bot.id}_[A-Za-z0-9]+.[A-Za-z0-9]+$`
      );
      return (
        stats.isFile() &&
        ['.token', '.jpg'].find((ext) => path.extname(f) === ext) ===
          undefined &&
        !generalFilename.test(path.basename(f))
      );
    },
    logic: (f: string) =>
      Effect.logError(
        `[filesCheck] 🚫 ${path.basename(f)} file doesn't comply to expected filename or not supported`
      ),
  },
  {
    check: (f: string) => {
      const stats = fs.statSync(f);
      return stats.isDirectory() || path.basename(f) === 'application.conf';
    },
    logic: (f: string) =>
      Effect.logWarning(`[filesCheck] ⚠️ Ignore ${path.basename(f)}`),
  },
];

export const unprocessedLogic = (
  bot: Bot
): {
  logic: (file: string) => Effect.Effect<void, unknown>;
} => ({
  logic: (file: string) =>
    Effect.logWarning(
      `[filesCheck] ⚠️  Not Processed ${path.basename(file)} for ${bot.artist}`
    ),
});
