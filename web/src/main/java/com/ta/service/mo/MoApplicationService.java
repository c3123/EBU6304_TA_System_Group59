package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationAttachmentResponse;
import com.ta.dto.mo.MoApplicationDetailResponse;
import com.ta.dto.mo.MoApplicationListItemResponse;
import com.ta.dto.mo.MoApplicationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.Attachment;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.service.admin.WorkloadOverloadAnnouncementService;
import com.ta.service.student.JobMatchResult;
import com.ta.service.student.JobMatchingService;
import com.ta.service.student.SkillRelationHint;
import com.ta.util.AgentDebugLog;
import com.ta.util.JsonUtility;
import com.ta.util.StudentWorkloadUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MO application viewing service boundary.
 *
 * Ownership: B side (applications and status tracking).
 * Rules:
 * 1) MO can only read applications for jobs owned by that MO.
 * 2) Detail read should auto-update status pending -> viewed.
 * 3) List should return active=true applications only.
 */
public class MoApplicationService {

    private static final int MAX_DECISION_FEEDBACK_CHARS = 200;
    private final WorkloadOverloadAnnouncementService workloadOverloadAnnouncementService =
            new WorkloadOverloadAnnouncementService();
    private final JobMatchingService jobMatchingService;

    public MoApplicationService() {
        this(new JobMatchingService());
    }

    public MoApplicationService(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService != null ? jobMatchingService : new JobMatchingService();
    }
    /** Query param value: show no applicants (all status checkboxes off in MO UI). */
    static final String STATUS_FILTER_NONE_SENTINEL = "__none__";

