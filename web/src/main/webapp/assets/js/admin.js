document.addEventListener("DOMContentLoaded", async () => {
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
<<<<<<< HEAD
  try {
=======
  async function loadAdminDashboard() {
>>>>>>> dev-Huishun-Hu
    const res = await fetch(`${window.location.origin}${getContextPath()}/api/admin/dashboard`, {
      method: "GET",
      credentials: "same-origin"
    });
    const body = await res.json();
<<<<<<< HEAD
    if (!res.ok || !body.success) {
      throw new Error(body.message || "Failed to load admin dashboard.");
    }
    const data = body.data || {};

    byId("statJobs").textContent = data.totalJobs ?? 0;
    byId("statUsers").textContent = data.totalUsers ?? 0;
    byId("statApps").textContent = data.totalApplications ?? 0;

=======
    if (!res.ok || !body.success) throw new Error(body.message || "Failed to load admin dashboard.");
    const data = body.data || {};
    byId("statJobs").textContent = data.totalJobs ?? 0;
    byId("statUsers").textContent = data.totalUsers ?? 0;
    byId("statApps").textContent = data.totalApplications ?? 0;
>>>>>>> dev-Huishun-Hu
    usersBody.innerHTML = (data.users || []).map(u => `
      <tr>
        <td>${u.name}</td>
        <td>${u.email}</td>
        <td>${formatRole(u.role)}</td>
        <td><button class="btn btn-outline" onclick="alert('Demo: Password reset')">Reset Password</button></td>
      </tr>
    `).join("");
<<<<<<< HEAD

=======
>>>>>>> dev-Huishun-Hu
    jobsBody.innerHTML = (data.jobs || []).map(j => `
      <tr>
        <td>${j.moduleCode}</td>
        <td>${j.title}</td>
        <td>${j.teacherName}</td>
        <td>${j.status}</td>
        <td>${j.recruitmentClosed ? "Recruitment Closed" : "Open"}</td>
        <td>${j.positions}</td>
        <td>${j.recruitmentClosed ? `<button class="btn btn-outline" data-reopen-job="${j.id}">Reopen</button>` : "-"}</td>
      </tr>
    `).join("");
  }

  jobsBody.addEventListener("click", async event => {
    const btn = event.target.closest("[data-reopen-job]");
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
    } catch (err) {
      byId("adminNotice").textContent = err.message;
    } finally {
      btn.disabled = false;
      btn.textContent = "Reopen";
    }
  });

  try {
    await loadAdminDashboard();
  } catch (err) {
    byId("adminNotice").textContent = err.message;
  }
});
