declare module 'mediainfo-parser' {
  export type MediaTrack = {
    readonly type?: string;
    readonly _type?: string;
  };

  export type MediaInfo = {
    media: {
      track: Array<MediaTrack>;
    };
  };

  export type ExecCallback = (err: unknown, result?: MediaInfo) => void;

  export function exec(file: string, cb: ExecCallback): void;
}
