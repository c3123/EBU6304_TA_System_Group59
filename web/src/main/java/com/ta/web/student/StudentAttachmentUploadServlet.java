package com.ta.web.student;

import com.ta.constant.ErrorCodes;
import com.ta.model.Attachment;
import com.ta.service.student.StudentBusinessException;
import com.ta.service.student.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;

@WebServlet(name = "StudentAttachmentUploadServlet", urlPatterns = {"/api/student/attachments"})
@MultipartConfig(maxFileSize = 50 * 1024 * 1024, maxRequestSize = 50 * 1024 * 1024)
public class StudentAttachmentUploadServlet extends StudentBaseServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String studentUserId = getStudentUserId(req);
            if (studentUserId == null || studentUserId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Student login required.");
                return;
            }

            Part filePart = req.getPart("file");
            String label = req.getParameter("label");

            if (filePart == null || filePart.getSize() == 0) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "No file provided.");
                return;
            }

            if (label == null || label.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Label is required.");
                return;
            }

            String fileName = getFileName(filePart);
            Attachment attachment = studentService.uploadAttachment(
                    getServletContext(),
                    studentUserId,
                    filePart.getInputStream(),
                    filePart.getSize(),
                    fileName,
                    label.trim()
            );

            writeSuccess(resp, attachment);
        } catch (StudentBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).replaceAll("\"", "");
            }
        }
        return "file_" + System.currentTimeMillis();
    }
}
