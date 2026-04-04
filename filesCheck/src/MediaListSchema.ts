import * as JSONSchema from 'effect/JSONSchema';
import * as Schema from 'effect/Schema';

export interface MediaListItem {
  filename: string;
  kinds?: ReadonlyArray<string>;
  mime?: string;
  sources: ReadonlyArray<string>;
}

export type MediaList = ReadonlyArray<MediaListItem>;

export const mediaListItemSchema: Schema.Schema<MediaListItem, MediaListItem> =
  Schema.Struct({
    filename: Schema.String,
    kinds: Schema.optional(Schema.Array(Schema.String)),
    mime: Schema.optional(Schema.String),
    sources: Schema.Array(Schema.String),
  });

export const mediaListSchema: Schema.Schema<MediaList, MediaList> =
  Schema.Array(mediaListItemSchema);

export const mediaListJsonSchema: ReturnType<typeof JSONSchema.make> =
  JSONSchema.make(mediaListSchema);

export const decodeMediaList: (input: unknown) => MediaList = (input) =>
  Schema.decodeUnknownSync(mediaListSchema)(input);

export const decodeMediaListFromJson: (jsonText: string) => MediaList = (
  jsonText
) => decodeMediaList(JSON.parse(jsonText));
