document.addEventListener("DOMContentLoaded", async () => {
  const state = {
    activeTab: "jobs",
    loading: true,
    jobs: [],
    applications: [],
    student: null,
    search: "",
    statusFilter: "all",
    hoursFilter: "all",
    profile: {
      skills: "",
      experience: ""
    },
    attachments: [],
    notifications: [],
    unreadCount: 0
  };

  const tabButtons = Array.from(document.querySelectorAll(".student-tab"));
  const panels = {
    jobs: byId("panel-jobs"),
    applications: byId("panel-applications"),
    hired: byId("panel-hired"),
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
  const hiredListEl = byId("hiredList");
  const hiredLoadingEl = byId("hiredLoading");
  const hiredEmptyEl = byId("hiredEmpty");
  const hiredContentEl = byId("hiredContent");
  const hiredCountTextEl = byId("hiredCountText");
  const hiredTotalHoursEl = byId("hiredTotalHours");
  const hiredSummaryNoteEl = byId("hiredSummaryNote");
  const studentWelcomeEl = byId("studentWelcome");
  const noticeEl = byId("studentNotice");
  const studentNotificationBtn = byId("studentNotificationBtn");
  const studentNotificationDot = byId("studentNotificationDot");
  const studentNotificationPanel = byId("studentNotificationPanel");
  const studentTopNameEl = byId("studentTopName");
  const studentAvatarEl = byId("studentAvatar");
  const globalStudentSearchEl = byId("globalStudentSearch");

  const jobSearchInput = byId("jobSearchInput");
  const jobStatusFilter = byId("jobStatusFilter");
  const jobHoursFilter = byId("jobHoursFilter");

  const profileNameEl = byId("profileName");
  const profileEmailEl = byId("profileEmail");
  const profilePhoneEl = byId("profilePhone");
  const profileStudentIdEl = byId("profileStudentId");
  const profileProgrammeEl = byId("profileProgramme");
  const profileSkillsEl = byId("profileSkills");
  const profileExperienceEl = byId("profileExperience");
  const profileSkillChipsEl = byId("profileSkillChips");
  const profileAvatarLargeEl = byId("profileAvatarLarge");
  const profileCardNameEl = byId("profileCardName");
  const profileCardProgrammeEl = byId("profileCardProgramme");
  const profileApplicationsStatEl = byId("profileApplicationsStat");
  const profileOffersStatEl = byId("profileOffersStat");
  const saveProfileBtn = byId("saveProfileBtn");
  const changePasswordBtn = byId("studentChangePasswordBtn");
  const aiAdvisorQuestionEl = byId("aiAdvisorQuestion");
  const aiAdvisorBtn = byId("aiAdvisorBtn");
  const aiAdvisorAnswerEl = byId("aiAdvisorAnswer");
  const aiAdvisorNoteEl = byId("aiAdvisorNote");

  const jobDetailOverlayEl = byId("jobDetailOverlay");
  const closeJobDetailBtn = byId("closeJobDetailBtn");
  const detailCancelBtn = byId("detailCancelBtn");
  const detailApplyBtn = byId("detailApplyBtn");
  const jobDetailTitleEl = byId("jobDetailTitle");
  const detailModuleEl = byId("detailModule");
  const detailTeacherEl = byId("detailTeacher");
  const detailHoursEl = byId("detailHours");
  const detailPositionsEl = byId("detailPositions");
  const detailDeadlineEl = byId("detailDeadline");
  const detailStatusEl = byId("detailStatus");
  const detailRequirementsEl = byId("detailRequirements");
  const detailProfileSnapshotEl = byId("detailProfileSnapshot");
  const detailAttachmentsListEl = byId("detailAttachmentsList");
  const detailAttachmentHintEl = byId("detailAttachmentHint");

  const uploadAreaEl = byId("uploadArea");
  const fileInputEl = byId("fileInput");
  const attachmentLabelEl = byId("attachmentLabel");
  const attachmentCustomLabelEl = byId("attachmentCustomLabel");
  const attachmentsListEl = byId("attachmentsList");

  let selectedJobId = "";

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

  function normalizeSkillList(value) {
    return Array.isArray(value) ? value.filter((item) => String(item || "").trim()) : [];
  }

  function formatSkillList(value, emptyText) {
    const skills = normalizeSkillList(value);
    return skills.length ? skills.map(escapeHtml).join(", ") : emptyText;
  }

  function matchPercent(job) {
    const score = Number(job && job.matchScore);
    if (!Number.isFinite(score) || score <= 0) return 0;
    return Math.round(score * 100);
  }

  function matchTone(percent) {
    if (percent >= 80) return "strong";
    if (percent >= 50) return "moderate";
    return "weak";
  }

  function getContextPath() {
    const parts = window.location.pathname.split("/").filter(Boolean);
    if (!parts.length) return "";
    const first = parts[0];
    if (first === "pages" || first === "assets") {
      return "";
    }
    return `/${first}`;
  }

  function buildApiUrl(path) {
    const clean = path.startsWith("/") ? path : `/${path}`;
    return `${window.location.origin}${getContextPath()}/api/student${clean}`;
  }

  async function requestApi(path, options) {
    const headers = {
      "Content-Type": "application/json"
    };

    const res = await fetch(buildApiUrl(path), {
      method: options && options.method ? options.method : "GET",
      credentials: "same-origin",
      headers,
      body: options && options.body ? JSON.stringify(options.body) : undefined
    });

    const contentType = res.headers.get("content-type") || "";
    const text = await res.text();
    if (!contentType.includes("application/json")) {
      throw new Error(`Unexpected response format: ${text.substring(0, 200)}`);
    }

    const body = text ? JSON.parse(text) : null;
    if (!res.ok || !body || !body.success) {
      const message = body && body.message ? body.message : "Request failed.";
      throw new Error(message);
    }

    return body.data;
  }

  function hasApplied(jobId) {
    return state.applications.some((app) => app.jobId === jobId);
  }

  function weeklyHoursForApplication(app) {
    const appHours = Number(app.hours);
    if (Number.isFinite(appHours) && appHours > 0) {
      return appHours;
    }
    const job = state.jobs.find((item) => item.id === app.jobId);
    const jobHours = Number(job && job.hours);
    return Number.isFinite(jobHours) && jobHours > 0 ? jobHours : 0;
  }

  function todayIsoDate() {
    const now = new Date();
    const m = `${now.getMonth() + 1}`.padStart(2, "0");
    const d = `${now.getDate()}`.padStart(2, "0");
    return `${now.getFullYear()}-${m}-${d}`;
  }

  function initialsForName(name) {
    const parts = String(name || "Student").trim().split(/\s+/).filter(Boolean);
    if (!parts.length) return "S";
    if (parts.length === 1) return parts[0].slice(0, 1).toUpperCase();
    return `${parts[0].slice(0, 1)}${parts[parts.length - 1].slice(0, 1)}`.toUpperCase();
  }

  function syncStudentChrome() {
    const student = state.student || {};
    const name = student.name || "Student";
    const initials = initialsForName(name);
    if (studentTopNameEl) studentTopNameEl.textContent = name;
    if (studentAvatarEl) studentAvatarEl.textContent = initials;
    if (profileAvatarLargeEl) profileAvatarLargeEl.textContent = initials;
    if (profileCardNameEl) profileCardNameEl.textContent = name;
    if (profileCardProgrammeEl) profileCardProgrammeEl.textContent = student.programme || "Programme";

    const applications = Array.isArray(state.applications) ? state.applications : [];
    const offers = applications.filter((app) => normalizeStatus(app.status) === "hired").length;
    if (profileApplicationsStatEl) profileApplicationsStatEl.textContent = String(applications.length);
    if (profileOffersStatEl) profileOffersStatEl.textContent = String(offers);
  }

  function renderSkillChips() {
    if (!profileSkillChipsEl) return;
    const skills = String(state.profile.skills || "")
      .split(",")
      .map((skill) => skill.trim())
      .filter(Boolean)
      .slice(0, 8);
    if (!skills.length) {
      profileSkillChipsEl.innerHTML = '<span class="profile-skill-chip">Add skills</span>';
      return;
    }
    profileSkillChipsEl.innerHTML = skills
      .map((skill) => `<span class="profile-skill-chip">${escapeHtml(skill)} <span aria-hidden="true">x</span></span>`)
      .join("");
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

      const matchesHours =
        state.hoursFilter === "all" ||
        (state.hoursFilter === "<=10" && (job.hours || 0) <= 10) ||
        (state.hoursFilter === ">10" && (job.hours || 0) > 10);

      return matchesSearch && matchesStatus && matchesHours;
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
      const percent = matchPercent(job);
      const tone = matchTone(percent);
      const detailBtn = `<button class="btn btn-outline open-detail-btn" data-job-id="${escapeHtml(job.id)}">View Details</button>`;
      const applyBtn = applied
        ? '<button class="btn btn-outline" disabled>Already Applied</button>'
        : `<button class="btn btn-primary open-detail-btn" data-job-id="${escapeHtml(job.id)}">Apply via Details</button>`;

      return `
        <article class="job-card">
          <h3>${escapeHtml(job.title || "Untitled Position")}</h3>
          <p class="job-meta">
            ${escapeHtml(job.moduleCode || "N/A")} | ${escapeHtml(job.teacherName || "N/A")}
          </p>
          <p class="job-meta">
            ${escapeHtml(job.hours || "0")}h/week | Deadline: ${escapeHtml(job.deadline || "TBA")}
          </p>
          <p class="job-meta">Schedule: ${escapeHtml(job.schedule || "-")} | Location: ${escapeHtml(job.location || "-")}</p>
          <p class="job-meta">Status: ${escapeHtml(job.status || "unknown")}</p>
          <div class="job-match job-match-${tone}">
            <div class="job-match-rate">
              <span>Match Rate</span>
              <strong>${escapeHtml(percent)}%</strong>
            </div>
            <p><span>Matched Skills:</span> ${formatSkillList(job.matchedSkills, "No matching skills detected.")}</p>
            <p><span>Missing Skills:</span> ${formatSkillList(job.missingSkills, "No major missing skills.")}</p>
          </div>
          <div class="job-actions">${detailBtn}${applyBtn}</div>
        </article>
      `;
    }).join("");
  }

  function renderApplications() {
    syncStudentChrome();
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
      const canWithdraw = normalizeStatus(app.status) !== "hired";
      const withdrawBtn = canWithdraw
        ? `<button class="withdraw-app-btn" data-app-id="${escapeHtml(app.id)}">Withdraw</button>`
        : "";
      return `
      <article class="app-item status-${statusClass}">
        ${withdrawBtn}
        <h3>${escapeHtml(app.jobTitle || "Unknown Job")}</h3>
        <p class="app-meta">Applied on ${escapeHtml(app.appliedAt || "Unknown Date")}</p>
        <div>${toStatusTag(app.status)}</div>
        <div class="app-feedback">${escapeHtml(app.feedback || "No feedback yet.")}</div>
      </article>
    `;
    }).join("");

    appsListEl.querySelectorAll(".withdraw-app-btn").forEach((btn) => {
      btn.addEventListener("click", async (e) => {
        e.preventDefault();
        const appId = btn.dataset.appId;
        await withdrawApplication(appId);
      });
    });
  }

  function renderHiredJobs() {
    if (!hiredLoadingEl || !hiredContentEl || !hiredListEl || !hiredEmptyEl) return;

    if (state.loading) {
      hiredLoadingEl.classList.remove("hidden");
      hiredContentEl.classList.add("hidden");
      if (hiredCountTextEl) hiredCountTextEl.textContent = "Loading your confirmed workload...";
      return;
    }

    hiredLoadingEl.classList.add("hidden");
    hiredContentEl.classList.remove("hidden");

    const hiredApps = state.applications.filter((app) => normalizeStatus(app.status) === "hired");
    const totalHours = hiredApps.reduce((sum, app) => sum + weeklyHoursForApplication(app), 0);

    if (hiredTotalHoursEl) hiredTotalHoursEl.textContent = `${totalHours}h`;
    if (hiredCountTextEl) {
      hiredCountTextEl.textContent = `${hiredApps.length} confirmed job(s), ${totalHours}h/week in total.`;
    }
    if (hiredSummaryNoteEl) {
      hiredSummaryNoteEl.textContent = hiredApps.length
        ? "This total is calculated from all applications currently marked as Hired."
        : "Confirmed TA jobs will appear here after a teacher finalizes hiring.";
    }

    if (!hiredApps.length) {
      hiredListEl.classList.add("hidden");
      hiredEmptyEl.classList.remove("hidden");
      return;
    }

    hiredEmptyEl.classList.add("hidden");
    hiredListEl.classList.remove("hidden");
    hiredListEl.innerHTML = hiredApps.map((app) => {
      const hours = weeklyHoursForApplication(app);
      return `
        <article class="hired-item">
          <h3>${escapeHtml(app.jobTitle || "Unknown Job")}</h3>
          <p class="hired-meta">
            ${escapeHtml(app.moduleCode || app.jobId || "N/A")} | ${escapeHtml(app.teacherName || "N/A")}
          </p>
          <p class="hired-meta">${hours}h/week | Applied on ${escapeHtml(app.appliedAt || "Unknown Date")}</p>
          <div>${toStatusTag(app.status)}</div>
        </article>
      `;
    }).join("");
  }

  function renderProfile() {
    const student = state.student || {};
    profileNameEl.value = student.name || "";
    profileEmailEl.value = student.email || "";
    if (profilePhoneEl) profilePhoneEl.value = student.phone || "";
    profileStudentIdEl.value = student.studentId || "";
    profileProgrammeEl.value = student.programme || "";
    profileSkillsEl.value = state.profile.skills;
    profileExperienceEl.value = state.profile.experience;
    syncStudentChrome();
    renderSkillChips();
    renderAttachmentsList();
  }

  function renderAttachmentsList() {
    if (!attachmentsListEl) return;

    const attachments = state.attachments || [];
    if (attachments.length === 0) {
      attachmentsListEl.innerHTML = '<p style="margin: 14px; text-align: center; color: #6b7280; font-size: 13px;">No documents uploaded yet</p>';
      return;
    }

    const html = attachments.map((att) => `
      <div class="student-attachment-row">
        <div>
          <p style="margin: 0; font-size: 13px; font-weight: 500;">${escapeHtml(att.fileName)}</p>
          <p style="margin: 4px 0 0 0; font-size: 12px; color: #6b7280;">
            ${escapeHtml(att.label || "Unlabeled")} - ${formatFileSize(att.fileSize)} - ${extractDate(att.uploadedAt)}
          </p>
        </div>
        <button class="delete-attachment-btn" data-attachment-id="${escapeHtml(att.id)}">Delete</button>
      </div>
    `).join("");

    attachmentsListEl.innerHTML = html;

    attachmentsListEl.querySelectorAll(".delete-attachment-btn").forEach((btn) => {
      btn.addEventListener("click", async (e) => {
        e.preventDefault();
        const attachmentId = btn.dataset.attachmentId;
        await deleteAttachment(attachmentId);
      });
    });
  }

  function formatFileSize(bytes) {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  }

  function getAttachmentLabel() {
    const preset = attachmentLabelEl ? attachmentLabelEl.value.trim() : "";
    if (preset === "Custom") {
      return attachmentCustomLabelEl ? attachmentCustomLabelEl.value.trim() : "";
    }
    return preset;
  }

  function syncAttachmentLabelUi() {
    if (!attachmentLabelEl || !attachmentCustomLabelEl) return;
    const isCustom = attachmentLabelEl.value === "Custom";
    attachmentCustomLabelEl.style.display = isCustom ? "block" : "none";
    if (!isCustom) {
      attachmentCustomLabelEl.value = "";
    }
  }

  function extractDate(isoDateTime) {
    if (!isoDateTime) return "";
    try {
      const date = new Date(isoDateTime);
      if (Number.isNaN(date.getTime())) {
        return String(isoDateTime);
      }
      return date.toISOString().slice(0, 10);
    } catch (_) {
      return String(isoDateTime);
    }
  }

  async function uploadFile(file) {
    const label = getAttachmentLabel();
    if (!label) {
      showNotice("Please select or enter a document type before uploading.", true);
      return;
    }

    const allowedExtensions = ["pdf", "docx", "xlsx", "jpg", "png"];
    const fileExtension = file.name.split(".").pop().toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      showNotice(`File type not allowed. Allowed types: ${allowedExtensions.join(", ")}`, true);
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      showNotice("File size exceeds 50MB limit.", true);
      return;
    }

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("label", label);

      const res = await fetch(buildApiUrl("/attachments"), {
        method: "POST",
        credentials: "same-origin",
        body: formData
      });

      const body = await res.json();
      if (!res.ok || !body.success) {
        throw new Error(body.message || "Upload failed.");
      }

      state.attachments.push(body.data);
      renderAttachmentsList();
      fileInputEl.value = "";
      if (attachmentLabelEl) attachmentLabelEl.value = "Resume";
      if (attachmentCustomLabelEl) attachmentCustomLabelEl.value = "";
      syncAttachmentLabelUi();
      showNotice("Document uploaded successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to upload file.", true);
    }
  }

  async function deleteAttachment(attachmentId) {
    if (!confirm("Delete this document?")) return;

    try {
      const res = await fetch(buildApiUrl(`/attachments/${attachmentId}`), {
        method: "DELETE",
        credentials: "same-origin"
      });

      const body = await res.json();
      if (!res.ok || !body.success) {
        throw new Error(body.message || "Delete failed.");
      }

      state.attachments = state.attachments.filter((a) => a.id !== attachmentId);
      renderAttachmentsList();
      showNotice("Document deleted successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to delete document.", true);
    }
  }

  async function fetchApplications() {
    try {
      const appData = await requestApi("/applications");
      state.applications = Array.isArray(appData.items) ? appData.items : [];
      renderApplications();
      renderHiredJobs();
    } catch (err) {
      showNotice(err.message || "Failed to refresh applications.", true);
    }
  }

  async function withdrawApplication(applicationId) {
    if (!confirm("Are you sure you want to withdraw this application?")) return;

    try {
      await requestApi(`/applications?applicationId=${encodeURIComponent(applicationId)}`, {
        method: "DELETE"
      });
      await fetchApplications();
      renderJobs();
      showNotice("Application withdrawn successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to withdraw application.", true);
    }
  }

  function showNotice(message, isError) {
    noticeEl.textContent = message;
    noticeEl.style.color = isError ? "#dc2626" : "#1e5eff";
  }

  function closeJobDetail() {
    selectedJobId = "";
    if (!jobDetailOverlayEl) return;
    jobDetailOverlayEl.classList.remove("open");
    jobDetailOverlayEl.setAttribute("aria-hidden", "true");
  }

  function openJobDetail(jobId) {
    const job = state.jobs.find((item) => item.id === jobId);
    if (!job || !jobDetailOverlayEl) return;

    selectedJobId = jobId;
    if (jobDetailTitleEl) {
      jobDetailTitleEl.textContent = job.title || "Job Detail";
    }
    if (detailModuleEl) detailModuleEl.textContent = job.moduleCode || "-";
    if (detailTeacherEl) detailTeacherEl.textContent = job.teacherName || "-";
    if (detailHoursEl) detailHoursEl.textContent = `${job.hours || 0}h/week`;
    if (detailPositionsEl) detailPositionsEl.textContent = String(job.positions || "-");
    if (detailDeadlineEl) detailDeadlineEl.textContent = job.deadline || "-";
    if (detailStatusEl) detailStatusEl.textContent = `${job.status || "-"} | Schedule: ${job.schedule || "-"} | Location: ${job.location || "-"}`;
    if (detailRequirementsEl) {
      detailRequirementsEl.textContent = job.requirements || "No detailed requirements provided yet.";
    }
    if (detailProfileSnapshotEl) {
      const snapshot = [
        `Name: ${profileNameEl.value || (state.student && state.student.name) || "-"}`,
        `Email: ${profileEmailEl.value || (state.student && state.student.email) || "-"}`,
        `Skills: ${profileSkillsEl.value || "(empty)"}`,
        `Experience: ${profileExperienceEl.value || "(empty)"}`
      ].join("\n");
      detailProfileSnapshotEl.textContent = snapshot;
    }
    renderDetailAttachmentSelection();

    jobDetailOverlayEl.classList.add("open");
    jobDetailOverlayEl.setAttribute("aria-hidden", "false");
  }

  function renderDetailAttachmentSelection() {
    if (!detailAttachmentsListEl) return;
    const attachments = Array.isArray(state.attachments) ? state.attachments : [];
    if (attachments.length === 0) {
      detailAttachmentsListEl.innerHTML = '<p style="margin:0;color:#dc2626;">No profile attachments found. Please upload at least one document in Profile first.</p>';
      if (detailApplyBtn) detailApplyBtn.disabled = true;
      if (detailAttachmentHintEl) detailAttachmentHintEl.textContent = "At least one attachment is required before applying.";
      return;
    }

    detailAttachmentsListEl.innerHTML = attachments.map((att) => `
      <label style="display:flex;align-items:center;gap:8px;margin:6px 0;">
        <input type="checkbox" class="detail-attachment-checkbox" data-attachment-id="${escapeHtml(att.id)}" checked />
        <span>${escapeHtml(att.fileName || "Unnamed file")} (${escapeHtml(att.label || "Unlabeled")}, ${formatFileSize(att.fileSize || 0)})</span>
      </label>
    `).join("");

    if (detailApplyBtn) detailApplyBtn.disabled = false;
    if (detailAttachmentHintEl) {
      detailAttachmentHintEl.textContent = "At least one attachment is required. All are selected by default.";
      detailAttachmentHintEl.style.color = "";
    }

    detailAttachmentsListEl.querySelectorAll(".detail-attachment-checkbox").forEach((checkbox) => {
      checkbox.addEventListener("change", () => {
        const selectedCount = getSelectedAttachmentIds().length;
        if (detailApplyBtn) detailApplyBtn.disabled = selectedCount === 0;
        if (detailAttachmentHintEl) {
          detailAttachmentHintEl.textContent = selectedCount === 0
            ? "Please select at least one attachment."
            : "At least one attachment is required. All are selected by default.";
          detailAttachmentHintEl.style.color = selectedCount === 0 ? "#dc2626" : "";
        }
      });
    });
  }

  function getSelectedAttachmentIds() {
    if (!detailAttachmentsListEl) return [];
    return Array.from(detailAttachmentsListEl.querySelectorAll(".detail-attachment-checkbox:checked"))
      .map((el) => el.getAttribute("data-attachment-id"))
      .filter((v) => !!v);
  }

  async function applyForJob(jobId, selectedAttachmentIds) {
    const job = state.jobs.find((item) => item.id === jobId);
    if (!job || hasApplied(jobId)) return;

    if (!selectedAttachmentIds || selectedAttachmentIds.length === 0) {
      throw new Error("Please select at least one attachment.");
    }

    await requestApi("/applications", {
      method: "POST",
      body: { jobId, selectedAttachmentIds }
    });
    await fetchApplications();
    renderJobs();
    renderApplications();
    renderHiredJobs();
    showNotice("Application submitted successfully.", false);
  }

  async function changePassword() {
    try {
      changePasswordBtn.disabled = true;
      changePasswordBtn.textContent = "Changing...";
      const response = await fetch(`${window.location.origin}${getContextPath()}/api/account/change-password`, {
        method: "POST",
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          oldPassword: byId("studentOldPassword").value.trim(),
          newPassword: byId("studentNewPassword").value.trim(),
          confirmPassword: byId("studentConfirmPassword").value.trim()
        })
      });
      const body = await response.json();
      if (!response.ok || !body.success) {
        throw new Error(body.message || "Request failed.");
      }
      byId("studentOldPassword").value = "";
      byId("studentNewPassword").value = "";
      byId("studentConfirmPassword").value = "";
      showNotice("Password changed successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to change password.", true);
    } finally {
      changePasswordBtn.disabled = false;
      changePasswordBtn.textContent = "Change Password";
    }
  }

  async function askAiAdvisor() {
    if (!aiAdvisorQuestionEl || !aiAdvisorBtn || !aiAdvisorAnswerEl || !aiAdvisorNoteEl) return;

    const question = aiAdvisorQuestionEl.value.trim();
    if (!question) {
      aiAdvisorAnswerEl.textContent = "Please enter a question for the AI Job Advisor.";
      aiAdvisorAnswerEl.classList.remove("hidden");
      aiAdvisorNoteEl.classList.add("hidden");
      return;
    }

    aiAdvisorBtn.disabled = true;
    aiAdvisorBtn.textContent = "Asking...";
    aiAdvisorAnswerEl.textContent = "Preparing advice from your current matching results...";
    aiAdvisorAnswerEl.classList.remove("hidden");
    aiAdvisorNoteEl.classList.add("hidden");

    try {
      const data = await requestApi("/ai-advisor", {
        method: "POST",
        body: { question }
      });
      aiAdvisorAnswerEl.textContent = data && data.answer ? data.answer : "No advice is available yet.";
      aiAdvisorNoteEl.classList.toggle("hidden", !(data && data.fallback));
    } catch (err) {
      aiAdvisorAnswerEl.textContent = err.message || "AI advisor is unavailable right now.";
      aiAdvisorNoteEl.classList.add("hidden");
    } finally {
      aiAdvisorBtn.disabled = false;
      aiAdvisorBtn.textContent = "Ask AI Advisor";
    }
  }

  tabButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      switchTab(btn.dataset.tab);
    });
  });

  document.querySelectorAll("[data-tab-jump]").forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      switchTab(link.getAttribute("data-tab-jump"));
    });
  });

  jobSearchInput.addEventListener("input", (event) => {
    state.search = event.target.value || "";
    if (globalStudentSearchEl && globalStudentSearchEl.value !== state.search) {
      globalStudentSearchEl.value = state.search;
    }
    renderJobs();
  });

  if (globalStudentSearchEl) {
    globalStudentSearchEl.addEventListener("input", (event) => {
      state.search = event.target.value || "";
      if (jobSearchInput && jobSearchInput.value !== state.search) {
        jobSearchInput.value = state.search;
      }
      renderJobs();
    });
    globalStudentSearchEl.addEventListener("focus", () => switchTab("jobs"));
  }

  jobStatusFilter.addEventListener("change", (event) => {
    state.statusFilter = event.target.value || "all";
    renderJobs();
  });

  jobHoursFilter.addEventListener("change", (event) => {
    state.hoursFilter = event.target.value || "all";
    renderJobs();
  });

  jobsListEl.addEventListener("click", (event) => {
    const target = event.target.closest(".open-detail-btn");
    if (!target) return;
    openJobDetail(target.dataset.jobId);
  });

  if (closeJobDetailBtn) {
    closeJobDetailBtn.addEventListener("click", closeJobDetail);
  }

  if (detailCancelBtn) {
    detailCancelBtn.addEventListener("click", closeJobDetail);
  }

  if (jobDetailOverlayEl) {
    jobDetailOverlayEl.addEventListener("click", (event) => {
      if (event.target === jobDetailOverlayEl) {
        closeJobDetail();
      }
    });
  }

  if (detailApplyBtn) {
    detailApplyBtn.addEventListener("click", async () => {
      if (!selectedJobId) return;
      try {
        const selectedAttachmentIds = getSelectedAttachmentIds();
        await applyForJob(selectedJobId, selectedAttachmentIds);
        closeJobDetail();
      } catch (err) {
        showNotice(err.message || "Failed to submit application.", true);
      }
    });
  }

  saveProfileBtn.addEventListener("click", async () => {
    const name = profileNameEl.value.trim();
    const phone = profilePhoneEl ? profilePhoneEl.value.trim() : "";
    if (!name) {
      showNotice("Full name cannot be empty.", true);
      switchTab("profile");
      return;
    }
    if (phone && !/^\d{11}$/.test(phone)) {
      showNotice("Phone must be 11 digits.", true);
      switchTab("profile");
      return;
    }

    try {
      const updated = await requestApi("/profile", {
        method: "PUT",
        body: {
          name,
          phone,
          skills: profileSkillsEl.value.trim(),
          experience: profileExperienceEl.value.trim()
        }
      });
      state.student = {
        id: updated.userId,
        name: updated.name,
        email: updated.email,
        phone: updated.phone,
        studentId: updated.studentId,
        programme: updated.programme
      };
      state.profile.skills = updated.skills || "";
      state.profile.experience = updated.experience || "";
      studentWelcomeEl.textContent = `Welcome, ${name}.`;
      syncStudentChrome();
      renderProfile();
      showNotice("Profile saved successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to save profile.", true);
    }
  });

  changePasswordBtn.addEventListener("click", changePassword);

  if (studentNotificationBtn && studentNotificationPanel) {
    studentNotificationBtn.addEventListener("click", () => {
      const visible = studentNotificationPanel.style.display === "block";
      studentNotificationPanel.style.display = visible ? "none" : "block";
      void loadStudentNotifications();
    });
    studentNotificationPanel.addEventListener("click", async (event) => {
      const markBtn = event.target.closest("[data-student-mark-read]");
      if (!markBtn) return;
      try {
        await markStudentNotificationRead(markBtn.getAttribute("data-student-mark-read"));
      } catch (err) {
        showNotice(err.message || "Failed to mark notification as read.", true);
      }
    });
  }

  if (aiAdvisorBtn) {
    aiAdvisorBtn.addEventListener("click", askAiAdvisor);
  }

  if (uploadAreaEl && fileInputEl) {
    syncAttachmentLabelUi();

    if (attachmentLabelEl) {
      attachmentLabelEl.addEventListener("change", syncAttachmentLabelUi);
    }

    uploadAreaEl.addEventListener("click", () => {
      fileInputEl.click();
    });

    uploadAreaEl.addEventListener("dragover", (e) => {
      e.preventDefault();
      uploadAreaEl.style.borderColor = "#1e5eff";
      uploadAreaEl.style.backgroundColor = "#eff6ff";
    });

    uploadAreaEl.addEventListener("dragleave", () => {
      uploadAreaEl.style.borderColor = "#9ca3af";
      uploadAreaEl.style.backgroundColor = "#f9fafb";
    });

    uploadAreaEl.addEventListener("drop", (e) => {
      e.preventDefault();
      uploadAreaEl.style.borderColor = "#9ca3af";
      uploadAreaEl.style.backgroundColor = "#f9fafb";
      const files = e.dataTransfer.files;
      if (files.length > 0) {
        (async () => {
          for (const file of files) {
            await uploadFile(file);
          }
        })();
      }
    });

    fileInputEl.addEventListener("change", (e) => {
      const files = e.target.files;
      if (files.length > 0) {
        (async () => {
          for (const file of files) {
            await uploadFile(file);
          }
        })();
      }
    });
  }

  function safeText(value) {
    return String(value ?? "").trim();
  }

  function renderStudentNotificationItem(n) {
    if (n.type === "announcement") {
      const title = safeText(n.title || "System announcement");
      const body = safeText(n.message || "");
      return `
        <div class="mo-notification-item mo-notification-item--announcement">
          <div style="min-width:0">
            <span class="mo-notification-badge">System announcement</span>
            <p class="mo-notification-announcement-title">${escapeHtml(title)}</p>
            <p class="mo-notification-announcement-body">${escapeHtml(body)}</p>
            <div style="font-size:12px;color:#64748b;margin-top:6px;">${escapeHtml(safeText(n.applicationTime))}</div>
          </div>
          <div class="row">
            ${n.read ? '<span class="notice" style="margin:0">Read</span>' : `<button class="btn btn-outline" type="button" data-student-mark-read="${escapeHtml(n.notificationId)}">Mark as Read</button>`}
          </div>
        </div>`;
    }
    const message = safeText(n.message) || `Update for ${safeText(n.jobName || n.jobId || "your application")}`;
    return `
      <div class="mo-notification-item">
        <div style="min-width:0">
          <div>${escapeHtml(message)}</div>
          <div style="font-size:12px;color:#64748b">${escapeHtml(safeText(n.applicationTime))}</div>
        </div>
        <div class="row">
          ${n.read ? '<span class="notice" style="margin:0">Read</span>' : `<button class="btn btn-outline" type="button" data-student-mark-read="${escapeHtml(n.notificationId)}">Mark as Read</button>`}
        </div>
      </div>`;
  }

  function renderStudentNotifications() {
    if (!studentNotificationPanel) return;
    if (studentNotificationDot) {
      if (state.unreadCount > 0) {
        studentNotificationDot.style.display = "inline-flex";
        studentNotificationDot.textContent = String(state.unreadCount);
      } else {
        studentNotificationDot.style.display = "none";
      }
    }
    if (!state.notifications.length) {
      studentNotificationPanel.innerHTML = '<p class="notice" style="margin:0">No notifications.</p>';
      return;
    }
    studentNotificationPanel.innerHTML = state.notifications.map(renderStudentNotificationItem).join("");
  }

  async function loadStudentNotifications() {
    try {
      const data = await requestApi("/notifications");
      state.notifications = data && Array.isArray(data.items) ? data.items : [];
      state.unreadCount = data && Number.isFinite(Number(data.unreadCount)) ? Number(data.unreadCount) : 0;
    } catch (_) {
      state.notifications = [];
      state.unreadCount = 0;
    }
    renderStudentNotifications();
  }

  async function markStudentNotificationRead(notificationId) {
    if (!notificationId) return;
    await requestApi(`/notifications/read/${encodeURIComponent(notificationId)}`, { method: "POST" });
    await loadStudentNotifications();
  }

  function startStudentNotificationPolling() {
    window.setInterval(loadStudentNotifications, 10000);
  }

  async function loadFromBackend() {
    const [jobData, appData, profileData] = await Promise.all([
      requestApi("/jobs"),
      requestApi("/applications"),
      requestApi("/profile")
    ]);

    state.jobs = Array.isArray(jobData.items) ? jobData.items : [];
    state.applications = Array.isArray(appData.items) ? appData.items : [];
    state.student = {
      id: profileData.userId,
      name: profileData.name,
      email: profileData.email,
      phone: profileData.phone,
      studentId: profileData.studentId,
      programme: profileData.programme
    };
    state.profile.skills = profileData.skills || "";
    state.profile.experience = profileData.experience || "";
    state.attachments = Array.isArray(profileData.attachments) ? profileData.attachments : [];

    studentWelcomeEl.textContent = state.student.name
      ? `Welcome, ${state.student.name}.`
      : "Welcome, student.";
    syncStudentChrome();
  }

  renderJobs();
  renderApplications();
  renderHiredJobs();

  try {
    await loadFromBackend();
    await loadStudentNotifications();
    startStudentNotificationPolling();
    showNotice("Connected to backend API.", false);
  } catch (backendErr) {
    showNotice(backendErr.message || "Failed to load data.", true);
  } finally {
    state.loading = false;
    renderJobs();
    renderApplications();
    renderHiredJobs();
    renderProfile();
  }
});

