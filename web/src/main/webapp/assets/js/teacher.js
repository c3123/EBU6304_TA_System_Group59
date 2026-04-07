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
  pollingTimer: null
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
  const canPublish = String(item.approvalStatus || "").toLowerCase() === "approved" && item.published !== true && item.withdrawn !== true;
  const publishLocked = item.published === true ? "Published" : "Publish job";
  const withdrawDisabled = item.withdrawn === true ? "disabled" : "";
  const publishDisabled = canPublish ? "" : "disabled";
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

      <p class="notice">Approval status: <strong>${teacherEscapeHtml(teacherSafeText(item.approvalStatus || "pending"))}</strong>. Published: <strong>${teacherEscapeHtml(String(item.published === true))}</strong>. Withdrawn: <strong>${teacherEscapeHtml(String(item.withdrawn === true))}</strong>.</p>

      <div class="mo-demand-actions">
        <button class="btn btn-primary" type="button" data-open-publish="${teacherEscapeHtml(item.jobId)}" ${publishDisabled}>${publishLocked}</button>
        <button class="btn btn-outline" type="button" data-withdraw-job="${teacherEscapeHtml(item.jobId)}" ${withdrawDisabled}>Withdraw job</button>
        <a class="btn btn-outline" href="mo-applications.jsp">Applicants</a>
      </div>

      ${detailBlock}
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

async function withdrawJob(jobId, button) {
  teacherSetButtonLoading(button, "Withdrawing...", "Withdraw job", true);
  try {
    await teacherRequest(`${teacherApiBase()}/jobs/withdraw/${encodeURIComponent(jobId)}`, {
      method: "POST"
    });
    teacherSetNotice("jobsNotice", `Job ${jobId} withdrawn successfully.`, false);
    await loadTeacherJobs();
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  } finally {
    teacherSetButtonLoading(button, "Withdrawing...", "Withdraw job", false);
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

    const withdrawBtn = event.target.closest("[data-withdraw-job]");
    if (withdrawBtn) {
      const jobId = withdrawBtn.getAttribute("data-withdraw-job");
      await withdrawJob(jobId, withdrawBtn);
    }
  });

  feed.addEventListener("submit", async event => {
    const form = event.target.closest("[data-publish-form]");
    if (!form) return;
    event.preventDefault();
    await submitPublishForm(form);
  });
}

async function reloadTeacherWorkflow() {
  try {
    teacherSetNotice("jobsNotice", "Loading demand list...", false);
    await loadTeacherJobs();
    teacherSetNotice("jobsNotice", `Loaded ${teacherState.items.length} job record(s).`, false);
  } catch (err) {
    teacherSetNotice("jobsNotice", `${err.code || "REQUEST_ERROR"}: ${err.message}`, true);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  byId("demandForm").addEventListener("submit", submitDemandForm);
  byId("reloadBtn").addEventListener("click", reloadTeacherWorkflow);
  bindTeacherFeedActions();
  await reloadTeacherWorkflow();
});
