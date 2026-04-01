document.addEventListener("DOMContentLoaded", async () => {
  const usersBody = byId("adminUsersBody");
  const jobsBody = byId("adminJobsBody");
  try {
    const data = await loadMockData();

    byId("statJobs").textContent = data.jobs.length;
    byId("statUsers").textContent = data.users.length;
    byId("statApps").textContent = data.applications.length;

    usersBody.innerHTML = data.users.map(u => `
      <tr>
        <td>${u.name}</td>
        <td>${u.email}</td>
        <td>${formatRole(u.role)}</td>
        <td><button class="btn btn-outline" onclick="alert('Demo: Password reset')">Reset Password</button></td>
      </tr>
    `).join("");

    jobsBody.innerHTML = data.jobs.map(j => `
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
