export type Bot = {
  id: string;
  artist: string;
  filePath: string;
  jsonFilePath: string;
};

export const baseDir = '/Dropbox/sBots/';

export const bots: Bot[] = [
  {
    id: 'rphjb',
    artist: 'Richard Philip Henry John Benson',
    filePath: 'RichardPHJBensonBot/src/main/resources',
    jsonFilePath: '../modules/bots/RichardPHJBensonBot/rphjb_list.json',
  },
  {
    id: 'abar',
    artist: 'Alessandro Barbero',
    filePath: 'ABarberoBot/src/main/resources',
    jsonFilePath: '../modules/bots/ABarberoBot/abar_list.json',
  },
  {
    id: 'xah',
    artist: 'Xah Lee',
    filePath: 'XahLeeBot/src/main/resources',
    jsonFilePath: '../modules/bots/XahLeeBot/xah_list.json',
  },
  {
    id: 'mos',
    artist: 'Germano Mosconi',
    filePath: 'M0sconiBot/src/main/resources',
    jsonFilePath: '../modules/bots/M0sconiBot/mos_list.json',
  },
  {
    id: 'ytai',
    artist: 'Omar Palermo',
    filePath: 'YouTuboAncheI0Bot/src/main/resources',
    jsonFilePath: '../modules/bots/YouTuboAncheI0Bot/ytai_list.json',
  },
  {
    id: 'cala',
    artist: 'Francesco Calandra',
    filePath: 'CalandroBot/src/main/resources',
    jsonFilePath: '../modules/bots/CalandroBot/cala_list.json',
  },
];
