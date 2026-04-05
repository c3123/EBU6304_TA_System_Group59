package com.ta.web.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.student.StudentApplicationCreateRequest;
import com.ta.service.student.StudentBusinessException;
import com.ta.service.student.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StudentApplicationsServlet", urlPatterns = {"/api/student/applications"})
public class StudentApplicationsServlet extends StudentBaseServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String studentUserId = getStudentUserId(req);
            if (studentUserId == null || studentUserId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Student login required.");
                return;
            }

            writeSuccess(resp, studentService.listMyApplications(getServletContext(), studentUserId));
        } catch (StudentBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String studentUserId = getStudentUserId(req);
            if (studentUserId == null || studentUserId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Student login required.");
                return;
            }

            StudentApplicationCreateRequest request = readJson(req, StudentApplicationCreateRequest.class);
            writeSuccess(resp, studentService.applyForJob(getServletContext(), studentUserId, request));
        } catch (StudentBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
