import {fixMp3ArtistId3Tag} from './id3Functions';
import * as path from 'node:path';
import {buildResourceDirectory, getFiles} from './fileFunctions';

// Types //////////////////////////////////////////////////////////////////////
type Input = {
  artist: string;
  path: string;
};

// Inputs /////////////////////////////////////////////////////////////////////

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

// Logic //////////////////////////////////////////////////////////////////////

function match(initialArtist: string) {
  return [
    {
      check: (f: string) => {
        return path.extname(f) == '.mp3';
      },
      logic: (f: string) => fixMp3ArtistId3Tag(f, initialArtist),
    },
  ];
}
const defaultLogic = {
  logic: (file: string) => {
    console.log(`No action on ${path.basename(file)}`);
    return;
  },
};

// Entry Point ////////////////////////////////////////////////////////////////

Promise.all(
  botsInput.map(input => {
    const {artist: initialArtist, path: initialPath} = input;
    return getFiles(initialPath).then(fs => {
      fs.forEach(f => {
        const {logic} =
          match(initialArtist).find(({check}) => {
            return check(f);
          }) ?? defaultLogic;
        return logic(f);
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
