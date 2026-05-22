package com.ta.util;

import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class FileStorageUtil {
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "xlsx", "jpg", "png");
    private static final String UPLOADS_BASE_DIR = "uploads/students";

    public static String getStudentUploadDir(ServletContext context, String studentId) {
        String baseDir = getBaseUploadDir(context);
        return baseDir + File.separator + studentId + File.separator + "profile-attachments";
    }

    private static String getBaseUploadDir(ServletContext context) {
        String userHome = System.getProperty("user.home");
        String fallbackDir = userHome + File.separator + ".ta-recruitment-data" + File.separator + UPLOADS_BASE_DIR;
        new File(fallbackDir).mkdirs();
        return fallbackDir;
    }

    private static String getLegacyBaseUploadDir(ServletContext context) {
        try {
            String webinfPath = context != null
                    ? context.getRealPath("/WEB-INF/" + UPLOADS_BASE_DIR)
                    : null;
            if (webinfPath == null || webinfPath.isBlank()) {
                return null;
            }
            return webinfPath;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getLegacyStudentUploadDir(ServletContext context, String studentId) {
        String legacyBase = getLegacyBaseUploadDir(context);
        return legacyBase == null ? null : legacyBase + File.separator + studentId + File.separator + "profile-attachments";
    }

    public static void validateFile(String fileName, long fileSize) throws IllegalArgumentException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0");
        }

        if (fileSize > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException("Single file exceeds 50MB limit");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }

    public static void validateTotalSize(ServletContext context, String studentId, long additionalFileSize) throws IllegalArgumentException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File dirFile = new File(uploadDir);

        if (!dirFile.exists()) {
            return;
        }

        long totalSize = 0;
        for (File file : dirFile.listFiles() != null ? dirFile.listFiles() : new File[0]) {
            if (file.isFile()) {
                totalSize += file.length();
            }
        }

        if (totalSize + additionalFileSize > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException("Total file size exceeds 50MB limit");
        }
    }

    public static String saveFile(ServletContext context, String studentId, InputStream inputStream, String originalFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File dirFile = new File(uploadDir);
        dirFile.mkdirs();

        String extension = getFileExtension(originalFileName);
        String storageFileName = UUID.randomUUID().toString() + "." + extension;
        File targetFile = new File(uploadDir, storageFileName);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return storageFileName;
    }

    public static void deleteFile(ServletContext context, String studentId, String storageFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File file = new File(uploadDir, storageFileName);
        if (!isSafeFile(file, uploadDir)) {
            throw new IOException("Invalid file path");
        }

        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + storageFileName);
            }
            return;
        }

        String legacyUploadDir = getLegacyStudentUploadDir(context, studentId);
        if (legacyUploadDir != null) {
            File legacyFile = new File(legacyUploadDir, storageFileName);
            if (!isSafeFile(legacyFile, legacyUploadDir)) {
                throw new IOException("Invalid file path");
            }
            if (legacyFile.exists() && legacyFile.isFile() && !legacyFile.delete()) {
                throw new IOException("Failed to delete file: " + storageFileName);
            }
        }
    }

    public static File getFile(ServletContext context, String studentId, String storageFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File file = new File(uploadDir, storageFileName);
        if (isSafeFile(file, uploadDir) && file.exists() && file.isFile()) {
            return file;
        }

        String legacyUploadDir = getLegacyStudentUploadDir(context, studentId);
        if (legacyUploadDir != null) {
            File legacyFile = new File(legacyUploadDir, storageFileName);
            if (isSafeFile(legacyFile, legacyUploadDir) && legacyFile.exists() && legacyFile.isFile()) {
                return legacyFile;
            }
        }

        throw new IOException("File not found: " + storageFileName);
    }

    private static boolean isSafeFile(File file, String uploadDir) throws IOException {
        return file.getCanonicalPath().startsWith(new File(uploadDir).getCanonicalPath());
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    public static String getMimeType(String extension) {
        String ext = extension.toLowerCase();
        if ("pdf".equals(ext)) {
            return "application/pdf";
        } else if ("docx".equals(ext)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if ("xlsx".equals(ext)) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return "image/jpeg";
        } else if ("png".equals(ext)) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }

    public static String getCurrentTimestamp() {
        return Instant.now().toString();
    }
}
