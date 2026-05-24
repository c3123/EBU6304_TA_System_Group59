package com.ta.util;

import com.ta.constant.ErrorCodes;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Shared recruitment capacity rules: planned count ({@link JobPosting#getPositions()})
 * vs hired applications, and auto-close when full.
 */
public final class JobRecruitmentUtil {

    private JobRecruitmentUtil() {
    }

    public static int countHired(ServletContext context, String jobId) throws IOException {
        if (jobId == null || jobId.isBlank()) {
            return 0;
        }
        return (int) JsonUtility.loadApplications(context).stream()
                .filter(ApplicationRecord::isActive)
                .filter(a -> jobId.equals(a.getJobId()))
                .filter(a -> "hired".equalsIgnoreCase(normalizeStatus(a.getStatus())))
                .count();
    }

    public static int countActiveHired(List<ApplicationRecord> applications, String jobId) {
        if (jobId == null || jobId.isBlank() || applications == null) {
            return 0;
        }
        return (int) applications.stream()
                .filter(ApplicationRecord::isActive)
                .filter(a -> jobId.equals(a.getJobId()))
                .filter(a -> "hired".equalsIgnoreCase(normalizeStatus(a.getStatus())))
                .count();
    }

    public static boolean isRecruitmentFull(JobPosting job, int hiredCount) {
        if (job == null) {
            return false;
        }
        int positions = job.getPositions();
        return positions > 0 && hiredCount >= positions;
    }

    public static boolean isRecruitmentFull(ServletContext context, JobPosting job) throws IOException {
        if (job == null || job.getId() == null) {
            return false;
        }
        return isRecruitmentFull(job, countHired(context, job.getId()));
    }

    public static void assertCanHire(JobPosting job, int currentHired, int additionalHires) {
        if (job == null || additionalHires <= 0) {
            return;
        }
        int positions = job.getPositions();
        if (positions <= 0) {
            return;
        }
        if (currentHired + additionalHires > positions) {
            int remaining = Math.max(0, positions - currentHired);
            throw new MoBusinessException(
                    ErrorCodes.JOB_RECRUITMENT_CLOSED,
                    remaining == 0
                            ? "This job has reached its position limit (" + positions + "). Cannot hire more applicants."
                            : "This job allows " + positions + " hire(s); only " + remaining + " slot(s) remaining.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }
    }

    public static void assertCanHire(ServletContext context, JobPosting job, int additionalHires) throws IOException {
        if (job == null || job.getId() == null) {
            return;
        }
        assertCanHire(job, countHired(context, job.getId()), additionalHires);
    }

    /**
     * Closes recruitment when hired count has reached planned positions.
     *
     * @return true if the job was closed by this call
     */
    public static boolean closeRecruitmentIfFull(ServletContext context, List<JobPosting> jobs, JobPosting job)
            throws IOException {
        if (job == null) {
            return false;
        }
        int hiredCount = countHired(context, job.getId());
        if (!isRecruitmentFull(job, hiredCount)) {
            return false;
        }
        String now = Instant.now().toString();
        job.setRecruitmentClosed(true);
        job.setClosedAt(now);
        if (!JobDeadlineUtil.isJobExpired(job)) {
            job.setStatus("open");
            job.setPublished(true);
        }
        job.setUpdatedAt(now);
        JsonUtility.saveJobs(context, jobs);
        return true;
    }

    public static boolean reopenRecruitmentIfCapacityAvailable(List<JobPosting> jobs,
                                                               JobPosting job,
                                                               List<ApplicationRecord> applications) {
        if (job == null || JobDeadlineUtil.isJobExpired(job) || Boolean.TRUE.equals(job.getWithdrawn())) {
            return false;
        }
        if (!Boolean.TRUE.equals(job.getRecruitmentClosed())) {
            return false;
        }
        if (isRecruitmentFull(job, countActiveHired(applications, job.getId()))) {
            return false;
        }
        job.setRecruitmentClosed(false);
        job.setClosedAt(null);
        job.setStatus("open");
        job.setPublished(true);
        job.setUpdatedAt(Instant.now().toString());
        return true;
    }

    private static String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase();
    }
}
