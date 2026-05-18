package com.ta.web.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.student.AiAdvisorRequest;
import com.ta.service.student.AiAdvisorService;
import com.ta.service.student.StudentBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StudentAiAdvisorServlet", urlPatterns = {"/api/student/ai-advisor"})
public class StudentAiAdvisorServlet extends StudentBaseServlet {
    private final AiAdvisorService aiAdvisorService = new AiAdvisorService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String studentUserId = getStudentUserId(req);
            if (studentUserId == null || studentUserId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Student login required.");
                return;
            }

            AiAdvisorRequest request = readJson(req, AiAdvisorRequest.class);
            if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "question is required.");
                return;
            }

            writeSuccess(resp, aiAdvisorService.advise(getServletContext(), studentUserId, request.getQuestion().trim()));
        } catch (StudentBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "AI advisor request failed.");
        }
    }
}
