function getTeacherContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (parts.length === 0) return "";
  return "/" + parts[0];
}

function teacherApiBase() {
  return `${window.location.origin}${getTeacherContextPath()}/api/mo`;
}

function teacherSafeText(value) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

function teacherFormatDateTime(value) {
  if (value === null || value === undefined || value === "") return "-";
  const text = String(value);
  const match = text.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})/);
  return match ? `${match[1]} ${match[2]}` : text;
}

function teacherEscapeHtml(value) {
  if (value == null) return "";
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function teacherStatusTag(item) {
  if (item.recruitmentClosed === true) {
    return '<span class="mo-status-pill mo-status-withdrawn">recruitment closed</span>';
  }
  if (item.withdrawn === true) {
    return '<span class="mo-status-pill mo-status-withdrawn">withdrawn</span>';
  }
  if (item.published === true) {
    return '<span class="mo-status-pill mo-status-published">published</span>';
  }
  const normalized = String(item.approvalStatus || "").toLowerCase();
  if (normalized === "approved") {
    return '<span class="mo-status-pill mo-status-approved">approved</span>';
  }
  if (normalized === "rejected") {
    return '<span class="mo-status-pill mo-status-rejected">rejected</span>';
  }
  return '<span class="mo-status-pill mo-status-pending">pending</span>';
}

function teacherSetNotice(id, message, isError) {
  const el = byId(id);
  if (!el) return;
  el.textContent = message || "";
  el.style.color = isError ? "#dc2626" : "#475569";
}

function teacherSetButtonLoading(button, loadingText, fallbackText, isLoading) {
  if (!button) return;
  if (isLoading) {
    button.dataset.label = button.textContent;
    button.textContent = loadingText;
    button.disabled = true;
  } else {
    button.textContent = button.dataset.label || fallbackText;
    button.disabled = false;
  }
}

const teacherState = {
  items: [],
  historyItems: [],
  currentHistoryJobId: null,
  pollingTimer: null,
  notifications: [],
  unreadCount: 0
};

async function teacherRequest(url, options) {
  const headers = Object.assign({}, options && options.headers ? options.headers : {});

  const response = await fetch(url, {
    method: options && options.method ? options.method : "GET",
    headers,
    credentials: "same-origin",
    body: options && options.body ? options.body : undefined
  });

  const body = await response.json();
  if (!response.ok || !body.success) {
    const err = new Error(body.message || "Request failed.");
    err.code = body.code || "REQUEST_ERROR";
    err.httpStatus = response.status;
    throw err;
  }
  return body.data;
}

async function loadTeacherJobs() {
  const data = await teacherRequest(`${teacherApiBase()}/demands/list`, { method: "GET" });
  teacherState.items = data && Array.isArray(data.items) ? data.items : [];
  renderTeacherJobs();
}

async function loadJobHistory() {
  const data = await teacherRequest(`${teacherApiBase()}/jobs/history`, { method: "GET" });
  teacherState.historyItems = data && Array.isArray(data.items) ? data.items : [];
  renderJobHistory();
}

function historyStatusTag(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized === "recruitment_closed") return '<span class="mo-status-pill mo-status-withdrawn">recruitment closed</span>';
  if (normalized === "withdrawn") return '<span class="mo-status-pill mo-status-withdrawn">withdrawn</span>';
  if (normalized === "open" || normalized === "published") return '<span class="mo-status-pill mo-status-published">published</span>';
  if (normalized === "approved") return '<span class="mo-status-pill mo-status-approved">approved</span>';
  if (normalized === "rejected") return '<span class="mo-status-pill mo-status-rejected">rejected</span>';
  return '<span class="mo-status-pill mo-status-pending">draft</span>';
}

function renderJobHistory() {
  const body = byId("historyTableBody");
  const empty = byId("historyEmpty");
  const tableWrap = document.querySelector(".mo-history-table-wrap");

  if (!teacherState.historyItems.length) {
    body.innerHTML = "";
    empty.style.display = "block";
    if (tableWrap) tableWrap.style.display = "none";
    return;
  }

  empty.style.display = "none";
  if (tableWrap) tableWrap.style.display = "block";
  body.innerHTML = teacherState.historyItems.map((item) => renderHistoryRow(item)).join("");
}

