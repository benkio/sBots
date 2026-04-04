import * as assert from 'node:assert';
import * as fs from 'node:fs';
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

void test('buildFromHomeDirectory resolves using home directory', async () => {
  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: () => Effect.succeed([]),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const buildFromHomeDirectory = Effect.gen(function* () {
    const service = yield* FileService;
    return yield* service.buildFromHomeDirectory('Dropbox/sBots', 'Bot/src');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  const directory = await Effect.runPromise(buildFromHomeDirectory);
  assert.strictEqual(
    directory,
    path.join(os.homedir(), 'Dropbox', 'sBots', 'Bot', 'src')
  );
});

void test('buildFromProjectDirectory resolves path from nearest package.json', async () => {
  const originalCwd = process.cwd();
  const projectDir = fs.mkdtempSync(
    path.join(os.tmpdir(), 'fileservice-test-')
  );
  const pkgDir = path.join(projectDir, 'repo');
  fs.mkdirSync(pkgDir, { recursive: true });
  fs.writeFileSync(
    path.join(projectDir, 'package.json'),
    JSON.stringify({ name: 'test-project' })
  );

  process.chdir(pkgDir);

  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: () => Effect.succeed([]),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const buildFromProjectDirectory = Effect.gen(function* () {
    const service = yield* FileService;
    return yield* service.buildFromProjectDirectory('src/bot');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  try {
    const directory = await Effect.runPromise(buildFromProjectDirectory);
    assert.strictEqual(
      directory,
      path.join(fs.realpathSync(projectDir), 'src', 'bot')
    );
  } finally {
    process.chdir(originalCwd);
  }
});

void test('buildFromProjectDirectory fails when no package.json is found', async () => {
  const originalCwd = process.cwd();
  const orphanDir = fs.mkdtempSync(
    path.join(os.tmpdir(), 'fileservice-test-no-pkg-')
  );
  process.chdir(orphanDir);

  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: () => Effect.succeed([]),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const buildFromProjectDirectory = Effect.gen(function* () {
    const service = yield* FileService;
    return yield* service.buildFromProjectDirectory('src/bot');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  try {
    await assert.rejects(
      () => Effect.runPromise(buildFromProjectDirectory),
      (error: unknown) =>
        error instanceof Error &&
        error.message.includes('Error occurred when resolving the path')
    );
  } finally {
    process.chdir(originalCwd);
  }
});
