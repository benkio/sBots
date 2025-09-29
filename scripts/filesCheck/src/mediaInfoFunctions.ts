// @ts-ignore
import {exec as mediainfoExec} from 'mediainfo-parser';
import {promisify} from 'node:util';

type Success = {};
type Failure = {tracks: string[]};
export type MediaInfoCheckReturn = Success | Failure;

export function checkAudioTrackMissing(
  filePath: string,
): Promise<MediaInfoCheckReturn> {
  return promisify(mediainfoExec)(filePath)
    .then((mediaInfoObj: any) => {
      const tracksType = mediaInfoObj.media.track.map((tr: {type: string}) => {
        tr.type;
      });
      if (tracksType.includes('Audio')) {
        return {
          tracks: mediaInfoObj.media.track.map((tr: any) => {
            return JSON.stringify(tr);
          }),
        };
      } else {
        return {};
      }
    })
    .catch((error: Error) => {
      console.log(
        `[mediaInfoFunctions] 🚫 ${filePath} checkAudioTrackMissing Error: ${error}`,
      );
    });
}

export function checkAudioVideoTrackExists(
  filePath: string,
): Promise<MediaInfoCheckReturn> {
  return promisify(mediainfoExec)(filePath)
    .then((mediaInfoObj: any) => {
      const tracksType = mediaInfoObj.media.track.map((tr: {type: string}) => {
        return tr.type;
      });
      if (tracksType.includes('Audio') && tracksType.includes('Video')) {
        return {};
      } else {
        return {
          tracks: mediaInfoObj.media.track.map((tr: any) => {
            return JSON.stringify(tr);
          }),
        };
      }
    })
    .catch((error: Error) => {
      console.log(
        `[mediaInfoFunctions] 🚫 ${filePath} checkAudioVideoTrackExists Error: ${error}`,
      );
    });
}
