package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationAttachmentResponse;
import com.ta.dto.mo.MoApplicationDetailResponse;
import com.ta.dto.mo.MoApplicationListItemResponse;
import com.ta.dto.mo.MoApplicationListResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.Attachment;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
                items.add(toListItem(a));
            }

            MoApplicationListResponse response = new MoApplicationListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to list applications.", e);
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
        return item;
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
