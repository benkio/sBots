import * as fs from 'node:fs';
import * as path from 'node:path';
import { Effect } from 'effect';
import { FileService, fileServiceLayer } from './FileService';
import { Id3TagService, Id3TagServiceLayer } from './Id3NodeService';
import { MediaInfoService, mediaInfoServiceLayer } from './MediaInfoService';
import logger from './logger';

type Bot = {
  id: string;
  artist: string;
  path: string;
};

type Matches = {
  check: (f: string) => Boolean;
  logic: (f: string) => Effect.Effect<void, unknown>;
};

type Id3TagServiceLike = {
  fixMp3ArtistId3Tag: (
    file: string,
    artist: string
  ) => Effect.Effect<void, unknown>;
};

type MediaInfoServiceLike = {
  checkAudioTrackMissing: (filePath: string) => Effect.Effect<boolean>;
  checkAudioVideoTrackExists: (filePath: string) => Effect.Effect<boolean>;
};

const baseDir = '/Dropbox/sBots/';

const bots: Bot[] = [
  {
    id: 'rphjb',
    artist: 'Richard Philip Henry John Benson',
    path: 'RichardPHJBensonBot/src/main/resources',
  },
  {
    id: 'abar',
    artist: 'Alessandro Barbero',
    path: 'ABarberoBot/src/main/resources',
  },
  { id: 'xah', artist: 'Xah Lee', path: 'XahLeeBot/src/main/resources' },
  {
    id: 'mos',
    artist: 'Germano Mosconi',
    path: 'M0sconiBot/src/main/resources',
  },
  {
    id: 'ytai',
    artist: 'Omar Palermo',
    path: 'YouTuboAncheI0Bot/src/main/resources',
  },
  {
    id: 'cala',
    artist: 'Francesco Calandra',
    path: 'CalandroBot/src/main/resources',
  },
];

const buildFileLogic = (
  bot: Bot,
  id3TagService: Id3TagServiceLike,
  mediaInfoService: MediaInfoServiceLike
): Matches[] => [
  {
    check: (f: string) => path.basename(f).length > 64,
    logic: (f: string) =>
      Effect.sync(() =>
        logger.error(
          `[filesCheck] 🚫 ${path.basename(f)} is too long (max 64): ${path.basename(f).length}`
        )
      ),
  },
  {
    check: (f: string) => {
      const mp3Regex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp3$`);
      return mp3Regex.test(path.basename(f));
    },
    logic: (f: string) => id3TagService.fixMp3ArtistId3Tag(f, bot.artist),
  },
  {
    check: (f: string) => {
      const gifRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+Gif.mp4$`);
      return gifRegex.test(path.basename(f));
    },
    logic: (f: string) =>
      mediaInfoService.checkAudioTrackMissing(f).pipe(
        Effect.flatMap((hasAudioTrack: boolean) => {
          if (hasAudioTrack) {
            return Effect.sync(() =>
              logger.error(
                `[filesCheck] 🚫  ${path.basename(f)} contains audio track`
              )
            );
          }
          return Effect.sync(() =>
            logger.verbose(`[filesCheck] ✓ ${path.basename(f)}`)
          );
        })
      ),
  },
  {
    check: (f: string) => {
      const videoRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp4$`);
      return videoRegex.test(path.basename(f));
    },
    logic: (f: string) =>
      mediaInfoService.checkAudioVideoTrackExists(f).pipe(
        Effect.flatMap((hasBothAudioAndVideo: boolean) => {
          if (!hasBothAudioAndVideo) {
            return Effect.sync(() =>
              logger.error(
                `[filesCheck] 🚫  ${path.basename(f)} doesn't contain both audio and video tracks`
              )
            );
          }
          return Effect.sync(() =>
            logger.verbose(`[filesCheck] ✓ ${path.basename(f)}`)
          );
        })
      ),
  },
  {
    check: (f: string) => path.extname(f) === '.gif',
    logic: (f: string) =>
      Effect.sync(() =>
        logger.error(
          `[filesCheck] 🚫  ${path.basename(f)} Gif is not supported`
        )
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
      Effect.sync(() =>
        logger.error(
          `[filesCheck] 🚫 ${path.basename(f)} file doesn't comply to expected filename or not supported`
        )
      ),
  },
  {
    check: (f: string) => {
      const stats = fs.statSync(f);
      return stats.isDirectory() || path.basename(f) === 'application.conf';
    },
    logic: (f: string) =>
      Effect.sync(() =>
        logger.warn(`[filesCheck] ⚠️ Ignore ${path.basename(f)}`)
      ),
  },
];

const unprocessedLogic = (
  bot: Bot
): {
  logic: (file: string) => Effect.Effect<void, unknown>;
} => ({
  logic: (file: string) =>
    Effect.sync(() =>
      logger.warn(
        `[filesCheck] ⚠️  Not Processed ${path.basename(file)} for ${bot.artist}`
      )
    ),
});

Effect.runPromise(
  Effect.gen(function* () {
    const fileService = yield* FileService;
    const id3TagService = yield* Id3TagService;
    const mediaInfoService = yield* MediaInfoService;

    yield* Effect.forEach(bots, (bot) =>
      Effect.gen(function* () {
        const botDir = yield* fileService.buildResourceDirectory(
          baseDir,
          bot.path
        );
        const files = yield* fileService.getFiles(botDir);
        const checks = buildFileLogic(bot, id3TagService, mediaInfoService);

        return yield* Effect.forEach(
          files,
          (f: string) => {
            const { logic } =
              checks.find(({ check }) => check(f) === true) ??
              unprocessedLogic(bot);
            return logic(f);
          },
          { concurrency: 'unbounded' }
        );
      })
    );
  }).pipe(
    Effect.provide(fileServiceLayer),
    Effect.provide(Id3TagServiceLayer),
    Effect.provide(mediaInfoServiceLayer)
  )
)
  .then(() => {
    logger.info('[filesCheck] ✓ Tag sanification completed');
  })
  .catch((err) => {
    logger.error(`[filesCheck] 🚫 Error occurred: ${err}`);
  });
