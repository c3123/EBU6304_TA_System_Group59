import { defineConfig, devices } from '@playwright/test';

const rawBaseURL = process.env.E2E_BASE_URL || 'http://127.0.0.1:18080/web';
const baseURL = rawBaseURL.endsWith('/') ? rawBaseURL : `${rawBaseURL}/`;
const useExternalServer = Boolean(process.env.E2E_BASE_URL) || process.env.E2E_SKIP_WEBSERVER === '1';

export default defineConfig({
  testDir: './e2e/tests',
  timeout: 30_000,
  expect: {
    timeout: 8_000
  },
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'web/target/playwright-report' }]],
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  webServer: useExternalServer
    ? undefined
    : {
        command: 'npm run e2e:tomcat',
        url: new URL('pages/login.jsp', baseURL).toString(),
        reuseExistingServer: true,
        timeout: 300_000,
        stdout: 'pipe',
        stderr: 'pipe'
      },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  outputDir: 'web/target/playwright-results'
});
