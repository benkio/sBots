import * as NodeID3 from 'node-id3';
import * as path from 'node:path';
import logger from './logger';

export function fixMp3ArtistId3Tag(file: string, initialArtist: string): void {
  const artistTag = NodeID3.read(file).artist;
  if (artistTag !== undefined && artistTag !== initialArtist) {
    logger.info(
      `[id3Functions] Update file ${path.basename(file)} with artist ${initialArtist}`,
    );
    const result = NodeID3.update({artist: initialArtist}, file);
    if (result) {
      logger.info('[id3Functions] Tag successfully written');
    } else {
      logger.info('[id3Functions] Tag not updated: operation failed');
    }
  } else {
    if (artistTag === initialArtist) {
      logger.verbose(`[id3Functions] ✓ ${path.basename(file)}`);
    } else {
      logger.warn(
        `[⚠️ id3Functions] ${path.basename(file)} not updated: initialArtist == undefined: ${initialArtist}`,
      );
    }
  }
}
