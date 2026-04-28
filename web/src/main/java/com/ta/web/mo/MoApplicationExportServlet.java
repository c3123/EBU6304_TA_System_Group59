package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.service.mo.MoApplicationExportService;
import com.ta.service.mo.MoApplicationExportService.ExportFile;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoApplicationExportServlet", urlPatterns = {"/api/mo/applications/export"})
public class MoApplicationExportServlet extends MoBaseServlet {
    private final MoApplicationExportService moApplicationExportService = new MoApplicationExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            String jobId = req.getParameter("jobId");
            String scope = defaultIfBlank(req.getParameter("scope"), "all");
            String format = defaultIfBlank(req.getParameter("format"), "csv");
            ExportFile file = moApplicationExportService.buildExport(getServletContext(), moId, jobId, scope, format);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(file.getContentType());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");
            resp.setContentLength(file.getContent().length);
            resp.getOutputStream().write(file.getContent());
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
