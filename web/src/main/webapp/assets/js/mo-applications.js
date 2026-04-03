function getContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (parts.length === 0) return "";
  return "/" + parts[0];
}

function apiBase() {
  return `${window.location.origin}${getContextPath()}/api/mo`;
}

/**
 * MO identity for API calls when dev fallback is enabled on the server.
 * Order: URL ?moId= → localStorage ta_mo_dev_id → default mo001 (local demo data).
 * With ENABLE_DEV_MO_ID_FALLBACK=false, session login is required and this only helps if you still pass moId.
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

function statusTag(status) {
  const normalized = String(status || "").toLowerCase();
  if (normalized === "pending") {
    return '<span class="status-pill status-pending">pending</span>';
  }
  if (normalized === "viewed") {
    return '<span class="status-pill status-viewed">viewed</span>';
  }
  return `<span class="status-pill">${safeText(status)}</span>`;
}

const state = {
  items: [],
  selectedId: null,
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

function setNotice(message, isError) {
  const notice = byId("pageNotice");
  notice.textContent = message || "";
  notice.style.color = isError ? "#dc2626" : "#475569";
}

function renderList(items) {
  const tbody = byId("applicationsBody");
  if (!items || items.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="notice">No active applications.</td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = items.map(item => {
    const activeClass = state.selectedId === item.applicationId ? "active" : "";
    return `
      <tr class="click-row ${activeClass}" data-id="${item.applicationId}">
        <td>${safeText(item.studentName)}</td>
        <td>${safeText(item.studentNo)}</td>
        <td>${safeText(item.courseGrade)}</td>
        <td>${safeText(item.appliedAt)}</td>
        <td>${statusTag(item.status)}</td>
      </tr>
    `;
  }).join("");

  tbody.querySelectorAll("tr[data-id]").forEach(row => {
    row.addEventListener("click", async () => {
      const applicationId = row.getAttribute("data-id");
      await loadDetail(applicationId);
    });
  });
}

function showDetail(detail) {
  byId("detailEmpty").style.display = "none";
  byId("detailPanel").style.display = "grid";
  byId("dApplicationId").textContent = safeText(detail.applicationId);
  byId("dJobId").textContent = safeText(detail.jobId);
  byId("dStudentName").textContent = safeText(detail.studentName);
  byId("dStudentNo").textContent = safeText(detail.studentNo);
  byId("dCourseGrade").textContent = safeText(detail.courseGrade);
  byId("dAppliedAt").textContent = safeText(detail.appliedAt);
  byId("dStatus").innerHTML = statusTag(detail.status);
  byId("dUpdatedAt").textContent = safeText(detail.updatedAt);
}

function hideDetail() {
  byId("detailEmpty").style.display = "block";
  byId("detailPanel").style.display = "none";
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

  if (state.selectedId && !state.items.some(i => i.applicationId === state.selectedId)) {
    state.selectedId = null;
    hideDetail();
  }

  renderList(state.items);
}

async function loadDetail(applicationId) {
  state.selectedId = applicationId;
  renderList(state.items);

  const moId = getResolvedMoId();
  const detailUrl = moId
    ? `${apiBase()}/applications/detail/${encodeURIComponent(applicationId)}?moId=${encodeURIComponent(moId)}`
    : `${apiBase()}/applications/detail/${encodeURIComponent(applicationId)}`;
  const detail = await getJson(detailUrl);
  showDetail(detail);

  const listItem = state.items.find(i => i.applicationId === applicationId);
  if (listItem) {
    listItem.status = detail.status;
    renderList(state.items);
  }
}

async function queryWithFeedback() {
  try {
    setNotice("Loading applications...", false);
    await loadList();
    setNotice(`Loaded ${state.items.length} active application(s).`, false);
  } catch (err) {
    const code = err && err.code ? err.code : "REQUEST_ERROR";
    const message = err && err.message ? err.message : "Request failed.";
    setNotice(`${code}: ${message}`, true);
    renderList([]);
    hideDetail();
  }
}

function startPolling() {
  if (state.pollingTimer) {
    clearInterval(state.pollingTimer);
  }
  state.pollingTimer = setInterval(async () => {
    try {
      await loadList();
    } catch (_) {
      // Keep silent on polling errors to avoid noisy UX.
    }
  }, 12000);
}

document.addEventListener("DOMContentLoaded", async () => {
  byId("queryBtn").addEventListener("click", async () => {
    state.selectedId = null;
    hideDetail();
    await queryWithFeedback();
  });

  byId("resetBtn").addEventListener("click", async () => {
    byId("jobIdInput").value = "";
    state.selectedId = null;
    hideDetail();
    await queryWithFeedback();
  });

  hideDetail();
  await queryWithFeedback();
  startPolling();
});
