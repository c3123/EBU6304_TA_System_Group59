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
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "xlsx", "jpg", "png");
    private static final String UPLOADS_BASE_DIR = "uploads/students";

    /**
     * 获取学生的上传目录
     */
    public static String getStudentUploadDir(ServletContext context, String studentId) {
        String baseDir = getBaseUploadDir(context);
        return baseDir + File.separator + studentId + File.separator + "profile-attachments";
    }

    /**
     * 获取上传基础目录（考虑 realPath 和 fallback）
     */
    private static String getBaseUploadDir(ServletContext context) {
        String userHome = System.getProperty("user.home");
        String fallbackDir = userHome + File.separator + ".ta-recruitment-data" + File.separator + UPLOADS_BASE_DIR;

        // 优先写入 web 应用路径下的 WEB-INF/uploads/students
        try {
            String webinfPath = context != null
                    ? context.getRealPath("/WEB-INF/" + UPLOADS_BASE_DIR)
                    : null;
            if (webinfPath == null || webinfPath.isBlank()) {
                throw new IOException("Real path not available");
            }
            File webinfDir = new File(webinfPath);
            if (webinfDir.mkdirs() || webinfDir.exists()) {
                return webinfPath;
            }
        } catch (Exception e) {
            // fallback to user-home storage
        }

        new File(fallbackDir).mkdirs();
        return fallbackDir;
    }

    /**
     * 校验文件大小和类型
     */
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

    /**
     * 校验总大小（学生所有文件总和）
     */
    public static void validateTotalSize(ServletContext context, String studentId, long additionalFileSize) throws IllegalArgumentException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File dirFile = new File(uploadDir);
        
        if (!dirFile.exists()) {
            return; // 目录不存在，总大小为 0
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

    /**
     * 保存文件
     */
    public static String saveFile(ServletContext context, String studentId, InputStream inputStream, String originalFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File dirFile = new File(uploadDir);
        dirFile.mkdirs();

        // 生成唯一的文件 ID，保留原始扩展名
        String extension = getFileExtension(originalFileName);
        String storageFileName = UUID.randomUUID().toString() + "." + extension;
        File targetFile = new File(uploadDir, storageFileName);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return storageFileName;
    }

    /**
     * 删除文件
     */
    public static void deleteFile(ServletContext context, String studentId, String storageFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File file = new File(uploadDir, storageFileName);
        
        // 安全检查：确保文件在允许的目录内
        if (!file.getCanonicalPath().startsWith(new File(uploadDir).getCanonicalPath())) {
            throw new IOException("Invalid file path");
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + storageFileName);
        }
    }

    /**
     * 读取文件（用于下载）
     */
    public static File getFile(ServletContext context, String studentId, String storageFileName) throws IOException {
        String uploadDir = getStudentUploadDir(context, studentId);
        File file = new File(uploadDir, storageFileName);
        
        // 安全检查
        if (!file.getCanonicalPath().startsWith(new File(uploadDir).getCanonicalPath())) {
            throw new IOException("Invalid file path");
        }

        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found: " + storageFileName);
        }

        return file;
    }

    /**
     * 从完整文件名中提取扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * 获取文件的 MIME 类型
     */
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

    /**
     * 获取当前 ISO-8601 时间戳
     */
    public static String getCurrentTimestamp() {
        return Instant.now().toString();
    }
}