function renderHistoryRow(item) {
  const jobId = teacherEscapeHtml(teacherSafeText(item.jobId));
  return `
    <tr data-history-job-id="${jobId}">
      <td>
        <strong>${teacherEscapeHtml(teacherSafeText(item.courseName))}</strong>
        <div style="font-size:12px;color:#64748b">${teacherEscapeHtml(teacherSafeText(item.department))}</div>
      </td>
      <td>${historyStatusTag(item.status)}</td>
      <td><span class="mo-history-counts"><span>${teacherEscapeHtml(teacherSafeText(item.applicantCount))}</span></span></td>
      <td><span class="mo-history-counts"><span>${teacherEscapeHtml(teacherSafeText(item.hireCount))}</span></span></td>
      <td>${teacherEscapeHtml(teacherFormatDateTime(item.releaseTime))}</td>
      <td>${teacherEscapeHtml(teacherSafeText(item.deadline))}</td>
      <td>
        <div class="mo-history-actions">
          <button class="btn btn-outline" type="button" data-history-details="${jobId}">View Details</button>
          <button class="btn btn-outline" type="button" data-history-reuse="${jobId}">Reuse</button>
          <button class="btn btn-outline" type="button" data-history-export="${jobId}" data-scope="all" data-format="csv">Export All</button>
          <button class="btn btn-outline" type="button" data-history-export="${jobId}" data-scope="shortlisted" data-format="csv">Export Shortlisted</button>
        </div>
      </td>
    </tr>
  `;
}

function renderTeacherJobs() {
  const feed = byId("jobsFeed");
  const empty = byId("jobsEmpty");

  if (!teacherState.items.length) {
    feed.innerHTML = "";
    empty.style.display = "block";
    return;
  }

  empty.style.display = "none";
  feed.innerHTML = teacherState.items.map(item => renderTeacherJobCard(item)).join("");
}

