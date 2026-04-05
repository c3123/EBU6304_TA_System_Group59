function getContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (parts.length === 0) return "";
  return "/" + parts[0];
}

function apiBase() {
  return `${window.location.origin}${getContextPath()}/api/mo`;
}

/**
 * MO identity when dev fallback or query param is used.
 */
function getResolvedMoId() {
  const fromUrl = new URLSearchParams(window.location.search).get("moId");
  if (fromUrl && fromUrl.trim()) return fromUrl.trim();
  try {
    const fromStorage = localStorage.getItem("ta_mo_dev_id");
    if (fromStorage && fromStorage.trim()) return fromStorage.trim();
  } catch (_) {
    /* ignore */
  }
  return "mo001";
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

function statusTag(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized === "pending") {
    return '<span class="status-pill status-pending">pending</span>';
  }
  if (normalized === "viewed") {
    return '<span class="status-pill status-viewed">viewed</span>';
  }
  return `<span class="status-pill">${escapeHtml(safeText(status))}</span>`;
}

const state = {
  items: [],
  jobTitles: {},
  pollingTimer: null
};

async function getJson(url) {
  const moId = getResolvedMoId();
  const headers = {};
  if (moId) {
    headers["X-MO-ID"] = moId;
  }
  const res = await fetch(url, { method: "GET", headers, credentials: "same-origin" });
  const body = await res.json();
  if (!res.ok || !body.success) {
    const err = new Error(body.message || "Request failed.");
    err.code = body.code || "REQUEST_ERROR";
    err.httpStatus = res.status;
    throw err;
  }
  return body.data;
}

async function loadJobTitles() {
  try {
    const moId = getResolvedMoId();
    const q = moId ? `?moId=${encodeURIComponent(moId)}` : "";
    const data = await getJson(`${apiBase()}/demands/list${q}`);
    const map = {};
    (data.items || []).forEach(it => {
      if (it.jobId) {
        map[it.jobId] = it.courseName && String(it.courseName).trim()
          ? it.courseName
          : it.jobId;
      }
    });
    state.jobTitles = map;
  } catch (_) {
    state.jobTitles = state.jobTitles || {};
  }
}

function setNotice(message, isError) {
  const notice = byId("pageNotice");
  notice.textContent = message || "";
  notice.style.color = isError ? "#dc2626" : "#475569";
}

function renderApplicantCard(item) {
  const id = escapeHtml(item.applicationId);
  const jobLabel = escapeHtml(safeText(state.jobTitles[item.jobId] || item.jobId || "—"));
  return `
    <article class="mo-app-card" data-application-id="${id}">
      <div class="mo-app-card-head">
        <div>
          <h4>${escapeHtml(safeText(item.studentName))}</h4>
          <p class="mo-app-meta">${jobLabel} • Applied: ${escapeHtml(safeText(item.appliedAt))}</p>
        </div>
        <div class="mo-app-status-slot">${statusTag(item.status)}</div>
      </div>
      <div class="mo-app-grid">
        <div>
          <span class="mo-app-lbl">Student No</span>
          <div>${escapeHtml(safeText(item.studentNo))}</div>
        </div>
        <div>
          <span class="mo-app-lbl">Student ID (user)</span>
          <div>${escapeHtml(safeText(item.studentId))}</div>
        </div>
      </div>
      <div>
        <span class="mo-app-lbl">Course grade</span>
        <div>${escapeHtml(safeText(item.courseGrade))}</div>
      </div>
      <div class="mo-app-expand">
        <p class="mo-app-meta" style="margin-bottom:10px">Server record (GET detail updates <strong>pending</strong> → <strong>viewed</strong>).</p>
        <div class="mo-app-expand-grid">
          <div><span class="mo-app-lbl">Application ID</span><div data-field="applicationId"></div></div>
          <div><span class="mo-app-lbl">Job ID</span><div data-field="jobId"></div></div>
          <div><span class="mo-app-lbl">Student name</span><div data-field="studentName"></div></div>
          <div><span class="mo-app-lbl">Student No</span><div data-field="studentNo"></div></div>
          <div><span class="mo-app-lbl">Course grade</span><div data-field="courseGrade"></div></div>
          <div><span class="mo-app-lbl">Applied at</span><div data-field="appliedAt"></div></div>
          <div><span class="mo-app-lbl">Status</span><div data-field="status"></div></div>
          <div><span class="mo-app-lbl">Updated at</span><div data-field="updatedAt"></div></div>
          <div style="grid-column:1/-1;"><span class="mo-app-lbl">Attachments</span><div data-field="attachments"></div></div>
        </div>
      </div>
      <div class="mo-app-actions">
        <button type="button" class="btn btn-primary mo-app-detail-btn">View details</button>
      </div>
    </article>
  `;
}

