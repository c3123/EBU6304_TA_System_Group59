package com.ta.util;

/**
 * Classifies weekly TA hours into low / normal / warning / overload using
 * percentage cutoffs relative to the configured overload threshold.
 */
public final class WorkloadLevelUtil {

    public static final int DEFAULT_THRESHOLD_HOURS = 20;
    public static final int DEFAULT_NORMAL_PERCENT = 50;
    public static final int DEFAULT_WARNING_PERCENT = 75;

    private WorkloadLevelUtil() {
    }

    public static int resolveThresholdHours(Integer thresholdHours) {
        if (thresholdHours == null || thresholdHours <= 0) {
            return DEFAULT_THRESHOLD_HOURS;
        }
        return thresholdHours;
    }

    public static int resolveNormalPercent(Integer normalPercent) {
        int value = normalPercent == null ? DEFAULT_NORMAL_PERCENT : normalPercent;
        return clampPercent(value, DEFAULT_NORMAL_PERCENT);
    }

    public static int resolveWarningPercent(Integer normalPercent, Integer warningPercent) {
        int normal = resolveNormalPercent(normalPercent);
        int value = warningPercent == null ? DEFAULT_WARNING_PERCENT : warningPercent;
        value = clampPercent(value, DEFAULT_WARNING_PERCENT);
        if (value <= normal) {
            value = Math.min(99, normal + 1);
        }
        return value;
    }

    public static int hoursAtPercent(int thresholdHours, int percent) {
        int threshold = resolveThresholdHours(thresholdHours);
        return Math.max(1, (int) Math.ceil(threshold * percent / 100.0));
    }

    public static WorkloadLevel classify(int weeklyHours,
                                         Integer thresholdHours,
                                         Integer normalPercent,
                                         Integer warningPercent) {
        int threshold = resolveThresholdHours(thresholdHours);
        int normalPct = resolveNormalPercent(normalPercent);
        int warningPct = resolveWarningPercent(normalPct, warningPercent);
        int normalMinHours = hoursAtPercent(threshold, normalPct);
        int warningMinHours = hoursAtPercent(threshold, warningPct);

        if (weeklyHours >= threshold) {
            return new WorkloadLevel("overload", "Overload", true);
        }
        if (weeklyHours >= warningMinHours) {
            return new WorkloadLevel("warning", "Warning", true);
        }
        if (weeklyHours >= normalMinHours) {
            return new WorkloadLevel("normal", "Normal", false);
        }
        return new WorkloadLevel("low", "Low", false);
    }

    private static int clampPercent(int value, int fallback) {
        if (value < 1 || value > 99) {
            return fallback;
        }
        return value;
    }

    public static final class WorkloadLevel {
        private final String key;
        private final String label;
        private final boolean warning;

        public WorkloadLevel(String key, String label, boolean warning) {
            this.key = key;
            this.label = label;
            this.warning = warning;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public boolean isWarning() {
            return warning;
        }
    }
}
