import * as os from 'node:os';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as util from 'node:util';

export function buildResourceDirectory(
  baseDir: string,
  botDir: string,
): string {
  return path.join(os.homedir(), baseDir, botDir);
}
export function getFiles(path: string): Promise<string[]> {
  return util
    .promisify(fs.readdir)(path)
    .then(files => {
      return files.map(file => {
        return path + '/' + file;
      });
    });
}
