import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import * as util from 'node:util';
import * as NodeID3 from 'node-id3';

// Types //////////////////////////////////////////////////////////////////////
type Input = {
  artist: string;
  path: string;
};

// Inputs /////////////////////////////////////////////////////////////////////
function buildResourceDirectory(botDir: string): string {
  return path.join(os.homedir(), '/Dropbox/sBots/', botDir);
}
const botsInput: Input[] = [
  {
    artist: 'Richard Philip Henry John Benson',
    path: 'richardPHJBensonBot/src/main/resources',
  },
  {artist: 'Alessandro Barbero', path: 'aBarberoBot/src/main/resources'},
  {artist: 'Xah Lee', path: 'xahLeeBot/src/main/resources'},
  {artist: 'Omar Palermo', path: 'youTuboAncheI0Bot/src/main/resources'},
  {artist: 'Francesco Calandra', path: 'calandroBot/src/main/resources'},
].map(i => {
  i.path = buildResourceDirectory(i.path);
  return i;
});

function getMp3Files(path: string): Promise<string[]> {
  return util
    .promisify(fs.readdir)(path)
    .then(files => {
      return files
        .filter((file: string) => {
          return file.endsWith('mp3');
        })
        .map(file => {
          return path + '/' + file;
        });
    });
}

Promise.all(
  botsInput.map(input => {
    const {artist: initialArtist, path: initialPath} = input;
    return getMp3Files(initialPath).then(fs => {
      return fs
        .map(f => {
          const tags = NodeID3.read(f);
          // console.log(`f: ${f} - tags: ${JSON.stringify(tags)}`);
          return {file: f, artistTag: tags.artist};
        })
        .filter(x => {
          return x.artistTag !== undefined && x.artistTag !== initialArtist;
        })
        .forEach(t => {
          console.log(`Update file ${t.file} with artist ${initialArtist}`);
          const result = NodeID3.update({artist: initialArtist}, t.file);
          if (result) {
            console.log('Tag successfully written');
          } else {
            console.log('Tag not updated: operation failed');
          }
        });
    });
  }),
)
  .then(() => {
    console.log('Tag sanification completed');
  })
  .catch(err => {
    console.error(`Error occurred: ${err}`);
  });
