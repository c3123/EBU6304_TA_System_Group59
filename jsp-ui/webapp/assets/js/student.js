document.addEventListener("DOMContentLoaded", async () => {
  const jobsBody = byId("jobsBody");
  const appsBody = byId("appsBody");
  try {
    const data = await loadMockData();
    const jobs = data.jobs.filter(j => j.status === "open");
    const apps = data.applications.filter(a => a.studentId === "1");

    jobsBody.innerHTML = jobs.map(j => `
      <tr>
        <td>${j.moduleCode}</td>
        <td>${j.title}</td>
        <td>${j.hours}h/week</td>
        <td>${j.deadline}</td>
        <td><button class="btn btn-primary" onclick="alert('Demo: Application submitted')">Apply</button></td>
      </tr>
    `).join("");

    appsBody.innerHTML = apps.map(a => `
      <tr>
        <td>${a.jobTitle}</td>
        <td>${a.appliedDate}</td>
        <td><span class="tag ${a.status==='hired'?'ok':a.status==='rejected'?'danger':'warn'}">${a.status}</span></td>
        <td>${a.feedback || '-'}</td>
      </tr>
    `).join("");
  } catch (err) {
    byId("studentNotice").textContent = err.message;
  }
});
