function getTeacherContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  return parts.length ? `/${parts[0]}` : "";
}

function apiBase() {
  return `${window.location.origin}${getTeacherContextPath()}/api/mo`;
}

function byId(id) {
  return document.getElementById(id);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function safeText(value) {
  return value === null || value === undefined || value === "" ? "-" : String(value);
}

function setNotice(message, isError) {
  const el = byId("hiredNotice");
  if (!el) return;
  el.textContent = message || "";
  el.style.color = isError ? "#dc2626" : "#475569";
}

async function requestJson(url, options) {
  const res = await fetch(url, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json; charset=UTF-8" },
    ...(options || {})
  });
  const body = await res.json();
  if (!res.ok || !body.success) {
    const err = new Error(body.message || "Request failed.");
    err.code = body.code || "REQUEST_ERROR";
    throw err;
  }
  return body.data;
}

function statusPill(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized === "hired") return '<span class="status-pill status-hired">Hired</span>';
  if (normalized === "resigned") return '<span class="status-pill status-resigned">Resigned</span>';
  if (normalized === "dismissed") return '<span class="status-pill status-dismissed">Dismissed</span>';
  return `<span class="status-pill">${escapeHtml(safeText(status))}</span>`;
}

const state = {
  jobs: {},
  items: [],
  includeHistory: false,
  pendingDismissApplicationId: ""
};

async function loadJobs() {
  const data = await requestJson(`${apiBase()}/demands/list`);
  state.jobs = {};
  (data.items || []).forEach((job) => {
    if (job.jobId) state.jobs[job.jobId] = job.courseName || job.jobId;
  });
  const select = byId("hiredJobFilter");
  const current = select.value || "";
  const options = Object.keys(state.jobs)
    .sort((a, b) => state.jobs[a].localeCompare(state.jobs[b]))
    .map((id) => `<option value="${escapeHtml(id)}">${escapeHtml(state.jobs[id])}</option>`)
    .join("");
  select.innerHTML = `<option value="">All jobs</option>${options}`;
  select.value = current;
}

async function loadHiredStudents() {
  const params = new URLSearchParams();
  const jobId = byId("hiredJobFilter").value;
  if (jobId) params.set("jobId", jobId);
  if (state.includeHistory) params.set("includeHistory", "true");
  const url = params.toString() ? `${apiBase()}/hired-students?${params}` : `${apiBase()}/hired-students`;
  const data = await requestJson(url);
  state.items = Array.isArray(data.items) ? data.items : [];
  renderHiredStudents();
}

function renderHiredStudents() {
  const feed = byId("hiredStudentsFeed");
  const empty = byId("hiredStudentsEmpty");
  if (!state.items.length) {
    feed.innerHTML = "";
    empty.textContent = state.includeHistory ? "No hired student records found." : "No current hired students found.";
    empty.style.display = "block";
    return;
  }
  empty.style.display = "none";
  const groups = groupByCourse(state.items);
  feed.innerHTML = groups.map((group) => `
    <section class="course-group">
      <div class="course-group-head">
        <h3>${escapeHtml(group.title)}</h3>
        <span class="course-count">${group.items.length} TA${group.items.length === 1 ? "" : "s"}</span>
      </div>
      <div class="course-ta-grid">
        ${group.items.map(renderHiredCard).join("")}
      </div>
    </section>
  `).join("");
}

function groupByCourse(items) {
  const byJob = new Map();
  items.forEach((item) => {
    const key = item.jobId || item.jobTitle || "unknown";
    if (!byJob.has(key)) {
      const title = `${safeText(item.jobTitle || item.jobId)}${item.moduleCode ? ` (${item.moduleCode})` : ""}`;
      byJob.set(key, { title, items: [] });
    }
    byJob.get(key).items.push(item);
  });
  return Array.from(byJob.values()).sort((a, b) => a.title.localeCompare(b.title));
}

function renderHiredCard(item) {
    const status = String(item.status || "").toLowerCase();
    const dismiss = status === "hired"
      ? `<button type="button" class="btn btn-outline" style="color:#b91c1c;border-color:#fecaca" data-dismiss="${escapeHtml(item.applicationId)}" data-student-name="${escapeHtml(safeText(item.studentName))}">Dismiss</button>`
      : "";
    const remove = status === "resigned" || status === "dismissed"
      ? `<button type="button" class="btn btn-outline" data-hide-former="${escapeHtml(item.applicationId)}">Remove from page</button>`
      : "";
    return `
      <article class="hired-card">
        <div class="hired-card-head">
          <div>
            <h3>${escapeHtml(safeText(item.studentName))}</h3>
            <p class="hired-meta">${escapeHtml(safeText(item.jobTitle || item.jobId))}</p>
          </div>
          ${statusPill(item.status)}
        </div>
        <div class="hired-detail-grid">
          <div><span>Email</span><div>${escapeHtml(safeText(item.studentEmail))}</div></div>
          <div><span>Student ID</span><div>${escapeHtml(safeText(item.studentNo || item.studentId))}</div></div>
          <div><span>Hired date</span><div>${escapeHtml(safeText(item.hiredDate))}</div></div>
          <div><span>Workload</span><div>${escapeHtml(String(item.weeklyHours || 0))}h/week</div></div>
          <div><span>Module</span><div>${escapeHtml(safeText(item.moduleCode))}</div></div>
          <div><span>Schedule</span><div>${escapeHtml(safeText(item.schedule))}</div></div>
        </div>
        <div style="display:flex;justify-content:flex-end;gap:8px;">${dismiss}${remove}</div>
      </article>`;
}

