package com.ta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.model.StudentProfile;
import com.ta.model.SystemSettings;
import com.ta.model.User;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtility {
    private static final String DATA_ROOT = "/WEB-INF/data/";
    private static final String SYSTEM_SETTINGS_FILE = "system_settings.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type USER_LIST_TYPE = new TypeToken<List<User>>() { }.getType();
    private static final Type STUDENT_LIST_TYPE = new TypeToken<List<StudentProfile>>() { }.getType();
    private static final Type JOB_LIST_TYPE = new TypeToken<List<JobPosting>>() { }.getType();
    private static final Type APPLICATION_LIST_TYPE = new TypeToken<List<ApplicationRecord>>() { }.getType();
    private static final Type HIRING_HISTORY_LIST_TYPE = new TypeToken<List<HiringHistoryRecord>>() { }.getType();
    private static final Type NOTIFICATION_LIST_TYPE = new TypeToken<List<NotificationRecord>>() { }.getType();

    private JsonUtility() {
    }

    public static synchronized List<User> loadUsers(ServletContext context) throws IOException {
        return readList(context, "users.json", USER_LIST_TYPE);
    }

    public static synchronized void saveUsers(ServletContext context, List<User> users) throws IOException {
        writeJson(context, "users.json", users);
    }

    public static synchronized List<StudentProfile> loadStudents(ServletContext context) throws IOException {
        return readList(context, "students.json", STUDENT_LIST_TYPE);
    }

    public static synchronized void saveStudents(ServletContext context, List<StudentProfile> students) throws IOException {
        writeJson(context, "students.json", students);
    }

    public static synchronized List<JobPosting> loadJobs(ServletContext context) throws IOException {
        return readList(context, "jobs.json", JOB_LIST_TYPE);
    }

    public static synchronized void saveJobs(ServletContext context, List<JobPosting> jobs) throws IOException {
        writeJson(context, "jobs.json", jobs);
    }

    public static synchronized List<ApplicationRecord> loadApplications(ServletContext context) throws IOException {
        return readList(context, "applications.json", APPLICATION_LIST_TYPE);
    }

    public static synchronized void saveApplications(ServletContext context, List<ApplicationRecord> applications) throws IOException {
        writeJson(context, "applications.json", applications);
    }

    public static synchronized List<HiringHistoryRecord> loadHiringHistory(ServletContext context) throws IOException {
        return readList(context, "hiring_history.json", HIRING_HISTORY_LIST_TYPE);
    }

    public static synchronized void saveHiringHistory(ServletContext context, List<HiringHistoryRecord> records) throws IOException {
        writeJson(context, "hiring_history.json", records);
    }

    public static synchronized List<NotificationRecord> loadNotifications(ServletContext context) throws IOException {
        return readList(context, "notifications.json", NOTIFICATION_LIST_TYPE);
    }

    public static synchronized void saveNotifications(ServletContext context, List<NotificationRecord> records) throws IOException {
        writeJson(context, "notifications.json", records);
    }

    public static synchronized SystemSettings loadSystemSettings(ServletContext context) throws IOException {
        File file = resolveDataFile(context, SYSTEM_SETTINGS_FILE);
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            SystemSettings settings = GSON.fromJson(reader, SystemSettings.class);
            return normalizeSettings(settings);
        }
    }

    public static synchronized void saveSystemSettings(ServletContext context, SystemSettings settings) throws IOException {
        writeJson(context, SYSTEM_SETTINGS_FILE, normalizeSettings(settings));
    }

    private static <T> List<T> readList(ServletContext context, String fileName, Type listType) throws IOException {
        File file = resolveDataFile(context, fileName);
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<T> data = GSON.fromJson(reader, listType);
            return data == null ? new ArrayList<>() : data;
        }
    }

    private static void writeJson(ServletContext context, String fileName, Object data) throws IOException {
        File file = resolveDataFile(context, fileName);
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        }
    }

    private static File resolveDataFile(ServletContext context, String fileName) throws IOException {
        String resourcePath = DATA_ROOT + fileName;
        String realPath = context.getRealPath(resourcePath);
        if (realPath == null) {
            String contextRoot = context.getRealPath("/");
            if (contextRoot != null) {
                realPath = contextRoot + DATA_ROOT.substring(1) + fileName;
            }
        }

        if (realPath == null) {
            throw new IOException("Unable to resolve data file path for " + fileName);
        }

        File dataFile = new File(realPath);
        File dataDir = dataFile.getParentFile();
        if (dataDir != null && !dataDir.exists()) {
            Files.createDirectories(dataDir.toPath());
        }

        if (!dataFile.exists()) {
            Files.writeString(dataFile.toPath(), defaultJsonFor(fileName), StandardCharsets.UTF_8);
        }

        return dataFile;
    }

    private static String defaultJsonFor(String fileName) {
        if (SYSTEM_SETTINGS_FILE.equals(fileName)) {
            return GSON.toJson(defaultSystemSettings());
        }
        return "[]";
    }

    private static SystemSettings normalizeSettings(SystemSettings settings) {
        if (settings == null) {
            return defaultSystemSettings();
        }
        if (settings.getWorkloadThresholdHours() == null || settings.getWorkloadThresholdHours() <= 0) {
            settings.setWorkloadThresholdHours(20);
        }
        if (settings.getUpdatedAt() == null || settings.getUpdatedAt().isBlank()) {
            settings.setUpdatedAt(Instant.now().toString());
        }
        return settings;
    }

    private static SystemSettings defaultSystemSettings() {
        SystemSettings settings = new SystemSettings();
        settings.setWorkloadThresholdHours(20);
        settings.setUpdatedAt(Instant.now().toString());
        return settings;
    }
}
