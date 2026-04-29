package com.ta.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * UTC timestamps stored as ISO-8601 strings for JSON and UI.
 */
public final class IsoTime {

    private IsoTime() {
    }

    /**
     * Current instant as {@code yyyy-MM-dd'T'HH:mm:ss'Z'} (no fractional seconds).
     * {@link Instant#toString()} may include nanoseconds, which is noisy in the UI.
     */
    public static String utcNowSeconds() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
