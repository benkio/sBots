import * as NodeID3 from 'node-id3';
import * as path from 'node:path';
import logger from './logger';

function checkArtist(
  initialArtist: string,
  artistTag: string | undefined,
  file: string
): void {
  if (artistTag !== initialArtist) {
    logger.info(
      `[id3Functions] Update file ${path.basename(file)} with artist ${initialArtist}`
    );
    const result = NodeID3.update({ artist: initialArtist }, file);
    if (result) {
      return;
    } else {
      logger.info('[id3Functions] Tag not updated: operation failed');
    }
  } else {
    if (artistTag !== initialArtist) {
      logger.warn(
        `[id3Functions] ⚠️ ${path.basename(file)} not updated: ${artistTag}(file) !== ${initialArtist}(wanted)`
      );
    }
  }
}

function checkTitle(file: string, title: string | undefined): void {
  if (!title) {
    console.error(
      `[id3Functions] 🚫 ${path.basename(file)} doesn't contain a title`
    );
  }
}

export function fixMp3ArtistId3Tag(file: string, initialArtist: string): void {
  const tags = NodeID3.read(file);
  checkArtist(initialArtist, tags.artist, file);
  checkTitle(file, tags.title);
  logger.verbose(`[id3Functions] ✓ ${path.basename(file)}`);
}
