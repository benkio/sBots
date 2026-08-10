export type Bot = {
  id: string;
  artist: string;
  filePath: string;
  jsonFilePath: string;
};

export const bots: Bot[] = [
  {
    id: 'orl',
    artist: 'Alessandro Orlando',
    filePath: '/Mega/sBots/Alessandro0rlandoBot/src/main/resources',
    jsonFilePath: '../modules/bots/Alessandro0rlandoBot/orl_list.json',
  },
  {
    id: 'rphjb',
    artist: 'Richard Philip Henry John Benson',
    filePath: '/Mega/sBots/RichardPHJBensonBot/src/main/resources',
    jsonFilePath: '../modules/bots/RichardPHJBensonBot/rphjb_list.json',
  },
  {
    id: 'abar',
    artist: 'Alessandro Barbero',
    filePath: '/Dropbox/sBots/ABarberoBot/src/main/resources',
    jsonFilePath: '../modules/bots/ABarberoBot/abar_list.json',
  },
  {
    id: 'xah',
    artist: 'Xah Lee',
    filePath: '/Dropbox/sBots/XahLeeBot/src/main/resources',
    jsonFilePath: '../modules/bots/XahLeeBot/xah_list.json',
  },
  {
    id: 'mos',
    artist: 'Germano Mosconi',
    filePath: '/Dropbox/sBots/M0sconiBot/src/main/resources',
    jsonFilePath: '../modules/bots/M0sconiBot/mos_list.json',
  },
  {
    id: 'ytai',
    artist: 'Omar Palermo',
    filePath: '/Dropbox/sBots/YouTuboAncheI0Bot/src/main/resources',
    jsonFilePath: '../modules/bots/YouTuboAncheI0Bot/ytai_list.json',
  },
  {
    id: 'cala',
    artist: 'Francesco Calandra',
    filePath: '/Dropbox/sBots/CalandroBot/src/main/resources',
    jsonFilePath: '../modules/bots/CalandroBot/cala_list.json',
  },
];
