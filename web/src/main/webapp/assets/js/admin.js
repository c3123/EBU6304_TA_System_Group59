document.addEventListener("DOMContentLoaded", async () => {
  const portal = document.querySelector(".admin-portal");
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
  const workloadBody = byId("adminWorkloadBody");
  const usersGrouped = byId("adminUsersGrouped");
  const jobsCards = byId("adminJobsCards");
  const workloadCards = byId("adminWorkloadCards");
  const tabs = Array.from(document.querySelectorAll("[data-admin-tab]"));
  const panels = Array.from(document.querySelectorAll("[data-admin-panel]"));
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
    thresholdHoursEl.value = settings.workloadThresholdHours ?? 20;
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

  function renderWorkload(workload) {
    workloadCards.innerHTML = workload.map((item) => {
      const tag = item.warning
        ? { label: `Warning > ${item.thresholdHours || 0}h`, cls: "danger" }
        : { label: `Within ${item.thresholdHours || 0}h`, cls: "ok" };
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
        </article>
      `;
    }).join("") || `<div class="card"><p class="admin-empty-text">No hired records yet.</p></div>`;

    workloadBody.innerHTML = workload.map((item) => `
      <tr>
        <td>${escapeHtml(item.studentId)}</td>
        <td>${escapeHtml(item.studentName)}</td>
        <td>${escapeHtml(String(item.hiredCount || 0))}</td>
        <td>${escapeHtml(String(item.weeklyHours || 0))}</td>
        <td>${escapeHtml(String(item.thresholdHours || 0))}</td>
        <td>${item.warning ? "Warning" : "OK"}</td>
      </tr>
    `).join("") || `
      <tr><td colspan="6">No hired records yet.</td></tr>
    `;
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
    try {
      thresholdSaveBtn.disabled = true;
      thresholdSaveBtn.textContent = "Saving...";
      const saved = await requestJson(`${window.location.origin}${getContextPath()}/api/admin/settings/workload-threshold`, {
        method: "POST",
        headers: { "Content-Type": "application/json;charset=UTF-8" },
        body: JSON.stringify({ workloadThresholdHours: Number(thresholdHoursEl.value) })
      });
      thresholdHoursEl.value = saved.workloadThresholdHours ?? thresholdHoursEl.value;
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

  usersBody.addEventListener("click", handleUserActions);
  usersGrouped.addEventListener("click", handleUserActions);
  jobsBody.addEventListener("click", onReopen);
  jobsCards.addEventListener("click", onReopen);
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
