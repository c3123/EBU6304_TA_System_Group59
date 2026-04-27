document.addEventListener("DOMContentLoaded", async () => {
  const state = {
    activeTab: "jobs",
    loading: true,
    jobs: [],
    applications: [],
    assignedJobs: [],
    student: null,
    search: "",
    statusFilter: "all",
    hoursFilter: "all",
    profile: {
      skills: "",
      experience: ""
    },
    attachments: []
  };

  const tabButtons = Array.from(document.querySelectorAll(".student-tab"));
  const panels = {
    jobs: byId("panel-jobs"),
    applications: byId("panel-applications"),
    assigned: byId("panel-assigned"),
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
  const assignedListEl = byId("assignedList");
  const assignedLoadingEl = byId("assignedLoading");
  const assignedEmptyEl = byId("assignedEmpty");
  const assignedCountTextEl = byId("assignedCountText");
  const studentWelcomeEl = byId("studentWelcome");
  const noticeEl = byId("studentNotice");

  const jobSearchInput = byId("jobSearchInput");
  const jobStatusFilter = byId("jobStatusFilter");
  const jobHoursFilter = byId("jobHoursFilter");

  const profileNameEl = byId("profileName");
  const profileEmailEl = byId("profileEmail");
  const profileStudentIdEl = byId("profileStudentId");
  const profileProgrammeEl = byId("profileProgramme");
  const profileSkillsEl = byId("profileSkills");
  const profileExperienceEl = byId("profileExperience");
  const saveProfileBtn = byId("saveProfileBtn");
  const changePasswordBtn = byId("studentChangePasswordBtn");

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
          <div class="job-actions">${detailBtn}${applyBtn}</div>
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

  function renderAssignedJobs() {
    if (state.loading) {
      assignedLoadingEl.classList.remove("hidden");
      assignedListEl.classList.add("hidden");
      assignedEmptyEl.classList.add("hidden");
      assignedCountTextEl.textContent = "Loading your confirmed TA jobs...";
      return;
    }

    assignedLoadingEl.classList.add("hidden");
    const items = state.assignedJobs;
    if (!items.length) {
      assignedListEl.classList.add("hidden");
      assignedEmptyEl.classList.remove("hidden");
      assignedCountTextEl.textContent = "No hired TA jobs found.";
      return;
    }

    assignedEmptyEl.classList.add("hidden");
    assignedListEl.classList.remove("hidden");
    assignedCountTextEl.textContent = `${items.length} hired job(s) confirmed.`;
    assignedListEl.innerHTML = items.map((item) => `
      <article class="app-item status-hired">
        <h3>${escapeHtml(item.title || "Untitled Job")}</h3>
        <p class="app-meta">${escapeHtml(item.moduleCode || "-")} | ${escapeHtml(item.teacherName || "-")}</p>
        <div class="app-feedback">
          Weekly Hours: ${escapeHtml(String(item.weeklyHours || 0))}<br />
          Schedule: ${escapeHtml(item.schedule || "-")}<br />
          Location: ${escapeHtml(item.location || "-")}<br />
          Deadline: ${escapeHtml(item.deadline || "-")}<br />
          Recruitment Closed: ${item.recruitmentClosed ? "Yes" : "No"}
        </div>
      </article>
    `).join("");
  }

  function renderProfile() {
    const student = state.student || {};
    profileNameEl.value = student.name || "";
    profileEmailEl.value = student.email || "";
    profileStudentIdEl.value = student.studentId || "";
    profileProgrammeEl.value = student.programme || "";
    profileSkillsEl.value = state.profile.skills;
    profileExperienceEl.value = state.profile.experience;
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
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-bottom: 1px solid #f3f4f6;">
        <div>
          <p style="margin: 0; font-size: 13px; font-weight: 500;">${escapeHtml(att.fileName)}</p>
          <p style="margin: 4px 0 0 0; font-size: 12px; color: #6b7280;">
            ${escapeHtml(att.label || "Unlabeled")} • ${formatFileSize(att.fileSize)} • ${extractDate(att.uploadedAt)}
          </p>
        </div>
        <button class="delete-attachment-btn" data-attachment-id="${escapeHtml(att.id)}" style="padding: 6px 10px; background-color: #fee2e2; color: #991b1b; border: none; border-radius: 4px; font-size: 12px; cursor: pointer;">Delete</button>
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

  async function fetchApplicationsAndAssigned() {
    try {
      const [appData, assignedData] = await Promise.all([
        requestApi("/applications"),
        requestApi("/my-jobs")
      ]);
      state.applications = Array.isArray(appData.items) ? appData.items : [];
      state.assignedJobs = Array.isArray(assignedData.items) ? assignedData.items : [];
      renderApplications();
      renderAssignedJobs();
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
      await fetchApplicationsAndAssigned();
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
    await fetchApplicationsAndAssigned();
    renderJobs();
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
    if (!name) {
      showNotice("Full name cannot be empty.", true);
      switchTab("profile");
      return;
    }

    try {
      const updated = await requestApi("/profile", {
        method: "PUT",
        body: {
          name,
          skills: profileSkillsEl.value.trim(),
          experience: profileExperienceEl.value.trim()
        }
      });
      state.student = {
        id: updated.userId,
        name: updated.name,
        email: updated.email,
        studentId: updated.studentId,
        programme: updated.programme
      };
      state.profile.skills = updated.skills || "";
      state.profile.experience = updated.experience || "";
      studentWelcomeEl.textContent = `Welcome, ${name}.`;
      renderProfile();
      showNotice("Profile saved successfully.", false);
    } catch (err) {
      showNotice(err.message || "Failed to save profile.", true);
    }
  });

  changePasswordBtn.addEventListener("click", changePassword);

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

  async function loadFromBackend() {
    const [jobData, appData, assignedData, profileData] = await Promise.all([
      requestApi("/jobs"),
      requestApi("/applications"),
      requestApi("/my-jobs"),
      requestApi("/profile")
    ]);

    state.jobs = Array.isArray(jobData.items) ? jobData.items : [];
    state.applications = Array.isArray(appData.items) ? appData.items : [];
    state.assignedJobs = Array.isArray(assignedData.items) ? assignedData.items : [];
    state.student = {
      id: profileData.userId,
      name: profileData.name,
      email: profileData.email,
      studentId: profileData.studentId,
      programme: profileData.programme
    };
    state.profile.skills = profileData.skills || "";
    state.profile.experience = profileData.experience || "";
    state.attachments = Array.isArray(profileData.attachments) ? profileData.attachments : [];

    studentWelcomeEl.textContent = state.student.name
      ? `Welcome, ${state.student.name}.`
      : "Welcome, student.";
  }

  renderJobs();
  renderApplications();
  renderAssignedJobs();

  try {
    await loadFromBackend();
    showNotice("Connected to backend API.", false);
  } catch (backendErr) {
    showNotice(backendErr.message || "Failed to load data.", true);
  } finally {
    state.loading = false;
    renderJobs();
    renderApplications();
    renderAssignedJobs();
    renderProfile();
  }
});
