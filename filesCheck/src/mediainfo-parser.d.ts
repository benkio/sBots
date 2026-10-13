declare module 'mediainfo-parser' {
  export interface MediaTrack {
    readonly type?: string;
    readonly _type?: string;
  }

  export interface MediaInfo {
    media: {
      track: MediaTrack[];
    };
  }

  export type ExecCallback = (err: unknown, result?: MediaInfo) => void;

  export function exec(file: string, cb: ExecCallback): void;
}
