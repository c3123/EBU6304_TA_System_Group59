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
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
                enrichFromProfile(item, profileByUserId.get(a.getStudentId()));
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
            Map<String, StudentProfile> profileByUserId = profiles.stream()
                    .filter(p -> p.getUserId() != null)
                    .collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));

            List<MoApplicationListItemResponse> items = new ArrayList<>();
            for (ApplicationRecord a : applications) {
                if (!a.isActive()) {
                    continue;
                }
                if (jobIdFilter != null && !jobIdFilter.isBlank() && !jobIdFilter.equals(a.getJobId())) {
                    continue;
                }
                MoApplicationListItemResponse item = toListItem(a);
                enrichFromProfile(item, profileByUserId.get(a.getStudentId()));
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
     *                        If exactly {pending, shortlisted, rejected} (all three MO filters), no status filter (includes hired).
     */
    static Set<String> parseStatusFilter(String statusFilterCsv) {
        if (statusFilterCsv == null || statusFilterCsv.isBlank()) {
            return null;
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
        Set<String> legacyThree = Set.of("pending", "shortlisted", "rejected");
        if (raw.size() == 3 && raw.containsAll(legacyThree) && !raw.contains("hired")) {
            return null;
        }
        return raw;
    }

    static boolean matchesStatusFilter(String normalizedRecordStatus, Set<String> filterTokens) {
        if (filterTokens == null) {
            return true;
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

            applyMoApplicationStatusTransition(record, job, normalized);
            if ("hired".equals(normalized)) {
                appendManualHireHistory(context, moId, record);
            }
            JsonUtility.saveApplications(context, applications);

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

            for (int i = 0; i < targets.size(); i++) {
                ApplicationRecord record = targets.get(i);
                JobPosting job = jobById.get(record.getJobId());
                applyMoApplicationStatusTransition(record, job, normalized);
            }

            JsonUtility.saveApplications(context, applications);
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
        if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            throw new MoBusinessException(
                    ErrorCodes.JOB_RECRUITMENT_CLOSED,
                    "Recruitment is closed for this job (read-only).",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        String current = normalizeStatus(record.getStatus());

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
            if ("hired".equals(current)) {
                throw new MoBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Application status is final and cannot be changed.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }
            if ("pending".equals(current)) {
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

    private void appendManualHireHistory(ServletContext context, String moId, ApplicationRecord hiredRecord) throws IOException {
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
            r.setDownloadUrl("/api/attachments/" + safe(record.getStudentNo()) + "/" + safe(att.getId()) + "/download");
            items.add(r);
        }
        return items;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