function renderApplicantFeed(items) {
  const feed = byId("applicationsFeed");
  const emptyEl = byId("applicationsEmpty");
  const hasItems = items && items.length > 0;

  if (!hasItems) {
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
    parts.push(`<section class="mo-job-group">`);
    parts.push(
      `<h3 class="mo-job-group-title">${escapeHtml(label)} ` +
      `<span style="font-weight:500;color:#64748b">(${groupItems.length} applicant${groupItems.length === 1 ? "" : "s"})</span></h3>`
    );
    for (const item of groupItems) {
      parts.push(renderApplicantCard(item));
    }
    parts.push(`</section>`);
  }

  feed.innerHTML = parts.join("");
}

function fillDetailFields(expandEl, detail) {
  expandEl.querySelectorAll("[data-field]").forEach(el => {
    const k = el.getAttribute("data-field");
    if (k === "attachments") {
      const list = Array.isArray(detail.attachments) ? detail.attachments : [];
      if (list.length === 0) {
        el.innerHTML = '<span style="color:#64748b;">No attachments submitted.</span>';
        return;
      }
      const contextPath = getContextPath();
      el.innerHTML = list.map(att => {
        const href = `${window.location.origin}${contextPath}${att.downloadUrl}`;
        const sizeText = Number(att.fileSize || 0) > 0 ? ` (${Math.round((att.fileSize / 1024) * 10) / 10} KB)` : "";
        return `<div style="margin:6px 0;display:flex;justify-content:space-between;gap:12px;align-items:center;">`
          + `<span>${escapeHtml(safeText(att.label || "Attachment"))}: ${escapeHtml(safeText(att.fileName || "file"))}${escapeHtml(sizeText)}</span>`
          + `<a class="btn btn-outline" style="padding:4px 10px;font-size:12px;" href="${encodeURI(href)}" target="_blank" rel="noopener">Download</a>`
          + `</div>`;
      }).join("");
      return;
    }
    const val = detail[k];
    if (k === "status") {
      el.innerHTML = statusTag(val);
    } else {
      el.textContent = safeText(val);
    }
  });
}

async function openCardDetail(card, btn) {
  const rawId = card.getAttribute("data-application-id");
  const expand = card.querySelector(".mo-app-expand");
  if (!rawId || !expand || !btn) return;

  btn.disabled = true;
  try {
    const moId = getResolvedMoId();
    const url = moId
      ? `${apiBase()}/applications/detail/${encodeURIComponent(rawId)}?moId=${encodeURIComponent(moId)}`
      : `${apiBase()}/applications/detail/${encodeURIComponent(rawId)}`;
    const detail = await getJson(url);
    fillDetailFields(expand, detail);
    expand.classList.add("mo-open");

    const slot = card.querySelector(".mo-app-status-slot");
    if (slot) slot.innerHTML = statusTag(detail.status);

    const it = state.items.find(i => i.applicationId === rawId);
    if (it) it.status = detail.status;
  } finally {
    btn.disabled = false;
  }
}

async function loadList() {
  const jobId = byId("jobIdInput").value.trim();
  const moId = getResolvedMoId();
  const params = new URLSearchParams();
  if (jobId) params.set("jobId", jobId);
  if (moId) params.set("moId", moId);
  const query = params.toString();
  const url = query
    ? `${apiBase()}/applications?${query}`
    : `${apiBase()}/applications`;

  const data = await getJson(url);
  state.items = data && Array.isArray(data.items) ? data.items : [];
  renderApplicantFeed(state.items);
}

async function queryWithFeedback() {
  try {
    setNotice("Loading applications...", false);
    await loadJobTitles();
    await loadList();
    setNotice(`Loaded ${state.items.length} active application(s).`, false);
  } catch (err) {
    const code = err && err.code ? err.code : "REQUEST_ERROR";
    const message = err && err.message ? err.message : "Request failed.";
    setNotice(`${code}: ${message}`, true);
    renderApplicantFeed([]);
  }
}

function startPolling() {
  if (state.pollingTimer) {
    clearInterval(state.pollingTimer);
  }
  state.pollingTimer = setInterval(async () => {
    try {
      await loadJobTitles();
      await loadList();
    } catch (_) {
      /* silent */
    }
  }, 12000);
}

document.addEventListener("DOMContentLoaded", async () => {
  const feed = byId("applicationsFeed");
  feed.addEventListener("click", async e => {
    const btn = e.target.closest(".mo-app-detail-btn");
    if (!btn) return;
    const card = btn.closest(".mo-app-card");
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

  byId("queryBtn").addEventListener("click", async () => {
    await queryWithFeedback();
  });

  byId("resetBtn").addEventListener("click", async () => {
    byId("jobIdInput").value = "";
    await queryWithFeedback();
  });

  await queryWithFeedback();
  startPolling();
});
