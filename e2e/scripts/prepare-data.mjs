import { cp, mkdir, rm } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..', '..');
const source = path.join(root, 'web', 'src', 'main', 'webapp', 'WEB-INF', 'data');
const target = path.join(root, 'web', 'target', 'e2e-data');

await rm(target, { recursive: true, force: true });
await mkdir(target, { recursive: true });
await cp(source, target, { recursive: true });

console.log(`Prepared isolated E2E JSON data: ${target}`);
