package com.ta.util;

import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;

import java.util.List;
import java.util.Map;

public final class StudentWorkloadUtil {
    private StudentWorkloadUtil() {
    }

  /**
   * Weekly hours from other hired applications for the same student, excluding one application id.
   */
    public static int currentHiredHoursElsewhere(String studentId,
                                                 String excludeApplicationId,
                                                 List<ApplicationRecord> applications,
                                                 Map<String, JobPosting> jobById) {
        if (studentId == null || studentId.isBlank() || applications == null) {
            return 0;
        }
        int total = 0;
        for (ApplicationRecord application : applications) {
            if (application == null || !studentId.equals(application.getStudentId())) {
                continue;
            }
            if (excludeApplicationId != null && excludeApplicationId.equals(application.getId())) {
                continue;
            }
            if (!"hired".equalsIgnoreCase(trim(application.getStatus()))) {
                continue;
            }
            total += JobHoursUtil.resolveWeeklyHours(jobById.get(application.getJobId()));
        }
        return total;
    }

    public static int projectedIfHired(int currentElsewhereHours, JobPosting job) {
        return currentElsewhereHours + JobHoursUtil.resolveWeeklyHours(job);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
