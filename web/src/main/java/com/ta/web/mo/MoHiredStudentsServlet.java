package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoApplicationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoHiredStudentsServlet", urlPatterns = {"/api/mo/hired-students"})
public class MoHiredStudentsServlet extends MoBaseServlet {
    private final MoApplicationService moApplicationService = new MoApplicationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }
            boolean includeHistory = Boolean.parseBoolean(req.getParameter("includeHistory"));
            writeSuccess(resp, moApplicationService.listHiredStudents(
                    getServletContext(),
                    moId,
                    req.getParameter("jobId"),
                    includeHistory));
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
