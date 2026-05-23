import { expect, test } from '@playwright/test';
import { expectApiOk, login } from './helpers';

test('admin APIs work through a real browser session', async ({ page }) => {
  await login(page, 'admin');

  const dashboard = await expectApiOk(await page.request.get('api/admin/dashboard'));
  expect(dashboard.data.totalUsers).toBeGreaterThan(0);
  expect(Array.isArray(dashboard.data.jobs)).toBe(true);

  const demands = await expectApiOk(await page.request.get('api/admin/demands?status=all'));
  expect(Array.isArray(demands.data.items)).toBe(true);

  const outcome = await expectApiOk(await page.request.get('api/admin/recruitment-outcome'));
  expect(outcome.data.totalApplications).toBeGreaterThanOrEqual(0);
});

test('student APIs work through a real browser session', async ({ page }) => {
  await login(page, 'student');

  const profile = await expectApiOk(await page.request.get('api/student/profile'));
  expect(profile.data.email).toContain('@');

  const jobs = await expectApiOk(await page.request.get('api/student/jobs'));
  expect(Array.isArray(jobs.data.items)).toBe(true);

  const applications = await expectApiOk(await page.request.get('api/student/applications'));
  expect(Array.isArray(applications.data.items)).toBe(true);

  const assigned = await expectApiOk(await page.request.get('api/student/my-jobs'));
  expect(Array.isArray(assigned.data.items)).toBe(true);

  const notifications = await expectApiOk(await page.request.get('api/student/notifications'));
  expect(Array.isArray(notifications.data.items)).toBe(true);
});

test('module organiser APIs work through a real browser session', async ({ page }) => {
  await login(page, 'teacher');

  const applications = await expectApiOk(await page.request.get('api/mo/applications'));
  expect(Array.isArray(applications.data.items)).toBe(true);

  const demands = await expectApiOk(await page.request.get('api/mo/demands/list'));
  expect(Array.isArray(demands.data.items)).toBe(true);

  const history = await expectApiOk(await page.request.get('api/mo/jobs/history'));
  expect(Array.isArray(history.data.items)).toBe(true);

  const notifications = await expectApiOk(await page.request.get('api/mo/notifications'));
  expect(Array.isArray(notifications.data.items)).toBe(true);
});

test('wrong role cannot call admin APIs', async ({ page }) => {
  await login(page, 'student');

  const response = await page.request.get('api/admin/dashboard');
  expect(response.status()).toBe(403);
});
