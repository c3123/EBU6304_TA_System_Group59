document.addEventListener("DOMContentLoaded", async () => {
  const state = {
    activeTab: "jobs",
    loading: true,
    jobs: [],
    applications: [],
    student: null,
    search: "",
    statusFilter: "all",
    profile: {
      skills: "",
      experience: ""
    }
  };

  const tabButtons = Array.from(document.querySelectorAll(".student-tab"));
  const panels = {
    jobs: byId("panel-jobs"),
    applications: byId("panel-applications"),
    profile: byId("panel-profile")
  };

  const jobsListEl = byId("jobsList");
  const jobsLoadingEl = byId("jobsLoading");
  const jobsEmptyEl = byId("jobsEmpty");
  const jobsCountTextEl = byId("jobsCountText");
  const appsListEl = byId("appsList");
  const appsLoadingEl = byId("appsLoading");
  const appsEmptyEl = byId("appsEmpty");
  const appsCountTextEl = byId("appsCountText");
  const studentWelcomeEl = byId("studentWelcome");
  const noticeEl = byId("studentNotice");

  const jobSearchInput = byId("jobSearchInput");
  const jobStatusFilter = byId("jobStatusFilter");

  const profileNameEl = byId("profileName");
  const profileEmailEl = byId("profileEmail");
  const profileStudentIdEl = byId("profileStudentId");
  const profileProgrammeEl = byId("profileProgramme");
  const profileSkillsEl = byId("profileSkills");
  const profileExperienceEl = byId("profileExperience");
  const saveProfileBtn = byId("saveProfileBtn");

  function escapeHtml(value) {
    return String(value ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function normalizeStatus(status) {
    const raw = String(status || "pending").toLowerCase();
    if (raw === "hired") return "hired";
    if (raw === "rejected" || raw === "not selected") return "rejected";
    if (raw === "shortlisted") return "shortlisted";
    return "pending";
  }

  function toStatusTag(status) {
    const normalized = normalizeStatus(status);
    if (normalized === "hired") {
      return '<span class="tag ok">Hired</span>';
    }
    if (normalized === "rejected") {
      return '<span class="tag danger">Not Selected</span>';
    }
    if (normalized === "shortlisted") {
      return '<span class="tag">Shortlisted</span>';
    }
    return '<span class="tag warn">Under Review</span>';
  }

  function hasApplied(jobId) {
    return state.applications.some((app) => app.jobId === jobId);
  }

  function todayIsoDate() {
    const now = new Date();
    const m = `${now.getMonth() + 1}`.padStart(2, "0");
    const d = `${now.getDate()}`.padStart(2, "0");
    return `${now.getFullYear()}-${m}-${d}`;
  }

  function switchTab(tabKey) {
    state.activeTab = tabKey;
    tabButtons.forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.tab === tabKey);
    });

    Object.keys(panels).forEach((key) => {
      panels[key].classList.toggle("active", key === tabKey);
    });
  }

  function renderJobs() {
    if (state.loading) {
      jobsLoadingEl.classList.remove("hidden");
      jobsListEl.classList.add("hidden");
      jobsEmptyEl.classList.add("hidden");
      jobsCountTextEl.textContent = "Loading available positions...";
      return;
    }

    const query = state.search.trim().toLowerCase();
    const filtered = state.jobs.filter((job) => {
      const matchesSearch =
        !query ||
        String(job.moduleCode || "").toLowerCase().includes(query) ||
        String(job.title || "").toLowerCase().includes(query);

      const matchesStatus =
        state.statusFilter === "all" || String(job.status || "").toLowerCase() === state.statusFilter;

      return matchesSearch && matchesStatus;
    });

    jobsLoadingEl.classList.add("hidden");

    if (!filtered.length) {
      jobsListEl.classList.add("hidden");
      jobsEmptyEl.classList.remove("hidden");
      jobsCountTextEl.textContent = "0 result found.";
      return;
    }

    jobsEmptyEl.classList.add("hidden");
    jobsListEl.classList.remove("hidden");
    jobsCountTextEl.textContent = `${filtered.length} position(s) shown.`;

    jobsListEl.innerHTML = filtered.map((job) => {
      const applied = hasApplied(job.id);
      const actionBtn = applied
        ? '<button class="btn btn-outline" disabled>Already Applied</button>'
        : `<button class="btn btn-primary apply-btn" data-job-id="${escapeHtml(job.id)}">Apply</button>`;

      return `
        <article class="job-card">
          <h3>${escapeHtml(job.title || "Untitled Position")}</h3>
          <p class="job-meta">
            ${escapeHtml(job.moduleCode || "N/A")} | ${escapeHtml(job.teacherName || "N/A")}
          </p>
          <p class="job-meta">
            ${escapeHtml(job.hours || "0")}h/week | Deadline: ${escapeHtml(job.deadline || "TBA")}
          </p>
          <p class="job-meta">Status: ${escapeHtml(job.status || "unknown")}</p>
          <div class="job-actions">${actionBtn}</div>
        </article>
      `;
    }).join("");
  }

  function renderApplications() {
    if (state.loading) {
      appsLoadingEl.classList.remove("hidden");
      appsListEl.classList.add("hidden");
      appsEmptyEl.classList.add("hidden");
      appsCountTextEl.textContent = "Loading your records...";
      return;
    }

    appsLoadingEl.classList.add("hidden");
    const apps = state.applications;

    if (!apps.length) {
      appsListEl.classList.add("hidden");
      appsEmptyEl.classList.remove("hidden");
      appsCountTextEl.textContent = "No applications found.";
      return;
    }

    appsEmptyEl.classList.add("hidden");
    appsListEl.classList.remove("hidden");
    appsCountTextEl.textContent = `${apps.length} application(s) recorded.`;

    appsListEl.innerHTML = apps.map((app) => {
      const statusClass = normalizeStatus(app.status);
      return `
      <article class="app-item status-${statusClass}">
        <h3>${escapeHtml(app.jobTitle || "Unknown Job")}</h3>
        <p class="app-meta">Applied on ${escapeHtml(app.appliedDate || "Unknown Date")}</p>
        <div>${toStatusTag(app.status)}</div>
        <div class="app-feedback">${escapeHtml(app.feedback || "No feedback yet.")}</div>
      </article>
    `;
    }).join("");
  }

  function renderProfile() {
    const student = state.student || {};
    profileNameEl.value = student.name || "";
    profileEmailEl.value = student.email || "";
    profileStudentIdEl.value = student.studentId || "";
    profileProgrammeEl.value = student.programme || "";
    profileSkillsEl.value = state.profile.skills;
    profileExperienceEl.value = state.profile.experience;
  }

  function showNotice(message, isError) {
    noticeEl.textContent = message;
    noticeEl.style.color = isError ? "#dc2626" : "#1e5eff";
  }

  function applyForJob(jobId) {
    const job = state.jobs.find((item) => item.id === jobId);
    if (!job || hasApplied(jobId)) return;

    state.applications.unshift({
      id: `local-${Date.now()}`,
      jobId: job.id,
      jobTitle: job.title,
      studentId: (state.student && state.student.id) || "",
      studentName: (state.student && state.student.name) || "",
      appliedDate: todayIsoDate(),
      status: "pending",
      feedback: ""
    });

    renderJobs();
    renderApplications();
    showNotice("Application submitted locally (frontend demo mode).", false);
  }

  tabButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      switchTab(btn.dataset.tab);
    });
  });

  jobSearchInput.addEventListener("input", (event) => {
    state.search = event.target.value || "";
    renderJobs();
  });

  jobStatusFilter.addEventListener("change", (event) => {
    state.statusFilter = event.target.value || "all";
    renderJobs();
  });

  jobsListEl.addEventListener("click", (event) => {
    const target = event.target.closest(".apply-btn");
    if (!target) return;
    applyForJob(target.dataset.jobId);
  });

  saveProfileBtn.addEventListener("click", () => {
    const name = profileNameEl.value.trim();
    if (!name) {
      showNotice("Full name cannot be empty.", true);
      switchTab("profile");
      return;
    }

    if (state.student) {
      state.student.name = name;
    }
    state.profile.skills = profileSkillsEl.value.trim();
    state.profile.experience = profileExperienceEl.value.trim();
    studentWelcomeEl.textContent = `Welcome, ${name}.`;
    showNotice("Profile saved locally (frontend demo mode).", false);
  });

  renderJobs();
  renderApplications();

  try {
    const data = await loadMockData();
    const users = Array.isArray(data.users) ? data.users : [];
    const student = users.find((user) => user.role === "student") || null;

    state.jobs = Array.isArray(data.jobs) ? data.jobs : [];
    state.student = student;
    state.applications = Array.isArray(data.applications)
      ? data.applications.filter((app) => !student || app.studentId === student.id)
      : [];

    state.loading = false;
    studentWelcomeEl.textContent = student && student.name
      ? `Welcome, ${student.name}.`
      : "Welcome, student.";

    renderJobs();
    renderApplications();
    renderProfile();
  } catch (err) {
    state.loading = false;
    renderJobs();
    renderApplications();
    renderProfile();
    showNotice(err.message || "Failed to load data.", true);
  }
});
