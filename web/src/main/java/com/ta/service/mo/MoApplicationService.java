package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationDetailResponse;
import com.ta.dto.mo.MoApplicationListItemResponse;
import com.ta.dto.mo.MoApplicationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    public MoApplicationListResponse listApplications(ServletContext context, String moId, String jobIdFilter) {
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
                MoApplicationListItemResponse item = toListItem(a);
                enrichFromProfile(item, profileByUserId.get(a.getStudentId()));
                items.add(item);
            }

            MoApplicationListResponse response = new MoApplicationListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list applications.", e);
        }
    }

    /**
     * MO sets shortlisted / hired / rejected. Persists to applications.json.
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
        String normalized = newStatus == null ? "" : newStatus.trim().toLowerCase();
        if (!Set.of("shortlisted", "hired", "rejected", "viewed").contains(normalized)) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "status must be shortlisted, hired, rejected, or viewed (undo reject only).",
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
            if (Boolean.TRUE.equals(job.getRecruitmentClosed())) {
                throw new MoBusinessException(
                        ErrorCodes.JOB_RECRUITMENT_CLOSED,
                        "Recruitment is closed for this job (read-only).",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            String current = record.getStatus() == null ? "" : record.getStatus().trim().toLowerCase();

            if ("viewed".equals(normalized)) {
                if (!"rejected".equals(current)) {
                    throw new MoBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "Only rejected applications can be restored to viewed (undo reject).",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }
                record.setStatus("viewed");
                JsonUtility.saveApplications(context, applications);
            } else {
                if ("hired".equals(current) || "rejected".equals(current)) {
                    throw new MoBusinessException(
                            ErrorCodes.VALIDATION_ERROR,
                            "Application status is final and cannot be changed.",
                            HttpServletResponse.SC_BAD_REQUEST
                    );
                }

                record.setStatus(normalized);
                JsonUtility.saveApplications(context, applications);
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
        return item;
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
        return d;
    }
}
