document.addEventListener("DOMContentLoaded", async () => {
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
  try {
    const res = await fetch(`${window.location.origin}${getContextPath()}/api/admin/dashboard`, {
      method: "GET",
      credentials: "same-origin"
    });
    const body = await res.json();
    if (!res.ok || !body.success) {
      throw new Error(body.message || "Failed to load admin dashboard.");
    }
    const data = body.data || {};

    byId("statJobs").textContent = data.totalJobs ?? 0;
    byId("statUsers").textContent = data.totalUsers ?? 0;
    byId("statApps").textContent = data.totalApplications ?? 0;

    usersBody.innerHTML = (data.users || []).map(u => `
      <tr>
        <td>${u.name}</td>
        <td>${u.email}</td>
        <td>${formatRole(u.role)}</td>
        <td><button class="btn btn-outline" onclick="alert('Demo: Password reset')">Reset Password</button></td>
      </tr>
    `).join("");

    jobsBody.innerHTML = (data.jobs || []).map(j => `
      <tr>
        <td>${j.moduleCode}</td>
        <td>${j.title}</td>
        <td>${j.teacherName}</td>
        <td>${j.status}</td>
        <td>${j.positions}</td>
      </tr>
    `).join("");
  } catch (err) {
    byId("adminNotice").textContent = err.message;
  }
});
