function getContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (parts.length === 0) return "";
  return "/" + parts[0];
}

function apiBase() {
  return `${window.location.origin}${getContextPath()}/api/mo`;
}

function safeText(value) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

function escapeHtml(s) {
  if (s == null) return "";
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function statusPill(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized === "pending" || normalized === "viewed") return '<span class="status-pill status-pending">Pending</span>';
  if (normalized === "shortlisted") return '<span class="status-pill status-shortlisted">Shortlisted</span>';
  if (normalized === "hired") return '<span class="status-pill status-hired">Hired</span>';
  if (normalized === "rejected") return '<span class="status-pill status-rejected">Rejected</span>';
  return `<span class="status-pill">${escapeHtml(safeText(status))}</span>`;
}

const state = {
  items: [],
  jobTitles: {},
  jobMeta: {},
  hiringState: {},
  pollingTimer: null,
  finalModalJobId: null
};

async function getJson(url) {
  const res = await fetch(url, { method: "GET", credentials: "same-origin" });
  const body = await res.json();
  if (!res.ok || !body.success) {
    const err = new Error(body.message || "Request failed.");
    err.code = body.code || "REQUEST_ERROR";
    err.httpStatus = res.status;
    throw err;
  }
  return body.data;
}

async function postJson(url, payload) {
  const res = await fetch(url, {
    method: "POST",
    credentials: "same-origin",
    headers: { "Content-Type": "application/json;charset=UTF-8" },
    body: payload ? JSON.stringify(payload) : null
  });
  const body = await res.json();
  if (!res.ok || !body.success) {
    const err = new Error(body.message || "Request failed.");
    err.code = body.code || "REQUEST_ERROR";
    err.httpStatus = res.status;
    throw err;
  }
  return body.data;
}

function setNotice(message, isError) {
  const notice = byId("pageNotice");
  notice.textContent = message || "";
  notice.style.color = isError ? "#dc2626" : "#475569";
}

function jobClosed(jobId) {
  if (!jobId) return false;
  if (state.hiringState[jobId]) return state.hiringState[jobId].recruitmentClosed === true;
  const m = state.jobMeta[jobId];
  return !!(m && m.recruitmentClosed === true);
}

function jobWeeklyHours(jobId) {
  const j = state.jobMeta[jobId];
  if (!j) return 0;
  const min = Number(j.hourMin);
  const max = Number(j.hourMax);
  if (Number.isFinite(min) && Number.isFinite(max)) return Math.round((min + max) / 2);
  if (Number.isFinite(max)) return max;
  if (Number.isFinite(min)) return min;
  const legacy = Number(j.hours);
  if (Number.isFinite(legacy) && legacy > 0) return Math.round(legacy);
  return 0;
}

function currentHiredHoursElsewhere(items, studentId, excludeApplicationId) {
  let sum = 0;
  for (const it of items) {
    if (it.studentId !== studentId || it.applicationId === excludeApplicationId) continue;
    if (String(it.status || "").toLowerCase() !== "hired") continue;
    sum += jobWeeklyHours(it.jobId);
  }
  return sum;
}

function workloadTier(totalIfHired) {
  const t = Number(totalIfHired);
  if (!Number.isFinite(t)) return { key: "normal", label: "Normal" };
  if (t < 10) return { key: "low", label: "Low" };
  if (t <= 14) return { key: "normal", label: "Normal" };
  if (t <= 19) return { key: "warn", label: "Warning" };
  return { key: "over", label: "Overload" };
}

function wlPanelClass(key) {
  if (key === "low") return "mo-wl-low";
  if (key === "normal") return "mo-wl-normal";
  if (key === "warn") return "mo-wl-warn";
  return "mo-wl-over";
}

function wlCardClass(key) {
  if (key === "low") return "mo-wl-low";
  if (key === "normal") return "mo-wl-normal";
  if (key === "warn") return "mo-wl-warn";
  return "mo-wl-over";
}

async function loadJobTitlesAndMeta() {
  const data = await getJson(`${apiBase()}/demands/list`);
  const map = {};
  const meta = {};
  (data.items || []).forEach(it => {
    if (!it.jobId) return;
    map[it.jobId] = it.courseName && String(it.courseName).trim() ? it.courseName : it.jobId;
    meta[it.jobId] = it;
  });
  state.jobTitles = map;
  state.jobMeta = meta;
}

async function loadHiringState() {
  const data = await getJson(`${apiBase()}/hiring/state`);
  const map = {};
  (data.items || []).forEach(it => {
    if (it.jobId) map[it.jobId] = it;
  });
  state.hiringState = map;
}