    public MoApplicationListResponse listApplications(ServletContext context, String moId, String jobIdFilter, String statusFilterCsv) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Set<String> ownedJobIds = jobs.stream()
                    .filter(j -> moId.equals(j.getTeacherId()))
                    .map(JobPosting::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));

            if (jobIdFilter != null && !jobIdFilter.isBlank()) {
                JobPosting job = jobs.stream()
                        .filter(j -> jobIdFilter.equals(j.getId()))
                        .findFirst()
                        .orElseThrow(() -> new MoBusinessException(
                                ErrorCodes.JOB_NOT_FOUND,
                                "Job not found.",
                                HttpServletResponse.SC_NOT_FOUND
                        ));
                if (!moId.equals(job.getTeacherId())) {
                    throw new MoBusinessException(
                            ErrorCodes.FORBIDDEN_NOT_OWNER,
                            "You can only view applications for your own jobs.",
                            HttpServletResponse.SC_FORBIDDEN
                    );
                }
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            Map<String, StudentProfile> profileByUserId = profiles.stream()
                    .filter(p -> p.getUserId() != null)
                    .collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));
            Map<String, JobPosting> jobById = jobs.stream()
                    .filter(j -> j.getId() != null)
                    .collect(Collectors.toMap(JobPosting::getId, Function.identity(), (a, b) -> a));

            Set<String> statusTokens = parseStatusFilter(statusFilterCsv);

            List<MoApplicationListItemResponse> items = new ArrayList<>();

            for (ApplicationRecord a : applications) {
                if (!a.isActive()) {
                    continue;
                }
                if (!ownedJobIds.contains(a.getJobId())) {
                    continue;
                }
                if (jobIdFilter != null && !jobIdFilter.isBlank() && !jobIdFilter.equals(a.getJobId())) {
                    continue;
                }
                if (statusTokens != null && !matchesStatusFilter(normalizeStatus(a.getStatus()), statusTokens)) {
                    continue;
                }
                MoApplicationListItemResponse item = toListItem(a);
                StudentProfile profile = profileByUserId.get(a.getStudentId());
                enrichFromProfile(item, profile);
                JobPosting itemJob = jobById.get(a.getJobId());
                enrichWithSkillMatch(item, profile, itemJob);
                enrichWithWorkload(item, a, applications, jobById);
                items.add(item);
            }

            items.sort(Comparator.comparing(MoApplicationListItemResponse::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed());

            MoApplicationListResponse response = new MoApplicationListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list applications.", e);
        }
    }

    /**
     * Admin read-only: all active applications (optional job filter), including MO-only fields for archiving.
     */
    public MoApplicationListResponse listApplicationsForAdmin(ServletContext context, String jobIdFilter) {
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Map<String, StudentProfile> profileByUserId = profiles.stream()
                    .filter(p -> p.getUserId() != null)
                    .collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));
            Map<String, JobPosting> jobById = jobs.stream()
                    .filter(j -> j.getId() != null)
                    .collect(Collectors.toMap(JobPosting::getId, Function.identity(), (a, b) -> a));

            List<MoApplicationListItemResponse> items = new ArrayList<>();
            for (ApplicationRecord a : applications) {
                if (!a.isActive()) {
                    continue;
                }
                if (jobIdFilter != null && !jobIdFilter.isBlank() && !jobIdFilter.equals(a.getJobId())) {
                    continue;
                }
                MoApplicationListItemResponse item = toListItem(a);
                StudentProfile profile = profileByUserId.get(a.getStudentId());
                enrichFromProfile(item, profile);
                JobPosting itemJob = jobById.get(a.getJobId());
                enrichWithSkillMatch(item, profile, itemJob);
                enrichWithWorkload(item, a, applications, jobById);
                items.add(item);
            }
            items.sort(Comparator.comparing(MoApplicationListItemResponse::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed());
            MoApplicationListResponse response = new MoApplicationListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list applications for admin.", e);
        }
    }

    /**
     * @param statusFilterCsv comma-separated tokens: pending, shortlisted, rejected, hired.
     *                        Token "pending" matches records in pending or viewed.
     *                        Special value "__none__": match no records (UI: zero checkboxes).
     *                        Omit param or all four tokens: no status filter (show all).
     */
    static Set<String> parseStatusFilter(String statusFilterCsv) {
        if (statusFilterCsv == null || statusFilterCsv.isBlank()) {
            return null;
        }
        String trimmedIn = statusFilterCsv.trim();
        if (STATUS_FILTER_NONE_SENTINEL.equalsIgnoreCase(trimmedIn)) {
            return Collections.emptySet();
        }
        Set<String> raw = new LinkedHashSet<>();
        for (String part : statusFilterCsv.split(",")) {
            if (part == null) {
                continue;
            }
            String t = part.trim().toLowerCase();
            if (!t.isEmpty()) {
                raw.add(t);
            }
        }
        if (raw.isEmpty()) {
            return null;
        }
        Set<String> allFour = Set.of("pending", "shortlisted", "rejected", "hired");
        if (raw.size() == 4 && raw.containsAll(allFour)) {
            return null;
        }
        return raw;
    }

    static boolean matchesStatusFilter(String normalizedRecordStatus, Set<String> filterTokens) {
        if (filterTokens == null) {
            return true;
        }
        if (filterTokens.isEmpty()) {
            return false;
        }
        for (String token : filterTokens) {
            if ("pending".equals(token) && ("pending".equals(normalizedRecordStatus) || "viewed".equals(normalizedRecordStatus))) {
                return true;
            }
            if (token.equals(normalizedRecordStatus)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeStatus(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * MO sets shortlisted / hired / rejected / pending / viewed (undo). Persists to applications.json.
     */
    public MoApplicationListItemResponse updateApplicationStatus(ServletContext context,
                                                                  String moId,
                                                                  String applicationId,
                                                                  String newStatus) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "applicationId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        String normalized = normalizeStatus(newStatus);
        if (!Set.of("shortlisted", "hired", "rejected", "viewed", "pending").contains(normalized)) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "status must be pending, shortlisted, hired, rejected, or viewed (undo reject only).",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            ApplicationRecord record = applications.stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Application not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!record.isActive()) {
                throw new MoBusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND,
                        "Application not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> record.getJobId() != null && record.getJobId().equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Job not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!moId.equals(job.getTeacherId())) {
                throw new MoBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only update applications for your own jobs.",
                        HttpServletResponse.SC_FORBIDDEN
                );
            }

            int weeklyHoursBefore = 0;
            if ("hired".equals(normalized) && !"hired".equalsIgnoreCase(normalizeStatus(record.getStatus()))) {
                weeklyHoursBefore = workloadOverloadAnnouncementService.calculateWeeklyHours(context, record.getStudentId());
            }

            if ("hired".equals(normalized)) {
                checkAndRejectIfJobFull(context, jobs, job);
            }

            applyMoApplicationStatusTransition(record, job, normalized);
            // #region agent log
            try {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("normalized", normalized);
                d.put("recordStatusAfter", record.getStatus());
                d.put("applicationId", applicationId);
                d.put("jobId", record.getJobId());
                d.put("willAppendManualHistory", Boolean.valueOf("hired".equals(normalized)));
                AgentDebugLog.log("H1", "MoApplicationService.updateApplicationStatus", "after_transition", d);
            } catch (Throwable ignored) {
                // ignore
            }
            // #endregion
            if ("hired".equals(normalized)) {
                appendManualHireHistory(context, moId, record);
                autoCloseJobIfHiredFull(context, jobs, job);
            }
            JsonUtility.saveApplications(context, applications);
            if ("hired".equals(normalized)) {
                workloadOverloadAnnouncementService.notifyIfNewlyOverloaded(context, record.getStudentId(), weeklyHoursBefore);
            }

            MoApplicationListItemResponse item = toListItem(record);
            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> record.getStudentId() != null && record.getStudentId().equals(p.getUserId()))
                    .findFirst()
                    .orElse(null);
            enrichFromProfile(item, profile);
            return item;
        } catch (IOException e) {
            throw new RuntimeException("Failed to update application status.", e);
        }
    }

    /**
     * Batch status update: validate all, then one save (all-or-nothing for this process).
     */
    public Map<String, Object> batchUpdateApplicationStatus(ServletContext context,
                                                            String moId,
                                                            List<String> applicationIds,
                                                            String newStatus) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "ids must be a non-empty array.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        String normalized = normalizeStatus(newStatus);
        if (!Set.of("shortlisted", "hired", "rejected", "viewed", "pending").contains(normalized)) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "status must be pending, shortlisted, hired, rejected, or viewed (undo reject only).",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Map<String, JobPosting> jobById = jobs.stream()
                    .filter(j -> j.getId() != null)
                    .collect(Collectors.toMap(JobPosting::getId, Function.identity(), (a, b) -> a));

            List<ApplicationRecord> targets = new ArrayList<>();
            for (String id : applicationIds) {
                if (id == null || id.isBlank()) {
                    throw new MoBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "Each id must be non-blank.",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }
                ApplicationRecord record = applications.stream()
                        .filter(a -> id.equals(a.getId()))
                        .findFirst()
                        .orElseThrow(() -> new MoBusinessException(
                                ErrorCodes.APPLICATION_NOT_FOUND,
                                "Application not found: " + id,
                                HttpServletResponse.SC_NOT_FOUND
                        ));
                if (!record.isActive()) {
                    throw new MoBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Application not found: " + id,
                            HttpServletResponse.SC_NOT_FOUND
                    );
                }
                JobPosting job = jobById.get(record.getJobId());
                if (job == null) {
                    throw new MoBusinessException(ErrorCodes.JOB_NOT_FOUND, "Job not found.", HttpServletResponse.SC_NOT_FOUND);
                }
                if (!moId.equals(job.getTeacherId())) {
                    throw new MoBusinessException(
                            ErrorCodes.FORBIDDEN_NOT_OWNER,
                            "You can only update applications for your own jobs.",
                            HttpServletResponse.SC_FORBIDDEN
                    );
                }
                targets.add(record);
            }

            Map<String, Integer> weeklyHoursBeforeByStudent = new LinkedHashMap<>();
            if ("hired".equals(normalized)) {
                for (ApplicationRecord record : targets) {
                    if (record.getStudentId() == null || record.getStudentId().isBlank()) {
                        continue;
                    }
                    if (!"hired".equalsIgnoreCase(normalizeStatus(record.getStatus()))) {
                        weeklyHoursBeforeByStudent.computeIfAbsent(
                                record.getStudentId(),
                                studentId -> {
                                    try {
                                        return workloadOverloadAnnouncementService.calculateWeeklyHours(context, studentId);
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                        );
                    }
                }

                Map<String, JobPosting> jobsToCheck = new LinkedHashMap<>();
                for (ApplicationRecord record : targets) {
                    JobPosting job = jobById.get(record.getJobId());
                    if (job != null && !jobsToCheck.containsKey(job.getId())) {
                        jobsToCheck.put(job.getId(), job);
                    }
                }
                for (JobPosting job : jobsToCheck.values()) {
                    checkAndRejectIfJobFull(context, jobs, job);
                }
            }

            for (int i = 0; i < targets.size(); i++) {
                ApplicationRecord record = targets.get(i);
                JobPosting job = jobById.get(record.getJobId());
                applyMoApplicationStatusTransition(record, job, normalized);
                if ("hired".equals(normalized)) {
                    appendManualHireHistory(context, moId, record);
                }
            }

            // #region agent log
            if ("hired".equals(normalized)) {
                try {
                    Map<String, Object> d = new LinkedHashMap<>();
                    d.put("targetCount", Integer.valueOf(targets.size()));
                    d.put("note", "batch path now appends manual_hire per record");
                    AgentDebugLog.log("H5", "MoApplicationService.batchUpdateApplicationStatus", "batch_hired_complete", d);
                } catch (Throwable ignored) {
                    // ignore
                }
            }
            // #endregion

            JsonUtility.saveApplications(context, applications);
            if ("hired".equals(normalized)) {
                for (Map.Entry<String, Integer> entry : weeklyHoursBeforeByStudent.entrySet()) {
                    workloadOverloadAnnouncementService.notifyIfNewlyOverloaded(
                            context, entry.getKey(), entry.getValue());
                }
            }
            return Map.of("updated", targets.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to batch update application status.", e);
        }
    }

    public void updateEvaluationNotes(ServletContext context, String moId, String applicationId, String evaluationNotes) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "applicationId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        String notes = evaluationNotes == null ? "" : evaluationNotes;
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            ApplicationRecord record = applications.stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Application not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));
            if (!record.isActive()) {
                throw new MoBusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND,
                        "Application not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }
            JobPosting job = requireOwnedJob(context, moId, record.getJobId());
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment is closed for this job (read-only).",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            record.setEvaluationNotes(notes);
            JsonUtility.saveApplications(context, applications);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save evaluation notes.", e);
        }
    }

    public void updateDecisionFeedback(ServletContext context, String moId, String applicationId, String decisionFeedback) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "applicationId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        String text = decisionFeedback == null ? "" : decisionFeedback;
        if (text.length() > MAX_DECISION_FEEDBACK_CHARS) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Feedback must not exceed 200 characters.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            ApplicationRecord record = applications.stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Application not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));
            if (!record.isActive()) {
                throw new MoBusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND,
                        "Application not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }
            JobPosting job = requireOwnedJob(context, moId, record.getJobId());
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment is closed for this job (read-only).",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            String st = normalizeStatus(record.getStatus());
            if (!Set.of("hired", "shortlisted", "rejected").contains(st)) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Feedback is only allowed for hired, shortlisted, or rejected applicants.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            record.setDecisionFeedback(text);
            JsonUtility.saveApplications(context, applications);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save decision feedback.", e);
        }
    }

    private JobPosting requireOwnedJob(ServletContext context, String moId, String jobId) throws IOException {
        List<JobPosting> jobs = JsonUtility.loadJobs(context);
        JobPosting job = jobs.stream()
                .filter(j -> jobId != null && jobId.equals(j.getId()))
                .findFirst()
                .orElseThrow(() -> new MoBusinessException(
                        ErrorCodes.JOB_NOT_FOUND,
                        "Job not found.",
                        HttpServletResponse.SC_NOT_FOUND
                ));
        if (!moId.equals(job.getTeacherId())) {
            throw new MoBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only update applications for your own jobs.",
                        HttpServletResponse.SC_FORBIDDEN
            );
        }
        return job;
    }

    static void applyMoApplicationStatusTransition(ApplicationRecord record, JobPosting job, String normalized) {
        String current = normalizeStatus(record.getStatus());
        boolean revertHireToPending = "pending".equals(normalized) && "hired".equals(current);

        if (Boolean.TRUE.equals(job.getRecruitmentClosed()) && !revertHireToPending) {
            throw new MoBusinessException(
                    ErrorCodes.JOB_RECRUITMENT_CLOSED,
                    "Recruitment is closed for this job (read-only).",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        if ("viewed".equals(normalized)) {
            if (!"rejected".equals(current)) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "viewed is only allowed when undoing a reject.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            record.setStatus("viewed");
            record.setDecisionFeedback("");
            return;
        }

        if ("pending".equals(normalized)) {
            if ("pending".equals(current)) {
                return;
            }
            if ("hired".equals(current)) {
                record.setStatus("pending");
                record.setDecisionFeedback("");
                return;
            }
            if (!Set.of("shortlisted", "viewed", "rejected").contains(current)) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Cannot set pending from the current status.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            record.setStatus("pending");
            record.setDecisionFeedback("");
            return;
        }

        if (current.equals(normalized)) {
            return;
        }

        if ("hired".equals(current)) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Application status is final and cannot be changed.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if ("rejected".equals(current)) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "Undo reject (viewed) or set to pending before changing status.",
                    HttpServletResponse.SC_BAD_REQUEST
                );
        }

        record.setStatus(normalized);
    }

    private void autoCloseJobIfHiredFull(ServletContext context, List<JobPosting> jobs, JobPosting job) throws IOException {
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            return;
        }
        int hiredCount = (int) JsonUtility.loadApplications(context).stream()
                .filter(a -> job.getId().equals(a.getJobId()))
                .filter(a -> "hired".equalsIgnoreCase(a.getStatus()))
                .count();
        if (job.getPositions() > 0 && hiredCount >= job.getPositions()) {
            String now = Instant.now().toString();
            job.setRecruitmentClosed(true);
            job.setClosedAt(now);
            job.setStatus("closed");
            job.setPublished(false);
            job.setUpdatedAt(now);
            JsonUtility.saveJobs(context, jobs);
            // #region agent log
            try {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("jobId", job.getId());
                d.put("positions", job.getPositions());
                d.put("hiredCount", hiredCount);
                d.put("autoClosed", true);
                AgentDebugLog.log("H6", "MoApplicationService.autoCloseJobIfHiredFull", "job_auto_closed", d);
            } catch (Throwable ignored) {
            }
            // #endregion
        }
    }

    private void checkAndRejectIfJobFull(ServletContext context, List<JobPosting> jobs, JobPosting job) throws IOException {
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            return;
        }
        int hiredCount = (int) JsonUtility.loadApplications(context).stream()
                .filter(a -> job.getId().equals(a.getJobId()))
                .filter(a -> "hired".equalsIgnoreCase(a.getStatus()))
                .count();
        if (job.getPositions() > 0 && hiredCount >= job.getPositions()) {
            throw new MoBusinessException(
                    ErrorCodes.JOB_RECRUITMENT_CLOSED,
                    "This job has reached its position limit (" + job.getPositions() + "). Cannot hire more applicants.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    private void appendManualHireHistory(ServletContext context, String moId, ApplicationRecord hiredRecord) throws IOException {
        // #region agent log
        try {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("jobId", hiredRecord.getJobId());
            d.put("applicationId", hiredRecord.getId());
            d.put("moId", moId);
            AgentDebugLog.log("H1", "MoApplicationService.appendManualHireHistory", "entry", d);
        } catch (Throwable ignored) {
            // ignore
        }
        // #endregion
        List<HiringHistoryRecord> history = JsonUtility.loadHiringHistory(context);
        HiringHistoryRecord record = new HiringHistoryRecord();
        record.setId("hist_" + UUID.randomUUID().toString().replace("-", ""));
        record.setAction("manual_hire");
        record.setJobId(hiredRecord.getJobId());
        record.setMoId(moId);
        record.setSubmittedAt(Instant.now().toString());
        record.setHiredApplicationIds(List.of(hiredRecord.getId()));
        String studentName = hiredRecord.getStudentName() == null ? hiredRecord.getStudentId() : hiredRecord.getStudentName();
        record.setHiredStudentNames(List.of(studentName));
        history.add(record);
        JsonUtility.saveHiringHistory(context, history);
    }

    public MoApplicationDetailResponse getDetailAndMarkViewed(ServletContext context, String moId, String applicationId) {
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            ApplicationRecord record = applications.stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.APPLICATION_NOT_FOUND,
                            "Application not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!record.isActive()) {
                throw new MoBusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND,
                        "Application not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> record.getJobId() != null && record.getJobId().equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Job not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!moId.equals(job.getTeacherId())) {
                throw new MoBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only view applications for your own jobs.",
                        HttpServletResponse.SC_FORBIDDEN
                );
            }

            String updatedAt = null;
            if ("pending".equalsIgnoreCase(record.getStatus())) {
                record.setStatus("viewed");
                updatedAt = Instant.now().toString();
                JsonUtility.saveApplications(context, applications);
            }

            MoApplicationDetailResponse response = toDetail(record);
            response.setAttachments(buildAttachmentDetails(context, record));
            response.setUpdatedAt(updatedAt);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load or save application.", e);
        }
    }

    private static MoApplicationListItemResponse toListItem(ApplicationRecord a) {
        MoApplicationListItemResponse item = new MoApplicationListItemResponse();
        item.setApplicationId(a.getId());
        item.setJobId(a.getJobId());
        item.setStudentId(a.getStudentId());
        item.setStudentName(a.getStudentName());
        item.setStudentNo(a.getStudentNo());
        item.setCourseGrade(a.getCourseGrade());
        item.setAppliedAt(a.getAppliedAt());
        item.setStatus(a.getStatus());
        item.setEvaluationNotes(blankToEmpty(a.getEvaluationNotes()));
        item.setDecisionFeedback(blankToEmpty(a.getDecisionFeedback()));
        return item;
    }

    private static String blankToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static void enrichFromProfile(MoApplicationListItemResponse item, StudentProfile p) {
        if (p == null || item == null) {
            return;
        }
        item.setProgramme(p.getProgramme());
        item.setSkills(p.getSkills());
        item.setExperience(p.getExperience());
    }

    private void enrichWithSkillMatch(MoApplicationListItemResponse item, StudentProfile profile, JobPosting job) {
        if (item == null) {
            return;
        }
        if (profile == null || job == null) {
            item.setMatchScore(0.0);
            item.setMatchedSkills(new ArrayList<>());
            item.setMissingSkills(new ArrayList<>());
            item.setRequiredSkills(new ArrayList<>());
            item.setDetectedStudentSkills(new ArrayList<>());
            item.setRelatedMatches(new ArrayList<>());
            return;
        }
        JobMatchResult match = jobMatchingService.match(profile, job);
        item.setMatchScore(roundToTwoDecimals(match.getMatchScore()));
        item.setMatchedSkills(new ArrayList<>(match.getMatchedSkills()));
        item.setMissingSkills(new ArrayList<>(match.getMissingSkills()));
        item.setRequiredSkills(new ArrayList<>(match.getRequiredSkills()));
        item.setDetectedStudentSkills(new ArrayList<>(match.getStudentSkills()));
        item.setRelatedMatches(toRelatedLabels(match.getRelatedMatches()));
    }

    private void enrichWithWorkload(MoApplicationListItemResponse item,
                                    ApplicationRecord application,
                                    List<ApplicationRecord> applications,
                                    Map<String, JobPosting> jobById) {
        if (item == null || application == null) {
            return;
        }
        int currentElsewhere = StudentWorkloadUtil.currentHiredHoursElsewhere(
                application.getStudentId(),
                application.getId(),
                applications,
                jobById
        );
        item.setCurrentHiredHours(currentElsewhere);
        item.setProjectedIfHiredHours(StudentWorkloadUtil.projectedIfHired(
                currentElsewhere,
                jobById.get(application.getJobId())
        ));
    }

    private static List<String> toRelatedLabels(List<SkillRelationHint> hints) {
        List<String> labels = new ArrayList<>();
        if (hints == null) {
            return labels;
        }
        for (SkillRelationHint hint : hints) {
            if (hint != null) {
                labels.add(hint.toDisplayLabel());
            }
        }
        return labels;
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static MoApplicationDetailResponse toDetail(ApplicationRecord a) {
        MoApplicationDetailResponse d = new MoApplicationDetailResponse();
        d.setApplicationId(a.getId());
        d.setJobId(a.getJobId());
        d.setStudentId(a.getStudentId());
        d.setStudentName(a.getStudentName());
        d.setStudentNo(a.getStudentNo());
        d.setCourseGrade(a.getCourseGrade());
        d.setAppliedAt(a.getAppliedAt());
        d.setStatus(a.getStatus());
        d.setEvaluationNotes(blankToEmpty(a.getEvaluationNotes()));
        d.setDecisionFeedback(blankToEmpty(a.getDecisionFeedback()));
        return d;
    }

    private List<MoApplicationAttachmentResponse> buildAttachmentDetails(ServletContext context, ApplicationRecord record) throws IOException {
        List<String> selectedIds = record.getSelectedAttachmentIds() != null
                ? record.getSelectedAttachmentIds()
                : new ArrayList<>();
        if (selectedIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<StudentProfile> profiles = JsonUtility.loadStudents(context);
        StudentProfile profile = profiles.stream()
                .filter(p -> record.getStudentId() != null && record.getStudentId().equals(p.getUserId()))
                .findFirst()
                .orElse(null);

        if (profile == null || profile.getAttachments() == null || profile.getAttachments().isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Attachment> byId = profile.getAttachments().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Attachment::getId, a -> a, (a, b) -> a));

        List<MoApplicationAttachmentResponse> items = new ArrayList<>();
        for (String id : selectedIds) {
            Attachment att = byId.get(id);
            if (att == null) {
                continue;
            }
            MoApplicationAttachmentResponse r = new MoApplicationAttachmentResponse();
            r.setAttachmentId(att.getId());
            r.setFileName(att.getFileName());
            r.setLabel(att.getLabel());
            r.setFileSize(att.getFileSize());
            r.setUploadedAt(att.getUploadedAt());
            r.setDownloadUrl("/api/attachments/" + safe(preferredStudentKey(profile, record)) + "/" + safe(att.getId()) + "/download");
            items.add(r);
        }
        return items;
    }

    private String preferredStudentKey(StudentProfile profile, ApplicationRecord record) {
        if (profile != null && profile.getStudentId() != null && !profile.getStudentId().isBlank()) {
            return profile.getStudentId();
        }
        if (record.getStudentNo() != null && !record.getStudentNo().isBlank()) {
            return record.getStudentNo();
        }
        return record.getStudentId();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