function renderTeacherJobCard(item) {
  const isClosed = item.recruitmentClosed === true;
  const isPublished = item.published === true;
  const isWithdrawn = item.withdrawn === true;
  const canPublish = String(item.approvalStatus || "").toLowerCase() === "approved" && !isPublished && !isClosed;
  const publishLocked = isPublished ? "Published" : "Publish job";
  const publishDisabled = canPublish ? "" : "disabled";
  const canEdit = !isClosed && !isPublished;
  const canDelete = !isClosed && !isPublished;
  const canTakeOffline = isPublished && !isClosed;
  const detailBlock = item.published === true
    ? `
      <div class="mo-inline-form open" style="display:block">
        <div class="mo-publish-grid">
          <div><span>Published</span><div>${teacherStatusTag(item)}</div></div>
          <div><span>Withdrawn</span><div>${teacherSafeText(item.withdrawn)}</div></div>
          <div><span>Created At</span><div>${teacherEscapeHtml(teacherFormatDateTime(item.createdAt))}</div></div>
          <div><span>Updated At</span><div>${teacherEscapeHtml(teacherFormatDateTime(item.updatedAt))}</div></div>
        </div>
      </div>
    `
    : `
      <form class="mo-inline-form" data-publish-form="${teacherEscapeHtml(item.jobId)}">
        <div class="mo-publish-grid">
          <div class="field">
            <label>Location</label>
            <select name="location" required>
              <option value="offline">Offline</option>
              <option value="online">Online</option>
            </select>
          </div>
          <div class="field">
            <label>Deadline</label>
            <input name="deadline" type="date" required />
          </div>
        </div>
        <div class="field">
          <label>Requirements</label>
          <textarea name="requirements" placeholder="e.g. GPA>=3.0, Java foundation" required></textarea>
        </div>
        <div class="row">
          <button type="submit" class="btn btn-primary">Confirm publish</button>
          <button type="button" class="btn btn-outline" data-cancel-publish="${teacherEscapeHtml(item.jobId)}">Cancel</button>
        </div>
      </form>
    `;

  const editBlock = canEdit
    ? `
      <form class="mo-inline-form" data-edit-form="${teacherEscapeHtml(item.jobId)}">
        <div class="mo-publish-grid">
          <div class="field">
            <label>Course Name</label>
            <input name="courseName" type="text" required value="${teacherEscapeHtml(teacherSafeText(item.courseName))}" />
          </div>
          <div class="field">
            <label>Planned Count</label>
            <input name="plannedCount" type="number" min="1" required value="${teacherEscapeHtml(teacherSafeText(item.plannedCount))}" />
          </div>
          <div class="field">
            <label>Hour Min</label>
            <input name="hourMin" type="number" min="1" required value="${teacherEscapeHtml(teacherSafeText(item.hourMin))}" />
          </div>
          <div class="field">
            <label>Hour Max</label>
            <input name="hourMax" type="number" min="1" required value="${teacherEscapeHtml(teacherSafeText(item.hourMax))}" />
          </div>
        </div>
        <div class="row">
          <button type="submit" class="btn btn-primary">Save edit</button>
          <button type="button" class="btn btn-outline" data-cancel-edit="${teacherEscapeHtml(item.jobId)}">Cancel</button>
        </div>
      </form>
    ` : "";
  return `
    <article class="mo-job-card" data-job-id="${teacherEscapeHtml(item.jobId)}">
      <div class="mo-job-card-head">
        <div>
          <h4>${teacherEscapeHtml(teacherSafeText(item.courseName))}</h4>
          <p>Job ID: ${teacherEscapeHtml(teacherSafeText(item.jobId))}</p>
        </div>
        <div>${teacherStatusTag(item)}</div>
      </div>

      <div class="mo-demand-meta">
        <div><span>Planned Count</span><strong>${teacherEscapeHtml(teacherSafeText(item.plannedCount))}</strong></div>
        <div><span>Hours</span><strong>${teacherEscapeHtml(teacherSafeText(item.hourMin))} - ${teacherEscapeHtml(teacherSafeText(item.hourMax))}</strong></div>
        <div><span>Created</span><strong>${teacherEscapeHtml(teacherFormatDateTime(item.createdAt))}</strong></div>
        <div><span>Updated</span><strong>${teacherEscapeHtml(teacherFormatDateTime(item.updatedAt))}</strong></div>
      </div>

      <p class="notice">Approval status: <strong>${teacherEscapeHtml(teacherSafeText(item.approvalStatus || "pending"))}</strong>. Job status: <strong>${teacherEscapeHtml(teacherSafeText(item.status || "-"))}</strong>. Published: <strong>${teacherEscapeHtml(String(item.published === true))}</strong>. Withdrawn: <strong>${teacherEscapeHtml(String(item.withdrawn === true))}</strong>.</p>

      <div class="mo-demand-actions">
        <button class="btn btn-primary" type="button" data-open-publish="${teacherEscapeHtml(item.jobId)}" ${publishDisabled}>${publishLocked}</button>
        <button class="btn btn-outline" type="button" data-open-edit="${teacherEscapeHtml(item.jobId)}" ${canEdit ? "" : "disabled"}>Edit</button>
        <button class="btn btn-outline" type="button" data-delete-job="${teacherEscapeHtml(item.jobId)}" ${canDelete ? "" : "disabled"}>Delete</button>
        <button class="btn btn-outline" type="button" data-offline-job="${teacherEscapeHtml(item.jobId)}" ${canTakeOffline ? "" : "disabled"}>Take offline</button>
        <a class="btn btn-outline" href="mo-applications.jsp?jobId=${teacherEscapeHtml(item.jobId)}">Applicants</a>
      </div>

      ${detailBlock}
      ${editBlock}
    </article>
  `;
}

async function submitDemandForm(event) {
  event.preventDefault();
  const form = event.target;
  const button = form.querySelector('button[type="submit"]');
  teacherSetButtonLoading(button, "Submitting...", "Submit Demand", true);

  try {
    const payload = {
      courseName: byId("courseName").value.trim(),
      plannedCount: Number(byId("plannedCount").value),
      hourMin: Number(byId("hourMin").value),
      hourMax: Number(byId("hourMax").value)
    };

    const data = await teacherRequest(`${teacherApiBase()}/demands`, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify(payload)
    });

    teacherSetNotice("globalNotice", `Demand submitted successfully. jobId=${data.jobId}`, false);
    form.reset();
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("globalNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Submitting...", "Submit Demand", false);
  }
}

