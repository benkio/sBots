import { Effect, Logger } from 'effect';
import { bots, baseDir } from './Config';
import { FileService, fileServiceLayer } from './FileService';
import { Id3TagService, Id3TagServiceLayer } from './Id3NodeService';
import { MediaInfoService, mediaInfoServiceLayer } from './MediaInfoService';
import { buildFileLogic, unprocessedLogic } from './Logic';

const program = Effect.gen(function* () {
  const fileService = yield* FileService;
  const id3TagService = yield* Id3TagService;
  const mediaInfoService = yield* MediaInfoService;

  yield* Effect.forEach(bots, (bot) =>
    Effect.gen(function* () {
      const botDir = yield* fileService.buildFromHomeDirectory(
        baseDir,
        bot.filePath
      );
      const files = yield* fileService.getFiles(botDir);
      const checks = buildFileLogic(bot, id3TagService, mediaInfoService);

      yield* Effect.forEach(
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
  yield* Effect.logInfo('[filesCheck] ✓ Tag sanification completed');
});

void Effect.runPromise(
  program.pipe(
    Effect.provide(fileServiceLayer),
    Effect.provide(Id3TagServiceLayer),
    Effect.provide(mediaInfoServiceLayer),
    Effect.provide(Logger.logFmt),
    Effect.catchAll((err) =>
      Effect.logError(`[filesCheck] 🚫 Error occurred: ${String(err)}`)
    )
  )
);
