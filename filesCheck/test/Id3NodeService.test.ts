import * as assert from 'node:assert';
import * as os from 'node:os';
import { test } from 'node:test';
import * as path from 'node:path';
import { Effect, Layer } from 'effect';
import * as NodeID3 from 'node-id3';
import { FileSystem } from '@effect/platform';
import { NodePath } from '@effect/platform-node';
import { FileService, fileService } from '../src/FileService';
import {
  Id3NodeService,
  Id3TagService,
  id3TagService,
} from '../src/Id3NodeService';

void test('getFiles maps filesystem entries with mocked FileSystem', async () => {
  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: (directoryPath) =>
      Effect.succeed(
        directoryPath === '/resource' ? ['song.mp3', 'meta.txt'] : []
      ),
  });

  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const getFiles = Effect.gen(function* () {
    const fileService = yield* FileService;
    return yield* fileService.getFiles('/resource');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  const files = await Effect.runPromise(getFiles);
  assert.deepStrictEqual(files, ['/resource/song.mp3', '/resource/meta.txt']);
});

void test('buildFromHomeDirectory uses mocked path operations', async () => {
  const mockFileSystemLayer = FileSystem.layerNoop({
    readDirectory: () => Effect.succeed([]),
  });
  const mockedFileServiceLayer = Layer.effect(FileService, fileService).pipe(
    Layer.provide(mockFileSystemLayer),
    Layer.provide(NodePath.layer)
  );

  const buildFromHomeDirectory = Effect.gen(function* () {
    const fileService = yield* FileService;
    return yield* fileService.buildFromHomeDirectory('/base', 'bot');
  }).pipe(Effect.provide(mockedFileServiceLayer));

  const directory = await Effect.runPromise(buildFromHomeDirectory);
  assert.strictEqual(directory, path.join(os.homedir(), 'base', 'bot'));
});

void test('fixMp3ArtistId3Tag updates artist when different and keeps title when present', async () => {
  let written = 0;
  let writtenArtist = '';
  const mockId3NodeLayer = Layer.succeed(Id3NodeService, {
    read: () =>
      Effect.succeed({ artist: 'Wrong', title: 'Song Title' } as NodeID3.Tags),
    write: (tags, _file) => {
      written += 1;
      writtenArtist = String(tags.artist);
      return Effect.void;
    },
  });

  const mockTagServiceLayer = Layer.effect(Id3TagService, id3TagService).pipe(
    Layer.provide(mockId3NodeLayer)
  );

  await Effect.runPromise(
    Effect.gen(function* () {
      const service = yield* Id3TagService;
      yield* service.fixMp3ArtistId3Tag(
        '/resource/song.mp3',
        'Expected Artist'
      );
    }).pipe(Effect.provide(mockTagServiceLayer))
  );

  assert.strictEqual(written, 1);
  assert.strictEqual(writtenArtist, 'Expected Artist');
});

void test('fixMp3ArtistId3Tag does not call write when artist already matches', async () => {
  let written = 0;
  const mockId3NodeLayer = Layer.succeed(Id3NodeService, {
    read: () =>
      Effect.succeed({
        artist: 'Expected Artist',
        title: 'Song Title',
      } as NodeID3.Tags),
    write: () => {
      written += 1;
      return Effect.void;
    },
  });

  const mockTagServiceLayer = Layer.effect(Id3TagService, id3TagService).pipe(
    Layer.provide(mockId3NodeLayer)
  );

  await Effect.runPromise(
    Effect.gen(function* () {
      const service = yield* Id3TagService;
      yield* service.fixMp3ArtistId3Tag(
        '/resource/song.mp3',
        'Expected Artist'
      );
    }).pipe(Effect.provide(mockTagServiceLayer))
  );

  assert.strictEqual(written, 0);
});

void test('fixMp3ArtistId3Tag works when title is missing', async () => {
  const mockId3NodeLayer = Layer.succeed(Id3NodeService, {
    read: () => Effect.succeed({ artist: 'Expected Artist' } as NodeID3.Tags),
    write: () => Effect.void,
  });

  const mockTagServiceLayer = Layer.effect(Id3TagService, id3TagService).pipe(
    Layer.provide(mockId3NodeLayer)
  );

  await Effect.runPromise(
    Effect.gen(function* () {
      const service = yield* Id3TagService;
      yield* service.fixMp3ArtistId3Tag(
        '/resource/song.mp3',
        'Expected Artist'
      );
    }).pipe(Effect.provide(mockTagServiceLayer))
  );
});
