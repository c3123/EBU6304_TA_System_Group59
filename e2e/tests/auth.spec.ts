import { expect, test } from '@playwright/test';
import { credentials, login, type Role } from './helpers';

test('protected page redirects anonymous users to login', async ({ page }) => {
  await page.goto('pages/admin.jsp');

  await expect(page).toHaveURL(/\/pages\/login\.jsp\?error=auth$/);
  await expect(page.getByRole('heading', { name: 'Sign In' })).toBeVisible();
});

test('protected API rejects anonymous users', async ({ request }) => {
  const response = await request.get('api/admin/dashboard');

  expect(response.status()).toBe(401);
});

test('invalid login shows an error and stays on login page', async ({ page }) => {
  await page.goto('pages/login.jsp');
  await page.locator('input[name="identifier"]').fill('student@demo.com');
  await page.locator('input[name="password"]').fill('wrong-password');
  await Promise.all([
    page.waitForURL(/\/pages\/login\.jsp\?error=invalid$/),
    page.getByRole('button', { name: 'Login' }).click()
  ]);

  await expect(page.getByText('Invalid ID or Password.')).toBeVisible();
});

for (const role of Object.keys(credentials) as Role[]) {
  test(`${role} can log in and reach the correct portal`, async ({ page }) => {
    await login(page, role);

    if (role === 'student') {
      await expect(page.getByRole('heading', { name: 'Available Jobs' })).toBeVisible();
    } else if (role === 'teacher') {
      await expect(page.getByText(/Module Organiser|Applicants|Teacher Portal/).first()).toBeVisible();
    } else {
      await expect(page.getByRole('heading', { name: 'System Overview' })).toBeVisible();
    }
  });
}
