document.addEventListener("DOMContentLoaded", async () => {
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
  const workloadBody = byId("adminWorkloadBody");
  const usersGrouped = byId("adminUsersGrouped");
  const jobsCards = byId("adminJobsCards");
  const workloadCards = byId("adminWorkloadCards");
  const tabs = Array.from(document.querySelectorAll("[data-admin-tab]"));
  const panels = Array.from(document.querySelectorAll("[data-admin-panel]"));
  let latestData = null;

  function statusTag(hours) {
    if (hours >= 20) return { label: "Overload", cls: "danger" };
    if (hours >= 15) return { label: "Warning", cls: "warn" };
    if (hours >= 10) return { label: "Normal", cls: "ok" };
    return { label: "Low", cls: "low" };
  }

  function activateTab(tabName) {
    tabs.forEach(tab => {
      const active = tab.getAttribute("data-admin-tab") === tabName;
      tab.classList.toggle("active", active);
      tab.setAttribute("aria-selected", active ? "true" : "false");
    });
    panels.forEach(panel => {
      panel.classList.toggle("admin-hidden", panel.getAttribute("data-admin-panel") !== tabName);
    });
    const title = byId("adminSubTitle");
    if (!title) return;
    if (tabName === "workload") title.textContent = "TA Workload Statistics";
    if (tabName === "users") title.textContent = "User Management";
    if (tabName === "jobs") title.textContent = "Job Management";
    if (tabName === "overview") title.textContent = "Welcome, Admin User";
  }

  function renderUsers(users) {
    const roleGroups = {
      student: { title: "Students", items: [] },
      teacher: { title: "Teachers", items: [] },
      admin: { title: "Administrators", items: [] }
    };
    users.forEach(user => {
      const key = user.role in roleGroups ? user.role : "student";
      roleGroups[key].items.push(user);
    });

    usersGrouped.innerHTML = Object.values(roleGroups).map(group => `
      <div class="card">
        <h3 class="admin-subtitle">${group.title} (${group.items.length})</h3>
        <div class="admin-list">
          ${group.items.map(user => `
            <div class="admin-list-item">
              <div>
                <p class="admin-list-name">${user.name}</p>
                <p class="admin-list-meta">${user.email}</p>
              </div>
              <button class="btn btn-outline" onclick="alert('Demo: Password reset')">Reset Password</button>
            </div>
          `).join("") || `<p class="admin-empty-text">No users found.</p>`}
        </div>
      </div>
    `).join("");

    usersBody.innerHTML = users.map(user => `
      <tr>
        <td>${user.name}</td>
        <td>${user.email}</td>
        <td>${formatRole(user.role)}</td>
        <td><button class="btn btn-outline" onclick="alert('Demo: Password reset')">Reset Password</button></td>
      </tr>
    `).join("");
  }

  function renderJobs(jobs) {
    jobsCards.innerHTML = jobs.map(job => {
      const isOpen = !job.recruitmentClosed;
      const recruitment = isOpen ? "Open" : "Closed";
      return `
        <article class="card admin-job-card ${isOpen ? "" : "is-closed"}">
          <div class="admin-job-top">
            <div>
              <h3 class="admin-subtitle">${job.moduleCode} - ${job.title}</h3>
              <p class="admin-list-meta">Module Organiser: ${job.teacherName}</p>
            </div>
            <span class="tag ${isOpen ? "ok" : ""}">${recruitment}</span>
          </div>
          <div class="admin-job-grid">
            <div><span class="admin-key">Status</span><strong>${job.status}</strong></div>
            <div><span class="admin-key">Positions</span><strong>${job.positions}</strong></div>
            <div><span class="admin-key">Recruitment</span><strong>${recruitment}</strong></div>
            <div><span class="admin-key">Action</span>${job.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${job.id}">Reopen</button>` : "<span>-</span>"}</div>
          </div>
        </article>
      `;
    }).join("") || `<div class="card"><p class="admin-empty-text">No jobs found.</p></div>`;

    jobsBody.innerHTML = jobs.map(job => `
      <tr>
        <td>${job.moduleCode}</td>
        <td>${job.title}</td>
        <td>${job.teacherName}</td>
        <td>${job.status}</td>
        <td>${job.recruitmentClosed ? "Recruitment Closed" : "Open"}</td>
        <td>${job.positions}</td>
        <td>${job.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${job.id}">Reopen</button>` : "-"}</td>
      </tr>
    `).join("");
  }

  function renderWorkload(workload) {
    workloadCards.innerHTML = workload.map(item => {
      const tag = statusTag(Number(item.weeklyHours || 0));
      return `
        <article class="card admin-work-card ${tag.cls === "danger" ? "is-danger" : ""}">
          <div class="admin-work-top">
            <div>
              <h3 class="admin-subtitle">${item.studentName}</h3>
              <p class="admin-list-meta">Student ID: ${item.studentId}</p>
            </div>
            <div class="admin-work-hours">
              <strong>${item.weeklyHours}</strong>
              <span>hrs/week</span>
              <em class="tag ${tag.cls}">${tag.label}</em>
            </div>
          </div>
          <p class="admin-list-meta">Hired Jobs: ${item.hiredCount}</p>
        </article>
      `;
    }).join("") || `<div class="card"><p class="admin-empty-text">No hired records yet.</p></div>`;

    workloadBody.innerHTML = workload.map(item => `
      <tr>
        <td>${item.studentId}</td>
        <td>${item.studentName}</td>
        <td>${item.hiredCount}</td>
        <td>${item.weeklyHours}</td>
      </tr>
    `).join("") || `
      <tr><td colspan="4">No hired records yet.</td></tr>
    `;
  }

  function renderOverview(data) {
    const jobs = data.jobs || [];
    const openJobs = jobs.filter(job => !job.recruitmentClosed).length;
    const closedJobs = jobs.filter(job => job.recruitmentClosed).length;
    const totalHired = (data.workload || []).reduce((sum, item) => sum + Number(item.hiredCount || 0), 0);
    byId("overviewOpenJobs").textContent = openJobs;
    byId("overviewClosedJobs").textContent = closedJobs;
    byId("overviewHiredCount").textContent = totalHired;
  }

  async function loadAdminDashboard() {
    const res = await fetch(`${window.location.origin}${getContextPath()}/api/admin/dashboard`, {
      method: "GET",
      credentials: "same-origin"
    });
    const body = await res.json();
    if (!res.ok || !body.success) {
      throw new Error(body.message || "Failed to load admin dashboard.");
    }
    const data = body.data || {};
    latestData = data;

    byId("statJobs").textContent = data.totalJobs ?? 0;
    byId("statUsers").textContent = data.totalUsers ?? 0;
    byId("statApps").textContent = data.totalApplications ?? 0;
    renderOverview(data);
    renderUsers(data.users || []);
    renderJobs(data.jobs || []);
    renderWorkload(data.workload || []);
  }

  function handleReopenClick(event) {
    return event.target.closest("[data-reopen-job]");
  }

  async function onReopen(event) {
    const btn = handleReopenClick(event);
    if (!btn) return;
    const jobId = btn.getAttribute("data-reopen-job");
    try {
      btn.disabled = true;
      btn.textContent = "Reopening...";
      const res = await fetch(`${window.location.origin}${getContextPath()}/api/admin/jobs/reopen/${encodeURIComponent(jobId)}`, {
        method: "POST",
        credentials: "same-origin"
      });
      const body = await res.json();
      if (!res.ok || !body.success) throw new Error(body.message || "Failed to reopen.");
      byId("adminNotice").textContent = `Job ${jobId} reopened.`;
      await loadAdminDashboard();
      if (latestData) activateTab("jobs");
    } catch (err) {
      byId("adminNotice").textContent = err.message;
    } finally {
      btn.disabled = false;
      btn.textContent = "Reopen";
    }
  }

  jobsBody.addEventListener("click", onReopen);
  jobsCards.addEventListener("click", onReopen);
  tabs.forEach(tab => {
    tab.addEventListener("click", () => activateTab(tab.getAttribute("data-admin-tab")));
  });

  activateTab("overview");
  try {
    await loadAdminDashboard();
  } catch (err) {
    byId("adminNotice").textContent = err.message;
  }
});
