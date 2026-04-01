document.addEventListener("DOMContentLoaded", async () => {
  const jobsBody = byId("teacherJobsBody");
  const applicantsBody = byId("teacherApplicantsBody");
  try {
    const data = await loadMockData();
    const myJobs = data.jobs.filter(j => j.teacherId === "2");
    const myApps = data.applications.filter(a => myJobs.some(j => j.id === a.jobId));

    jobsBody.innerHTML = myJobs.map(j => `
      <tr>
        <td>${j.moduleCode}</td>
        <td>${j.title}</td>
        <td>${j.positions}</td>
        <td>${j.status}</td>
        <td><button class="btn btn-outline" onclick="alert('Demo: Edit job post')">Edit</button></td>
      </tr>
    `).join("");

    applicantsBody.innerHTML = myApps.map(a => `
      <tr>
        <td>${a.studentName}</td>
        <td>${a.jobTitle}</td>
        <td>${a.appliedDate}</td>
        <td><span class="tag ${a.status==='hired'?'ok':a.status==='rejected'?'danger':'warn'}">${a.status}</span></td>
        <td>
          <button class="btn btn-success" onclick="alert('Demo: Applicant hired')">Hire</button>
          <button class="btn btn-danger" onclick="alert('Demo: Applicant rejected')">Reject</button>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    byId("teacherNotice").textContent = err.message;
  }
});
