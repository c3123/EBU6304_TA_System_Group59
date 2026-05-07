package com.ta.util;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Debug-session NDJSON logger (session fce7f7).
 */
public final class AgentDebugLog {
    private static final Gson GSON = new Gson();
    private AgentDebugLog() {
    }

    private static List<Path> logPathCandidates() {
        List<Path> out = new ArrayList<>();
        out.add(Path.of("D:\\大学\\大三\\大三下\\软件工程\\Group59\\EBU6304_TA_System_Group59-main\\debug-fce7f7.log"));
        String cat = System.getProperty("catalina.base");
        if (cat != null && !cat.isBlank()) {
            out.add(Path.of(cat, "logs", "debug-fce7f7.log"));
        }
        out.add(Path.of(System.getProperty("java.io.tmpdir"), "debug-fce7f7.log"));
        return out;
    }

    public static void log(String hypothesisId, String location, String message, Map<String, Object> data) {
        // #region agent log
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessionId", "fce7f7");
            payload.put("hypothesisId", hypothesisId);
            payload.put("location", location);
            payload.put("message", message);
            payload.put("data", data);
            payload.put("timestamp", System.currentTimeMillis());
            String line = GSON.toJson(payload) + System.lineSeparator();
            for (Path path : logPathCandidates()) {
                try {
                    Path parent = path.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.writeString(path, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    break;
                } catch (Throwable ignored) {
                    // try next
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        // #endregion
    }
}
