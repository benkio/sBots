import { Effect, Logger, References, Layer } from 'effect';
import { bots } from './Config';
import { FileService, fileServiceLayer } from './FileService';
import { Id3TagService, Id3TagServiceLayer } from './Id3NodeService';
import { MediaInfoService, mediaInfoServiceLayer } from './MediaInfoService';
import { buildFileLogic, unprocessedLogic } from './Logic';
import { decodeMediaListFromJson } from './MediaListSchema';

// Raise the minimum level to "Warn" to skip debug/info logs.
export const WarnAndAbove = Layer.succeed(References.MinimumLogLevel, 'Warn');

const program = Effect.gen(function* () {
  const fileService = yield* FileService;
  const id3TagService = yield* Id3TagService;
  const mediaInfoService = yield* MediaInfoService;

  yield* Effect.forEach(bots, (bot) =>
    Effect.gen(function* () {
      const botDir = yield* fileService.buildFromHomeDirectory(bot.filePath);
      const botListFilePath = yield* fileService.buildFromProjectDirectory(
        bot.jsonFilePath
      );
      const files = yield* fileService.getFiles(botDir);
      const mediaListFileContent = yield* fileService.getFile(botListFilePath);
      const mediaList = decodeMediaListFromJson(mediaListFileContent);
      const checks = buildFileLogic(
        bot,
        id3TagService,
        mediaInfoService,
        mediaList
      );

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
    Effect.provide(
      Logger.layer([Logger.consoleLogFmt]).pipe(
        Layer.provideMerge(WarnAndAbove) // Start with "Warn" level for the app logger.
      )
    ),
    Effect.catch((err) =>
      Effect.logError(`[filesCheck] 🚫 Error occurred: ${String(err)}`)
    )
  )
);
