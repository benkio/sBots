import { Schema } from 'effect';

export interface MediaListItem {
  filename: string;
  kinds?: ReadonlyArray<string>;
  mime?: string;
  sources: ReadonlyArray<string>;
}

export type MediaList = ReadonlyArray<MediaListItem>;

export const mediaListItemSchema = Schema.Struct({
  filename: Schema.String,
  kinds: Schema.optional(Schema.Array(Schema.String)),
  mime: Schema.optional(Schema.String),
  sources: Schema.Array(Schema.String),
});

export const mediaListSchema = Schema.Array(mediaListItemSchema);

export const mediaListJsonSchema: ReturnType<
  typeof Schema.toJsonSchemaDocument
> = Schema.toJsonSchemaDocument(mediaListSchema);

export const decodeMediaList: (input: unknown) => MediaList = (input) =>
  Schema.decodeUnknownSync(mediaListSchema)(input);

export const decodeMediaListFromJson: (jsonText: string) => MediaList = (
  jsonText
) => decodeMediaList(JSON.parse(jsonText));
