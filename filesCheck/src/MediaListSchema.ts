import { Schema } from 'effect';

export interface MediaListItem {
  filename: string;
  kinds?: readonly string[];
  mime?: string;
  sources: readonly string[];
}

export type MediaList = readonly MediaListItem[];

/** The platforms a file reference can be tagged with: the ids of the `Platform` enum in chatCore. */
export const platformIds = ['telegram', 'discord', 'irc', 'slack'] as const;

/** A source is an url, or a file reference tagged as `<platform id>:<reference>`. An untagged reference is invalid. */
export const sourceSchema = Schema.String.check(
  Schema.isPattern(
    new RegExp(`^(https?://\\S+|(${platformIds.join('|')}):\\S+)$`)
  )
);

export const mediaListItemSchema = Schema.Struct({
  filename: Schema.String,
  kinds: Schema.optional(Schema.Array(Schema.String)),
  mime: Schema.optional(Schema.String),
  sources: Schema.Array(sourceSchema),
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