async function submitPublishForm(form) {
  const jobId = form.getAttribute("data-publish-form");
  const button = form.querySelector('button[type="submit"]');
  teacherSetButtonLoading(button, "Publishing...", "Confirm publish", true);

  try {
    const payload = {
      location: form.location.value,
      deadline: form.deadline.value,
      requirements: form.requirements.value.trim()
    };
    await teacherRequest(`${teacherApiBase()}/jobs/publish/${encodeURIComponent(jobId)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify(payload)
    });
    teacherSetNotice("jobsNotice", `Job ${jobId} published successfully.`, false);
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Publishing...", "Confirm publish", false);
  }
}

async function takeOffline(jobId, button) {
  teacherSetButtonLoading(button, "Processing...", "Take offline", true);
  try {
    await teacherRequest(`${teacherApiBase()}/jobs/offline/${encodeURIComponent(jobId)}`, {
      method: "POST"
    });
    teacherSetNotice("jobsNotice", `Job ${jobId} taken offline.`, false);
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Processing...", "Take offline", false);
  }
}

function openPublishForm(jobId) {
  document.querySelectorAll(".mo-inline-form").forEach(el => el.classList.remove("open"));
  const form = document.querySelector(`[data-publish-form="${CSS.escape(jobId)}"]`);
  if (form) {
    form.classList.add("open");
  }
}

function resetPublishForm(jobId) {
  const form = document.querySelector(`[data-publish-form="${CSS.escape(jobId)}"]`);
  if (form) {
    form.reset();
    form.classList.remove("open");
  }
}

function openEditForm(jobId) {
  document.querySelectorAll("[data-edit-form]").forEach(el => el.classList.remove("open"));
  const form = document.querySelector(`[data-edit-form="${CSS.escape(jobId)}"]`);
  if (form) form.classList.add("open");
}

function resetEditForm(jobId) {
  const form = document.querySelector(`[data-edit-form="${CSS.escape(jobId)}"]`);
  if (form) {
    form.reset();
    form.classList.remove("open");
  }
}

async function submitEditForm(form) {
  const jobId = form.getAttribute("data-edit-form");
  const button = form.querySelector('button[type="submit"]');
  teacherSetButtonLoading(button, "Saving...", "Save edit", true);
  try {
    const payload = {
      courseName: form.courseName.value.trim(),
      plannedCount: Number(form.plannedCount.value),
      hourMin: Number(form.hourMin.value),
      hourMax: Number(form.hourMax.value)
    };
    await teacherRequest(`${teacherApiBase()}/jobs/edit/${encodeURIComponent(jobId)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify(payload)
    });
    teacherSetNotice("jobsNotice", `Job ${jobId} updated successfully. Please publish it again to make the changes live.`, false);
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Saving...", "Save edit", false);
  }
}

async function deleteJob(jobId, button) {
  teacherSetButtonLoading(button, "Deleting...", "Delete", true);
  try {
    await teacherRequest(`${teacherApiBase()}/jobs/delete/${encodeURIComponent(jobId)}`, { method: "POST" });
    teacherSetNotice("jobsNotice", `Job ${jobId} deleted successfully.`, false);
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Deleting...", "Delete", false);
  }
}

async function loadNotifications() {
  try {
    const data = await teacherRequest(`${teacherApiBase()}/notifications`, { method: "GET" });
    teacherState.notifications = data && Array.isArray(data.items) ? data.items : [];
    teacherState.unreadCount = data && Number.isFinite(Number(data.unreadCount)) ? Number(data.unreadCount) : 0;
    renderNotifications();
  } catch (_) {
    teacherState.notifications = [];
    teacherState.unreadCount = 0;
    renderNotifications();
  }
}

function renderNotifications() {
  const dot = byId("notificationDot");
  const panel = byId("notificationPanel");
  if (teacherState.unreadCount > 0) {
    dot.style.display = "inline-flex";
    dot.textContent = String(teacherState.unreadCount);
  } else {
    dot.style.display = "none";
  }
  if (!teacherState.notifications.length) {
    panel.innerHTML = '<p class="notice" style="margin:0">No notifications.</p>';
    return;
  }
  panel.innerHTML = teacherState.notifications.map(n => `
    <div class="mo-notification-item">
      <div style="min-width:0">
        <div><strong>${teacherEscapeHtml(teacherSafeText(n.applicantName))}</strong> applied to <strong>${teacherEscapeHtml(teacherSafeText(n.jobName || n.jobId))}</strong></div>
        <div style="font-size:12px;color:#64748b">${teacherEscapeHtml(teacherSafeText(n.applicationTime))}</div>
      </div>
      <div class="row">
        ${n.read ? '<span class="notice" style="margin:0">Read</span>' : `<button class="btn btn-outline" type="button" data-mark-read="${teacherEscapeHtml(n.notificationId)}">Mark as Read</button>`}
      </div>
    </div>
  `).join("");
}

