package com.ta.service.admin;

import com.ta.dto.admin.AdminApplicationArchiveItemResponse;
import com.ta.dto.admin.AdminApplicationArchiveResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminApplicationArchiveService {

    public AdminApplicationArchiveResponse listArchive(ServletContext context,
                                                        String statusFilter,
                                                        String jobIdFilter,
                                                        String teacherFilter,
                                                        String studentFilter) {
        try {
            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<StudentProfile> students = JsonUtility.loadStudents(context);

            Map<String, JobPosting> jobById = new LinkedHashMap<>();
            for (JobPosting job : jobs) {
                if (job.getId() != null) {
                    jobById.put(job.getId(), job);
                }
            }

            Map<String, StudentProfile> studentByUserId = new LinkedHashMap<>();
            for (StudentProfile student : students) {
                if (student.getUserId() != null) {
                    studentByUserId.put(student.getUserId(), student);
                }
            }

            String status = normalizeFilter(statusFilter);
            String jobId = normalizeFilter(jobIdFilter);
            String teacher = normalizeFilter(teacherFilter);
            String student = normalizeFilter(studentFilter);

            List<AdminApplicationArchiveItemResponse> items = new ArrayList<>();
            for (ApplicationRecord application : applications) {
                JobPosting job = jobById.get(application.getJobId());
                StudentProfile profile = studentByUserId.get(application.getStudentId());
                if (!matchesStatus(application, status)
                        || !matchesJob(application, jobId)
                        || !matchesTeacher(job, teacher)
                        || !matchesStudent(application, profile, student)) {
                    continue;
                }
                items.add(toItem(application, job, profile));
            }

            items.sort(Comparator
                    .comparing(AdminApplicationArchiveItemResponse::getAppliedAt, Comparator.nullsLast(String::compareTo))
                    .reversed());

            AdminApplicationArchiveResponse response = new AdminApplicationArchiveResponse();
            response.setItems(items);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admin application archive.", e);
        }
    }

    private AdminApplicationArchiveItemResponse toItem(ApplicationRecord application,
                                                       JobPosting job,
                                                       StudentProfile profile) {
        AdminApplicationArchiveItemResponse item = new AdminApplicationArchiveItemResponse();
        item.setApplicationId(application.getId());
        item.setJobId(application.getJobId());
        item.setStudentId(application.getStudentId());
        item.setStudentNo(application.getStudentNo());
        item.setStudentName(firstNonBlank(application.getStudentName(), profile == null ? "" : profile.getName()));
        item.setProgramme(profile == null ? "" : profile.getProgramme());
        item.setStatus(application.getStatus());
        item.setAppliedAt(application.getAppliedAt());
        item.setActive(application.isActive());
        item.setEvaluationNotes(blankToEmpty(application.getEvaluationNotes()));
        item.setDecisionFeedback(blankToEmpty(application.getDecisionFeedback()));
        if (job != null) {
            item.setModuleCode(job.getModuleCode());
            item.setTitle(job.getTitle());
            item.setTeacherName(job.getTeacherName());
            item.setDepartment(job.getDepartment());
        }
        return item;
    }

    private boolean matchesStatus(ApplicationRecord application, String statusFilter) {
        if ("all".equals(statusFilter)) {
            return true;
        }
        return normalizeFilter(application.getStatus()).equals(statusFilter);
    }

    private boolean matchesJob(ApplicationRecord application, String jobIdFilter) {
        if ("all".equals(jobIdFilter)) {
            return true;
        }
        return normalizeFilter(application.getJobId()).equals(jobIdFilter);
    }

    private boolean matchesTeacher(JobPosting job, String teacherFilter) {
        if ("all".equals(teacherFilter)) {
            return true;
        }
        if (job == null) {
            return false;
        }
        return normalizeFilter(job.getTeacherName()).equals(teacherFilter)
                || normalizeFilter(job.getTeacherId()).equals(teacherFilter);
    }

    private boolean matchesStudent(ApplicationRecord application, StudentProfile profile, String studentFilter) {
        if ("all".equals(studentFilter)) {
            return true;
        }
        String normalized = studentFilter.toLowerCase(Locale.ROOT);
        return contains(application.getStudentId(), normalized)
                || contains(application.getStudentNo(), normalized)
                || contains(application.getStudentName(), normalized)
                || contains(profile == null ? "" : profile.getName(), normalized);
    }

    private boolean contains(String value, String normalizedNeedle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedNeedle);
    }

    private String normalizeFilter(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() || "all".equals(normalized) ? "all" : normalized;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : blankToEmpty(second);
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
