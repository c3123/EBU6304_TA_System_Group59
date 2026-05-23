package com.ta.util;

import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return currentHiredHoursElsewhere(studentId, excludeApplicationId, applications, jobById, null);
    }

    public static int currentHiredHoursElsewhere(String studentId,
                                                 String excludeApplicationId,
                                                 List<ApplicationRecord> applications,
                                                 Map<String, JobPosting> jobById,
                                                 List<HiringHistoryRecord> history) {
        if (studentId == null || studentId.isBlank() || applications == null) {
            return 0;
        }
        Set<String> historyHiredIds = collectHistoryHiredIds(history);
        int total = 0;
        Set<String> countedApplicationIds = new HashSet<>();
        for (ApplicationRecord application : applications) {
            if (application == null || !studentId.equals(application.getStudentId())) {
                continue;
            }
            if (excludeApplicationId != null && !excludeApplicationId.isBlank()
                    && excludeApplicationId.equals(application.getId())) {
                continue;
            }
            boolean hired = "hired".equalsIgnoreCase(trim(application.getStatus()))
                    || historyHiredIds.contains(application.getId());
            if (!hired || !countedApplicationIds.add(application.getId())) {
                continue;
            }
            total += JobHoursUtil.resolveWeeklyHours(jobById.get(application.getJobId()));
        }
        return total;
    }

    private static Set<String> collectHistoryHiredIds(List<HiringHistoryRecord> history) {
        Set<String> historyHiredIds = new HashSet<>();
        if (history == null) {
            return historyHiredIds;
        }
        for (HiringHistoryRecord record : history) {
            if (record != null && record.getHiredApplicationIds() != null) {
                historyHiredIds.addAll(record.getHiredApplicationIds());
            }
        }
        return historyHiredIds;
    }

    public static int projectedIfHired(int currentElsewhereHours, JobPosting job) {
        return currentElsewhereHours + JobHoursUtil.resolveWeeklyHours(job);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
