import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { decodeMediaList } from '../src/MediaListSchema';

const item = (sources: string[]) => [{ filename: 'bot_a.mp3', sources }];

describe('MediaListSchema sources', () => {
  it('accepts urls and tagged file references', () => {
    assert.doesNotThrow(() =>
      decodeMediaList(
        item([
          'telegram:AwACAgQ',
          'https://www.dropbox.com/s/x/a.mp3?dl=1',
          'discord:123',
        ])
      )
    );
  });

  it('rejects an untagged file reference', () => {
    assert.throws(() => decodeMediaList(item(['AwACAgQ'])));
  });

  it('rejects an unknown platform tag', () => {
    assert.throws(() => decodeMediaList(item(['whatsapp:123'])));
  });
});
