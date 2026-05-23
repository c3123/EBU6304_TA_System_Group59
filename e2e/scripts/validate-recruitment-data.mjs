import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataDir = path.join(__dirname, '..', '..', 'web', 'src', 'main', 'webapp', 'WEB-INF', 'data');

const jobs = JSON.parse(await readFile(path.join(dataDir, 'jobs.json'), 'utf8'));
const applications = JSON.parse(await readFile(path.join(dataDir, 'applications.json'), 'utf8'));

const hiredByJob = {};
for (const app of applications) {
  if (!app.active || String(app.status).toLowerCase() !== 'hired') continue;
  hiredByJob[app.jobId] = (hiredByJob[app.jobId] || 0) + 1;
}

const issues = [];
for (const job of jobs) {
  const hired = hiredByJob[job.id] || 0;
  const positions = job.positions || 0;
  if (positions > 0 && hired > positions) {
    issues.push(`over-hired: job ${job.id} (${job.title}) hired=${hired} positions=${positions}`);
  }
  if (
    positions > 0 &&
    hired >= positions &&
    job.status === 'open' &&
    job.recruitmentClosed !== true
  ) {
    issues.push(`should-close: job ${job.id} (${job.title}) hired=${hired} positions=${positions}`);
  }
}

if (issues.length) {
  console.error('Recruitment data validation failed:\n' + issues.map(i => `  - ${i}`).join('\n'));
  process.exit(1);
}

console.log('Recruitment seed data OK.');