async function loadList() {
  const jobId = byId("jobIdInput").value.trim();
  const params = new URLSearchParams();
  if (jobId) params.set("jobId", jobId);
  const url = params.toString() ? `${apiBase()}/applications?${params}` : `${apiBase()}/applications`;
  const data = await getJson(url);
  state.items = data && Array.isArray(data.items) ? data.items : [];
  renderApplicantFeed(state.items);
}

function renderApplicantCard(item, closed) {
  const st = String(item.status || "").toLowerCase();
  const id = escapeHtml(item.applicationId);
  const jobLabel = escapeHtml(safeText(state.jobTitles[item.jobId] || item.jobId || "—"));
  const positionHrs = jobWeeklyHours(item.jobId);
  const currentOther = currentHiredHoursElsewhere(state.items, item.studentId, item.applicationId);
  const ifHiredTotal = currentOther + positionHrs;
  const tier = workloadTier(ifHiredTotal);
  const showWorkloadPanel = st !== "hired";
  const borderClass = showWorkloadPanel ? wlCardClass(tier.key) : "mo-wl-neutral";
  const hireLabel = ifHiredTotal >= 20 ? "Hire (Override Warning)" : "Hire";

  const warnNote = tier.key === "over"
    ? `<p style="margin:8px 0 0;font-size:12px;color:#b91c1c;font-weight:600;">Warning: Hiring this student will exceed 20h/week workload limit!</p>`
    : `<p style="margin:8px 0 0;font-size:12px;color:#854d0e;">If hired, total workload would be <strong>${ifHiredTotal}h/week</strong> (${tier.label} level).</p>`;
  const workloadBlock = showWorkloadPanel ? `
    <div class="mo-wl-panel ${wlPanelClass(tier.key)}">
      <div style="font-weight:700;margin-bottom:8px;color:#0f172a;">Current Workload Status</div>
      <div class="mo-wl-grid">
        <div><span class="mo-app-lbl">Current Hours/Week</span><div class="mo-wl-big" style="color:#2563eb">${currentOther}h</div></div>
        <div><span class="mo-app-lbl">If Hired (Total)</span><div class="mo-wl-big" style="color:${tier.key === "over" ? "#dc2626" : "#ca8a04"}">${ifHiredTotal}h</div></div>
        <div><span class="mo-app-lbl">Status</span><div><span class="status-pill ${tier.key === "over" ? "status-rejected" : tier.key === "warn" ? "status-pending" : "status-hired"}">${tier.label}</span></div></div>
      </div>
      ${warnNote}
    </div>` : "";

  let actionsBlock = "";
  if (closed) {
    actionsBlock = `<div class="mo-wl-actions"><span class="mo-closed-flag">Recruitment Closed</span><button type="button" class="btn btn-primary mo-app-detail-btn">View details</button></div>`;
  } else if (st === "rejected") {
    actionsBlock = `<div class="mo-wl-actions"><button type="button" class="btn btn-outline" data-mo-action="viewed" data-app-id="${id}">Undo reject</button><button type="button" class="btn btn-primary mo-app-detail-btn">View details</button></div>`;
  } else if (st === "hired") {
    actionsBlock = `<div class="mo-wl-actions"><button type="button" class="btn btn-success" disabled>Hire</button><button type="button" class="btn btn-outline" disabled>Shortlist</button><button type="button" class="btn btn-outline" style="color:#b91c1c;border-color:#fecaca" disabled>Reject</button><button type="button" class="btn btn-primary mo-app-detail-btn">View details</button></div>`;
  } else {
    actionsBlock = `<div class="mo-wl-actions"><button type="button" class="btn btn-success" data-mo-action="hired" data-app-id="${id}">${hireLabel}</button><button type="button" class="btn btn-outline" data-mo-action="shortlisted" data-app-id="${id}">Shortlist</button><button type="button" class="btn btn-outline" style="color:#b91c1c;border-color:#fecaca" data-mo-action="rejected" data-app-id="${id}">Reject</button><button type="button" class="btn btn-primary mo-app-detail-btn">View details</button></div>`;
  }

  return `
    <article class="mo-app-card-proto ${borderClass}" data-application-id="${id}">
      <div class="mo-app-card-head">
        <div><h4>${escapeHtml(safeText(item.studentName))}</h4><p class="mo-app-meta">${jobLabel} • Applied: ${escapeHtml(safeText(item.appliedAt))}</p></div>
        <div class="mo-app-status-slot">${statusPill(item.status)}</div>
      </div>
      ${workloadBlock}
      ${actionsBlock}
      <div class="mo-app-grid">
        <div><span class="mo-app-lbl">Student No</span><div>${escapeHtml(safeText(item.studentNo))}</div></div>
        <div><span class="mo-app-lbl">Programme</span><div>${escapeHtml(safeText(item.programme))}</div></div>
      </div>
      <div style="margin-bottom:12px;"><span class="mo-app-lbl">Skills</span><div>${escapeHtml(safeText(item.skills))}</div></div>
      <div style="margin-bottom:12px;"><span class="mo-app-lbl">Experience / Statement</span><div style="font-size:14px;color:#334155;">${escapeHtml(safeText(item.experience))}</div></div>
      <div class="mo-app-expand">
        <p class="mo-app-meta" style="margin-bottom:10px">Attachments and full record (opening details marks <strong>pending</strong> as <strong>viewed</strong> on the server).</p>
        <div class="mo-app-expand-grid">
          <div><span class="mo-app-lbl">Application ID</span><div data-field="applicationId"></div></div>
          <div><span class="mo-app-lbl">Job ID</span><div data-field="jobId"></div></div>
          <div><span class="mo-app-lbl">Course grade</span><div data-field="courseGrade"></div></div>
          <div><span class="mo-app-lbl">Applied at</span><div data-field="appliedAt"></div></div>
          <div><span class="mo-app-lbl">Status</span><div data-field="status"></div></div>
          <div><span class="mo-app-lbl">Updated at</span><div data-field="updatedAt"></div></div>
          <div style="grid-column:1/-1;"><span class="mo-app-lbl">Attachments</span><div data-field="attachments"></div></div>
        </div>
      </div>
    </article>`;
}

