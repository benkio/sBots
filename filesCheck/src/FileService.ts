import * as os from 'node:os';
import { Buffer } from 'node:buffer';
import {
  Context,
  PlatformError,
  Effect,
  Layer,
  Data,
  FileSystem,
  Path,
} from 'effect';
import { NodeFileSystem, NodePath } from '@effect/platform-node';
import { readPackageUp } from 'read-package-up';

export class FileServiceError extends Data.TaggedError('FileServiceError')<{
  readonly message: string;
}> {}

export class FileService extends Context.Service<
  FileService,
  {
    getFiles: (
      directoryPath: string
    ) => Effect.Effect<string[], PlatformError.PlatformError>;
    getFile: (
      filePath: string
    ) => Effect.Effect<string, PlatformError.PlatformError>;
    buildFromHomeDirectory: (botDir: string) => Effect.Effect<string, never>;
    buildFromProjectDirectory: (
      relPath: string
    ) => Effect.Effect<string, FileServiceError>;
  }
>()('FileService') {}

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
    buildFromHomeDirectory: (botDir: string) =>
      Effect.succeed(path.join(os.homedir(), botDir)),
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
