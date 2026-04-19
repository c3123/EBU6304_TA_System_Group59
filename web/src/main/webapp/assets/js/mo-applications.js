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

function statusSelectValue(st) {
  const s = String(st || "").toLowerCase();
  if (s === "viewed") return "pending";
  return s || "pending";
}

const state = {
  items: [],
  jobTitles: {},
  jobMeta: {},
  hiringState: {},
  pollingTimer: null,
  finalModalJobId: null,
  selectedIds: new Set()
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

function buildStatusQueryParam() {
  const p = byId("filterPending").checked;
  const s = byId("filterShortlisted").checked;
  const r = byId("filterRejected").checked;
  const h = byId("filterHired").checked;
  if (p && s && r && h) {
    // #region agent log
    fetch("http://127.0.0.1:7553/ingest/5e55b3f4-868b-425c-8b63-3dc520885d6a", {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Debug-Session-Id": "7b80e4" },
      body: JSON.stringify({
        sessionId: "7b80e4",
        hypothesisId: "H2",
        location: "mo-applications.js:buildStatusQueryParam",
        message: "all four checked -> null param",
        data: { p, s, r, h },
        timestamp: Date.now()
      })
    }).catch(() => {});
    // #endregion
    return null;
  }
  const parts = [];
  if (p) parts.push("pending");
  if (s) parts.push("shortlisted");
  if (r) parts.push("rejected");
  if (h) parts.push("hired");
  if (parts.length === 0) {
    // #region agent log
    fetch("http://127.0.0.1:7553/ingest/5e55b3f4-868b-425c-8b63-3dc520885d6a", {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-Debug-Session-Id": "7b80e4" },
      body: JSON.stringify({
        sessionId: "7b80e4",
        runId: "post-fix",
        hypothesisId: "H2",
        location: "mo-applications.js:buildStatusQueryParam",
        message: "zero checked -> status=__none__ (show no applicants)",
        data: { p, s, r, h },
        timestamp: Date.now()
      })
    }).catch(() => {});
    // #endregion
    return "__none__";
  }
  const joined = parts.join(",");
  // #region agent log
  fetch("http://127.0.0.1:7553/ingest/5e55b3f4-868b-425c-8b63-3dc520885d6a", {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Debug-Session-Id": "7b80e4" },
    body: JSON.stringify({
      sessionId: "7b80e4",
      hypothesisId: "H2",
      location: "mo-applications.js:buildStatusQueryParam",
      message: "partial filter",
      data: { p, s, r, h, joined },
      timestamp: Date.now()
    })
  }).catch(() => {});
  // #endregion
  return joined;
}

function updateBatchBar() {
  const bar = byId("batchBar");
  const label = byId("batchBarLabel");
  const n = state.selectedIds.size;
  if (n === 0) {
    bar.classList.remove("visible");
    return;
  }
  bar.classList.add("visible");
  label.textContent = `${n} applicant${n === 1 ? "" : "s"} selected`;
}

function pruneSelectionToItems() {
  const next = new Set();
  for (const id of state.selectedIds) {
    if (state.items.some(i => i.applicationId === id)) next.add(id);
  }
  state.selectedIds = next;
}

function setIndicator(el, mode, errMsg) {
  if (!el) return;
  el.textContent = "";
  el.removeAttribute("title");
  el.className = "mo-save-indicator";
  if (mode === "loading") {
    el.textContent = "…";
  } else if (mode === "ok") {
    el.textContent = "✓";
    el.classList.add("ok");
    setTimeout(() => {
      if (el.textContent === "✓") {
        el.textContent = "";
        el.classList.remove("ok");
      }
    }, 1000);
  } else if (mode === "err") {
    el.textContent = "!";
    el.classList.add("err");
    if (errMsg) el.setAttribute("title", errMsg);
  }
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
  const st = buildStatusQueryParam();
  if (st != null) {
    params.set("status", st);
  }
  const url = params.toString() ? `${apiBase()}/applications?${params}` : `${apiBase()}/applications`;
  // #region agent log
  fetch("http://127.0.0.1:7553/ingest/5e55b3f4-868b-425c-8b63-3dc520885d6a", {
    method: "POST",
    headers: { "Content-Type": "application/json", "X-Debug-Session-Id": "7b80e4" },
    body: JSON.stringify({
      sessionId: "7b80e4",
      runId: "post-fix",
      hypothesisId: "H3",
      location: "mo-applications.js:loadList",
      message: "applications request url",
      data: { url, statusParam: st },
      timestamp: Date.now()
    })
  }).catch(() => {});
  // #endregion
  const data = await getJson(url);
  state.items = data && Array.isArray(data.items) ? data.items : [];
  pruneSelectionToItems();
  renderApplicantFeed(state.items);
  updateBatchBar();
}

function renderStatusSelect(item, closed) {
  const st = String(item.status || "").toLowerCase();
  const id = escapeHtml(item.applicationId);
  const selVal = statusSelectValue(item.status);
  const selDis = closed || st === "hired" ? "disabled" : "";
  return `<select class="mo-status-select" data-mo-status data-app-id="${id}" data-prev="${escapeHtml(selVal)}" ${selDis}>
    <option value="pending" ${selVal === "pending" ? "selected" : ""}>Pending</option>
    <option value="shortlisted" ${selVal === "shortlisted" ? "selected" : ""}>Shortlisted</option>
    <option value="rejected" ${selVal === "rejected" ? "selected" : ""}>Rejected</option>
    <option value="hired" ${selVal === "hired" ? "selected" : ""}>Hired</option>
  </select>`;
}

function renderNotesAndFeedback(item, closed) {
  const st = String(item.status || "").toLowerCase();
  const id = escapeHtml(item.applicationId);
  const notesVal = escapeHtml(item.evaluationNotes || "");
  const fbAllowed = st === "shortlisted" || st === "rejected" || st === "hired";
  const fbRaw = item.decisionFeedback || "";
  const fbVal = escapeHtml(fbRaw);
  const fbLen = String(fbRaw).length;
  const notesDis = closed ? "disabled" : "";
  const fbBlock = fbAllowed
    ? `<input type="text" maxlength="200" style="flex:1;min-width:140px;" data-mo-feedback data-app-id="${id}" placeholder="Reason (max 200 chars)" value="${fbVal}" ${closed ? "disabled" : ""}/>`
    : `<span style="color:#94a3b8;">—</span>`;
  const fbSaveBtn = fbAllowed
    ? `<button type="button" class="btn btn-outline" data-fb-save data-app-id="${id}" ${closed ? "disabled" : ""}>Save</button>`
    : "";
  const cnt = fbAllowed ? `<span class="mo-fb-count" data-fb-count data-app-id="${id}">${fbLen}/200</span>` : "";
  return `
    <div class="mo-field-inline" data-notes-wrap>
      <label>Evaluation notes</label>
      <div style="flex:1;display:flex;align-items:flex-start;gap:8px;">
        <textarea rows="2" style="flex:1;" data-mo-notes data-app-id="${id}" placeholder="Private evaluation notes..." ${notesDis}>${notesVal}</textarea>
        <span class="mo-save-indicator" data-notes-ind data-app-id="${id}" style="padding-top:6px;"></span>
      </div>
    </div>
    <div class="mo-field-inline" data-fb-wrap>
      <label>Decision feedback</label>
      <div style="flex:1;display:flex;flex-wrap:wrap;align-items:center;gap:8px;">
        ${fbBlock}
        ${fbSaveBtn}
        <span class="mo-save-indicator" data-fb-ind data-app-id="${id}"></span>
        ${cnt}
      </div>
    </div>`;
}

function renderApplicantCard(item, closed) {
  const st = String(item.status || "").toLowerCase();
  const id = escapeHtml(item.applicationId);
  const rawId = item.applicationId;
  const jobLabel = escapeHtml(safeText(state.jobTitles[item.jobId] || item.jobId || "—"));
  const positionHrs = jobWeeklyHours(item.jobId);
  const currentOther = currentHiredHoursElsewhere(state.items, item.studentId, item.applicationId);
  const ifHiredTotal = currentOther + positionHrs;
  const tier = workloadTier(ifHiredTotal);
  const showWorkloadPanel = st !== "hired";
  const borderClass = (showWorkloadPanel ? wlCardClass(tier.key) : "mo-wl-neutral") + (state.selectedIds.has(rawId) ? " mo-app-card-selected" : "");
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

  const chk = closed
    ? ""
    : `<label style="display:flex;align-items:center;gap:6px;margin-right:8px;flex-shrink:0;"><input type="checkbox" data-app-select="${id}" ${state.selectedIds.has(rawId) ? "checked" : ""} /></label>`;

  return `
    <article class="mo-app-card-proto ${borderClass}" data-application-id="${id}">
      <div class="mo-app-card-head" style="display:flex;align-items:flex-start;justify-content:space-between;gap:12px;">
        <div style="display:flex;align-items:flex-start;gap:8px;min-width:0;">
          ${chk}
          <div style="min-width:0;"><h4>${escapeHtml(safeText(item.studentName))}</h4><p class="mo-app-meta">${jobLabel} • Applied: ${escapeHtml(safeText(item.appliedAt))}</p></div>
        </div>
        <div class="mo-app-status-slot" style="flex-shrink:0;">${renderStatusSelect(item, closed)}</div>
      </div>
      ${workloadBlock}
      ${actionsBlock}
      ${renderNotesAndFeedback(item, closed)}
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
          <div><span class="mo-app-lbl">Evaluation notes</span><div data-field="evaluationNotes"></div></div>
          <div><span class="mo-app-lbl">Decision feedback</span><div data-field="decisionFeedback"></div></div>
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
    const jEsc = escapeHtml(jobId);
    parts.push(`<section class="mo-job-group" data-job-group="${jEsc}"><div class="mo-job-group-bar">`);
    parts.push(`<h3 class="mo-job-group-title">${escapeHtml(label)} <span style="font-weight:500;color:#64748b">(${groupItems.length} applicant${groupItems.length === 1 ? "" : "s"})</span></h3>`);
    parts.push(`<div class="mo-job-tools"><label style="display:flex;align-items:center;gap:6px;font-size:13px;margin-right:8px;color:#475569;"><input type="checkbox" data-select-all-job="${jEsc}" ${closed ? "disabled" : ""}/> Select all</label><span class="mo-pos-hrs">This position: <strong>${posHrs || "—"}</strong> hours/week</span>${closed ? `<span class="mo-closed-flag">Recruitment Closed${closedAt ? ` (${escapeHtml(closedAt)})` : ""}</span>` : ""}<button class="btn btn-outline" type="button" data-open-history="${jEsc}">View history</button><button class="btn btn-primary" type="button" data-open-final="${jEsc}" ${closed ? "disabled" : ""}>Confirm Final Hiring</button></div>`);
    parts.push(`</div>`);
    if (!closed && shortlistedCount === 0) {
      parts.push(`<p class="notice">No shortlisted applicant yet for final confirmation.</p>`);
    }
    for (const item of groupItems) parts.push(renderApplicantCard(item, closed));
    parts.push(`</section>`);
  }
  feed.innerHTML = parts.join("");
  syncSelectAllMasters();
}

function syncSelectAllMasters() {
  const feed = byId("applicationsFeed");
  if (!feed) return;
  feed.querySelectorAll(".mo-job-group").forEach(sec => {
    const master = sec.querySelector("[data-select-all-job]");
    if (!master || master.disabled) return;
    const boxes = [...sec.querySelectorAll("[data-app-select]")];
    if (!boxes.length) return;
    const allOn = boxes.every(b => b.checked);
    const someOn = boxes.some(b => b.checked);
    master.checked = allOn;
    master.indeterminate = !allOn && someOn;
  });
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
    const it = state.items.find(i => i.applicationId === rawId);
    if (it) {
      it.status = detail.status;
      if (detail.evaluationNotes != null) it.evaluationNotes = detail.evaluationNotes;
      if (detail.decisionFeedback != null) it.decisionFeedback = detail.decisionFeedback;
    }
    const slot = card.querySelector("[data-mo-status]");
    if (slot && !slot.disabled) {
      const nv = statusSelectValue(detail.status);
      slot.value = nv;
      slot.setAttribute("data-prev", nv);
    }
  } finally {
    btn.disabled = false;
  }
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

function mergeUpdatedItem(applicationId, updated) {
  const it = state.items.find(i => i.applicationId === applicationId);
  if (!it || !updated) return;
  if (updated.status != null) it.status = updated.status;
  if (updated.evaluationNotes != null) it.evaluationNotes = updated.evaluationNotes;
  if (updated.decisionFeedback != null) it.decisionFeedback = updated.decisionFeedback;
}

async function submitDecision(applicationId, status) {
  const updated = await postJson(`${apiBase()}/applications/status`, { applicationId, status });
  mergeUpdatedItem(applicationId, updated);
}

async function saveNotes(applicationId, text) {
  await postJson(`${apiBase()}/applications/notes`, { applicationId, evaluationNotes: text });
  const it = state.items.find(i => i.applicationId === applicationId);
  if (it) it.evaluationNotes = text;
}

async function saveFeedback(applicationId, text) {
  await postJson(`${apiBase()}/applications/feedback`, { applicationId, decisionFeedback: text });
  const it = state.items.find(i => i.applicationId === applicationId);
  if (it) it.decisionFeedback = text;
}

async function persistFeedbackFromInput(feed, fbEl) {
  const appId = fbEl.getAttribute("data-app-id");
  const ind = feed.querySelector(`[data-fb-ind][data-app-id="${appId}"]`);
  const wrap = fbEl.closest("[data-fb-wrap]");
  wrap.classList.remove("mo-field-error");
  setIndicator(ind, "loading");
  try {
    await saveFeedback(appId, fbEl.value);
    setIndicator(ind, "ok");
    setNotice("Decision feedback saved.", false);
  } catch (err) {
    wrap.classList.add("mo-field-error");
    setIndicator(ind, "err", err.message || "Save failed");
    setNotice(`${err.code || "ERROR"}: ${err.message || "Save failed"}`, true);
  }
}

async function runBatchStatus(status, label) {
  const ids = Array.from(state.selectedIds);
  if (!ids.length) return;
  const ok = window.confirm(`Are you sure you want to mark ${ids.length} applicant${ids.length === 1 ? "" : "s"} as ${label}?`);
  if (!ok) return;
  try {
    setNotice("Updating applicants...", false);
    const data = await postJson(`${apiBase()}/applications/batch/status`, { ids, status });
    setNotice(`${data.updated || ids.length} applicants updated successfully.`, false);
    state.selectedIds.clear();
    await queryWithFeedback();
  } catch (err) {
    setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
  }
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
    state.items = [];
    renderApplicantFeed([]);
  }
}

function exportCsv() {
  const lines = ["applicationId,jobId,studentName,status,appliedAt,evaluationNotes,decisionFeedback"];
  for (const it of state.items) {
    const row = [it.applicationId, it.jobId, it.studentName, it.status, it.appliedAt, it.evaluationNotes, it.decisionFeedback].map(v =>
      `"${String(v ?? "").replace(/"/g, '""')}"`
    );
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

  let filterDeb;
  ["filterPending", "filterShortlisted", "filterRejected", "filterHired"].forEach(fid => {
    byId(fid).addEventListener("change", () => {
      clearTimeout(filterDeb);
      filterDeb = setTimeout(() => queryWithFeedback(), 200);
    });
  });

  feed.addEventListener("change", async e => {
    if (e.target.matches("input[type=checkbox][data-select-all-job]")) {
      const section = e.target.closest(".mo-job-group");
      if (!section) return;
      const boxes = section.querySelectorAll("[data-app-select]");
      const on = e.target.checked;
      boxes.forEach(b => {
        b.checked = on;
        const aid = b.getAttribute("data-app-select");
        if (!aid) return;
        if (on) state.selectedIds.add(aid);
        else state.selectedIds.delete(aid);
      });
      renderApplicantFeed(state.items);
      updateBatchBar();
      return;
    }

    if (e.target.matches("input[type=checkbox][data-app-select]")) {
      const aid = e.target.getAttribute("data-app-select");
      if (e.target.checked) state.selectedIds.add(aid);
      else state.selectedIds.delete(aid);
      renderApplicantFeed(state.items);
      updateBatchBar();
      return;
    }

    const statusSel = e.target.closest("[data-mo-status]");
    if (statusSel && !statusSel.disabled) {
      const appId = statusSel.getAttribute("data-app-id");
      const prev = statusSel.getAttribute("data-prev") || "pending";
      const v = statusSel.value;
      if (v === prev) return;
      statusSel.disabled = true;
      try {
        setNotice("Saving status...", false);
        await submitDecision(appId, v);
        statusSel.setAttribute("data-prev", v);
        renderApplicantFeed(state.items);
        updateBatchBar();
        setNotice("Saved. Status is stored on the server.", false);
      } catch (err) {
        statusSel.value = prev;
        setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
      } finally {
        statusSel.disabled = false;
      }
    }
  });

  feed.addEventListener(
    "blur",
    async e => {
      const notesEl = e.target.closest("[data-mo-notes]");
      if (notesEl && !notesEl.disabled) {
        const appId = notesEl.getAttribute("data-app-id");
        const ind = feed.querySelector(`[data-notes-ind][data-app-id="${appId}"]`);
        const wrap = notesEl.closest("[data-notes-wrap]");
        wrap.classList.remove("mo-field-error");
        setIndicator(ind, "loading");
        try {
          await saveNotes(appId, notesEl.value);
          setIndicator(ind, "ok");
        } catch (err) {
          wrap.classList.add("mo-field-error");
          setIndicator(ind, "err", err.message || "Save failed");
        }
        return;
      }
      const fbEl = e.target.closest("[data-mo-feedback]");
      if (fbEl && !fbEl.disabled) {
        await persistFeedbackFromInput(feed, fbEl);
      }
    },
    true
  );

  feed.addEventListener("input", e => {
    const fbEl = e.target.closest("[data-mo-feedback]");
    if (!fbEl) return;
    const appId = fbEl.getAttribute("data-app-id");
    const cnt = feed.querySelector(`[data-fb-count][data-app-id="${appId}"]`);
    if (cnt) cnt.textContent = `${fbEl.value.length}/200`;
  });

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
        renderApplicantFeed(state.items);
        updateBatchBar();
        setNotice("Saved. Status is stored on the server.", false);
      } catch (err) {
        setNotice(`${err.code || "ERROR"}: ${err.message}`, true);
      } finally {
        actionBtn.disabled = false;
      }
      return;
    }
    const saveBtn = e.target.closest("[data-fb-save]");
    if (saveBtn && !saveBtn.disabled) {
      const appId = saveBtn.getAttribute("data-app-id");
      const fbEl = feed.querySelector(`[data-mo-feedback][data-app-id="${appId}"]`);
      if (!fbEl || fbEl.disabled) return;
      saveBtn.disabled = true;
      await persistFeedbackFromInput(feed, fbEl);
      saveBtn.disabled = false;
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

  byId("batchShortlistBtn").addEventListener("click", () => runBatchStatus("shortlisted", "Shortlisted"));
  byId("batchRejectBtn").addEventListener("click", () => runBatchStatus("rejected", "Rejected"));
  byId("batchPendingBtn").addEventListener("click", () => runBatchStatus("pending", "Pending"));

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
    byId("filterPending").checked = true;
    byId("filterShortlisted").checked = true;
    byId("filterRejected").checked = true;
    byId("filterHired").checked = true;
    await queryWithFeedback();
  });
  byId("exportCsvBtn").addEventListener("click", exportCsv);

  const urlParams = new URLSearchParams(window.location.search);
  const jobIdFromUrl = urlParams.get("jobId");
  if (jobIdFromUrl) {
    byId("jobIdInput").value = jobIdFromUrl;
  }

  await queryWithFeedback();
  startPolling();
});
