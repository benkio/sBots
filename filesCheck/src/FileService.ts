import * as os from 'node:os';
import { Buffer } from 'node:buffer';
import { Context, Effect, Layer, Data } from 'effect';
import { FileSystem, Path } from '@effect/platform';
import { PlatformError } from '@effect/platform/Error';
import { NodeFileSystem, NodePath } from '@effect/platform-node';
import { readPackageUp } from 'read-package-up';

export class FileServiceError extends Data.TaggedError('FileServiceError')<{
  readonly message: string;
}> {}

export class FileService extends Context.Tag('FileService')<
  FileService,
  {
    getFiles: (directoryPath: string) => Effect.Effect<string[], PlatformError>;
    getFile: (filePath: string) => Effect.Effect<string, PlatformError>;
    buildFromHomeDirectory: (
      baseDir: string,
      botDir: string
    ) => Effect.Effect<string, never>;
    buildFromProjectDirectory: (
      relPath: string
    ) => Effect.Effect<string, FileServiceError>;
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
    getFile: (filePath: string) =>
      fileSystem.readFile(filePath).pipe(
        Effect.map((contents) => {
          if (typeof contents === 'string') {
            return contents;
          }

          return Buffer.from(contents).toString('utf8');
        })
      ),
    buildFromHomeDirectory: (baseDir: string, botDir: string) =>
      Effect.succeed(path.join(os.homedir(), baseDir, botDir)),
    buildFromProjectDirectory: (relPath: string) =>
      Effect.promise(() => readPackageUp()).pipe(
        Effect.flatMap((packageUpResult) => {
          if (packageUpResult) {
            return Effect.succeed(
              path.join(path.dirname(packageUpResult.path), relPath)
            );
          } else {
            return Effect.fail(
              new FileServiceError({
                message: `[FileService] 🚫 Error occurred when resolving the path: ${relPath}`,
              })
            );
          }
        })
      ),
  };
});

export const fileServiceLayer = Layer.effect(FileService, fileService).pipe(
  Layer.provide(NodeFileSystem.layer),
  Layer.provide(NodePath.layer)
);