async function markNotificationRead(notificationId) {
  await teacherRequest(`${teacherApiBase()}/notifications/read/${encodeURIComponent(notificationId)}`, { method: "POST" });
  await loadNotifications();
}

async function changeTeacherPassword(event) {
  event.preventDefault();
  const button = byId("teacherChangePasswordBtn");
  teacherSetButtonLoading(button, "Changing...", "Change Password", true);
  try {
    await teacherRequest(`${accountApiBase()}/change-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=UTF-8" },
      body: JSON.stringify({
        oldPassword: byId("teacherOldPassword").value.trim(),
        newPassword: byId("teacherNewPassword").value.trim(),
        confirmPassword: byId("teacherConfirmPassword").value.trim()
      })
    });
    byId("teacherChangePasswordForm").reset();
    teacherSetNotice("globalNotice", "Password changed successfully.", false);
  } catch (err) {
    teacherSetNotice("globalNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Changing...", "Change Password", false);
  }
}

function exportApplicants(jobId, scope, format) {
  const params = new URLSearchParams({ jobId, scope, format });
  window.location.href = `${teacherApiBase()}/applications/export?${params.toString()}`;
}

async function reuseHistoryJob(jobId, button) {
  if (!window.confirm("Create a new draft job by reusing this historical job?")) {
    return;
  }
  teacherSetButtonLoading(button, "Reusing...", "Reuse", true);
  try {
    const params = new URLSearchParams({ jobId });
    const data = await teacherRequest(`${teacherApiBase()}/jobs/reuse?${params.toString()}`, { method: "POST" });
    teacherSetNotice("historyNotice", `Created new draft job ${data.jobId}.`, false);
    await loadTeacherJobs();
    await loadJobHistory();
  } catch (err) {
    teacherSetNotice("historyNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Reusing...", "Reuse", false);
  }
}

async function openHistoryDetails(jobId) {
  teacherState.currentHistoryJobId = jobId;
  const item = teacherState.historyItems.find((it) => it.jobId === jobId);
  byId("historyDetailsTitle").textContent = item ? teacherSafeText(item.courseName) : "Job Details";
  byId("historyDetailsSubtitle").textContent = `Job ID: ${jobId}`;
  byId("historyDetailsBody").innerHTML = "";
  byId("historyDetailsModal").classList.add("open");
  teacherSetNotice("historyDetailsNotice", "Loading applicants...", false);

  try {
    const params = new URLSearchParams({ jobId });
    const data = await teacherRequest(`${teacherApiBase()}/applications?${params.toString()}`, { method: "GET" });
    const items = data && Array.isArray(data.items) ? data.items : [];
    renderHistoryDetails(items);
    teacherSetNotice("historyDetailsNotice", `Loaded ${items.length} applicant record(s).`, false);
  } catch (err) {
    teacherSetNotice("historyDetailsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  }
}

function renderHistoryDetails(items) {
  const body = byId("historyDetailsBody");
  if (!items.length) {
    body.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b">No applicants for this job.</td></tr>';
    return;
  }
  body.innerHTML = items.map((item) => `
    <tr>
      <td>${teacherEscapeHtml(teacherSafeText(item.studentName))}</td>
      <td>${teacherEscapeHtml(teacherSafeText(item.studentNo || item.studentId))}</td>
      <td>${teacherEscapeHtml(teacherSafeText(item.programme))}</td>
      <td>${teacherEscapeHtml(teacherFormatDateTime(item.appliedAt))}</td>
      <td>${teacherEscapeHtml(teacherSafeText(item.status))}</td>
      <td>${teacherEscapeHtml(teacherSafeText(item.skills))}</td>
    </tr>
  `).join("");
}

function closeHistoryDetails() {
  teacherState.currentHistoryJobId = null;
  byId("historyDetailsModal").classList.remove("open");
}

function bindHistoryActions() {
  byId("historyReloadBtn").addEventListener("click", reloadJobHistory);
  byId("historyTableBody").addEventListener("click", async (event) => {
    const detailsBtn = event.target.closest("[data-history-details]");
    if (detailsBtn) {
      await openHistoryDetails(detailsBtn.getAttribute("data-history-details"));
      return;
    }

    const reuseBtn = event.target.closest("[data-history-reuse]");
    if (reuseBtn) {
      await reuseHistoryJob(reuseBtn.getAttribute("data-history-reuse"), reuseBtn);
      return;
    }

    const exportBtn = event.target.closest("[data-history-export]");
    if (exportBtn) {
      exportApplicants(
        exportBtn.getAttribute("data-history-export"),
        exportBtn.getAttribute("data-scope") || "all",
        exportBtn.getAttribute("data-format") || "csv"
      );
    }
  });

  byId("historyDetailsCloseBtn").addEventListener("click", closeHistoryDetails);
  byId("historyDetailsModal").addEventListener("click", (event) => {
    if (event.target.id === "historyDetailsModal") closeHistoryDetails();
  });
  byId("modalExportAllCsvBtn").addEventListener("click", () => {
    if (teacherState.currentHistoryJobId) exportApplicants(teacherState.currentHistoryJobId, "all", "csv");
  });
  byId("modalExportShortlistedCsvBtn").addEventListener("click", () => {
    if (teacherState.currentHistoryJobId) exportApplicants(teacherState.currentHistoryJobId, "shortlisted", "csv");
  });
  byId("modalExportAllJsonBtn").addEventListener("click", () => {
    if (teacherState.currentHistoryJobId) exportApplicants(teacherState.currentHistoryJobId, "all", "json");
  });
  byId("modalExportShortlistedJsonBtn").addEventListener("click", () => {
    if (teacherState.currentHistoryJobId) exportApplicants(teacherState.currentHistoryJobId, "shortlisted", "json");
  });
}

async function reloadJobHistory() {
  try {
    teacherSetNotice("historyNotice", "Loading job history...", false);
    await loadJobHistory();
    teacherSetNotice("historyNotice", `Loaded ${teacherState.historyItems.length} historical job record(s).`, false);
  } catch (err) {
    teacherSetNotice("historyNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  }
}

function bindTeacherFeedActions() {
  const feed = byId("jobsFeed");
  feed.addEventListener("click", async event => {
    const openBtn = event.target.closest("[data-open-publish]");
    if (openBtn) {
      openPublishForm(openBtn.getAttribute("data-open-publish"));
      return;
    }

    const cancelBtn = event.target.closest("[data-cancel-publish]");
    if (cancelBtn) {
      resetPublishForm(cancelBtn.getAttribute("data-cancel-publish"));
      return;
    }

    const openEditBtn = event.target.closest("[data-open-edit]");
    if (openEditBtn) {
      openEditForm(openEditBtn.getAttribute("data-open-edit"));
      return;
    }

    const cancelEditBtn = event.target.closest("[data-cancel-edit]");
    if (cancelEditBtn) {
      resetEditForm(cancelEditBtn.getAttribute("data-cancel-edit"));
      return;
    }

    const deleteBtn = event.target.closest("[data-delete-job]");
    if (deleteBtn) {
      const jobId = deleteBtn.getAttribute("data-delete-job");
      await deleteJob(jobId, deleteBtn);
      return;
    }

    const offlineBtn = event.target.closest("[data-offline-job]");
    if (offlineBtn) {
      const jobId = offlineBtn.getAttribute("data-offline-job");
      await takeOffline(jobId, offlineBtn);
    }
  });

  feed.addEventListener("submit", async event => {
    const form = event.target.closest("[data-publish-form]");
    if (form) {
      event.preventDefault();
      await submitPublishForm(form);
      return;
    }
    const editForm = event.target.closest("[data-edit-form]");
    if (editForm) {
      event.preventDefault();
      await submitEditForm(editForm);
    }
  });
}

async function reloadTeacherWorkflow() {
  try {
    teacherSetNotice("jobsNotice", "Loading demand list...", false);
    await loadTeacherJobs();
    await loadJobHistory();
    teacherSetNotice("jobsNotice", `Loaded ${teacherState.items.length} job record(s).`, false);
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  byId("demandForm").addEventListener("submit", submitDemandForm);
  byId("reloadBtn").addEventListener("click", reloadTeacherWorkflow);
  byId("notificationBtn").addEventListener("click", () => {
    const panel = byId("notificationPanel");
    panel.style.display = panel.style.display === "block" ? "none" : "block";
  });
  byId("notificationPanel").addEventListener("click", async event => {
    const markBtn = event.target.closest("[data-mark-read]");
    if (!markBtn) return;
    await markNotificationRead(markBtn.getAttribute("data-mark-read"));
  });
  bindTeacherFeedActions();
  bindHistoryActions();
  await reloadTeacherWorkflow();
  await loadNotifications();
});