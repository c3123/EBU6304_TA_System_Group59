package com.ta.web.student;

import com.ta.model.Attachment;
import com.ta.model.StudentProfile;
import com.ta.util.FileStorageUtil;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "StudentAttachmentDownloadServlet", urlPatterns = {"/api/attachments/*"})
public class StudentAttachmentDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] parts = pathInfo.substring(1).split("/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String studentKey = parts[0];
        String attachmentId = parts[1];

        List<StudentProfile> profiles = JsonUtility.loadStudents(getServletContext());
        StudentProfile owner = profiles.stream()
                .filter(p -> matchesStudentKey(p, studentKey))
                .findFirst()
                .orElse(null);

        if (owner == null || owner.getAttachments() == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Attachment attachment = owner.getAttachments().stream()
                .filter(a -> attachmentId.equals(a.getId()))
                .findFirst()
                .orElse(null);

        if (attachment == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file;
        try {
            file = FileStorageUtil.getFile(getServletContext(), owner.getStudentId(), attachmentId);
        } catch (IOException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String extension = getFileExtension(file.getName());
        String mimeType = FileStorageUtil.getMimeType(extension);
        String downloadName = attachment.getFileName() != null && !attachment.getFileName().isBlank()
                ? attachment.getFileName()
                : file.getName();
        String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");

        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + sanitizeHeaderFileName(downloadName) + "\"; filename*=UTF-8''" + encodedName);

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    private boolean matchesStudentKey(StudentProfile profile, String studentKey) {
        if (profile == null || studentKey == null) {
            return false;
        }
        return studentKey.equals(profile.getStudentId()) || studentKey.equals(profile.getUserId());
    }

    private String sanitizeHeaderFileName(String fileName) {
        return fileName == null ? "download" : fileName.replace("\\", "_").replace("/", "_").replace("\r", "_").replace("\n", "_").replace("\"", "'");
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}

