import { expect, type APIResponse, type Page } from '@playwright/test';

export const credentials = {
  student: { email: 'student@demo.com', password: 'demo123', path: /\/pages\/student\.jsp$/ },
  teacher: { email: 'teacher@demo.com', password: 'demo123', path: /\/pages\/(teacher|mo-applications)\.jsp$/ },
  admin: { email: 'admin@demo.com', password: 'demo123', path: /\/pages\/admin\.jsp$/ }
} as const;

export type Role = keyof typeof credentials;

export async function login(page: Page, role: Role) {
  const account = credentials[role];
  await page.goto('pages/login.jsp');
  await page.locator('input[name="identifier"]').fill(account.email);
  await page.locator('input[name="password"]').fill(account.password);
  await Promise.all([
    page.waitForURL(account.path),
    page.getByRole('button', { name: 'Login' }).click()
  ]);
}

export async function expectApiOk(response: APIResponse) {
  expect(response.ok(), `${response.url()} should return HTTP 2xx`).toBeTruthy();
  const contentType = response.headers()['content-type'] || '';
  expect(contentType).toContain('application/json');
  const body = await response.json();
  expect(body.success, `${response.url()} should use ApiResponse success=true`).toBe(true);
  expect(body.code).toBe('OK');
  return body;
}
