document.addEventListener("DOMContentLoaded", async () => {
  const state = {
    activeTab: "jobs",
    loading: true,
    backendMode: true,
    jobs: [],
    applications: [],
    student: null,
    search: "",
    statusFilter: "all",
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

  function getDevStudentId() {
    const byQuery = new URLSearchParams(window.location.search).get("userId");
    if (byQuery && byQuery.trim()) return byQuery.trim();

    try {
      const byStorage = localStorage.getItem("ta_student_dev_id");
      if (byStorage && byStorage.trim()) return byStorage.trim();
    } catch (_) {
      // ignore localStorage access failures
    }

    return "";
  }

  function buildApiUrl(path) {
    const clean = path.startsWith("/") ? path : `/${path}`;
    return `${window.location.origin}${getContextPath()}/api/student${clean}`;
  }

  async function requestApi(path, options) {
    const devStudentId = getDevStudentId();
    const headers = {
      "Content-Type": "application/json"
    };
    if (devStudentId) {
      headers["X-STUDENT-ID"] = devStudentId;
    }

    const res = await fetch(buildApiUrl(path), {
      method: options && options.method ? options.method : "GET",
      credentials: "same-origin",
      headers,
      body: options && options.body ? JSON.stringify(options.body) : undefined
    });

    const body = await res.json();
    if (!res.ok || !body.success) {
      const message = body && body.message ? body.message : "Request failed.";
      throw new Error(message);
    }

    return body.data;
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
    renderAttachmentsList();
  }

  function renderAttachmentsList() {
    if (!attachmentsListEl) return;

    const attachments = state.attachments || [];
    if (attachments.length === 0) {
      attachmentsListEl.innerHTML = '<p style="margin: 14px; text-align: center; color: #6b7280; font-size: 13px;">No documents uploaded yet</p>';
      return;
    }

    const html = attachments.map(att => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-bottom: 1px solid #f3f4f6;">
        <div>
          <p style="margin: 0; font-size: 13px; font-weight: 500;">${escapeHtml(att.fileName)}</p>
          <p style="margin: 4px 0 0 0; font-size: 12px; color: #6b7280;">
            ${escapeHtml(att.label || 'Unlabeled')} • ${formatFileSize(att.fileSize)} • ${extractDate(att.uploadedAt)}
          </p>
        </div>
        <button class="delete-attachment-btn" data-attachment-id="${escapeHtml(att.id)}" style="padding: 6px 10px; background-color: #fee2e2; color: #991b1b; border: none; border-radius: 4px; font-size: 12px; cursor: pointer;">Delete</button>
      </div>
    `).join('');

    attachmentsListEl.innerHTML = html;

    // Attach delete event listeners
    attachmentsListEl.querySelectorAll('.delete-attachment-btn').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        const attachmentId = btn.dataset.attachmentId;
        await deleteAttachment(attachmentId);
      });
    });
  }

  function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
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

    // Validate file
    const allowedExtensions = ['pdf', 'docx', 'xlsx', 'jpg', 'png'];
    const fileExtension = file.name.split('.').pop().toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      showNotice(`File type not allowed. Allowed types: ${allowedExtensions.join(', ')}`, true);
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      showNotice("File size exceeds 50MB limit.", true);
      return;
    }

    if (state.backendMode) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('label', label);

        const devStudentId = getDevStudentId();
        const headers = {};
        if (devStudentId) {
          headers['X-STUDENT-ID'] = devStudentId;
        }

        const res = await fetch(buildApiUrl("/attachments"), {
          method: 'POST',
          credentials: 'same-origin',
          headers,
          body: formData
        });

        const body = await res.json();
        if (!res.ok || !body.success) {
          throw new Error(body.message || "Upload failed.");
        }

        state.attachments.push(body.data);
        renderAttachmentsList();
        fileInputEl.value = '';
        if (attachmentLabelEl) attachmentLabelEl.value = 'Resume';
        if (attachmentCustomLabelEl) attachmentCustomLabelEl.value = '';
        syncAttachmentLabelUi();
        showNotice("Document uploaded successfully.", false);
      } catch (err) {
        showNotice(err.message || "Failed to upload file.", true);
      }
    } else {
      // Mock mode
      state.attachments.push({
        id: 'local-' + Date.now(),
        fileName: file.name,
        fileType: fileExtension,
        label: label,
        fileSize: file.size,
        uploadedAt: new Date().toISOString()
      });
      renderAttachmentsList();
      fileInputEl.value = '';
      if (attachmentLabelEl) attachmentLabelEl.value = 'Resume';
      if (attachmentCustomLabelEl) attachmentCustomLabelEl.value = '';
      syncAttachmentLabelUi();
      showNotice("Document uploaded locally (frontend demo mode).", false);
    }
  }

  async function deleteAttachment(attachmentId) {
    if (!confirm("Delete this document?")) return;

    if (state.backendMode) {
      try {
        const devStudentId = getDevStudentId();
        const headers = {};
        if (devStudentId) {
          headers['X-STUDENT-ID'] = devStudentId;
        }

        const res = await fetch(buildApiUrl(`/attachments/${attachmentId}`), {
          method: 'DELETE',
          credentials: 'same-origin',
          headers
        });

        const body = await res.json();
        if (!res.ok || !body.success) {
          throw new Error(body.message || "Delete failed.");
        }

        state.attachments = state.attachments.filter(a => a.id !== attachmentId);
        renderAttachmentsList();
        showNotice("Document deleted successfully.", false);
      } catch (err) {
        showNotice(err.message || "Failed to delete document.", true);
      }
    } else {
      // Mock mode
      state.attachments = state.attachments.filter(a => a.id !== attachmentId);
      renderAttachmentsList();
      showNotice("Document deleted locally (frontend demo mode).", false);
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
    if (detailStatusEl) detailStatusEl.textContent = job.status || "-";
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

    detailAttachmentsListEl.innerHTML = attachments.map((att, idx) => `
      <label style="display:flex;align-items:center;gap:8px;margin:6px 0;">
        <input type="checkbox" class="detail-attachment-checkbox" data-attachment-id="${escapeHtml(att.id)}" ${idx === 0 ? "checked" : "checked"} />
        <span>${escapeHtml(att.fileName || "Unnamed file")} (${escapeHtml(att.label || "Unlabeled")}, ${formatFileSize(att.fileSize || 0)})</span>
      </label>
    `).join("");

    if (detailApplyBtn) detailApplyBtn.disabled = false;
    if (detailAttachmentHintEl) detailAttachmentHintEl.textContent = "At least one attachment is required. All are selected by default.";

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

    if (state.backendMode) {
      const created = await requestApi("/applications", {
        method: "POST",
        body: { jobId, selectedAttachmentIds }
      });
      state.applications.unshift(created);
      renderJobs();
      renderApplications();
      showNotice("Application submitted successfully.", false);
      return;
    }

    state.applications.unshift({
      id: `local-${Date.now()}`,
      jobId: job.id,
      jobTitle: job.title,
      studentId: (state.student && state.student.id) || "",
      studentName: (state.student && state.student.name) || "",
      appliedDate: todayIsoDate(),
      status: "pending",
      feedback: "",
      selectedAttachmentIds: selectedAttachmentIds
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
      if (state.backendMode) {
        const updated = await requestApi("/profile", {
          method: "PUT",
          body: { name }
        });
        state.student = {
          id: updated.userId,
          name: updated.name,
          email: updated.email,
          studentId: updated.studentId,
          programme: updated.programme
        };
      } else if (state.student) {
        state.student.name = name;
      }

      state.profile.skills = profileSkillsEl.value.trim();
      state.profile.experience = profileExperienceEl.value.trim();
      studentWelcomeEl.textContent = `Welcome, ${name}.`;
      showNotice(state.backendMode ? "Profile saved successfully." : "Profile saved locally (frontend demo mode).", false);
    } catch (err) {
      showNotice(err.message || "Failed to save profile.", true);
    }
  });

  // File upload handling
  if (uploadAreaEl && fileInputEl) {
    syncAttachmentLabelUi();

    if (attachmentLabelEl) {
      attachmentLabelEl.addEventListener('change', syncAttachmentLabelUi);
    }

    uploadAreaEl.addEventListener('click', () => {
      fileInputEl.click();
    });

    uploadAreaEl.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadAreaEl.style.borderColor = '#1e5eff';
      uploadAreaEl.style.backgroundColor = '#eff6ff';
    });

    uploadAreaEl.addEventListener('dragleave', () => {
      uploadAreaEl.style.borderColor = '#9ca3af';
      uploadAreaEl.style.backgroundColor = '#f9fafb';
    });

    uploadAreaEl.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadAreaEl.style.borderColor = '#9ca3af';
      uploadAreaEl.style.backgroundColor = '#f9fafb';
      const files = e.dataTransfer.files;
      if (files.length > 0) {
        (async () => {
          for (let file of files) {
            await uploadFile(file);
          }
        })();
      }
    });

    fileInputEl.addEventListener('change', (e) => {
      const files = e.target.files;
      if (files.length > 0) {
        (async () => {
          for (let file of files) {
            await uploadFile(file);
          }
        })();
      }
    });
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
      studentId: profileData.studentId,
      programme: profileData.programme
    };
    state.attachments = Array.isArray(profileData.attachments) ? profileData.attachments : [];

    studentWelcomeEl.textContent = state.student.name
      ? `Welcome, ${state.student.name}.`
      : "Welcome, student.";
  }

  async function loadFromMock() {
    const data = await loadMockData();
    const users = Array.isArray(data.users) ? data.users : [];
    const student = users.find((user) => user.role === "student") || null;

    state.jobs = Array.isArray(data.jobs) ? data.jobs : [];
    state.student = student;
    state.applications = Array.isArray(data.applications)
      ? data.applications
          .filter((app) => !student || app.studentId === student.id)
          .map((app) => ({
            id: app.id,
            jobId: app.jobId,
            jobTitle: app.jobTitle,
            appliedDate: app.appliedDate,
            status: app.status,
            feedback: app.feedback || ""
          }))
      : [];

    studentWelcomeEl.textContent = student && student.name
      ? `Welcome, ${student.name}.`
      : "Welcome, student.";
  }

  renderJobs();
  renderApplications();

  try {
    await loadFromBackend();
    state.backendMode = true;
    showNotice("Connected to backend API.", false);
  } catch (backendErr) {
    try {
      await loadFromMock();
      state.backendMode = false;
      showNotice("Backend unavailable, switched to local mock mode.", true);
    } catch (mockErr) {
      showNotice(mockErr.message || backendErr.message || "Failed to load data.", true);
    }
  } finally {
    state.loading = false;
    renderJobs();
    renderApplications();
    renderProfile();
  }
});
