package com.ta.util;

import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class JobDeadlineUtil {
    public static final String STATUS_OVERDUE = "overdue";

    private JobDeadlineUtil() {
    }

    public static boolean isJobExpired(JobPosting job) {
        if (job == null || isBlank(job.getDeadline())) {
            return false;
        }
        LocalDateTime deadline = parseDeadline(job.getDeadline());
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public static boolean isAcceptingApplications(JobPosting job) {
        return job != null
                && !isJobExpired(job)
                && !Boolean.TRUE.equals(job.getRecruitmentClosed())
                && !Boolean.TRUE.equals(job.getWithdrawn())
                && ("open".equalsIgnoreCase(safe(job.getStatus()))
                    || "published".equalsIgnoreCase(safe(job.getStatus())));
    }

    public static boolean syncOverdueApplications(List<ApplicationRecord> applications, List<JobPosting> jobs) {
        if (applications == null || applications.isEmpty() || jobs == null || jobs.isEmpty()) {
            return false;
        }
        Map<String, JobPosting> jobById = jobs.stream()
                .filter(job -> !isBlank(job.getId()))
                .collect(Collectors.toMap(JobPosting::getId, Function.identity(), (a, b) -> a));

        boolean changed = false;
        for (ApplicationRecord application : applications) {
            if (application == null || !application.isActive() || !isUnderReview(application.getStatus())) {
                continue;
            }
            JobPosting job = jobById.get(application.getJobId());
            if (isJobExpired(job)) {
                application.setStatus(STATUS_OVERDUE);
                changed = true;
            }
        }
        return changed;
    }

    public static boolean isOverdue(String status) {
        return STATUS_OVERDUE.equalsIgnoreCase(safe(status));
    }

    public static boolean isUnderReview(String status) {
        String normalized = safe(status).toLowerCase();
        return "pending".equals(normalized) || "viewed".equals(normalized);
    }

    private static LocalDateTime parseDeadline(String raw) {
        String value = safe(raw);
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // try other supported persisted formats
        }
        try {
            return Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // try date-only
        }
        try {
            return LocalDate.parse(value).atTime(LocalTime.MAX);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
