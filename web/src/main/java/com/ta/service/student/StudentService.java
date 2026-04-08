package com.ta.service.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.student.StudentApplicationCreateRequest;
import com.ta.dto.student.StudentApplicationItemResponse;
import com.ta.dto.student.StudentApplicationListResponse;
import com.ta.dto.student.StudentJobItemResponse;
import com.ta.dto.student.StudentJobListResponse;
import com.ta.dto.student.StudentProfileResponse;
import com.ta.dto.student.StudentProfileUpdateRequest;
import com.ta.model.ApplicationRecord;
import com.ta.model.Attachment;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import com.ta.util.FileStorageUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StudentService {

    public StudentJobListResponse listJobs(ServletContext context) {
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<StudentJobItemResponse> items = new ArrayList<>();
            for (JobPosting job : jobs) {
                if (!isVisibleJob(job)) {
                    continue;
                }
                items.add(toJobItem(job));
            }

            items.sort(Comparator.comparing(StudentJobItemResponse::getDeadline, Comparator.nullsLast(String::compareTo)));

            StudentJobListResponse response = new StudentJobListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jobs.", e);
        }
    }

    public StudentApplicationListResponse listMyApplications(ServletContext context, String studentUserId) {
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            Map<String, JobPosting> jobsById = new HashMap<>();
            for (JobPosting job : jobs) {
                jobsById.put(job.getId(), job);
            }

            List<StudentApplicationItemResponse> items = new ArrayList<>();
            for (ApplicationRecord record : applications) {
                if (!studentUserId.equals(record.getStudentId())) {
                    continue;
                }
                if (!record.isActive()) {
                    continue;
                }

                JobPosting job = jobsById.get(record.getJobId());
                StudentApplicationItemResponse item = new StudentApplicationItemResponse();
                item.setId(record.getId());
                item.setJobId(record.getJobId());
                item.setJobTitle(job != null ? job.getTitle() : "Unknown Job");
                item.setAppliedAt(extractDate(record.getAppliedAt()));
                item.setStatus(toStudentStatus(record.getStatus()));
                item.setFeedback("");
                items.add(item);
            }

            items.sort(Comparator.comparing(StudentApplicationItemResponse::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed());

            StudentApplicationListResponse response = new StudentApplicationListResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load applications.", e);
        }
    }

    public StudentApplicationItemResponse applyForJob(ServletContext context, String studentUserId, StudentApplicationCreateRequest request) {
        if (request == null || isBlank(request.getJobId())) {
            throw new StudentBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "jobId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
        if (request.getSelectedAttachmentIds() == null || request.getSelectedAttachmentIds().isEmpty()) {
            throw new StudentBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "At least one attachment is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<User> users = JsonUtility.loadUsers(context);
            User student = users.stream()
                    .filter(u -> studentUserId.equals(u.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Student login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> request.getJobId().equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Job not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));

            if (!isOpenStatus(job.getStatus())) {
                throw new StudentBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Only open jobs can be applied.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            boolean exists = applications.stream().anyMatch(a ->
                    studentUserId.equals(a.getStudentId())
                            && request.getJobId().equals(a.getJobId())
                            && a.isActive()
            );

            if (exists) {
                throw new StudentBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "You have already applied for this job.",
                        HttpServletResponse.SC_BAD_REQUEST
                );
            }

                List<StudentProfile> profiles = JsonUtility.loadStudents(context);
                StudentProfile profile = profiles.stream()
                    .filter(p -> studentUserId.equals(p.getUserId()))
                    .findFirst()
                    .orElseGet(() -> fallbackProfile(student));

                List<Attachment> profileAttachments = profile.getAttachments() != null
                    ? profile.getAttachments()
                    : new ArrayList<>();
                Set<String> availableAttachmentIds = profileAttachments.stream()
                    .map(Attachment::getId)
                    .filter(v -> v != null && !v.isBlank())
                    .collect(java.util.stream.Collectors.toSet());

                for (String selectedId : request.getSelectedAttachmentIds()) {
                if (!availableAttachmentIds.contains(selectedId)) {
                    throw new StudentBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Selected attachment does not exist in profile.",
                        HttpServletResponse.SC_BAD_REQUEST
                    );
                }
                }

            String now = Instant.now().toString();
            ApplicationRecord record = new ApplicationRecord();
            record.setId("app_" + Instant.now().toEpochMilli());
            record.setJobId(job.getId());
            record.setStudentId(student.getId());
            record.setStudentName(student.getName());
            record.setStudentNo(student.getStudentId());
            record.setCourseGrade("");
            record.setAppliedAt(now);
            record.setStatus("pending");
            record.setActive(true);
            record.setSelectedAttachmentIds(new ArrayList<>(request.getSelectedAttachmentIds()));

            applications.add(record);
            JsonUtility.saveApplications(context, applications);

            StudentApplicationItemResponse response = new StudentApplicationItemResponse();
            response.setId(record.getId());
            response.setJobId(record.getJobId());
            response.setJobTitle(job.getTitle());
            response.setAppliedAt(extractDate(record.getAppliedAt()));
            response.setStatus("pending");
            response.setFeedback("");
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create application.", e);
        }
    }

    public StudentProfileResponse getMyProfile(ServletContext context, String studentUserId) {
        try {
            List<User> users = JsonUtility.loadUsers(context);
            User student = users.stream()
                    .filter(u -> studentUserId.equals(u.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Student login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> studentUserId.equals(p.getUserId()))
                    .findFirst()
                    .orElseGet(() -> fallbackProfile(student));

            return toProfileResponse(student, profile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load profile.", e);
        }
    }

    public StudentProfileResponse updateMyProfile(ServletContext context, String studentUserId, StudentProfileUpdateRequest request) {
        if (request == null || isBlank(request.getName())) {
            throw new StudentBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "name is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<User> users = JsonUtility.loadUsers(context);
            User student = users.stream()
                    .filter(u -> studentUserId.equals(u.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Student login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            student.setName(request.getName().trim());
            JsonUtility.saveUsers(context, users);

            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> studentUserId.equals(p.getUserId()))
                    .findFirst()
                    .orElseGet(() -> {
                        StudentProfile created = fallbackProfile(student);
                        profiles.add(created);
                        return created;
                    });

            profile.setName(student.getName());
            profile.setEmail(student.getEmail());
            profile.setStudentId(student.getStudentId());
            profile.setSkills(trimToEmpty(request.getSkills()));
            profile.setExperience(trimToEmpty(request.getExperience()));
            JsonUtility.saveStudents(context, profiles);

            return toProfileResponse(student, profile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update profile.", e);
        }
    }

    private StudentJobItemResponse toJobItem(JobPosting job) {
        StudentJobItemResponse item = new StudentJobItemResponse();
        item.setId(job.getId());
        item.setModuleCode(job.getModuleCode());
        item.setTitle(job.getTitle());
        item.setHours(job.getHours());
        item.setPositions(job.getPositions());
        item.setStatus(job.getStatus());
        item.setDeadline(job.getDeadline());
        item.setTeacherName(job.getTeacherName());
        return item;
    }

    private StudentProfileResponse toProfileResponse(User user, StudentProfile profile) {
        StudentProfileResponse response = new StudentProfileResponse();
        response.setUserId(user.getId());
        response.setName(profile != null && !isBlank(profile.getName()) ? profile.getName() : user.getName());
        response.setEmail(profile != null && !isBlank(profile.getEmail()) ? profile.getEmail() : user.getEmail());
        response.setStudentId(profile != null && !isBlank(profile.getStudentId()) ? profile.getStudentId() : user.getStudentId());
        response.setProgramme(profile != null ? profile.getProgramme() : user.getProgramme());
        response.setSkills(profile != null ? trimToEmpty(profile.getSkills()) : "");
        response.setExperience(profile != null ? trimToEmpty(profile.getExperience()) : "");
        response.setAttachments(profile != null && profile.getAttachments() != null ? profile.getAttachments() : new ArrayList<>());
        return response;
    }

    private StudentProfile fallbackProfile(User user) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setName(user.getName());
        profile.setEmail(user.getEmail());
        profile.setStudentId(user.getStudentId());
        profile.setProgramme(user.getProgramme());
        profile.setSkills("");
        profile.setExperience("");
        return profile;
    }

    private boolean isVisibleJob(JobPosting job) {
        if (job == null) {
            return false;
        }
        if (Boolean.TRUE.equals(job.getWithdrawn())) {
            return false;
        }
        if (Boolean.FALSE.equals(job.getPublished()) && hasApprovalInfo(job)) {
            return false;
        }
        return true;
    }

    private boolean hasApprovalInfo(JobPosting job) {
        return job.getApprovalStatus() != null || job.getPublished() != null || job.getWithdrawn() != null;
    }

    private boolean isOpenStatus(String status) {
        return "open".equalsIgnoreCase(status) || "published".equalsIgnoreCase(status);
    }

    private String toStudentStatus(String status) {
        if ("hired".equalsIgnoreCase(status)) {
            return "hired";
        }
        if ("rejected".equalsIgnoreCase(status)) {
            return "rejected";
        }
        if ("shortlisted".equalsIgnoreCase(status)) {
            return "shortlisted";
        }
        return "pending";
    }

    private String extractDate(String isoDateTime) {
        if (isBlank(isoDateTime)) {
            return "";
        }
        try {
            return Instant.parse(isoDateTime).atZone(ZoneOffset.UTC).toLocalDate().toString();
        } catch (Exception ex) {
            try {
                return LocalDate.parse(isoDateTime).toString();
            } catch (Exception ignored) {
                return isoDateTime;
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Upload a file attachment for the student
     */
    public Attachment uploadAttachment(ServletContext context, String studentUserId, InputStream fileStream,
                                       long fileSizeBytes, String fileName, String label) throws IOException {
        if (isBlank(fileName) || isBlank(label)) {
            throw new StudentBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "fileName and label are required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            // Get student profile to validate student ID
            List<User> users = JsonUtility.loadUsers(context);
            User student = users.stream()
                    .filter(u -> studentUserId.equals(u.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Student login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            String studentId = student.getStudentId();

            // Validate file
            FileStorageUtil.validateFile(fileName, fileSizeBytes);
            FileStorageUtil.validateTotalSize(context, studentId, fileSizeBytes);

            // Save file
            String storageFileName = FileStorageUtil.saveFile(context, studentId, fileStream, fileName);

            // Create attachment metadata
            Attachment attachment = new Attachment();
            attachment.setId("attach_" + Instant.now().toEpochMilli());
            attachment.setFileName(fileName);
            attachment.setFileType(getFileExtension(fileName));
            attachment.setLabel(label);
            attachment.setFileSize(fileSizeBytes);
            attachment.setUploadedAt(FileStorageUtil.getCurrentTimestamp());

            // Store mapping (storageFileName -> attachmentId) so we can link them
            // For now, we'll use the attachment ID as a reference to storageFileName
            attachment.setId(storageFileName); // Use actual storage filename as ID

            // Update student profile with attachment
            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> studentUserId.equals(p.getUserId()))
                    .findFirst()
                    .orElseGet(() -> {
                        StudentProfile created = fallbackProfile(student);
                        profiles.add(created);
                        return created;
                    });

            if (profile.getAttachments() == null) {
                profile.setAttachments(new ArrayList<>());
            }
            profile.getAttachments().add(attachment);

            JsonUtility.saveStudents(context, profiles);

            return attachment;
        } catch (StudentBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload attachment.", e);
        }
    }

    /**
     * Delete an attachment for the student
     */
    public void deleteAttachment(ServletContext context, String studentUserId, String attachmentId) throws IOException {
        try {
            List<User> users = JsonUtility.loadUsers(context);
            User student = users.stream()
                    .filter(u -> studentUserId.equals(u.getId()))
                    .findFirst()
                    .orElseThrow(() -> new StudentBusinessException(
                            ErrorCodes.UNAUTHORIZED,
                            "Student login required.",
                            HttpServletResponse.SC_UNAUTHORIZED
                    ));

            String studentId = student.getStudentId();

            // Update student profile - remove attachment
            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> studentUserId.equals(p.getUserId()))
                    .findFirst()
                    .orElse(null);

            if (profile == null || profile.getAttachments() == null) {
                throw new StudentBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Attachment not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            boolean removed = profile.getAttachments().removeIf(a -> attachmentId.equals(a.getId()));
            if (!removed) {
                throw new StudentBusinessException(
                        ErrorCodes.VALIDATION_ERROR,
                        "Attachment not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            JsonUtility.saveStudents(context, profiles);

            // Delete physical file
            FileStorageUtil.deleteFile(context, studentId, attachmentId);
        } catch (StudentBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete attachment.", e);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