function renderApplicantFeed(items) {
  const feed = byId("applicationsFeed");
  const emptyEl = byId("applicationsEmpty");
  if (!items || !items.length) {
    emptyEl.style.display = "block";
    feed.style.display = "none";
    feed.innerHTML = "";
    return;
  }
  emptyEl.style.display = "none";
  feed.style.display = "flex";
  const groups = new Map();
  for (const item of items) {
    const j = item.jobId != null ? String(item.jobId) : "";
    if (!groups.has(j)) groups.set(j, []);
    groups.get(j).push(item);
  }
  const keys = Array.from(groups.keys()).sort();
  const parts = [];
  for (const jobId of keys) {
    const groupItems = groups.get(jobId);
    const label = state.jobTitles[jobId] || (jobId ? `Job ${jobId}` : "Unknown job");
    const posHrs = jobWeeklyHours(jobId);
    const closed = jobClosed(jobId);
    const closedAt = state.hiringState[jobId] ? state.hiringState[jobId].closedAt : "";
    const shortlistedCount = groupItems.filter(x => String(x.status || "").toLowerCase() === "shortlisted").length;
    parts.push(`<section class="mo-job-group"><div class="mo-job-group-bar">`);
    parts.push(`<h3 class="mo-job-group-title">${escapeHtml(label)} <span style="font-weight:500;color:#64748b">(${groupItems.length} applicant${groupItems.length === 1 ? "" : "s"})</span></h3>`);
    parts.push(`<div class="mo-job-tools"><span class="mo-pos-hrs">This position: <strong>${posHrs || "—"}</strong> hours/week</span>${closed ? `<span class="mo-closed-flag">Recruitment Closed${closedAt ? ` (${escapeHtml(closedAt)})` : ""}</span>` : ""}<button class="btn btn-outline" type="button" data-open-history="${escapeHtml(jobId)}">View history</button><button class="btn btn-primary" type="button" data-open-final="${escapeHtml(jobId)}" ${closed ? "disabled" : ""}>Confirm Final Hiring</button></div>`);
    parts.push(`</div>`);
    if (!closed && shortlistedCount === 0) {
      parts.push(`<p class="notice">No shortlisted applicant yet for final confirmation.</p>`);
    }
    for (const item of groupItems) parts.push(renderApplicantCard(item, closed));
    parts.push(`</section>`);
  }
  feed.innerHTML = parts.join("");
}

