package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoHiredStudentDismissRequest;
import com.ta.service.mo.MoApplicationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoHiredStudentDismissServlet", urlPatterns = {"/api/mo/hired-students/dismiss"})
public class MoHiredStudentDismissServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }
            MoHiredStudentDismissRequest request = readJson(req, MoHiredStudentDismissRequest.class);
            if (request == null || request.getApplicationId() == null || request.getApplicationId().isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "applicationId is required.");
                return;
            }
            writeSuccess(resp, moApplicationService.dismissHiredStudent(
                    getServletContext(),
                    moId,
                    request.getApplicationId(),
                    request.getReason()));
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
