// @ts-ignore
import { exec as mediainfoExec } from "mediainfo-parser";
import { promisify } from "node:util";
import logger from "./logger";

type Success = {};
type Failure = { tracks: string[] };
export type MediaInfoCheckReturn = Success | Failure;

export function checkAudioTrackMissing(
  filePath: string,
): Promise<MediaInfoCheckReturn> {
  return promisify(mediainfoExec)(filePath)
    .then((mediaInfoObj: any) => {
      const tracksTypes = mediaInfoObj.media.track.some((tr: any) => {
        if ("type" in tr && tr.type) {
          return tr.type === "Audio";
        } else if ("_type" in tr && tr._type) {
          return tr._type === "Audio";
        } else {
          return false;
        }
      });
      if (tracksTypes) {
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
      logger.error(
        `[mediaInfoFunctions] 🚫 ${filePath} checkAudioTrackMissing Error: ${error}`,
      );
    });
}

export function checkAudioVideoTrackExists(
  filePath: string,
): Promise<MediaInfoCheckReturn> {
  return promisify(mediainfoExec)(filePath)
    .then((mediaInfoObj: any) => {
      const tracksTypes = mediaInfoObj.media.track.map(
        (tr: { type: string } | { _type: string }) => {
          if ("type" in tr && tr.type) {
            return tr.type;
          } else if ("_type" in tr && tr._type) {
            return tr._type;
          } else {
            return "";
          }
        },
      );
      if (tracksTypes.includes("Audio") && tracksTypes.includes("Video")) {
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
      logger.error(
        `[mediaInfoFunctions] 🚫 ${filePath} checkAudioVideoTrackExists Error: ${error}`,
      );
    });
}