function openDismissDialog(applicationId, studentName) {
  state.pendingDismissApplicationId = applicationId || "";
  const overlay = byId("dismissDialogOverlay");
  const nameEl = byId("dismissStudentName");
  const reasonEl = byId("dismissReasonInput");
  if (nameEl) nameEl.textContent = studentName || "Selected TA";
  if (reasonEl) reasonEl.value = "";
  if (overlay) {
    overlay.classList.add("open");
    overlay.setAttribute("aria-hidden", "false");
    reasonEl?.focus();
  }
}

function closeDismissDialog() {
  state.pendingDismissApplicationId = "";
  const overlay = byId("dismissDialogOverlay");
  const reasonEl = byId("dismissReasonInput");
  if (reasonEl) reasonEl.value = "";
  if (overlay) {
    overlay.classList.remove("open");
    overlay.setAttribute("aria-hidden", "true");
  }
}

async function dismissStudent(applicationId) {
  const reasonEl = byId("dismissReasonInput");
  const reason = reasonEl ? reasonEl.value.trim() : "";
  await requestJson(`${apiBase()}/hired-students/dismiss`, {
    method: "POST",
    body: JSON.stringify({ applicationId, reason })
  });
  closeDismissDialog();
  setNotice("The student has been dismissed from this TA position.", false);
  await loadHiredStudents();
}

async function hideFormerStudent(applicationId) {
  await requestJson(`${apiBase()}/hired-students/hide`, {
    method: "POST",
    body: JSON.stringify({ applicationId })
  });
  setNotice("Former TA record removed from this page.", false);
  await loadHiredStudents();
}

document.addEventListener("DOMContentLoaded", async () => {
  byId("refreshHiredStudentsBtn").addEventListener("click", async () => {
    try {
      await loadHiredStudents();
      setNotice("Hired students refreshed.", false);
    } catch (err) {
      setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
    }
  });
  byId("hiredJobFilter").addEventListener("change", loadHiredStudents);
  byId("showHiredHistoryToggle")?.addEventListener("change", async (event) => {
    state.includeHistory = Boolean(event.target.checked);
    await loadHiredStudents();
    setNotice(state.includeHistory ? "Showing current and former TA records." : "Showing current hired TAs only.", false);
  });
  byId("hiredStudentsFeed").addEventListener("click", async (event) => {
    const btn = event.target.closest("[data-dismiss]");
    if (btn) {
      openDismissDialog(btn.getAttribute("data-dismiss"), btn.getAttribute("data-student-name"));
      return;
    }
    const hideBtn = event.target.closest("[data-hide-former]");
    if (!hideBtn) return;
    try {
      hideBtn.disabled = true;
      await hideFormerStudent(hideBtn.getAttribute("data-hide-former"));
    } catch (err) {
      setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
    } finally {
      hideBtn.disabled = false;
    }
  });
  byId("closeDismissDialogBtn")?.addEventListener("click", closeDismissDialog);
  byId("cancelDismissBtn")?.addEventListener("click", closeDismissDialog);
  byId("dismissDialogOverlay")?.addEventListener("click", (event) => {
    if (event.target === byId("dismissDialogOverlay")) {
      closeDismissDialog();
    }
  });
  byId("confirmDismissBtn")?.addEventListener("click", async () => {
    if (!state.pendingDismissApplicationId) return;
    const btn = byId("confirmDismissBtn");
    try {
      btn.disabled = true;
      btn.textContent = "Dismissing...";
      await dismissStudent(state.pendingDismissApplicationId);
    } catch (err) {
      setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
    } finally {
      btn.disabled = false;
      btn.textContent = "Dismiss TA";
    }
  });

  try {
    setNotice("Loading hired students...", false);
    await loadJobs();
    await loadHiredStudents();
    setNotice(`Loaded ${state.items.length} hired student record(s).`, false);
  } catch (err) {
    setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
  }
});
