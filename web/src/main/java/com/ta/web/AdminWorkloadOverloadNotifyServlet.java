package com.ta.web;

import com.ta.service.admin.AdminBusinessException;
import com.ta.service.admin.WorkloadOverloadAnnouncementService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminWorkloadOverloadNotifyServlet", urlPatterns = {"/api/admin/workload/notify-overload"})
public class AdminWorkloadOverloadNotifyServlet extends AdminBaseServlet {

    private final WorkloadOverloadAnnouncementService workloadOverloadAnnouncementService =
            new WorkloadOverloadAnnouncementService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!requireAdmin(req, resp)) {
                return;
            }
            writeSuccess(resp, workloadOverloadAnnouncementService.notifyAllCurrentlyOverloaded(getServletContext()));
        } catch (AdminBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }
}