function fillDetailFields(expandEl, detail) {
  expandEl.querySelectorAll("[data-field]").forEach(el => {
    const k = el.getAttribute("data-field");
    if (k === "attachments") {
      const list = Array.isArray(detail.attachments) ? detail.attachments : [];
      if (!list.length) {
        el.innerHTML = '<span style="color:#64748b;">No attachments submitted.</span>';
        return;
      }
      const contextPath = getContextPath();
      el.innerHTML = list.map(att => {
        const href = `${window.location.origin}${contextPath}${att.downloadUrl}`;
        const sizeText = Number(att.fileSize || 0) > 0 ? ` (${Math.round((att.fileSize / 1024) * 10) / 10} KB)` : "";
        return `<div style="margin:6px 0;display:flex;justify-content:space-between;gap:12px;align-items:center;"><span>${escapeHtml(safeText(att.label || "Attachment"))}: ${escapeHtml(safeText(att.fileName || "file"))}${escapeHtml(sizeText)}</span><a class="btn btn-outline" style="padding:4px 10px;font-size:12px;" href="${encodeURI(href)}" target="_blank" rel="noopener">Download</a></div>`;
      }).join("");
      return;
    }
    const val = detail[k];
    if (k === "status") el.innerHTML = statusPill(val);
    else el.textContent = safeText(val);
  });
}

async function openCardDetail(card, btn) {
  const rawId = card.getAttribute("data-application-id");
  const expand = card.querySelector(".mo-app-expand");
  if (!rawId || !expand || !btn) return;
  btn.disabled = true;
  try {
    const detail = await getJson(`${apiBase()}/applications/detail/${encodeURIComponent(rawId)}`);
    fillDetailFields(expand, detail);
    expand.classList.add("mo-open");
    const slot = card.querySelector(".mo-app-status-slot");
    if (slot) slot.innerHTML = statusPill(detail.status);
    const it = state.items.find(i => i.applicationId === rawId);
    if (it) it.status = detail.status;
  } finally {
    btn.disabled = false;
  }
}

async function loadList() {
  const jobId = byId("jobIdInput").value.trim();
  const params = new URLSearchParams();
  if (jobId) params.set("jobId", jobId);
  const query = params.toString();
  const url = query
    ? `${apiBase()}/applications?${query}`
    : `${apiBase()}/applications`;
  const data = await getJson(url);
  state.items = data && Array.isArray(data.items) ? data.items : [];
  renderApplicantFeed(state.items);
}

async function openFinalHiringModal(jobId) {
  state.finalModalJobId = jobId;
  const modal = byId("finalHiringModal");
  const label = byId("finalHiringJobLabel");
  const listEl = byId("finalHiringList");
  const items = state.items.filter(x => x.jobId === jobId && String(x.status || "").toLowerCase() === "shortlisted");
  label.textContent = safeText(state.jobTitles[jobId] || jobId);
  if (!items.length) {
    listEl.innerHTML = `<p class="notice">No shortlisted applicants for this job.</p>`;
    byId("finalHiringConfirmBtn").disabled = true;
  } else {
    byId("finalHiringConfirmBtn").disabled = false;
    listEl.innerHTML = items.map(it => `
      <label class="mo-modal-row">
        <input type="checkbox" data-final-app-id="${escapeHtml(it.applicationId)}" checked />
        <span><strong>${escapeHtml(safeText(it.studentName))}</strong><br/><span style="font-size:12px;color:#64748b">${escapeHtml(safeText(it.studentNo))} • ${escapeHtml(safeText(it.programme))}</span></span>
        ${statusPill(it.status)}
      </label>`).join("");
  }
  modal.classList.add("open");
}

function closeFinalHiringModal() {
  byId("finalHiringModal").classList.remove("open");
  state.finalModalJobId = null;
}

async function submitFinalHiring() {
  const jobId = state.finalModalJobId;
  if (!jobId) return;
  const checked = Array.from(document.querySelectorAll("#finalHiringList [data-final-app-id]:checked")).map(el => el.getAttribute("data-final-app-id"));
  await postJson(`${apiBase()}/hiring/finalize`, { jobId, hiredApplicationIds: checked });
}

async function openHistoryModal(jobId) {
  const modal = byId("historyModal");
  byId("historyJobLabel").textContent = safeText(state.jobTitles[jobId] || jobId);
  const listEl = byId("historyList");
  listEl.innerHTML = "<p class='notice'>Loading...</p>";
  modal.classList.add("open");
  const history = await getJson(`${apiBase()}/hiring/history?jobId=${encodeURIComponent(jobId)}`);
  if (!history.items || !history.items.length) {
    listEl.innerHTML = "<p class='notice'>No history records yet.</p>";
    return;
  }
  listEl.innerHTML = history.items.map(it => `
    <div class="mo-modal-row" style="grid-template-columns:1fr auto;">
      <span><strong>${escapeHtml(safeText(it.action))}</strong><br/><span style="font-size:12px;color:#64748b">${escapeHtml((it.hiredStudentNames || []).join(", ") || "No hired candidates")}</span></span>
      <span style="font-size:12px;color:#64748b">${escapeHtml(safeText(it.submittedAt))}</span>
    </div>`).join("");
}

