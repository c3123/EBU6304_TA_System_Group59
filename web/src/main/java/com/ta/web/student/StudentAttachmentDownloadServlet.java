package com.ta.web.student;

import com.ta.constant.ErrorCodes;
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
import java.util.List;

@WebServlet(name = "StudentAttachmentDownloadServlet", urlPatterns = {"/api/attachments/*"})
public class StudentAttachmentDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Extract path: /api/attachments/{studentId}/{attachmentId}/download
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

            String studentId = parts[0];
            String attachmentId = parts[1];

            // Validate that the student exists and has this attachment
            List<StudentProfile> profiles = JsonUtility.loadStudents(getServletContext());
            boolean hasAttachment = profiles.stream()
                    .filter(p -> studentId.equals(p.getStudentId()))
                    .flatMap(p -> p.getAttachments() != null ? p.getAttachments().stream() : java.util.stream.Stream.empty())
                    .anyMatch(a -> attachmentId.equals(a.getId()));

            if (!hasAttachment) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Get the file
            File file = FileStorageUtil.getFile(getServletContext(), studentId, attachmentId);

            // Set response headers
            String extension = getFileExtension(file.getName());
            String mimeType = FileStorageUtil.getMimeType(extension);
            resp.setContentType(mimeType);
            resp.setContentLength((int) file.length());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            // Write file to response
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
