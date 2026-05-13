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
  const tabs = Array.from(document.querySelectorAll("[data-admin-tab]"));
  const panels = Array.from(document.querySelectorAll("[data-admin-panel]"));
  const workloadPanel = panels.find((p) => p.getAttribute("data-admin-panel") === "workload") || null;
  const noticeEl = byId("adminNotice");

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

  const thresholdForm = byId("adminThresholdForm");
  const thresholdHoursEl = byId("adminThresholdHours");
  const thresholdUpdatedAtEl = byId("adminThresholdUpdatedAt");
  const thresholdSaveBtn = byId("adminThresholdSaveBtn");

  const statusFilterEl = byId("adminJobStatusFilter");
  const departmentFilterEl = byId("adminJobDepartmentFilter");
  const departmentOptionsEl = byId("adminDepartmentOptions");
  const applyFiltersBtn = byId("adminApplyFiltersBtn");
  const resetFiltersBtn = byId("adminResetFiltersBtn");
  const exportCsvBtn = byId("adminExportCsvBtn");
  const exportTxtBtn = byId("adminExportTxtBtn");

  const changePasswordForm = byId("adminChangePasswordForm");
  const changePasswordBtn = byId("adminChangePasswordBtn");

  const currentUserId = portal?.getAttribute("data-current-user-id") || "";
  const currentUserName = portal?.getAttribute("data-current-user-name") || "Admin User";

  let latestData = null;
  /** When set, workload table re-expands this student after dashboard reload (e.g. save threshold). */
  let openWorkloadStudentId = null;
  let knownDepartments = [];
  const filters = {
    status: "all",
    department: "all"
  };

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

    const title = byId("adminSubTitle");
    if (!title) return;
    if (tabName === "workload") title.textContent = "TA Workload Statistics";
    if (tabName === "users") title.textContent = "User Management";
    if (tabName === "jobs") title.textContent = "Job Management";
    if (tabName === "account") title.textContent = "My Account";
    if (tabName === "overview") title.textContent = `Welcome, ${currentUserName}`;
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
    if (!departmentOptionsEl) return;
    departmentOptionsEl.innerHTML = knownDepartments
      .map((value) => `<option value="${escapeHtml(value)}"></option>`)
      .join("");
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
      renderDepartmentOptions();
    }

    byId("statJobs").textContent = latestData.totalJobs ?? 0;
    byId("statUsers").textContent = latestData.totalUsers ?? 0;
    byId("statApps").textContent = latestData.totalApplications ?? 0;
    renderOverview(latestData);
    renderUsers(latestData.users || []);
    renderJobs(latestData.jobs || []);
    renderWorkload(latestData.workload || []);
  }

  async function loadThresholdSettings() {
    const settings = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/settings/workload-threshold`, {
      method: "GET"
    });
    thresholdHoursEl.value = settings.workloadThresholdHours;
    thresholdUpdatedAtEl.value = settings.updatedAt || "";
  }

  function renderOverview(data) {
    const jobs = data.jobs || [];
    const openJobs = jobs.filter((job) => !job.recruitmentClosed).length;
    const closedJobs = jobs.filter((job) => job.recruitmentClosed).length;
    const totalHired = (data.workload || []).reduce((sum, item) => sum + Number(item.hiredCount || 0), 0);
    byId("overviewOpenJobs").textContent = openJobs;
    byId("overviewClosedJobs").textContent = closedJobs;
    byId("overviewHiredCount").textContent = totalHired;
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

  function renderUsers(users) {
    const adminCount = users.filter((user) => user.role === "admin").length;
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
    `).join("");
  }

  function formatHiredAtDisplay(value) {
    if (!value) return "—";
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
        <td>${escapeHtml(job.moduleCode || "—")}</td>
        <td>${escapeHtml(job.title || job.jobId || "—")}</td>
        <td>${escapeHtml(String(job.weeklyHours ?? 0))}</td>
        <td>${escapeHtml(formatHiredAtDisplay(job.hiredAt))}</td>
      </tr>
    `).join("");
  }

  function renderJobs(jobs) {
    jobsCards.innerHTML = jobs.map((job) => {
      const recruitment = job.recruitmentClosed ? "Closed" : "Open";
      return `
        <article class="card admin-job-card ${job.recruitmentClosed ? "is-closed" : ""}">
          <div class="admin-job-top">
            <div>
              <h3 class="admin-subtitle">${escapeHtml(job.moduleCode)} - ${escapeHtml(job.title)}</h3>
              <p class="admin-list-meta">Module Organiser: ${escapeHtml(job.teacherName)}</p>
            </div>
            <span class="tag ${job.recruitmentClosed ? "" : "ok"}">${recruitment}</span>
          </div>
          <div class="admin-job-grid">
            <div><span class="admin-key">Status</span><strong>${escapeHtml(job.status)}</strong></div>
            <div><span class="admin-key">Department</span><strong>${escapeHtml(job.department || "-")}</strong></div>
            <div><span class="admin-key">Positions</span><strong>${escapeHtml(String(job.positions))}</strong></div>
            <div><span class="admin-key">Recruitment</span><strong>${recruitment}</strong></div>
            <div><span class="admin-key">Action</span>${job.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${escapeHtml(job.id)}">Reopen</button>` : "<span>-</span>"}</div>
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
        <td>${escapeHtml(job.status)}</td>
        <td>${job.recruitmentClosed ? "Recruitment Closed" : "Open"}</td>
        <td>${escapeHtml(String(job.positions))}</td>
        <td>${job.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${escapeHtml(job.id)}">Reopen</button>` : "-"}</td>
      </tr>
    `).join("");
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
      const tag = item.warning
        ? { label: `Warning > ${item.thresholdHours || 0}h`, cls: "danger" }
        : { label: `Within ${item.thresholdHours || 0}h`, cls: "ok" };
      const assignedJobs = sortAssignedJobsForDisplay(item.assignedJobs);
      const cardJobLines = assignedJobs.length
        ? `<ul>${assignedJobs.map((job) => `
          <li>${escapeHtml(job.moduleCode || job.jobId || "Job")}: ${escapeHtml(job.title || "")} — ${escapeHtml(String(job.weeklyHours ?? 0))}h/wk, hired ${escapeHtml(formatHiredAtDisplay(job.hiredAt))}</li>
        `).join("")}</ul>`
        : `<p class="admin-list-meta">No job-level breakdown.</p>`;
      return `
        <article class="card admin-work-card ${tag.cls === "danger" ? "is-danger" : ""}">
          <div class="admin-work-top">
            <div>
              <h3 class="admin-subtitle">${escapeHtml(item.studentName)}</h3>
              <p class="admin-list-meta">Student ID: ${escapeHtml(item.studentId)}</p>
            </div>
            <div class="admin-work-hours">
              <strong>${escapeHtml(String(item.weeklyHours || 0))}</strong>
              <span>hrs/week</span>
              <em class="tag ${tag.cls}">${tag.label}</em>
            </div>
          </div>
          <p class="admin-list-meta">Hired Jobs: ${escapeHtml(String(item.hiredCount || 0))} | Threshold: ${escapeHtml(String(item.thresholdHours || 0))}h</p>
          <div class="admin-workload-card-jobs">
            <div class="admin-workload-card-jobs-head">
              <strong class="admin-list-meta">By position</strong>
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
        <td>${item.warning ? "Warning" : "OK"}</td>
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
    if (!Number.isInteger(thresholdValue) || thresholdValue <= 0) {
      setNotice("Threshold must be a positive integer.", true);
      activateTab("workload");
      return;
    }
    try {
      thresholdSaveBtn.disabled = true;
      thresholdSaveBtn.textContent = "Saving...";
      const saved = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/settings/workload-threshold`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({ workloadThresholdHours: thresholdValue })
      });
      thresholdHoursEl.value = saved.workloadThresholdHours;
      thresholdUpdatedAtEl.value = saved.updatedAt || "";
      setNotice("Workload threshold saved.", false);
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

  async function applyFilters() {
    filters.status = statusFilterEl.value || "all";
    const department = (departmentFilterEl.value || "").trim();
    filters.department = department ? department : "all";
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
    statusFilterEl.value = "all";
    departmentFilterEl.value = "";
    try {
      await loadAdminDashboard();
      setNotice("Job filters reset.", false);
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("jobs");
    }
  }

  async function downloadReport(format) {
    const button = format === "csv" ? exportCsvBtn : exportTxtBtn;
    const defaultText = format === "csv" ? "Export CSV" : "Export TXT";
    try {
      button.disabled = true;
      button.textContent = "Preparing...";
      const response = await fetch(`${window.location.origin}${getContextPath()}/api/admin/reports/weekly?format=${encodeURIComponent(format)}`, {
        method: "GET",
        credentials: "same-origin"
      });
      const contentType = response.headers.get("content-type") || "";
      if (!response.ok || contentType.includes("application/json")) {
        const errorBody = await response.json();
        throw new Error(errorBody.message || "Export failed.");
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `weekly-recruitment-report.${format}`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setNotice(`Weekly ${format.toUpperCase()} report exported.`, false);
      activateTab("jobs");
    } catch (err) {
      setNotice(err.message, true);
      activateTab("jobs");
    } finally {
      button.disabled = false;
      button.textContent = defaultText;
    }
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
  jobsBody.addEventListener("click", onReopen);
  jobsCards.addEventListener("click", onReopen);
  workloadBody.addEventListener("click", onWorkloadTableClick);
  workloadCards.addEventListener("click", onWorkloadCardClick);
  if (workloadDrawerClose) {
    workloadDrawerClose.addEventListener("click", onWorkloadDrawerCloseClick);
  }
  tabs.forEach((tab) => {
    tab.addEventListener("click", () => activateTab(tab.getAttribute("data-admin-tab")));
  });
  createRoleEl.addEventListener("change", syncCreateRoleFields);
  createUserForm.addEventListener("submit", createUser);
  thresholdForm.addEventListener("submit", saveThreshold);
  applyFiltersBtn.addEventListener("click", applyFilters);
  resetFiltersBtn.addEventListener("click", resetFilters);
  exportCsvBtn.addEventListener("click", () => downloadReport("csv"));
  exportTxtBtn.addEventListener("click", () => downloadReport("txt"));
  changePasswordForm.addEventListener("submit", changeOwnPassword);

  syncCreateRoleFields();
  activateTab("overview");
  try {
    await Promise.all([loadAdminDashboard(), loadThresholdSettings()]);
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
