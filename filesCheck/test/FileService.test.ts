import * as assert from 'node:assert';
import * as os from 'node:os';
import * as path from 'node:path';
import { test } from 'node:test';
import { Effect, Layer } from 'effect';
import { FileSystem } from '@effect/platform';
import { NodePath } from '@effect/platform-node';
import { FileService, fileService } from '../src/FileService';

void test('getFiles joins directory path and entry names', async () => {
  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: (directoryPath) =>
      Effect.succeed(
        directoryPath === '/tmp/resource'
          ? ['song.mp3', 'meta.txt', 'image.jpg']
          : []
      ),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const getFiles = Effect.gen(function* () {
    const service = yield* FileService;
    return yield* service.getFiles('/tmp/resource');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  const files = await Effect.runPromise(getFiles);
  assert.deepStrictEqual(files, [
    path.join('/tmp/resource', 'song.mp3'),
    path.join('/tmp/resource', 'meta.txt'),
    path.join('/tmp/resource', 'image.jpg'),
  ]);
});

void test('buildResourceDirectory resolves using home directory', async () => {
  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: () => Effect.succeed([]),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const buildResourceDirectory = Effect.gen(function* () {
    const service = yield* FileService;
    return yield* service.buildResourceDirectory('Dropbox/sBots', 'Bot/src');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  const directory = await Effect.runPromise(buildResourceDirectory);
  assert.strictEqual(
    directory,
    path.join(os.homedir(), 'Dropbox', 'sBots', 'Bot', 'src')
  );
});