function closeHistoryModal() {
  byId("historyModal").classList.remove("open");
}

async function submitDecision(applicationId, status) {
  await postJson(`${apiBase()}/applications/status`, { applicationId, status });
}

async function queryWithFeedback() {
  try {
    setNotice("Loading applications...", false);
    await loadJobTitlesAndMeta();
    await loadHiringState();
    await loadList();
    setNotice(`Loaded ${state.items.length} active application(s).`, false);
  } catch (err) {
    setNotice(`${err.code || "REQUEST_ERROR"}: ${err.message || "Request failed."}`, true);
    renderApplicantFeed([]);
  }
}

function exportCsv() {
  const lines = ["applicationId,jobId,studentName,status,appliedAt"];
  for (const it of state.items) {
    const row = [it.applicationId, it.jobId, it.studentName, it.status, it.appliedAt].map(v => `"${String(v ?? "").replace(/"/g, '""')}"`);
    lines.push(row.join(","));
  }
  const blob = new Blob([lines.join("\r\n")], { type: "text/csv;charset=utf-8" });
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = "mo-applications.csv";
  a.click();
  URL.revokeObjectURL(a.href);
}

function startPolling() {
  if (state.pollingTimer) clearInterval(state.pollingTimer);
  state.pollingTimer = setInterval(async () => {
    try {
      await queryWithFeedback();
    } catch (_) {
      /* silent */
    }
  }, 12000);
}

document.addEventListener("DOMContentLoaded", async () => {
  const feed = byId("applicationsFeed");
  feed.addEventListener("click", async e => {
    const finalBtn = e.target.closest("[data-open-final]");
    if (finalBtn && !finalBtn.disabled) {
      await openFinalHiringModal(finalBtn.getAttribute("data-open-final"));
      return;
    }
    const historyBtn = e.target.closest("[data-open-history]");
    if (historyBtn) {
      await openHistoryModal(historyBtn.getAttribute("data-open-history"));
      return;
    }
    const actionBtn = e.target.closest("[data-mo-action]");
    if (actionBtn && !actionBtn.disabled) {
      const appId = actionBtn.getAttribute("data-app-id");
      const action = actionBtn.getAttribute("data-mo-action");
      if (!appId || !action) return;
      actionBtn.disabled = true;
      try {
        setNotice("Saving...", false);
        await submitDecision(appId, action);
        await queryWithFeedback();
        setNotice("Saved. Status is stored on the server.", false);
      } catch (err) {
        setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
      } finally {
        actionBtn.disabled = false;
      }
      return;
    }
    const btn = e.target.closest(".mo-app-detail-btn");
    if (!btn) return;
    const card = btn.closest(".mo-app-card-proto");
    const expand = card.querySelector(".mo-app-expand");
    if (expand.classList.contains("mo-open")) {
      expand.classList.remove("mo-open");
      btn.textContent = "View details";
      return;
    }
    btn.textContent = "Loading...";
    await openCardDetail(card, btn);
    btn.textContent = "Hide details";
  });

  byId("finalHiringCloseBtn").addEventListener("click", closeFinalHiringModal);
  byId("historyCloseBtn").addEventListener("click", closeHistoryModal);
  byId("finalHiringConfirmBtn").addEventListener("click", async () => {
    try {
      byId("finalHiringConfirmBtn").disabled = true;
      setNotice("Submitting final hiring...", false);
      await submitFinalHiring();
      closeFinalHiringModal();
      await queryWithFeedback();
      setNotice("Final hiring submitted. Recruitment closed.", false);
    } catch (err) {
      setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
    } finally {
      byId("finalHiringConfirmBtn").disabled = false;
    }
  });

  byId("queryBtn").addEventListener("click", queryWithFeedback);
  byId("resetBtn").addEventListener("click", async () => {
    byId("jobIdInput").value = "";
    await queryWithFeedback();
  });
  byId("exportCsvBtn").addEventListener("click", exportCsv);

  // Check URL parameters for jobId filter
  const urlParams = new URLSearchParams(window.location.search);
  const jobIdFromUrl = urlParams.get("jobId");
  if (jobIdFromUrl) {
    byId("jobIdInput").value = jobIdFromUrl;
  }

  await queryWithFeedback();
  startPolling();
});
