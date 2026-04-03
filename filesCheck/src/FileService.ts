import * as os from 'node:os';
import { Context, Effect, Layer } from 'effect';
import { FileSystem, Path } from '@effect/platform';
import { PlatformError } from '@effect/platform/Error';
import { NodeFileSystem, NodePath } from '@effect/platform-node';

export class FileService extends Context.Tag('FileService')<
  FileService,
  {
    getFiles: (directoryPath: string) => Effect.Effect<string[], PlatformError>;
    buildResourceDirectory: (
      baseDir: string,
      botDir: string
    ) => Effect.Effect<string, never>;
  }
>() {}

export const fileService = Effect.gen(function* () {
  const fileSystem = yield* FileSystem.FileSystem;
  const path = yield* Path.Path;

  return {
    getFiles: (directoryPath: string) =>
      fileSystem
        .readDirectory(directoryPath)
        .pipe(
          Effect.map((entries) =>
            entries.map((entry) => path.join(directoryPath, entry))
          )
        ),
    buildResourceDirectory: (baseDir: string, botDir: string) =>
      Effect.succeed(path.join(os.homedir(), baseDir, botDir)),
  };
});

export const fileServiceLayer = Layer.effect(FileService, fileService).pipe(
  Layer.provide(NodeFileSystem.layer),
  Layer.provide(NodePath.layer)
);
