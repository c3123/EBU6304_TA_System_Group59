package com.ta.web.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.student.AttachmentDeleteRequest;
import com.ta.service.student.StudentBusinessException;
import com.ta.service.student.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StudentAttachmentDeleteServlet", urlPatterns = {"/api/student/attachments/*"})
public class StudentAttachmentDeleteServlet extends StudentBaseServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String studentUserId = getStudentUserId(req);
            if (studentUserId == null || studentUserId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Student login required.");
                return;
            }

            // Extract attachmentId from URL path: /api/student/attachments/{attachmentId}
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Attachment ID is required.");
                return;
            }

            String attachmentId = pathInfo.substring(1); // Remove leading slash

            studentService.deleteAttachment(getServletContext(), studentUserId, attachmentId);

            // Return success response with empty data
            writeSuccess(resp, new Object());
        } catch (StudentBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
