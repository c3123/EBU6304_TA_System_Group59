package com.ta.util;

import com.ta.model.JobPosting;

public final class JobHoursUtil {
    private JobHoursUtil() {
    }

    public static int resolveWeeklyHours(JobPosting job) {
        if (job == null) {
            return 0;
        }
        if (job.getHours() > 0) {
            return job.getHours();
        }

        Integer min = job.getHourMin();
        Integer max = job.getHourMax();
        if (min != null && max != null) {
            return (int) Math.round((min + max) / 2.0);
        }
        return 0;
    }
}
