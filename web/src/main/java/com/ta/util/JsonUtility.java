package com.ta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.model.User;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtility {
    private static final String DATA_ROOT = "/WEB-INF/data/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type USER_LIST_TYPE = new TypeToken<List<User>>() { }.getType();
    private static final Type STUDENT_LIST_TYPE = new TypeToken<List<StudentProfile>>() { }.getType();
    private static final Type JOB_LIST_TYPE = new TypeToken<List<JobPosting>>() { }.getType();

    private JsonUtility() {
    }

    public static synchronized List<User> loadUsers(ServletContext context) throws IOException {
        return readList(context, "users.json", USER_LIST_TYPE);
    }

    public static synchronized void saveUsers(ServletContext context, List<User> users) throws IOException {
        writeList(context, "users.json", users);
    }

    public static synchronized List<StudentProfile> loadStudents(ServletContext context) throws IOException {
        return readList(context, "students.json", STUDENT_LIST_TYPE);
    }

    public static synchronized void saveStudents(ServletContext context, List<StudentProfile> students) throws IOException {
        writeList(context, "students.json", students);
    }

    public static synchronized List<JobPosting> loadJobs(ServletContext context) throws IOException {
        return readList(context, "jobs.json", JOB_LIST_TYPE);
    }

    public static synchronized void saveJobs(ServletContext context, List<JobPosting> jobs) throws IOException {
        writeList(context, "jobs.json", jobs);
    }

    private static <T> List<T> readList(ServletContext context, String fileName, Type listType) throws IOException {
        File file = resolveDataFile(context, fileName);
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<T> data = GSON.fromJson(reader, listType);
            return data == null ? new ArrayList<>() : data;
        }
    }

    private static void writeList(ServletContext context, String fileName, Object data) throws IOException {
        File file = resolveDataFile(context, fileName);
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        }
    }

    private static File resolveDataFile(ServletContext context, String fileName) throws IOException {
        String realPath = context.getRealPath(DATA_ROOT + fileName);
        File targetFile;

        if (realPath != null) {
            targetFile = new File(realPath);
        } else {
            File fallbackDir = new File(System.getProperty("user.home"), ".ta-recruitment-data");
            targetFile = new File(fallbackDir, fileName);
        }

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }

        if (!targetFile.exists()) {
            try (InputStream inputStream = context.getResourceAsStream(DATA_ROOT + fileName)) {
                if (inputStream != null) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.writeString(targetFile.toPath(), "[]", StandardCharsets.UTF_8);
                }
            }
        }

        return targetFile;
    }
}
