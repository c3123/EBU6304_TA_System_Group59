package com.ta.web.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicantAiRecommendationRequest;
import com.ta.service.mo.ApplicantRecommendationService;
import com.ta.service.mo.MoBusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "MoApplicantAiRecommendationServlet", urlPatterns = {"/api/mo/applicant-ai-recommendation"})
public class MoApplicantAiRecommendationServlet extends MoBaseServlet {
    private final ApplicantRecommendationService recommendationService = new ApplicantRecommendationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String moId = getMoIdFromSession(req);
            if (moId == null || moId.isBlank()) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "MO login required.");
                return;
            }

            MoApplicantAiRecommendationRequest body = readJson(req, MoApplicantAiRecommendationRequest.class);
            Object data = recommendationService.recommend(getServletContext(), moId, body);
            writeSuccess(resp, data);
        } catch (MoBusinessException ex) {
            writeError(resp, ex.getHttpStatus(), ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Applicant recommendation failed.");
        }
    }
}
