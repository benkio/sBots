import { fixMp3ArtistId3Tag } from './id3Functions';
import * as path from 'node:path';
import * as fs from 'node:fs';
import { buildResourceDirectory, getFiles } from './fileFunctions';
import {
  checkAudioTrackMissing,
  checkAudioVideoTrackExists,
  MediaInfoCheckReturn,
} from './mediaInfoFunctions';
import logger from './logger';

// Types //////////////////////////////////////////////////////////////////////
type Input = {
  prefix: string;
  artist: string;
  path: string;
};

type Matches = {
  check: (f: string) => Boolean;
  logic: (f: string) => void;
};
// Inputs /////////////////////////////////////////////////////////////////////

const baseDir = '/Dropbox/sBots/';

const botsInput: Input[] = [
  {
    prefix: 'rphjb_',
    artist: 'Richard Philip Henry John Benson',
    path: 'richardPHJBensonBot/src/main/resources',
  },
  {
    prefix: 'abar_',
    artist: 'Alessandro Barbero',
    path: 'aBarberoBot/src/main/resources',
  },
  { prefix: 'xah_', artist: 'Xah Lee', path: 'xahLeeBot/src/main/resources' },
  {
    prefix: 'ytai_',
    artist: 'Omar Palermo',
    path: 'youTuboAncheI0Bot/src/main/resources',
  },
  {
    prefix: 'cala_',
    artist: 'Francesco Calandra',
    path: 'calandroBot/src/main/resources',
  },
].map((i) => {
  i.path = buildResourceDirectory(baseDir, i.path);
  return i;
});

// Logic //////////////////////////////////////////////////////////////////////

function match(initialArtist: string): Matches[] {
  return [
    {
      check: (f: string) => {
        return (
          path.extname(f) === '.mp3' &&
          botsInput.find(
            (e) =>
              path.basename(f).startsWith(e.prefix) &&
              e.artist === initialArtist
          ) !== undefined
        );
      },
      logic: (f: string) => fixMp3ArtistId3Tag(f, initialArtist),
    },
    {
      check: (f: string) => {
        return (
          path.basename(f).endsWith('Gif.mp4') &&
          botsInput.find(
            (e) =>
              path.basename(f).startsWith(e.prefix) &&
              e.artist === initialArtist
          ) !== undefined
        );
      },
      logic: async (f: string) => {
        const result: MediaInfoCheckReturn = await checkAudioTrackMissing(f);
        if ('tracks' in result) {
          logger.error(
            `[filesCheck] 🚫  ${path.basename(f)} contains audio track ${result.tracks}`
          );
        } else {
          logger.verbose(`[filesCheck] ✓ ${path.basename(f)}`);
        }
      },
    },
    {
      check: (f: string) => {
        return (
          path.basename(f).endsWith('.mp4') &&
          botsInput.find(
            (e) =>
              path.basename(f).startsWith(e.prefix) &&
              e.artist === initialArtist
          ) !== undefined
        );
      },
      logic: async (f: string) => {
        const result: MediaInfoCheckReturn =
          await checkAudioVideoTrackExists(f);
        if ('tracks' in result) {
          logger.error(
            `[filesCheck] 🚫  ${path.basename(f)} doesn't contain both audio and video tracks: ${result.tracks}`
          );
        } else {
          logger.verbose(`[filesCheck] ✓ ${path.basename(f)}`);
        }
      },
    },
    {
      check: (f: string) => {
        const stats = fs.statSync(f);
        let isDir = false;
        if (stats !== undefined) {
          isDir = stats.isDirectory();
        }
        return (
          ([
            'token',
            'jpg',
            'gif', // #807 Remove this
          ].find((ext) => path.basename(f).endsWith(ext)) !== undefined &&
            botsInput.find(
              (e) =>
                path.basename(f).startsWith(e.prefix) &&
                e.artist === initialArtist
            ) !== undefined) ||
          isDir ||
          path.basename(f) === 'application.conf'
        );
      },
      logic: (f: string) => {
        logger.warn(
          `[filesCheck] ⚠️ Ignore ${path.extname(f)} file ${path.basename(f)}`
        );
      },
    },
  ];
}
const defaultLogic: (artist: string) => { logic: (file: string) => void } = (
  artist: string
) => {
  return {
    logic: (file: string) => {
      logger.warn(
        `[filesCheck] ⚠️  Not Processed ${path.basename(file)} for ${artist}`
      );
    },
  };
};

// Entry Point ////////////////////////////////////////////////////////////////

Promise.all(
  botsInput.map((input) => {
    const { artist: initialArtist, path: initialPath } = input;
    return getFiles(initialPath).then((fs) => {
      fs.forEach((f) => {
        const { logic } =
          match(initialArtist).find(({ check }) => {
            return check(f) ?? false;
          }) ?? defaultLogic(initialArtist);
        return logic(f);
      });
    });
  })
)
  .then(() => {
    logger.info('[filesCheck] ✓ Tag sanification completed');
  })
  .catch((err) => {
    logger.error(`[filesCheck] 🚫 Error occurred: ${err}`);
  });
