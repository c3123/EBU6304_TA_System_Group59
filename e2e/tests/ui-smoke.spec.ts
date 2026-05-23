import { expect, test } from '@playwright/test';
import { login } from './helpers';

test('admin portal renders major sections and switches tabs', async ({ page }) => {
  await login(page, 'admin');
  await expect(page.locator('#adminOverviewUsersPie')).toBeVisible();

  await page.locator('[data-admin-tab="workload"]').click();
  await expect(page.locator('[data-admin-panel="workload"]')).toBeVisible();
  await expect(page.locator('#adminWorkloadBody')).toBeVisible();

  await page.locator('[data-admin-tab="users"]').click();
  await expect(page.locator('[data-admin-panel="users"]')).toBeVisible();
  await expect(page.locator('#adminUsersBody')).toBeVisible();

  await page.locator('[data-admin-tab="announcements"]').click();
  await expect(page.locator('#adminAnnouncementForm')).toBeVisible();
});

test('student portal renders jobs, applications, my jobs, and profile panels', async ({ page }) => {
  await login(page, 'student');

  await expect(page.locator('#panel-jobs')).toBeVisible();
  await expect(page.locator('#jobsList:not(.hidden), #jobsEmpty:not(.hidden)')).toBeVisible();

  await page.locator('[data-tab="applications"]').click();
  await expect(page.locator('#panel-applications')).toBeVisible();
  await expect(page.locator('#appsList:not(.hidden), #appsEmpty:not(.hidden)')).toBeVisible();

  await page.locator('[data-tab="hired"]').click();
  await expect(page.locator('#panel-hired')).toBeVisible();
  await expect(page.locator('#jobCalendarGrid:not(.hidden), #hiredEmpty:not(.hidden)')).toBeVisible();

  await page.locator('[data-tab="profile"]').click();
  await expect(page.locator('#panel-profile')).toBeVisible();
  await expect(page.locator('#profileName')).toBeVisible();
});

test('module organiser applicant portal renders summaries and applicant list area', async ({ page }) => {
  await login(page, 'teacher');
  await page.goto('pages/mo-applications.jsp');

  await expect(page.locator('#summaryTotalApplications')).toBeVisible();
  await expect(page.locator('#summaryPendingApplications')).toBeVisible();
  await expect(page.locator('#summaryHiredApplications')).toBeVisible();
  await expect(page.locator('#applicationsFeed')).toBeVisible();
});
