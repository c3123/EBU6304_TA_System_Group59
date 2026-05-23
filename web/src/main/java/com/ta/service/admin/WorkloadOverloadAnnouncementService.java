package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminWorkloadOverloadNotifyResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.SystemSettings;
import com.ta.util.JobHoursUtil;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sends a system announcement when a student's weekly TA hours newly reach the overload threshold.
 */
public class WorkloadOverloadAnnouncementService {

    private static final int DEFAULT_THRESHOLD_HOURS = 20;
    private final AdminAnnouncementService announcementService = new AdminAnnouncementService();

    public int calculateWeeklyHours(ServletContext context, String studentId) throws IOException {
        if (studentId == null || studentId.isBlank()) {
            return 0;
        }
        List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
        List<JobPosting> jobs = JsonUtility.loadJobs(context);
        Map<String, JobPosting> jobById = new HashMap<>();
        for (JobPosting job : jobs) {
            if (job.getId() != null) {
                jobById.put(job.getId(), job);
            }
        }

        int total = 0;
        for (ApplicationRecord application : applications) {
            if (!studentId.equals(application.getStudentId())) {
                continue;
            }
            if (!"hired".equalsIgnoreCase(trim(application.getStatus()))) {
                continue;
            }
            JobPosting job = jobById.get(application.getJobId());
            total += JobHoursUtil.resolveWeeklyHours(job);
        }
        return total;
    }

    /**
     * @param weeklyHoursBefore hire action; pass hours from {@link #calculateWeeklyHours} before status becomes hired
     */
    public void notifyIfNewlyOverloaded(ServletContext context, String studentId, int weeklyHoursBefore) throws IOException {
        if (studentId == null || studentId.isBlank()) {
            return;
        }
        int threshold = resolveThreshold(context);
        int weeklyHoursAfter = calculateWeeklyHours(context, studentId);
        if (weeklyHoursBefore >= threshold || weeklyHoursAfter < threshold) {
            return;
        }

        sendOverloadReminder(context, studentId, weeklyHoursAfter, threshold);
    }

    /**
     * Admin-triggered bulk reminder to every student currently at or above the overload threshold.
     */
    public AdminWorkloadOverloadNotifyResponse notifyAllCurrentlyOverloaded(ServletContext context) throws IOException {
        int threshold = resolveThreshold(context);
        Set<String> studentIds = collectStudentsWithHiredApplications(context);
        List<String> notified = new ArrayList<>();

        for (String studentId : studentIds) {
            int weeklyHours = calculateWeeklyHours(context, studentId);
            if (weeklyHours < threshold) {
                continue;
            }
            sendOverloadReminder(context, studentId, weeklyHours, threshold);
            notified.add(studentId);
        }

        if (notified.isEmpty()) {
            throw new AdminBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "No students are currently at overload level.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        AdminWorkloadOverloadNotifyResponse response = new AdminWorkloadOverloadNotifyResponse();
        response.setNotifiedCount(notified.size());
        response.setThresholdHours(threshold);
        response.setStudentIds(notified);
        return response;
    }

    private void sendOverloadReminder(ServletContext context,
                                      String studentId,
                                      int weeklyHours,
                                      int threshold) throws IOException {
        String studentName = resolveStudentName(context, studentId);
        String title = "Workload overload reminder";
        String body = String.format(
                Locale.ENGLISH,
                "Hi %s, this is a reminder that your total assigned TA workload is %d hours per week, "
                        + "which meets or exceeds the platform limit of %d hours. "
                        + "Please review your assigned roles or contact your module organiser if you need to adjust your schedule.",
                studentName,
                weeklyHours,
                threshold
        );
        announcementService.notifyStudent(context, studentId, title, body);
    }

    private Set<String> collectStudentsWithHiredApplications(ServletContext context) throws IOException {
        Set<String> studentIds = new LinkedHashSet<>();
        for (ApplicationRecord application : JsonUtility.loadApplications(context)) {
            if (application.getStudentId() == null || application.getStudentId().isBlank()) {
                continue;
            }
            if (!"hired".equalsIgnoreCase(trim(application.getStatus()))) {
                continue;
            }
            studentIds.add(application.getStudentId());
        }
        return studentIds;
    }

    private int resolveThreshold(ServletContext context) throws IOException {
        SystemSettings settings = JsonUtility.loadSystemSettings(context);
        Integer threshold = settings == null ? null : settings.getWorkloadThresholdHours();
        if (threshold == null || threshold <= 0) {
            return DEFAULT_THRESHOLD_HOURS;
        }
        return threshold;
    }

    private String resolveStudentName(ServletContext context, String studentId) throws IOException {
        return JsonUtility.loadUsers(context).stream()
                .filter(user -> studentId.equals(user.getId()))
                .map(user -> trim(user.getName()))
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElse("Student");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
