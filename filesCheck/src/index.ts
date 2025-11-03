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
type Bot = {
  id: string;
  artist: string;
  path: string;
};

type Matches = {
  check: (f: string) => Boolean;
  logic: (f: string) => void;
};
// Inputs /////////////////////////////////////////////////////////////////////

const baseDir = '/Dropbox/sBots/';

const bots: Bot[] = [
  {
    id: 'rphjb',
    artist: 'Richard Philip Henry John Benson',
    path: 'richardPHJBensonBot/src/main/resources',
  },
  {
    id: 'abar',
    artist: 'Alessandro Barbero',
    path: 'aBarberoBot/src/main/resources',
  },
  { id: 'xah', artist: 'Xah Lee', path: 'xahLeeBot/src/main/resources' },
  {
    id: 'ytai',
    artist: 'Omar Palermo',
    path: 'youTuboAncheI0Bot/src/main/resources',
  },
  {
    id: 'cala',
    artist: 'Francesco Calandra',
    path: 'calandroBot/src/main/resources',
  },
].map((i) => {
  i.path = buildResourceDirectory(baseDir, i.path);
  return i;
});

// Logic //////////////////////////////////////////////////////////////////////

function match(bot: Bot): Matches[] {
  return [
    {
      check: (f: string) => {
        const mp3Regex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp3$`);
        return mp3Regex.test(path.basename(f));
      },
      logic: (f: string) => fixMp3ArtistId3Tag(f, bot.artist),
    },
    {
      check: (f: string) => {
        const gifRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+Gif.mp4$`);
        return gifRegex.test(path.basename(f));
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
        const videoRegex = new RegExp(`^${bot.id}_[A-Za-z0-9]+.mp4$`);
        return videoRegex.test(path.basename(f));
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
        return path.extname(f) === '.gif';
      },
      logic: (f: string) => {
        logger.error(
          `[filesCheck] 🚫  ${path.basename(f)} Gif is not supported`
        );
      },
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
      logic: (f: string) => {
        logger.error(
          `[filesCheck] 🚫 ${path.basename(f)} file doesn't comply to expected filename or not supported`
        );
      },
    },
    {
      check: (f: string) => {
        const stats = fs.statSync(f);
        return stats.isDirectory() || path.basename(f) === 'application.conf';
      },
      logic: (f: string) => {
        logger.warn(`[filesCheck] ⚠️ Ignore ${path.basename(f)}`);
      },
    },
  ];
}
const defaultLogic: (bot: Bot) => { logic: (file: string) => void } = (
  bot: Bot
) => {
  return {
    logic: (file: string) => {
      logger.warn(
        `[filesCheck] ⚠️  Not Processed ${path.basename(file)} for ${bot.artist}`
      );
    },
  };
};

// Entry Point ////////////////////////////////////////////////////////////////

Promise.all(
  bots.map((bot) => {
    return getFiles(bot.path).then((fs) => {
      fs.forEach((f) => {
        const { logic } =
          match(bot).find(({ check }) => {
            return check(f) ?? false;
          }) ?? defaultLogic(bot);
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
