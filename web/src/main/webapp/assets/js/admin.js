document.addEventListener("DOMContentLoaded", async () => {
  const portal = document.querySelector(".admin-portal");
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
  const workloadBody = byId("adminWorkloadBody");
  const workloadLayout = byId("adminWorkloadLayout");
  const workloadDrawer = byId("adminWorkloadDrawer");
  const workloadDrawerTitle = byId("adminWorkloadDrawerTitle");
  const workloadDrawerBody = byId("adminWorkloadDrawerBody");
  const workloadDrawerClose = byId("adminWorkloadDrawerClose");
  const usersGrouped = byId("adminUsersGrouped");
  const jobsCards = byId("adminJobsCards");
  const workloadCards = byId("adminWorkloadCards");
  const alertsButton = byId("adminAlertsButton");
  const alertsBadge = byId("adminAlertsBadge");
  const alertsModal = byId("adminAlertsModal");
  const alertsCloseBtn = byId("adminAlertsCloseBtn");
  const alertsList = byId("adminAlertsList");
  const overviewOutcomeBtn = byId("adminOverviewOutcomeBtn");
  const sidebarOutcomeBtn = byId("adminSidebarOutcomeBtn");
  const demandStatusFilterEl = byId("adminDemandStatusFilter");
  const demandRefreshBtn = byId("adminDemandRefreshBtn");
  const demandCards = byId("adminDemandCards");
  const demandBody = byId("adminDemandBody");
  const jobApplicationsPanel = byId("adminJobApplicationsPanel");
  const jobApplicationsTitle = byId("adminJobApplicationsTitle");
  const jobApplicationsCloseBtn = byId("adminJobApplicationsCloseBtn");
  const jobApplicationsStatusFilterEl = byId("adminJobApplicationStatusFilter");
  const jobApplicationsApplyBtn = byId("adminJobApplicationsApplyBtn");
  const jobApplicationsCsvBtn = byId("adminJobApplicationsCsvBtn");
  const jobApplicationsTxtBtn = byId("adminJobApplicationsTxtBtn");
  const jobApplicationCards = byId("adminJobApplicationCards");
  const jobApplicationBody = byId("adminJobApplicationBody");
  const tabs = Array.from(document.querySelectorAll("[data-admin-tab]"));
  const panels = Array.from(document.querySelectorAll("[data-admin-panel]"));
  const workloadPanel = panels.find((p) => p.getAttribute("data-admin-panel") === "workload") || null;
  const noticeEl = byId("adminNotice");
  const adminOutcomeJobSince = byId("adminOutcomeJobSince");
  const adminOutcomeJobUntil = byId("adminOutcomeJobUntil");
  const adminOutcomeApplyRangeBtn = byId("adminOutcomeApplyRangeBtn");
  const adminOutcomeClearRangeBtn = byId("adminOutcomeClearRangeBtn");
  const adminOutcomeExportCsvBtn = byId("adminOutcomeExportCsvBtn");
  const adminOutcomeBackBtn = byId("adminOutcomeBackBtn");

  const createUserForm = byId("adminCreateUserForm");
  const createRoleEl = byId("adminCreateRole");
  const createNameEl = byId("adminCreateName");
  const createEmailEl = byId("adminCreateEmail");
  const createPasswordEl = byId("adminCreatePassword");
  const createStudentIdEl = byId("adminCreateStudentId");
  const createProgrammeEl = byId("adminCreateProgramme");
  const createButton = byId("adminCreateUserBtn");
  const studentIdField = byId("adminStudentIdField");
  const programmeField = byId("adminProgrammeField");
  const userSearchInput = byId("adminUserSearchInput");
  const userSearchBtn = byId("adminUserSearchBtn");
  const userSearchClearBtn = byId("adminUserSearchClearBtn");
  const userSearchMeta = byId("adminUserSearchMeta");

  const thresholdForm = byId("adminThresholdForm");
  const thresholdHoursEl = byId("adminThresholdHours");
  const thresholdNormalPercentEl = byId("adminThresholdNormalPercent");
  const thresholdWarningPercentEl = byId("adminThresholdWarningPercent");
  const thresholdUpdatedAtEl = byId("adminThresholdUpdatedAt");
  const thresholdSaveBtn = byId("adminThresholdSaveBtn");
  const notifyOverloadBtn = byId("adminNotifyOverloadBtn");
  const exportWorkloadCsvBtn = byId("adminExportWorkloadCsvBtn");
  const exportWorkloadTxtBtn = byId("adminExportWorkloadTxtBtn");

  const statusFilterEl = byId("adminJobStatusFilter");
  const departmentFilterEl = byId("adminJobDepartmentFilter");
  const teacherFilterEl = byId("adminJobTeacherFilter");
  const applyFiltersBtn = byId("adminApplyFiltersBtn");
  const resetFiltersBtn = byId("adminResetFiltersBtn");
  const exportCsvBtn = byId("adminExportCsvBtn");
  const exportTxtBtn = byId("adminExportTxtBtn");
  const backupBtn = byId("adminBackupBtn");

  const changePasswordForm = byId("adminChangePasswordForm");
  const changePasswordBtn = byId("adminChangePasswordBtn");
  const announcementForm = byId("adminAnnouncementForm");
  const announcementTitleEl = byId("adminAnnouncementTitle");
  const announcementBodyEl = byId("adminAnnouncementBody");
  const announcementTargetEl = byId("adminAnnouncementTarget");
  const announcementSendBtn = byId("adminAnnouncementSendBtn");
  const announcementResultEl = byId("adminAnnouncementResult");
  const announcementHistoryEl = byId("adminAnnouncementHistory");
  const announcementRefreshBtn = byId("adminAnnouncementRefreshBtn");
  let latestAnnouncements = [];

  const currentUserId = portal?.getAttribute("data-current-user-id") || "";
  const currentUserName = portal?.getAttribute("data-current-user-name") || "Admin User";

  let latestData = null;
  let latestDemands = null;
  /** When set, workload table re-expands this student after dashboard reload (e.g. save threshold). */
  let openWorkloadStudentId = null;
  let currentJobApplicationJobId = null;
  let currentJobApplicationTitle = "";
  let knownDepartments = [];
  let knownTeachers = [];
  const filters = {
    status: "all",
    department: "all",
    teacher: "all"
  };
  const demandFilters = {
    status: "all"
  };
  const userFilters = {
    search: ""
  };
  const RECRUITMENT_VACANCY_TOP = 10;
  const outcomeJobDateRange = { since: "", until: "" };
  const JOB_STATUS_CHART_COLORS = {
    Pending: "#f59e0b",
    Reject: "#ef4444",
    Open: "#10b981",
    Overdue: "#dc2626"
  };

  function toApiDate(displayValue) {
    const raw = (displayValue || "").trim();
    if (!raw) {
      return "";
    }
    const normalized = raw.replace(/\//g, "-");
    return /^\d{4}-\d{2}-\d{2}$/.test(normalized) ? normalized : "";
  }

  function toDisplayDate(apiValue) {
    const raw = (apiValue || "").trim();
    if (!raw) {
      return "";
    }
    const part = raw.length >= 10 ? raw.substring(0, 10) : raw;
    return /^\d{4}-\d{2}-\d{2}$/.test(part) ? part.replace(/-/g, "/") : raw.replace(/-/g, "/");
  }
  const jobApplicationFilters = {
    status: "all"
  };

  const chartStore = {
    overviewUsersPie: null,
    overviewJobsLine: null,
    overviewAppsLine: null,
    jobsDeptPie: null,
    jobsStatusPie: null
  };

  const CHART_COLORS = ["#2563eb", "#10b981", "#f59e0b", "#8b5cf6", "#ef4444", "#06b6d4", "#64748b", "#ec4899"];

  function destroyChart(key) {
    if (chartStore[key]) {
      chartStore[key].destroy();
      chartStore[key] = null;
    }
  }

  function chartPalette(size) {
    return CHART_COLORS.slice(0, Math.max(size, 1));
  }

  function renderOverviewCharts(data) {
    if (typeof Chart === "undefined") {
      return;
    }
    const students = Number(data.totalStudents) || 0;
    const teachers = Number(data.totalTeachers) || 0;
    const admins = Number(data.totalAdmins) || 0;
    const usersCanvas = byId("adminOverviewUsersPie");
    if (usersCanvas) {
      destroyChart("overviewUsersPie");
      chartStore.overviewUsersPie = new Chart(usersCanvas, {
        type: "pie",
        data: {
          labels: ["Students", "Teachers", "Admins"],
          datasets: [{
            data: [students, teachers, admins],
            backgroundColor: chartPalette(3)
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: "bottom" } }
        }
      });
    }

    const jobDays = (data.dailyJobPublications || []).map((row) => row.day);
    const jobCounts = (data.dailyJobPublications || []).map((row) => Number(row.count) || 0);
    const jobsLineCanvas = byId("adminOverviewJobsLine");
    if (jobsLineCanvas) {
      destroyChart("overviewJobsLine");
      chartStore.overviewJobsLine = new Chart(jobsLineCanvas, {
        type: "line",
        data: {
          labels: jobDays,
          datasets: [{
            label: "Published jobs",
            data: jobCounts,
            borderColor: "#2563eb",
            backgroundColor: "rgba(37, 99, 235, 0.12)",
            fill: true,
            tension: 0.25
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
      });
    }

    const appDays = (data.dailyApplications || []).map((row) => row.day);
    const appCounts = (data.dailyApplications || []).map((row) => Number(row.count) || 0);
    const appsLineCanvas = byId("adminOverviewAppsLine");
    if (appsLineCanvas) {
      destroyChart("overviewAppsLine");
      chartStore.overviewAppsLine = new Chart(appsLineCanvas, {
        type: "line",
        data: {
          labels: appDays,
          datasets: [{
            label: "Applications",
            data: appCounts,
            borderColor: "#10b981",
            backgroundColor: "rgba(16, 185, 129, 0.12)",
            fill: true,
            tension: 0.25
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
      });
    }
  }

  function renderJobCharts(data) {
    if (typeof Chart === "undefined") {
      return;
    }
    const deptSlices = data.jobsByDepartment || [];
    const statusSlices = data.jobsByStatus || [];
    const deptCanvas = byId("adminJobsDeptPie");
    if (deptCanvas) {
      destroyChart("jobsDeptPie");
      chartStore.jobsDeptPie = new Chart(deptCanvas, {
        type: "pie",
        data: {
          labels: deptSlices.map((s) => s.label),
          datasets: [{
            data: deptSlices.map((s) => Number(s.count) || 0),
            backgroundColor: chartPalette(deptSlices.length)
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: "bottom" } }
        }
      });
    }
    const statusCanvas = byId("adminJobsStatusPie");
    if (statusCanvas) {
      destroyChart("jobsStatusPie");
      chartStore.jobsStatusPie = new Chart(statusCanvas, {
        type: "pie",
        data: {
          labels: statusSlices.map((s) => s.label),
          datasets: [{
            data: statusSlices.map((s) => Number(s.count) || 0),
            backgroundColor: statusSlices.map((s) => JOB_STATUS_CHART_COLORS[s.label] || "#64748b")
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: "bottom" } }
        }
      });
    }
  }

  function setNotice(message, isError) {
    if (!noticeEl) return;
    noticeEl.textContent = message || "";
    noticeEl.style.color = isError ? "#b91c1c" : "";
  }

  function activateTab(tabName) {
    tabs.forEach((tab) => {
      const active = tab.getAttribute("data-admin-tab") === tabName;
      tab.classList.toggle("active", active);
      tab.setAttribute("aria-selected", active ? "true" : "false");
    });
    panels.forEach((panel) => {
      panel.classList.toggle("admin-hidden", panel.getAttribute("data-admin-panel") !== tabName);
    });

    const portalMain = document.querySelector(".admin-portal-main");
    if (portalMain) {
      portalMain.classList.toggle("admin-portal-main--wide", tabName === "recruitment-outcome");
    }

    const title = byId("adminSubTitle");
    if (!title) return;
    if (tabName === "workload") title.textContent = "TA Workload Statistics";
    if (tabName === "users") title.textContent = "User Management";
    if (tabName === "demands") title.textContent = "Demand Approval Workbench";
    if (tabName === "jobs") {
      title.textContent = "Job Management";
      if (latestData) {
        renderJobCharts(latestData);
      }
    }
    if (tabName === "announcements") {
      title.textContent = "System Announcements";
      void loadAnnouncements();
    }
    if (tabName === "account") title.textContent = "My Account";
    if (tabName === "overview") title.textContent = `Welcome, ${currentUserName}`;
    if (tabName === "recruitment-outcome") title.textContent = "Recruitment Results (leadership view)";
  }

  function openRecruitmentOutcome() {
    tabs.forEach((tab) => {
      tab.classList.remove("active");
      tab.setAttribute("aria-selected", "false");
    });
    panels.forEach((panel) => {
      panel.classList.toggle("admin-hidden", panel.getAttribute("data-admin-panel") !== "recruitment-outcome");
    });
    const portalMain = document.querySelector(".admin-portal-main");
    if (portalMain) {
      portalMain.classList.add("admin-portal-main--wide");
    }
    const title = byId("adminSubTitle");
    if (title) {
      title.textContent = "Recruitment Results (leadership view)";
    }
    void loadRecruitmentOutcome();
  }

  function prefersReducedMotion() {
    return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  function syncCreateRoleFields() {
    const isStudent = createRoleEl.value === "student";
    studentIdField.style.display = isStudent ? "" : "none";
    programmeField.style.display = isStudent ? "" : "none";
    createStudentIdEl.required = isStudent;
    createProgrammeEl.required = isStudent;
    if (!isStudent) {
      createStudentIdEl.value = "";
      createProgrammeEl.value = "";
    }
  }

  async function requestJson(url, options) {
    const response = await fetch(url, Object.assign({
      credentials: "same-origin"
    }, options || {}));
    const body = await response.json();
    if (!response.ok || !body.success) {
      const error = new Error(body.message || "Request failed.");
      error.code = body.code || "REQUEST_ERROR";
      throw error;
    }
    return body.data;
  }

  function renderDepartmentOptions() {
    if (!departmentFilterEl) return;
    departmentFilterEl.innerHTML = `<option value="all">All Departments</option>` + knownDepartments
      .map((value) => `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`)
      .join("");
    departmentFilterEl.value = filters.department;
  }

  function renderTeacherOptions() {
    if (!teacherFilterEl) return;
    teacherFilterEl.innerHTML = `<option value="all">All Teachers</option>` + knownTeachers
      .map((value) => `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`)
      .join("");
    teacherFilterEl.value = filters.teacher;
  }

  function buildRecruitmentOutcomeQueryParams() {
    const params = new URLSearchParams();
    params.set("vacancyTop", String(RECRUITMENT_VACANCY_TOP));
    if (outcomeJobDateRange.since) {
      params.set("jobSince", outcomeJobDateRange.since);
    }
    if (outcomeJobDateRange.until) {
      params.set("jobUntil", outcomeJobDateRange.until);
    }
    return params;
  }

  function formatOutcomeGeneratedAt(iso) {
    if (!iso) return "";
    try {
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) {
        return String(iso);
      }
      return d.toLocaleString("en-GB", { dateStyle: "medium", timeStyle: "short" });
    } catch {
      return String(iso);
    }
  }

  function renderRecruitmentDepartmentChart(rows) {
    const chart = byId("adminOutcomeDeptChart");
    if (!chart) return;
    const list = Array.isArray(rows) ? rows : [];
    if (list.length === 0) {
      chart.innerHTML = `<p class="desc">No department breakdown (no jobs or applications yet).</p>`;
      return;
    }
    let max = 1;
    list.forEach((r) => {
      max = Math.max(max, Number(r.hiredCount) || 0, Number(r.vacancyCount) || 0);
    });
    chart.innerHTML = list.map((r) => {
      const h = Number(r.hiredCount) || 0;
      const v = Number(r.vacancyCount) || 0;
      const hw = max ? (h / max) * 100 : 0;
      const vw = max ? (v / max) * 100 : 0;
      return `
      <div class="admin-outcome-dept-row">
        <div class="admin-outcome-dept-name">${escapeHtml(r.department)}</div>
        <div class="admin-outcome-dept-metrics">
          <div class="admin-outcome-dept-metric">
            <span class="admin-outcome-dept-metric-label">Hired</span>
            <div class="admin-outcome-bar-track" role="presentation"><div class="admin-outcome-bar-fill admin-outcome-bar-fill--hired" style="width:${hw}%"></div></div>
            <span class="admin-outcome-dept-metric-val">${h}</span>
          </div>
          <div class="admin-outcome-dept-metric">
            <span class="admin-outcome-dept-metric-label">Vacancies</span>
            <div class="admin-outcome-bar-track" role="presentation"><div class="admin-outcome-bar-fill admin-outcome-bar-fill--vac" style="width:${vw}%"></div></div>
            <span class="admin-outcome-dept-metric-val">${v}</span>
          </div>
        </div>
      </div>`;
    }).join("");
  }

  function renderRecruitmentMixChart(data) {
    const chart = byId("adminOutcomeMixChart");
    if (!chart) return;
    const slots = Math.max(Number(data.totalPositionSlots) || 0, 0);
    const vacancies = Math.max(Number(data.totalVacancies) || 0, 0);
    const hired = Math.max(slots - vacancies, 0);
    if (slots === 0 && hired === 0 && vacancies === 0) {
      chart.innerHTML = `<p class="desc">No recruitment result data yet.</p>`;
      return;
    }
    const total = Math.max(slots, 1);
    const hiredDeg = Math.min(360, Math.round((hired / total) * 360));
    const vacancyDeg = Math.min(360 - hiredDeg, Math.round((vacancies / total) * 360));
    chart.innerHTML = `
      <div class="admin-donut" style="--hired-deg:${hiredDeg}deg;--vacancy-deg:${vacancyDeg}deg;--spare-deg:0deg;" role="img" aria-label="Hired ${hired}, vacancies ${vacancies}, total slots ${slots}">
        <div class="admin-donut-hole">
          <strong>${escapeHtml(String(slots))}</strong>
          <span>slots</span>
        </div>
      </div>
      <div class="admin-donut-legend">
        <span><i class="admin-dot admin-dot-hired"></i>Hired ${escapeHtml(String(hired))}</span>
        <span><i class="admin-dot admin-dot-vacancy"></i>Vacancies ${escapeHtml(String(vacancies))}</span>
        <span><i class="admin-dot admin-dot-capacity"></i>Total slots ${escapeHtml(String(slots))}</span>
      </div>
    `;
  }

  function recruitmentOutcomeDisplayCell(value) {
    const s = String(value ?? "").trim();
    return s ? escapeHtml(s) : "\u2014";
  }

  function renderRecruitmentVacancyTable(rows, vacancyTopLimit) {
    const body = byId("adminOutcomeVacancyBody");
    const help = byId("adminOutcomeVacancyHelp");
    if (help) {
      const cap = Number(vacancyTopLimit);
      if (Number.isFinite(cap)) {
        help.textContent = `Non-withdrawn jobs with unfilled slots, highest vacancy first (showing up to ${cap} rows).`;
      }
    }
    if (!body) return;
    const list = Array.isArray(rows) ? rows : [];
    if (list.length === 0) {
      body.innerHTML = `<tr><td colspan="8" class="desc" style="padding:12px;">No unfilled positions: every active job is fully hired or has zero headcount.</td></tr>`;
      return;
    }
    body.innerHTML = list.map((r, idx) => `
      <tr>
        <td>${idx + 1}</td>
        <td>${recruitmentOutcomeDisplayCell(r.moduleCode)}</td>
        <td>${recruitmentOutcomeDisplayCell(r.title)}</td>
        <td>${recruitmentOutcomeDisplayCell(r.department)}</td>
        <td>${recruitmentOutcomeDisplayCell(r.teacherName)}</td>
        <td>${Number(r.positions) || 0}</td>
        <td>${Number(r.hiredCount) || 0}</td>
        <td><span class="admin-outcome-vacancy-val">${Number(r.vacancyCount) || 0}</span></td>
      </tr>
    `).join("");
  }

  async function loadRecruitmentOutcome() {
    try {
      const data = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/recruitment-outcome?${buildRecruitmentOutcomeQueryParams().toString()}`, {
        method: "GET"
      });
      outcomeJobDateRange.since = data.jobSince || "";
      outcomeJobDateRange.until = data.jobUntil || "";
      if (adminOutcomeJobSince) {
        adminOutcomeJobSince.value = toDisplayDate(outcomeJobDateRange.since);
      }
      if (adminOutcomeJobUntil) {
        adminOutcomeJobUntil.value = toDisplayDate(outcomeJobDateRange.until);
      }
      const slots = byId("adminOutcomeTotalSlots");
      const hired = byId("adminOutcomeTotalHired");
      const vac = byId("adminOutcomeTotalVacancies");
      const openJobs = byId("adminOutcomeOpenJobs");
      const apps = byId("adminOutcomeTotalApplications");
      if (slots) slots.textContent = data.totalPositionSlots ?? 0;
      if (hired) hired.textContent = data.totalHired ?? 0;
      if (vac) vac.textContent = data.totalVacancies ?? 0;
      if (openJobs) openJobs.textContent = data.recruitingJobs ?? 0;
      if (apps) apps.textContent = data.totalApplications ?? 0;
      const genEl = byId("adminOutcomeGeneratedAt");
      if (genEl && data.generatedAt) {
        genEl.setAttribute("datetime", data.generatedAt);
        genEl.textContent = formatOutcomeGeneratedAt(data.generatedAt);
      } else if (genEl) {
        genEl.removeAttribute("datetime");
        genEl.textContent = "-";
      }
      renderRecruitmentDepartmentChart(data.departments);
      renderRecruitmentMixChart(data);
      renderRecruitmentVacancyTable(data.topVacancyJobs, data.vacancyTopLimit);
    } catch (err) {
      setNotice(err.message, true);
    }
  }

  async function loadAdminDashboard() {
    const params = new URLSearchParams();
    params.set("status", filters.status || "all");
    params.set("department", filters.department || "all");

    const data = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/dashboard?${params.toString()}`, {
      method: "GET"
    });
    latestData = data || {};

    if (filters.status === "all" && filters.department === "all") {
      knownDepartments = Array.from(new Set((latestData.jobs || [])
        .map((job) => String(job.department || "").trim())
        .filter(Boolean)))
        .sort((a, b) => a.localeCompare(b));
      knownTeachers = Array.from(new Set((latestData.jobs || [])
        .map((job) => String(job.teacherName || "").trim())
        .filter(Boolean)))
        .sort((a, b) => a.localeCompare(b));
      renderDepartmentOptions();
      renderTeacherOptions();
    }

    renderOverview(latestData);
    renderOverviewCharts(latestData);
    renderJobCharts(latestData);
    renderUsers(filteredUsersForDisplay(latestData.users || []), latestData.users || []);
    renderJobs(filteredJobsForDisplay(latestData.jobs || []));
    renderWorkload(latestData.workload || []);
    renderAlerts(latestData.alerts || []);
  }

  function filteredJobsForDisplay(jobs) {
    if (!filters.teacher || filters.teacher === "all") {
      return jobs;
    }
    return jobs.filter((job) => String(job.teacherName || "").trim() === filters.teacher);
  }

  function filteredUsersForDisplay(users) {
    const query = String(userFilters.search || "").trim().toLowerCase();
    if (!query) {
      return users;
    }
    return users.filter((user) => [
      user.name,
      user.email,
      user.role,
      user.id,
      formatRole(user.role)
    ].some((value) => String(value || "").toLowerCase().includes(query)));
  }

  async function loadDemands() {
    const params = new URLSearchParams();
    params.set("status", demandFilters.status || "pending");
    latestDemands = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/demands?${params.toString()}`, {
      method: "GET"
    });
    renderDemands((latestDemands && latestDemands.items) || []);
  }

  async function loadThresholdSettings() {
    const settings = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/settings/workload-threshold`, {
      method: "GET"
    });
    thresholdHoursEl.value = settings.workloadThresholdHours;
    if (thresholdNormalPercentEl) thresholdNormalPercentEl.value = settings.workloadNormalPercent ?? 50;
    if (thresholdWarningPercentEl) thresholdWarningPercentEl.value = settings.workloadWarningPercent ?? 75;
    thresholdUpdatedAtEl.value = settings.updatedAt || "";
  }

  function workloadHoursAtPercent(threshold, percent) {
    const t = Number(threshold);
    const p = Number(percent);
    if (!Number.isFinite(t) || t <= 0 || !Number.isFinite(p)) return 0;
    return Math.max(1, Math.ceil(t * p / 100));
  }

  function renderOverview(data) {
    byId("overviewOpenJobs").textContent = data.totalActiveJobs ?? 0;
    byId("overviewClosedJobs").textContent = data.totalClosedJobs ?? 0;
    byId("overviewHiredCount").textContent = data.totalHiredRecords ?? 0;
    byId("overviewUnfilledPositions").textContent = data.totalUnfilledPositions ?? 0;
    byId("overviewRiskStudents").textContent = Number(data.totalWarningStudents || 0) + Number(data.totalOverloadedStudents || 0);
    byId("overviewAlerts").textContent = (data.alerts || []).length;
  }

  function buildUserActions(user, adminCount) {
    const isSelf = currentUserId && user.id === currentUserId;
    const isLastAdmin = user.role === "admin" && adminCount <= 1;
    const deleteDisabled = isSelf || isLastAdmin;
    const deleteTitle = isSelf
      ? "You cannot delete your own account."
      : (isLastAdmin ? "You cannot delete the last admin account." : "Delete this user.");

    return `
      <div class="row" style="gap:8px;flex-wrap:wrap;">
        <button class="btn btn-outline" type="button" data-reset-user-id="${user.id}" data-user-name="${escapeHtml(user.name)}">Reset Password</button>
        <button class="btn btn-outline" type="button" data-delete-user-id="${user.id}" data-user-name="${escapeHtml(user.name)}" style="color:#b91c1c;border-color:#fecaca;" ${deleteDisabled ? "disabled" : ""} title="${escapeHtml(deleteTitle)}">Delete</button>
      </div>
    `;
  }

  function renderUsers(users, allUsers) {
    const sourceUsers = Array.isArray(allUsers) ? allUsers : users;
    const adminCount = sourceUsers.filter((user) => user.role === "admin").length;
    if (userSearchMeta) {
      const query = String(userFilters.search || "").trim();
      userSearchMeta.textContent = query
        ? `Showing ${users.length} of ${sourceUsers.length} users for "${query}".`
        : `Showing all ${sourceUsers.length} users.`;
    }
    const roleGroups = {
      student: { title: "Students", items: [] },
      teacher: { title: "Teachers", items: [] },
      admin: { title: "Administrators", items: [] }
    };
    users.forEach((user) => {
      const key = user.role in roleGroups ? user.role : "student";
      roleGroups[key].items.push(user);
    });

    usersGrouped.innerHTML = Object.values(roleGroups).map((group) => `
      <div class="card">
        <h3 class="admin-subtitle">${group.title} (${group.items.length})</h3>
        <div class="admin-list">
          ${group.items.map((user) => `
            <div class="admin-list-item">
              <div>
                <p class="admin-list-name">${escapeHtml(user.name)}</p>
                <p class="admin-list-meta">${escapeHtml(user.email)} | ${escapeHtml(user.id)}</p>
              </div>
              ${buildUserActions(user, adminCount)}
            </div>
          `).join("") || `<p class="admin-empty-text">No users found.</p>`}
        </div>
      </div>
    `).join("");

    usersBody.innerHTML = users.map((user) => `
      <tr>
        <td>${escapeHtml(user.name)}</td>
        <td>${escapeHtml(user.email)}</td>
        <td>${escapeHtml(formatRole(user.role))}</td>
        <td>${escapeHtml(user.id)}</td>
        <td>${buildUserActions(user, adminCount)}</td>
      </tr>
    `).join("") || `<tr><td colspan="5">No users match the current search.</td></tr>`;
  }

  function renderDemands(items) {
    const rows = Array.isArray(items) ? items : [];
    if (demandCards) {
      demandCards.innerHTML = rows.map((item) => {
        const status = String(item.approvalStatus || "pending").toLowerCase();
        return `
          <article class="card admin-demand-card admin-demand-${escapeHtml(status)}">
            <div class="admin-job-title-line">
              <h3 class="admin-job-title">${escapeHtml(item.moduleCode || "-")} - ${escapeHtml(item.title || "Untitled Demand")}</h3>
              <span class="tag ${demandStatusClass(status)}">${escapeHtml(formatStatus(status))}</span>
            </div>
            <p class="admin-list-meta">Submitted by ${escapeHtml(item.teacherName || item.moId || "-")} on ${escapeHtml(formatDate(item.createdAt) || "-")}</p>
            <div class="admin-demand-detail-grid">
              <div><span class="admin-key">Department</span><strong>${escapeHtml(item.department || "-")}</strong></div>
              <div><span class="admin-key">Planned Count</span><strong>${escapeHtml(String(item.plannedCount ?? "-"))}</strong></div>
              <div><span class="admin-key">Hours</span><strong>${escapeHtml(formatHourRange(item))}</strong></div>
              <div><span class="admin-key">Reviewed</span><strong>${escapeHtml(formatDate(item.reviewedAt) || "-")}</strong></div>
            </div>
            <p class="admin-demand-requirements">${escapeHtml(item.requirements || "No demand notes yet.")}</p>
            ${item.rejectionReason ? `<p class="admin-demand-reason"><strong>Reject reason:</strong> ${escapeHtml(item.rejectionReason)}</p>` : ""}
            ${demandDecisionControl(item)}
          </article>
        `;
      }).join("") || `<div class="card"><p class="admin-empty-text">No demands found for this status.</p></div>`;
    }

    if (demandBody) {
      demandBody.innerHTML = rows.map((item) => `
        <tr>
          <td>${escapeHtml(item.moduleCode || "-")}</td>
          <td>${escapeHtml(item.title || "-")}</td>
          <td>${escapeHtml(item.teacherName || item.moId || "-")}</td>
          <td>${escapeHtml(formatDate(item.createdAt) || "-")}</td>
          <td>${escapeHtml(formatStatus(item.approvalStatus))}</td>
          <td>${demandDecisionControl(item, true)}</td>
        </tr>
      `).join("") || `<tr><td colspan="6">No demands found for this status.</td></tr>`;
    }
  }

  function demandDecisionControl(item, compact) {
    const status = String(item.approvalStatus || "pending").toLowerCase();
    const reason = item.rejectionReason || "";
    return `
      <div class="admin-demand-review-control ${compact ? "admin-demand-review-control--compact" : ""}" data-demand-control="${escapeHtml(item.jobId)}">
        <select data-demand-status="${escapeHtml(item.jobId)}" aria-label="Demand approval status">
          <option value="pending" ${status === "pending" ? "selected" : ""}>Pending</option>
          <option value="approved" ${status === "approved" ? "selected" : ""}>Approved</option>
          <option value="rejected" ${status === "rejected" ? "selected" : ""}>Rejected</option>
        </select>
        <input type="text" maxlength="200" data-demand-reason="${escapeHtml(item.jobId)}" placeholder="Reject reason" value="${escapeHtml(reason)}" ${status === "rejected" ? "" : "disabled"} />
        <button class="btn btn-primary" type="button" data-demand-review="${escapeHtml(item.jobId)}">Save</button>
      </div>
    `;
  }

  function demandStatusClass(status) {
    const value = String(status || "").toLowerCase();
    if (value === "approved") return "ok";
    if (value === "rejected") return "danger";
    return "warn";
  }

  function formatHourRange(item) {
    if (item.hours && Number(item.hours) > 0) {
      return `${item.hours}h`;
    }
    if (item.hourMin !== null && item.hourMin !== undefined && item.hourMax !== null && item.hourMax !== undefined) {
      return `${item.hourMin}-${item.hourMax}h`;
    }
    return "-";
  }

  function formatHiredAtDisplay(value) {
    if (!value) return "-";
    const text = String(value);
    const isoDate = text.match(/^(\d{4}-\d{2}-\d{2})/);
    return isoDate ? isoDate[1] : text;
  }

  function hiredAtSortKey(value) {
    const raw = String(value || "").trim();
    return raw || "\u0000";
  }

  function sortAssignedJobsForDisplay(assignedJobs) {
    const jobs = Array.isArray(assignedJobs) ? assignedJobs.slice() : [];
    jobs.sort((a, b) => hiredAtSortKey(b.hiredAt).localeCompare(hiredAtSortKey(a.hiredAt))
      || String(a.moduleCode || "").localeCompare(String(b.moduleCode || ""), undefined, { sensitivity: "base" }));
    return jobs;
  }

  function renderWorkloadAssignedRows(assignedJobs) {
    const jobs = sortAssignedJobsForDisplay(assignedJobs);
    if (!jobs.length) {
      return `<tr><td colspan="4">No line items (no hired applications with job data).</td></tr>`;
    }
    return jobs.map((job) => `
      <tr>
        <td>${escapeHtml(job.moduleCode || "-")}</td>
        <td>${escapeHtml(job.title || job.jobId || "-")}</td>
        <td>${escapeHtml(String(job.weeklyHours ?? 0))}</td>
        <td>${escapeHtml(formatHiredAtDisplay(job.hiredAt))}</td>
      </tr>
    `).join("");
  }

  function normalizeJobStatus(job) {
    if (job.recruitmentClosed) {
      return { label: "Closed", cls: "danger", level: "is-closed", icon: "" };
    }
    if (String(job.status || "").toLowerCase() === "open") {
      return { label: "Open", cls: "ok", level: "is-open", icon: "\u2713 " };
    }
    if (String(job.status || "").toLowerCase() === "draft") {
      return { label: "Draft", cls: "warn", level: "is-draft", icon: "" };
    }
    return { label: job.status || "Unknown", cls: "low", level: "is-other", icon: "" };
  }

  function formatDate(value) {
    if (!value) return "";
    const text = String(value);
    const isoDate = text.match(/^(\d{4}-\d{2}-\d{2})/);
    return isoDate ? isoDate[1] : text;
  }

  function renderJobs(jobs) {
    jobsCards.innerHTML = jobs.map((job) => {
      const status = normalizeJobStatus(job);
      const positions = Number(job.positions || 0);
      const hired = Number(job.hiredCount || 0);
      const applicants = Number(job.applicantCount || 0);
      const unfilled = Number(job.unfilledCount ?? Math.max(positions - hired, 0));
      const postedDate = formatDate(job.publishedAt || job.createdAt);
      return `
        <article class="card admin-job-card admin-job-card-large ${status.level} admin-health-${escapeHtml(job.healthLevel || "unknown")}">
          <div class="admin-job-heading">
            <div>
              <div class="admin-job-title-line">
                <h3 class="admin-job-title">${escapeHtml(job.moduleCode || "-")} - ${escapeHtml(job.title || "Untitled Job")}</h3>
                <span class="tag ${status.cls}">${status.icon}${status.label}</span>
                <span class="tag ${healthTagClass(job.healthLevel)}">${escapeHtml(job.healthLabel || "Health Unknown")}</span>
              </div>
              <p class="admin-list-meta">Module Organiser: ${escapeHtml(job.teacherName || "-")} &middot; Posted: ${escapeHtml(postedDate || "-")}</p>
            </div>
          </div>
          <p class="admin-job-description">${escapeHtml(job.requirements || "No description provided.")}</p>
          <div class="admin-job-metrics">
            <div class="admin-job-metric metric-positions">
              <span>Positions</span>
              <strong>${escapeHtml(String(positions))}</strong>
            </div>
            <div class="admin-job-metric metric-applicants">
              <span>Applicants</span>
              <strong>${escapeHtml(String(applicants))}</strong>
            </div>
            <div class="admin-job-metric metric-hired">
              <span>Hired</span>
              <strong>${escapeHtml(String(hired))}</strong>
            </div>
            <div class="admin-job-metric metric-filled">
              <span>Status</span>
              <strong>${escapeHtml(job.filledLabel || `${hired}/${positions} Filled`)}</strong>
            </div>
            <div class="admin-job-metric metric-unfilled">
              <span>Unfilled</span>
              <strong>${escapeHtml(String(unfilled))}</strong>
            </div>
          </div>
          <div class="admin-job-footer">
            <p class="admin-job-meta">Hours/Week: ${escapeHtml(String(job.weeklyHours || 0))}h &middot; Rate: - &middot; Deadline: ${escapeHtml(job.deadline || "-")}${deadlineSuffix(job)}</p>
            <div class="row" style="gap:8px;">
              <button class="btn btn-outline" type="button" data-job-details="${escapeHtml(job.id)}">View Details</button>
              <button class="btn btn-outline" type="button" data-job-applications="${escapeHtml(job.id)}" data-job-title="${escapeHtml(job.moduleCode || job.id)} - ${escapeHtml(job.title || "Untitled Job")}">View Applications</button>
              ${job.recruitmentClosed ? `<button class="btn btn-outline" type="button" data-reopen-job="${escapeHtml(job.id)}">Reopen</button>` : ""}
            </div>
          </div>
          <div class="admin-job-details" data-job-details-panel="${escapeHtml(job.id)}">
            <div><span class="admin-key">Department</span><strong>${escapeHtml(job.department || "-")}</strong></div>
            <div><span class="admin-key">Schedule</span><strong>${escapeHtml(job.schedule || "-")}</strong></div>
            <div><span class="admin-key">Location</span><strong>${escapeHtml(job.location || "-")}</strong></div>
            <div><span class="admin-key">Closed At</span><strong>${escapeHtml(formatDate(job.closedAt) || "-")}</strong></div>
          </div>
        </article>
      `;
    }).join("") || `<div class="card"><p class="admin-empty-text">No jobs found.</p></div>`;

    jobsBody.innerHTML = jobs.map((job) => `
      <tr>
        <td>${escapeHtml(job.moduleCode)}</td>
        <td>${escapeHtml(job.title)}</td>
        <td>${escapeHtml(job.department || "-")}</td>
        <td>${escapeHtml(job.teacherName)}</td>
        <td>${escapeHtml(String(job.applicantCount || 0))}</td>
        <td>${escapeHtml(String(job.hiredCount || 0))}</td>
        <td>${escapeHtml(normalizeJobStatus(job).label)} / ${escapeHtml(job.healthLabel || "-")}</td>
        <td>
          <div class="row" style="gap:8px;">
            <button class="btn btn-outline" type="button" data-job-applications="${escapeHtml(job.id)}" data-job-title="${escapeHtml(job.moduleCode || job.id)} - ${escapeHtml(job.title || "Untitled Job")}">Applications</button>
            ${job.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${escapeHtml(job.id)}">Reopen</button>` : ""}
          </div>
        </td>
      </tr>
    `).join("");
  }

  function healthTagClass(level) {
    const value = String(level || "").toLowerCase();
    if (["overdue", "closed-unfilled", "no-applicants"].includes(value)) return "danger";
    if (["deadline-risk", "unfilled"].includes(value)) return "warn";
    if (["healthy", "complete"].includes(value)) return "ok";
    return "low";
  }

  function deadlineSuffix(job) {
    if (job.daysUntilDeadline === null || job.daysUntilDeadline === undefined) return "";
    const days = Number(job.daysUntilDeadline);
    if (Number.isNaN(days)) return "";
    if (days < 0) return ` (${Math.abs(days)} day(s) overdue)`;
    if (days === 0) return " (today)";
    return ` (${days} day(s) left)`;
  }

  function workloadTier(item) {
    const level = String(item.workloadLevel || "").toLowerCase();
    if (level === "overload") return { level: "overload", label: "Overload", cls: "danger", icon: "!" };
    if (level === "warning") return { level: "warning", label: "Warning", cls: "warn", icon: "!" };
    if (level === "normal") return { level: "normal", label: "Normal", cls: "ok", icon: "OK" };
    return { level: "low", label: "Low", cls: "low", icon: "" };
  }

  function workloadLegendEl() {
    return byId("adminWorkloadLegend");
  }

  function workloadLegendHtml() {
    const threshold = Number(thresholdHoursEl.value || 20);
    const normalPercent = Number(thresholdNormalPercentEl && thresholdNormalPercentEl.value || 50);
    const warningPercent = Number(thresholdWarningPercentEl && thresholdWarningPercentEl.value || 75);
    const normalHours = workloadHoursAtPercent(threshold, normalPercent);
    const warningHours = workloadHoursAtPercent(threshold, warningPercent);
    const warningMax = Math.max(warningHours, threshold - 1);
    return `
      <strong>Legend:</strong>
      <span><i class="legend-dot legend-overload"></i> Overload (&gt;=${escapeHtml(String(threshold))}h, 100%)</span>
      <span><i class="legend-dot legend-warning"></i> Warning (${escapeHtml(String(warningHours))}-${escapeHtml(String(warningMax))}h, ${escapeHtml(String(warningPercent))}%+)</span>
      <span><i class="legend-dot legend-normal"></i> Normal (${escapeHtml(String(normalHours))}-${escapeHtml(String(Math.max(normalHours, warningHours - 1)))}h, ${escapeHtml(String(normalPercent))}%+)</span>
      <span><i class="legend-dot legend-low"></i> Low (&lt;${escapeHtml(String(normalHours))}h, below ${escapeHtml(String(normalPercent))}%)</span>
    `;
  }

  function buildWorkloadNestedTableHtml(item) {
    const assignedJobs = item.assignedJobs;
    return `
      <table class="admin-workload-nested">
        <caption class="admin-sr-only">Hired positions for ${escapeHtml(item.studentName)}</caption>
        <thead>
          <tr><th scope="col">Module</th><th scope="col">Position title</th><th scope="col">Hours/week</th><th scope="col">Hired / recorded at</th></tr>
        </thead>
        <tbody>${renderWorkloadAssignedRows(assignedJobs)}</tbody>
      </table>
    `;
  }

  function syncWorkloadDrawer(idx, expand) {
    if (!workloadLayout || !workloadDrawer || !workloadDrawerBody) {
      return;
    }
    if (!expand || idx < 0) {
      workloadDrawer.classList.add("admin-hidden");
      workloadLayout.classList.remove("has-drawer-open");
      workloadDrawerBody.innerHTML = "";
      return;
    }
    const list = latestData && Array.isArray(latestData.workload) ? latestData.workload : [];
    const row = list[idx];
    if (!row) {
      workloadDrawer.classList.add("admin-hidden");
      workloadLayout.classList.remove("has-drawer-open");
      workloadDrawerBody.innerHTML = "";
      return;
    }
    if (workloadDrawerTitle) {
      workloadDrawerTitle.textContent = `${row.studentName || "Student"} (${row.studentId || ""})`;
    }
    workloadDrawerBody.innerHTML = buildWorkloadNestedTableHtml(row);
    workloadDrawer.classList.remove("admin-hidden");
    workloadLayout.classList.add("has-drawer-open");
  }

  function resolveExpandedWorkloadIndex(workload) {
    if (!openWorkloadStudentId) {
      return -1;
    }
    const sid = String(openWorkloadStudentId);
    return workload.findIndex((w) => String(w.studentId) === sid);
  }

  function setWorkloadTableExpanded(idx, expand) {
    const workload = latestData && Array.isArray(latestData.workload) ? latestData.workload : [];
    const detail = workloadBody.querySelector(`[data-workload-detail="${idx}"]`);
    const btn = workloadBody.querySelector(`[data-workload-expand="${idx}"]`);
    if (!detail || !btn) {
      syncWorkloadDrawer(-1, false);
      return;
    }

    workloadBody.querySelectorAll("[data-workload-detail]").forEach((row) => row.classList.add("admin-hidden"));
    workloadBody.querySelectorAll(".admin-workload-expand-btn").forEach((b) => {
      b.setAttribute("aria-expanded", "false");
      b.textContent = "Show";
    });

    if (expand) {
      detail.classList.remove("admin-hidden");
      btn.setAttribute("aria-expanded", "true");
      btn.textContent = "Hide";
      const row = workload[idx];
      openWorkloadStudentId = row && row.studentId != null ? String(row.studentId) : null;
      requestAnimationFrame(() => {
        detail.scrollIntoView({
          block: "nearest",
          behavior: prefersReducedMotion() ? "auto" : "smooth"
        });
        try {
          detail.focus({ preventScroll: true });
        } catch (e) {
          /* ignore */
        }
      });
      syncWorkloadDrawer(idx, true);
    } else {
      openWorkloadStudentId = null;
      syncWorkloadDrawer(-1, false);
    }
  }

  function toggleWorkloadTableAtIndex(idx) {
    const detail = workloadBody.querySelector(`[data-workload-detail="${idx}"]`);
    if (!detail) return;
    const willExpand = detail.classList.contains("admin-hidden");
    if (willExpand) {
      setWorkloadTableExpanded(idx, true);
    } else {
      setWorkloadTableExpanded(idx, false);
    }
  }

  function renderWorkload(workload) {
    const list = Array.isArray(workload) ? workload : [];
    let expandedIdx = resolveExpandedWorkloadIndex(list);
    if (expandedIdx < 0) {
      openWorkloadStudentId = null;
    }

    workloadCards.innerHTML = list.map((item, idx) => {
      const tier = workloadTier(item);
      const assignedJobs = sortAssignedJobsForDisplay(item.assignedJobs);
      const cardJobLines = assignedJobs.length
        ? `<ul>${assignedJobs.map((job) => `
          <li>${escapeHtml(job.moduleCode || job.jobId || "Job")}: ${escapeHtml(job.title || "")} - ${escapeHtml(String(job.weeklyHours ?? 0))}h/wk, hired ${escapeHtml(formatHiredAtDisplay(job.hiredAt))}</li>
        `).join("")}</ul>`
        : `<p class="admin-list-meta">No job-level breakdown.</p>`;
      return `
        <article class="card admin-work-card admin-work-${tier.level}">
          <div class="admin-work-top">
            <div>
              <h3 class="admin-subtitle">${escapeHtml(item.studentName)}</h3>
              <p class="admin-list-meta">Student ID: ${escapeHtml(item.studentId)}</p>
            </div>
            <div class="admin-work-hours">
              <strong>${escapeHtml(String(item.weeklyHours || 0))}</strong>
              <span>hrs/week</span>
              <em class="tag ${tier.cls}">${tier.icon}${tier.label}</em>
            </div>
          </div>
          <div class="admin-assigned-block">
            <p class="admin-list-meta">Assigned Positions:</p>
            <div class="admin-work-chips">
              ${assignedJobs.map((job) => `
                <span class="admin-work-chip">${escapeHtml(job.moduleCode || job.jobId || "Job")} ${escapeHtml(job.title || "")} (${escapeHtml(String(job.weeklyHours ?? 0))}h/week)</span>
              `).join("") || `<span class="admin-work-chip">No assigned job details</span>`}
            </div>
          </div>
          <p class="admin-list-meta">Hired Jobs: ${escapeHtml(String(item.hiredCount || 0))} | Threshold: ${escapeHtml(String(item.thresholdHours || 0))}h</p>
          <div class="admin-workload-card-jobs">
            <div class="admin-workload-card-jobs-head">
              <strong class="admin-list-meta">By position (full detail)</strong>
              <button type="button" class="btn btn-outline admin-workload-card-toggle"
                data-workload-card-expand="${idx}"
                aria-expanded="false"
                aria-controls="admin-wl-card-${idx}">
                Show positions
              </button>
            </div>
            <div id="admin-wl-card-${idx}" class="admin-workload-card-body admin-hidden">
              ${cardJobLines}
            </div>
          </div>
        </article>
      `;
    }).join("") || `<div class="card"><p class="admin-empty-text">No hired records yet.</p></div>`;

    if (workloadLegendEl()) {
      workloadLegendEl().innerHTML = workloadLegendHtml();
    }

    workloadBody.innerHTML = list.map((item, idx) => {
      const expanded = expandedIdx === idx;
      const detailHidden = expanded ? "" : " admin-hidden";
      const ariaExpanded = expanded ? "true" : "false";
      return `
      <tr class="admin-workload-summary-row" data-workload-summary="${idx}" id="admin-wl-sum-${idx}" title="Click row to show or hide job breakdown">
        <td>
          <button type="button" class="btn btn-outline admin-workload-expand-btn"
            data-workload-expand="${idx}"
            data-workload-student-id="${escapeHtml(item.studentId)}"
            aria-expanded="${ariaExpanded}"
            aria-controls="admin-wl-detail-${idx}">
            ${expanded ? "Hide" : "Show"}
          </button>
        </td>
        <td>${escapeHtml(item.studentId)}</td>
        <td>${escapeHtml(item.studentName)}</td>
        <td>${escapeHtml(String(item.hiredCount || 0))}</td>
        <td>${escapeHtml(String(item.weeklyHours || 0))}</td>
        <td>${escapeHtml(String(item.thresholdHours || 0))}</td>
        <td>${escapeHtml((item.workloadLabel || workloadTier(item).label))}</td>
      </tr>
      <tr class="admin-workload-detail${detailHidden}" data-workload-detail="${idx}" id="admin-wl-detail-${idx}" role="region" aria-labelledby="admin-wl-sum-${idx}" tabindex="-1">
        <td colspan="7">
          ${buildWorkloadNestedTableHtml(item)}
        </td>
      </tr>`;
    }).join("") || `
      <tr><td colspan="7">No hired records yet.</td></tr>
    `;

    if (expandedIdx >= 0) {
      syncWorkloadDrawer(expandedIdx, true);
    } else {
      syncWorkloadDrawer(-1, false);
    }
  }

  function onWorkloadCardClick(event) {
    const btn = event.target.closest("[data-workload-card-expand]");
    if (!btn || !workloadCards.contains(btn)) return;
    const idx = Number(btn.getAttribute("data-workload-card-expand"), 10);
    if (Number.isNaN(idx)) return;
    const panel = workloadCards.querySelector(`#admin-wl-card-${idx}`);
    if (!panel) return;
    const willShow = panel.classList.contains("admin-hidden");
    panel.classList.toggle("admin-hidden", !willShow);
    btn.setAttribute("aria-expanded", willShow ? "true" : "false");
    btn.textContent = willShow ? "Hide positions" : "Show positions";
  }

  function onWorkloadGlobalKeydown(event) {
    if (event.key !== "Escape") {
      return;
    }
    if (alertsModal && !alertsModal.classList.contains("admin-hidden")) {
      closeAlertsModal();
      event.preventDefault();
      return;
    }
    if (event.target.closest("input, textarea, select, option, [contenteditable='true']")) {
      return;
    }
    if (!openWorkloadStudentId) {
      return;
    }
    if (!workloadPanel || workloadPanel.classList.contains("admin-hidden")) {
      return;
    }
    const workload = latestData && Array.isArray(latestData.workload) ? latestData.workload : [];
    const idx = resolveExpandedWorkloadIndex(workload);
    if (idx < 0) {
      openWorkloadStudentId = null;
      syncWorkloadDrawer(-1, false);
      return;
    }
    setWorkloadTableExpanded(idx, false);
    const sumRow = workloadBody.querySelector(`[data-workload-summary="${idx}"]`);
    const expandBtn = sumRow && sumRow.querySelector(".admin-workload-expand-btn");
    if (expandBtn) {
      expandBtn.focus();
    }
    event.preventDefault();
  }

  function onWorkloadTableClick(event) {
    const expandBtn = event.target.closest("[data-workload-expand]");
    if (expandBtn && workloadBody.contains(expandBtn)) {
      const idx = Number(expandBtn.getAttribute("data-workload-expand"), 10);
      if (!Number.isNaN(idx)) {
        toggleWorkloadTableAtIndex(idx);
      }
      return;
    }

    const summaryRow = event.target.closest("[data-workload-summary]");
    if (!summaryRow || !workloadBody.contains(summaryRow)) return;
    if (event.target.closest("button")) return;
    const idx = Number(summaryRow.getAttribute("data-workload-summary"), 10);
    if (Number.isNaN(idx)) return;
    toggleWorkloadTableAtIndex(idx);
  }

  function renderAlerts(alerts) {
    const items = Array.isArray(alerts) ? alerts : [];
    if (alertsBadge) {
      alertsBadge.textContent = String(items.length);
      alertsBadge.classList.toggle("is-empty", items.length === 0);
    }
    if (alertsButton) {
      alertsButton.classList.toggle("has-alerts", items.length > 0);
    }
    if (alertsList) {
      alertsList.innerHTML = items.map(alertHtml).join("")
        || `<div class="admin-alert admin-alert-info"><strong>No alerts</strong><p>No workload, vacancy, deadline, or metadata alerts found.</p></div>`;
    }
  }

  function alertHtml(alert) {
    const severity = String(alert.severity || "info").toLowerCase();
    return `
      <article class="admin-alert admin-alert-${escapeHtml(severity)}">
        <div>
          <strong>${escapeHtml(alert.title || "Alert")}</strong>
          <p>${escapeHtml(alert.message || "")}</p>
          <small>${escapeHtml(alert.ownerName || "-")} ${alert.dueDate ? `| Due: ${escapeHtml(alert.dueDate)}` : ""}</small>
        </div>
        <span>${escapeHtml((alert.type || "info").toUpperCase())}</span>
      </article>
    `;
  }

  function applicationStatusClass(status) {
    const value = String(status || "").toLowerCase();
    if (value === "hired") return "ok";
    if (value === "rejected") return "danger";
    if (value === "shortlisted") return "warn";
    return "low";
  }

  async function openJobApplications(jobId, title) {
    currentJobApplicationJobId = jobId;
    currentJobApplicationTitle = title || jobId;
    jobApplicationFilters.status = "all";
    if (jobApplicationsStatusFilterEl) {
      jobApplicationsStatusFilterEl.value = "all";
    }
    if (jobApplicationsTitle) {
      jobApplicationsTitle.textContent = `Applications for ${currentJobApplicationTitle}`;
    }
    if (jobApplicationsPanel) {
      jobApplicationsPanel.classList.remove("admin-hidden");
    }
    await loadJobApplications();
    activateTab("jobs");
    if (jobApplicationsPanel) {
      jobApplicationsPanel.scrollIntoView({ block: "start", behavior: prefersReducedMotion() ? "auto" : "smooth" });
    }
  }

  async function loadJobApplications() {
    if (!currentJobApplicationJobId) {
      return;
    }
    const params = new URLSearchParams();
    params.set("jobId", currentJobApplicationJobId);
    params.set("status", jobApplicationFilters.status || "all");
    const data = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/applications?${params.toString()}`, {
      method: "GET"
    });
    renderJobApplications((data && data.items) || []);
  }

  function renderJobApplications(items) {
    const rows = Array.isArray(items) ? items : [];
    if (jobApplicationCards) {
      jobApplicationCards.innerHTML = rows.map((item) => `
        <article class="card admin-app-card">
          <div class="admin-job-title-line">
            <h3 class="admin-subtitle">${escapeHtml(item.studentName || item.studentId || "-")}</h3>
            <span class="tag ${applicationStatusClass(item.status)}">${escapeHtml(formatStatus(item.status))}</span>
          </div>
          <p class="admin-list-meta">Student No: ${escapeHtml(item.studentNo || "-")} | Applied: ${escapeHtml(formatDate(item.appliedAt) || "-")}</p>
          <div class="admin-application-notes">
            <div><span class="admin-key">Evaluation Notes</span><p>${escapeHtml(item.evaluationNotes || "-")}</p></div>
            <div><span class="admin-key">Decision Feedback</span><p>${escapeHtml(item.decisionFeedback || "-")}</p></div>
          </div>
        </article>
      `).join("") || `<div class="card"><p class="admin-empty-text">No applications found for this job and status.</p></div>`;
    }

    if (jobApplicationBody) {
      jobApplicationBody.innerHTML = rows.map((item) => `
        <tr>
          <td>${escapeHtml(item.studentName || item.studentId || "-")}</td>
          <td>${escapeHtml(item.studentNo || "-")}</td>
          <td>${escapeHtml(formatDate(item.appliedAt) || "-")}</td>
          <td>${escapeHtml(formatStatus(item.status))}</td>
          <td>
            <strong>Notes:</strong> ${escapeHtml(item.evaluationNotes || "-")}<br />
            <strong>Feedback:</strong> ${escapeHtml(item.decisionFeedback || "-")}
          </td>
        </tr>
      `).join("") || `<tr><td colspan="5">No applications found for this job and status.</td></tr>`;
    }
  }

  function formatAnnouncementTarget(targetRole) {
    if (targetRole === "teacher") return "Module organisers";
    if (targetRole === "all") return "All students and teachers";
    return "Students";
  }

  function renderAnnouncements(items) {
    if (!announcementHistoryEl) return;
    if (!items.length) {
      announcementHistoryEl.innerHTML = '<p class="admin-empty-text">No announcements sent yet.</p>';
      return;
    }
    announcementHistoryEl.innerHTML = items.map((item) => `
      <div class="admin-list-item">
        <div style="min-width:0">
          <p class="admin-list-name">${escapeHtml(item.title || "Untitled")}</p>
          <p class="admin-list-meta">${escapeHtml(formatAnnouncementTarget(item.targetRole))} · ${item.recipientCount ?? 0} recipient(s) · ${escapeHtml(item.createdAt || "-")}</p>
          <p class="desc" style="margin:6px 0 0;">${escapeHtml(item.bodyPreview || "")}</p>
          <p class="admin-list-meta" style="margin-top:4px;">ID: ${escapeHtml(item.announcementId || "")}</p>
        </div>
      </div>
    `).join("");
  }

  async function loadAnnouncements() {
    if (!announcementHistoryEl) return;
    try {
      if (announcementRefreshBtn) {
        announcementRefreshBtn.disabled = true;
        announcementRefreshBtn.textContent = "Loading...";
      }
      const data = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/announcements`, {
        method: "GET"
      });
      latestAnnouncements = (data && Array.isArray(data.items)) ? data.items : [];
      renderAnnouncements(latestAnnouncements);
    } catch (err) {
      latestAnnouncements = [];
      announcementHistoryEl.innerHTML = `<p class="admin-empty-text">${escapeHtml(err.message || "Failed to load announcement history.")}</p>`;
    } finally {
      if (announcementRefreshBtn) {
        announcementRefreshBtn.disabled = false;
        announcementRefreshBtn.textContent = "Refresh";
      }
    }
  }

  async function sendAnnouncement(event) {
    event.preventDefault();
    const title = (announcementTitleEl && announcementTitleEl.value || "").trim();
    const body = (announcementBodyEl && announcementBodyEl.value || "").trim();
    const targetRole = announcementTargetEl ? announcementTargetEl.value : "student";
    if (!title || !body) {
      setNotice("Title and body are required.", true);
      activateTab("announcements");
      return;
    }
    try {
      if (announcementSendBtn) {
        announcementSendBtn.disabled = true;
        announcementSendBtn.textContent = "Sending...";
      }
      const data = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/announcements`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, body, targetRole })
      });
      if (announcementForm) announcementForm.reset();
      const count = data.recipientCount ?? 0;
      const msg = `Announcement sent to ${count} recipient(s). ID: ${data.announcementId || ""}`;
      if (announcementResultEl) announcementResultEl.textContent = msg;
      setNotice(msg, false);
      await loadAnnouncements();
      activateTab("announcements");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("announcements");
    } finally {
      if (announcementSendBtn) {
        announcementSendBtn.disabled = false;
        announcementSendBtn.textContent = "Send announcement";
      }
    }
  }

  async function createUser(event) {
    event.preventDefault();
    const payload = {
      role: createRoleEl.value,
      name: createNameEl.value.trim(),
      email: createEmailEl.value.trim(),
      password: createPasswordEl.value.trim(),
      studentId: createStudentIdEl.value.trim(),
      programme: createProgrammeEl.value.trim()
    };

    try {
      createButton.disabled = true;
      createButton.textContent = "Creating...";
      const created = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/users`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify(payload)
      });
      setNotice(`User ${created.id} created successfully.`, false);
      createUserForm.reset();
      createRoleEl.value = "student";
      syncCreateRoleFields();
      await loadAdminDashboard();
      activateTab("users");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("users");
    } finally {
      createButton.disabled = false;
      createButton.textContent = "Create User";
    }
  }

  async function saveThreshold(event) {
    event.preventDefault();
    const thresholdValue = Number(thresholdHoursEl.value);
    const normalPercent = Number(thresholdNormalPercentEl && thresholdNormalPercentEl.value);
    const warningPercent = Number(thresholdWarningPercentEl && thresholdWarningPercentEl.value);
    if (!Number.isInteger(thresholdValue) || thresholdValue <= 0) {
      setNotice("Threshold must be a positive integer.", true);
      activateTab("workload");
      return;
    }
    if (!Number.isInteger(normalPercent) || normalPercent < 1 || normalPercent > 98) {
      setNotice("Normal percent must be an integer between 1 and 98.", true);
      activateTab("workload");
      return;
    }
    if (!Number.isInteger(warningPercent) || warningPercent < 2 || warningPercent > 99) {
      setNotice("Warning percent must be an integer between 2 and 99.", true);
      activateTab("workload");
      return;
    }
    if (warningPercent <= normalPercent) {
      setNotice("Warning percent must be greater than Normal percent.", true);
      activateTab("workload");
      return;
    }
    try {
      thresholdSaveBtn.disabled = true;
      thresholdSaveBtn.textContent = "Saving...";
      const saved = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/settings/workload-threshold`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({
          workloadThresholdHours: thresholdValue,
          workloadNormalPercent: normalPercent,
          workloadWarningPercent: warningPercent
        })
      });
      thresholdHoursEl.value = saved.workloadThresholdHours;
      if (thresholdNormalPercentEl) thresholdNormalPercentEl.value = saved.workloadNormalPercent ?? normalPercent;
      if (thresholdWarningPercentEl) thresholdWarningPercentEl.value = saved.workloadWarningPercent ?? warningPercent;
      thresholdUpdatedAtEl.value = saved.updatedAt || "";
      setNotice("Workload threshold settings saved.", false);
      await loadAdminDashboard();
      activateTab("workload");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("workload");
    } finally {
      thresholdSaveBtn.disabled = false;
      thresholdSaveBtn.textContent = "Save Threshold";
    }
  }

  async function refreshDemandFilters() {
    demandFilters.status = demandStatusFilterEl.value || "pending";
    try {
      await loadDemands();
      setNotice("Demand list refreshed.", false);
      activateTab("demands");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("demands");
    }
  }

  async function reviewDemand(button) {
    const jobId = button.getAttribute("data-demand-review");
    const control = button.closest("[data-demand-control]");
    const statusEl = control ? control.querySelector("[data-demand-status]") : null;
    const reasonEl = control ? control.querySelector("[data-demand-reason]") : null;
    const nextStatus = statusEl ? statusEl.value : "";
    if (!jobId || !nextStatus) {
      return;
    }
    const action = nextStatus === "approved" ? "approve" : (nextStatus === "rejected" ? "reject" : "pending");
    const reason = nextStatus === "rejected" && reasonEl ? reasonEl.value.trim() : "";
    if (nextStatus === "rejected") {
      if (reason.length > 200) {
        setNotice("Rejection reason must be 200 characters or fewer.", true);
        activateTab("demands");
        return;
      }
    } else if (!window.confirm(`Set demand ${jobId} to ${nextStatus}?`)) {
      return;
    }

    try {
      button.disabled = true;
      button.textContent = "Saving...";
      await requestJson(`${window.location.origin}${getContextPath()}/api/admin/demands/review/${encodeURIComponent(jobId)}?action=${encodeURIComponent(action)}`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({ reason })
      });
      setNotice(`Demand ${jobId} set to ${nextStatus}. The module organiser was notified via system announcement.`, false);
      await Promise.all([loadDemands(), loadAdminDashboard()]);
      activateTab("demands");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("demands");
    } finally {
      button.disabled = false;
      button.textContent = "Save";
    }
  }

  function openAlertsModal() {
    if (!alertsModal) return;
    alertsModal.classList.remove("admin-hidden");
    document.body.classList.add("admin-modal-open");
    if (alertsCloseBtn) {
      alertsCloseBtn.focus();
    }
  }

  function closeAlertsModal() {
    if (!alertsModal) return;
    alertsModal.classList.add("admin-hidden");
    document.body.classList.remove("admin-modal-open");
    if (alertsButton) {
      alertsButton.focus();
    }
  }

  async function applyFilters() {
    filters.status = statusFilterEl.value || "all";
    const department = (departmentFilterEl.value || "").trim();
    filters.department = department ? department : "all";
    filters.teacher = teacherFilterEl.value || "all";
    try {
      await loadAdminDashboard();
      setNotice("Job filters applied.", false);
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("jobs");
    }
  }

  async function resetFilters() {
    filters.status = "all";
    filters.department = "all";
    filters.teacher = "all";
    statusFilterEl.value = "all";
    departmentFilterEl.value = "all";
    teacherFilterEl.value = "all";
    try {
      await loadAdminDashboard();
      setNotice("Job filters reset.", false);
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("jobs");
    }
  }

  function applyUserSearch() {
    userFilters.search = userSearchInput ? userSearchInput.value.trim() : "";
    renderUsers(filteredUsersForDisplay((latestData && latestData.users) || []), (latestData && latestData.users) || []);
    setNotice(userFilters.search ? "User search applied." : "Showing all users.", false);
    activateTab("users");
  }

  function clearUserSearch() {
    userFilters.search = "";
    if (userSearchInput) {
      userSearchInput.value = "";
    }
    renderUsers(filteredUsersForDisplay((latestData && latestData.users) || []), (latestData && latestData.users) || []);
    setNotice("User search cleared.", false);
    activateTab("users");
  }

  async function downloadReport(format) {
    const button = format === "csv" ? exportCsvBtn : exportTxtBtn;
    const defaultText = format === "csv" ? "Export CSV" : "Export TXT";
    const params = new URLSearchParams();
    params.set("format", format);
    params.set("status", filters.status || "all");
    params.set("department", filters.department || "all");
    await downloadAdminFile(
      `/api/admin/reports/weekly?${params.toString()}`,
      weeklyReportFileName(format),
      button,
      defaultText,
      `Weekly ${format.toUpperCase()} report exported.`,
      "jobs"
    );
  }

  async function downloadWorkloadReport(format) {
    const button = format === "csv" ? exportWorkloadCsvBtn : exportWorkloadTxtBtn;
    const defaultText = format === "csv" ? "Export Workload CSV" : "Export Workload TXT";
    await downloadAdminFile(
      `/api/admin/reports/workload?format=${encodeURIComponent(format)}`,
      `workload-report.${format}`,
      button,
      defaultText,
      `Workload ${format.toUpperCase()} report exported.`,
      "workload"
    );
  }

  async function downloadRecruitmentOutcomeCsv() {
    const button = adminOutcomeExportCsvBtn;
    const defaultText = "Export CSV";
    const params = buildRecruitmentOutcomeQueryParams();
    await downloadAdminFile(
      `/api/admin/recruitment-outcome/export?${params.toString()}`,
      `recruitment-outcome-${new Date().toISOString().slice(0, 10).replace(/-/g, "")}.csv`,
      button,
      defaultText,
      "Recruitment outcome CSV exported.",
      "recruitment-outcome"
    );
  }

  async function applyJobApplicationFilter() {
    jobApplicationFilters.status = jobApplicationsStatusFilterEl.value || "all";
    try {
      await loadJobApplications();
      setNotice("Job application filter applied.", false);
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("jobs");
    }
  }

  function closeJobApplications() {
    currentJobApplicationJobId = null;
    currentJobApplicationTitle = "";
    if (jobApplicationsPanel) {
      jobApplicationsPanel.classList.add("admin-hidden");
    }
    if (jobApplicationCards) {
      jobApplicationCards.innerHTML = "";
    }
    if (jobApplicationBody) {
      jobApplicationBody.innerHTML = "";
    }
  }

  async function downloadJobApplications(format) {
    if (!currentJobApplicationJobId) {
      setNotice("Select a job before exporting applications.", true);
      activateTab("jobs");
      return;
    }
    const button = format === "csv" ? jobApplicationsCsvBtn : jobApplicationsTxtBtn;
    const defaultText = format === "csv" ? "Export Job CSV" : "Export Job TXT";
    const params = new URLSearchParams();
    params.set("format", format);
    params.set("jobId", currentJobApplicationJobId);
    params.set("status", jobApplicationFilters.status || "all");
    await downloadAdminFile(
      `/api/admin/reports/applications?${params.toString()}`,
      `job-applications-${safeFileNamePart(currentJobApplicationJobId)}-${jobApplicationFilters.status || "all"}.${format}`,
      button,
      defaultText,
      `Job application ${format.toUpperCase()} exported.`,
      "jobs"
    );
  }

  async function downloadBackup() {
    await downloadAdminFile(
      "/api/admin/reports/backup",
      "admin-data-backup.json",
      backupBtn,
      "Backup JSON",
      "Admin data backup exported.",
      "jobs",
      true
    );
  }

  async function downloadAdminFile(path, fileName, button, defaultText, successMessage, tabName, allowJson) {
    try {
      if (button) {
        button.disabled = true;
        button.textContent = "Preparing...";
      }
      const response = await fetch(`${window.location.origin}${getContextPath()}${path}`, {
        method: "GET",
        credentials: "same-origin"
      });
      const contentType = response.headers.get("content-type") || "";
      if (!response.ok || (!allowJson && contentType.includes("application/json"))) {
        const errorBody = await response.json();
        throw new Error(errorBody.message || "Export failed.");
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setNotice(successMessage, false);
      activateTab(tabName);
    } catch (err) {
      setNotice(err.message, true);
      activateTab(tabName);
    } finally {
      if (button) {
        button.disabled = false;
        button.textContent = defaultText;
      }
    }
  }

  function weeklyReportFileName(format) {
    const parts = ["weekly-report"];
    if (filters.status && filters.status !== "all") {
      parts.push(`status-${safeFileNamePart(filters.status)}`);
    }
    if (filters.department && filters.department !== "all") {
      parts.push(`dept-${safeFileNamePart(filters.department)}`);
    }
    parts.push(new Date().toISOString().slice(0, 10).replace(/-/g, ""));
    return `${parts.join("-")}.${format}`;
  }

  function safeFileNamePart(value) {
    return String(value || "all").trim().replace(/[^A-Za-z0-9_-]+/g, "-") || "all";
  }

  async function changeOwnPassword(event) {
    event.preventDefault();
    const oldPassword = byId("adminOldPassword").value.trim();
    const newPassword = byId("adminNewPassword").value.trim();
    const confirmPassword = byId("adminConfirmPassword").value.trim();

    try {
      changePasswordBtn.disabled = true;
      changePasswordBtn.textContent = "Changing...";
      await requestJson(`${window.location.origin}${getContextPath()}/api/account/change-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({ oldPassword, newPassword, confirmPassword })
      });
      changePasswordForm.reset();
      setNotice("Password changed successfully.", false);
      activateTab("account");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("account");
    } finally {
      changePasswordBtn.disabled = false;
      changePasswordBtn.textContent = "Change Password";
    }
  }

  async function resetPassword(button) {
    const userId = button.getAttribute("data-reset-user-id");
    const userName = button.getAttribute("data-user-name") || userId;
    const newPassword = window.prompt(`Enter a new password for ${userName}:`);
    if (newPassword === null) {
      return;
    }
    if (!newPassword.trim()) {
      setNotice("Password cannot be empty.", true);
      return;
    }

    try {
      button.disabled = true;
      button.textContent = "Resetting...";
      await requestJson(`${window.location.origin}${getContextPath()}/api/admin/users/reset-password/${encodeURIComponent(userId)}`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({ newPassword: newPassword.trim() })
      });
      setNotice(`Password reset for ${userName}.`, false);
    } catch (err) {
      setNotice(err.message, true);
    } finally {
      button.disabled = false;
      button.textContent = "Reset Password";
    }
  }

  async function deleteUser(button) {
    const userId = button.getAttribute("data-delete-user-id");
    const userName = button.getAttribute("data-user-name") || userId;
    const confirmed = window.confirm(`Delete user ${userName} (${userId})?`);
    if (!confirmed) {
      return;
    }

    try {
      button.disabled = true;
      button.textContent = "Deleting...";
      await requestJson(`${window.location.origin}${getContextPath()}/api/admin/users/${encodeURIComponent(userId)}`, {
        method: "DELETE"
      });
      setNotice(`User ${userName} deleted.`, false);
      await loadAdminDashboard();
      activateTab("users");
    } catch (err) {
      setNotice(err.message, true);
    } finally {
      button.disabled = false;
      button.textContent = "Delete";
    }
  }

  function handleUserActions(event) {
    const resetButton = event.target.closest("[data-reset-user-id]");
    if (resetButton) {
      resetPassword(resetButton);
      return;
    }

    const deleteButton = event.target.closest("[data-delete-user-id]");
    if (deleteButton) {
      deleteUser(deleteButton);
    }
  }

  function handleDemandActions(event) {
    const reviewButton = event.target.closest("[data-demand-review]");
    if (!reviewButton) {
      return;
    }
    reviewDemand(reviewButton);
  }

  function handleDemandStatusChange(event) {
    const statusSelect = event.target.closest("[data-demand-status]");
    if (!statusSelect) {
      return;
    }
    const control = statusSelect.closest("[data-demand-control]");
    const reasonInput = control ? control.querySelector("[data-demand-reason]") : null;
    if (!reasonInput) {
      return;
    }
    const rejected = statusSelect.value === "rejected";
    reasonInput.disabled = !rejected;
    if (!rejected) {
      reasonInput.value = "";
    }
  }

  function onJobDetails(event) {
    const btn = event.target.closest("[data-job-details]");
    if (!btn || !jobsCards.contains(btn)) return;
    const jobId = btn.getAttribute("data-job-details");
    if (!jobId) return;
    const panel = jobsCards.querySelector(`[data-job-details-panel="${CSS.escape(jobId)}"]`);
    if (!panel) return;
    const expanded = panel.classList.toggle("open");
    btn.textContent = expanded ? "Hide Details" : "View Details";
  }

  function onJobApplications(event) {
    const btn = event.target.closest("[data-job-applications]");
    if (!btn) return;
    const jobId = btn.getAttribute("data-job-applications");
    const title = btn.getAttribute("data-job-title") || jobId;
    if (!jobId) return;
    openJobApplications(jobId, title);
  }

  function onJobsCardsClick(event) {
    onJobDetails(event);
    onJobApplications(event);
    onReopen(event);
  }

  function onJobsTableClick(event) {
    onJobApplications(event);
    onReopen(event);
  }

  async function onReopen(event) {
    const btn = event.target.closest("[data-reopen-job]");
    if (!btn) return;
    const jobId = btn.getAttribute("data-reopen-job");
    try {
      btn.disabled = true;
      btn.textContent = "Reopening...";
      await requestJson(`${window.location.origin}${getContextPath()}/api/admin/jobs/reopen/${encodeURIComponent(jobId)}`, {
        method: "POST"
      });
      setNotice(`Job ${jobId} reopened.`, false);
      await loadAdminDashboard();
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
    } finally {
      btn.disabled = false;
      btn.textContent = "Reopen";
    }
  }

  function onWorkloadDrawerCloseClick() {
    const workload = latestData && Array.isArray(latestData.workload) ? latestData.workload : [];
    const idx = resolveExpandedWorkloadIndex(workload);
    if (idx >= 0) {
      setWorkloadTableExpanded(idx, false);
    } else {
      syncWorkloadDrawer(-1, false);
    }
  }

  document.addEventListener("keydown", onWorkloadGlobalKeydown);
  usersBody.addEventListener("click", handleUserActions);
  usersGrouped.addEventListener("click", handleUserActions);
  if (demandCards) demandCards.addEventListener("click", handleDemandActions);
  if (demandBody) demandBody.addEventListener("click", handleDemandActions);
  if (demandCards) demandCards.addEventListener("change", handleDemandStatusChange);
  if (demandBody) demandBody.addEventListener("change", handleDemandStatusChange);
  jobsBody.addEventListener("click", onJobsTableClick);
  jobsCards.addEventListener("click", onJobsCardsClick);
  workloadBody.addEventListener("click", onWorkloadTableClick);
  workloadCards.addEventListener("click", onWorkloadCardClick);
  if (workloadDrawerClose) {
    workloadDrawerClose.addEventListener("click", onWorkloadDrawerCloseClick);
  }
  tabs.forEach((tab) => {
    tab.addEventListener("click", () => activateTab(tab.getAttribute("data-admin-tab")));
  });
  if (overviewOutcomeBtn) overviewOutcomeBtn.addEventListener("click", openRecruitmentOutcome);
  if (sidebarOutcomeBtn) sidebarOutcomeBtn.addEventListener("click", openRecruitmentOutcome);
  if (adminOutcomeBackBtn) adminOutcomeBackBtn.addEventListener("click", () => activateTab("overview"));
  if (alertsButton) alertsButton.addEventListener("click", openAlertsModal);
  if (alertsModal) {
    alertsModal.addEventListener("click", (event) => {
      if (event.target.closest("[data-alerts-close]")) {
        closeAlertsModal();
      }
    });
  }
  createRoleEl.addEventListener("change", syncCreateRoleFields);
  createUserForm.addEventListener("submit", createUser);
  if (userSearchBtn) userSearchBtn.addEventListener("click", applyUserSearch);
  if (userSearchClearBtn) userSearchClearBtn.addEventListener("click", clearUserSearch);
  if (userSearchInput) {
    userSearchInput.addEventListener("keydown", (event) => {
      if (event.key === "Enter") {
        event.preventDefault();
        applyUserSearch();
      }
    });
  }
  thresholdForm.addEventListener("submit", saveThreshold);
  if (demandRefreshBtn) demandRefreshBtn.addEventListener("click", refreshDemandFilters);
  applyFiltersBtn.addEventListener("click", applyFilters);
  resetFiltersBtn.addEventListener("click", resetFilters);
  exportCsvBtn.addEventListener("click", () => downloadReport("csv"));
  exportTxtBtn.addEventListener("click", () => downloadReport("txt"));
  async function notifyAllOverloadStudents() {
    if (!notifyOverloadBtn) {
      return;
    }
    const overloaded = (latestData && Array.isArray(latestData.workload) ? latestData.workload : [])
      .filter((item) => String(item.workloadLevel || "").toLowerCase() === "overload");
    if (!overloaded.length) {
      setNotice("No students are currently at overload level.", true);
      activateTab("workload");
      return;
    }
    if (!window.confirm(`Send workload reminder announcements to ${overloaded.length} overloaded student(s)?`)) {
      return;
    }
    try {
      notifyOverloadBtn.disabled = true;
      notifyOverloadBtn.textContent = "Sending...";
      const result = await requestJson(
        `${window.location.origin}${getContextPath()}/api/admin/workload/notify-overload`,
        { method: "POST" }
      );
      const count = result.notifiedCount || 0;
      setNotice(`Sent workload overload reminders to ${count} student(s).`, false);
      activateTab("workload");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("workload");
    } finally {
      notifyOverloadBtn.disabled = false;
      notifyOverloadBtn.textContent = "Notify All Overload Students";
    }
  }

  if (notifyOverloadBtn) {
    notifyOverloadBtn.addEventListener("click", () => {
      void notifyAllOverloadStudents();
    });
  }
  exportWorkloadCsvBtn.addEventListener("click", () => downloadWorkloadReport("csv"));
  exportWorkloadTxtBtn.addEventListener("click", () => downloadWorkloadReport("txt"));
  backupBtn.addEventListener("click", downloadBackup);
  if (jobApplicationsApplyBtn) jobApplicationsApplyBtn.addEventListener("click", applyJobApplicationFilter);
  if (jobApplicationsCloseBtn) jobApplicationsCloseBtn.addEventListener("click", closeJobApplications);
  if (jobApplicationsCsvBtn) jobApplicationsCsvBtn.addEventListener("click", () => downloadJobApplications("csv"));
  if (jobApplicationsTxtBtn) jobApplicationsTxtBtn.addEventListener("click", () => downloadJobApplications("txt"));
  if (adminOutcomeApplyRangeBtn) {
    adminOutcomeApplyRangeBtn.addEventListener("click", () => {
      outcomeJobDateRange.since = toApiDate(adminOutcomeJobSince && adminOutcomeJobSince.value);
      outcomeJobDateRange.until = toApiDate(adminOutcomeJobUntil && adminOutcomeJobUntil.value);
      void loadRecruitmentOutcome();
    });
  }
  if (adminOutcomeClearRangeBtn) {
    adminOutcomeClearRangeBtn.addEventListener("click", () => {
      outcomeJobDateRange.since = "";
      outcomeJobDateRange.until = "";
      if (adminOutcomeJobSince) adminOutcomeJobSince.value = "";
      if (adminOutcomeJobUntil) adminOutcomeJobUntil.value = "";
      void loadRecruitmentOutcome();
    });
  }
  if (adminOutcomeExportCsvBtn) {
    adminOutcomeExportCsvBtn.addEventListener("click", () => void downloadRecruitmentOutcomeCsv());
  }
  if (announcementForm) announcementForm.addEventListener("submit", sendAnnouncement);
  if (announcementRefreshBtn) announcementRefreshBtn.addEventListener("click", () => loadAnnouncements());
  changePasswordForm.addEventListener("submit", changeOwnPassword);

  syncCreateRoleFields();
  activateTab("overview");
  try {
    await Promise.all([loadAdminDashboard(), loadThresholdSettings(), loadDemands()]);
  } catch (err) {
    setNotice(err.message, true);
  }
});

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatRole(role) {
  const value = String(role || "");
  return value ? value.charAt(0).toUpperCase() + value.slice(1) : "-";
}

function formatStatus(status) {
  const value = String(status || "");
  return value ? value.charAt(0).toUpperCase() + value.slice(1) : "-";
}
