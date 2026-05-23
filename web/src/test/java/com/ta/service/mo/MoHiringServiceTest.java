package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.testsupport.MoTestSupport;
import com.ta.util.JsonUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoHiringServiceTest extends MoTestSupport {

    private MoHiringService service;

    @BeforeEach
    void seed() throws Exception {
        service = new MoHiringService();
        JobPosting job = ownedJob(JOB_ID, false);
        job.setPositions(1);
        job.setApprovalStatus("approved");
        job.setStatus("open");
        writeJobs(List.of(job));

        List<ApplicationRecord> apps = new ArrayList<>();
        apps.add(application("app_s1", JOB_ID, "shortlisted", true));
        apps.add(application("app_s2", JOB_ID, "shortlisted", true));
        writeApplications(apps);
        JsonUtility.saveHiringHistory(servletContext, new ArrayList<>());
        JsonUtility.saveNotifications(servletContext, new ArrayList<>());
        writeUsers(new ArrayList<>());
    }

    @Test
    void finalizeHiring_twoSelectedWhenOnlyOneAllowed_throws() {
        assertMoBusinessException(
                () -> service.finalizeHiring(servletContext, MO_ID, JOB_ID, List.of("app_s1", "app_s2")),
                ErrorCodes.JOB_RECRUITMENT_CLOSED,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void finalizeHiring_oneSelected_closesJob() throws Exception {
        service.finalizeHiring(servletContext, MO_ID, JOB_ID, List.of("app_s1"));

        JobPosting job = readJobs().stream()
                .filter(j -> JOB_ID.equals(j.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(Boolean.TRUE.equals(job.getRecruitmentClosed()));
        assertEquals("closed", job.getStatus());
        assertEquals("hired", findApp(readApplications(), "app_s1").getStatus());
        assertEquals("rejected", findApp(readApplications(), "app_s2").getStatus());
    }

    private static ApplicationRecord findApp(List<ApplicationRecord> apps, String id) {
        return apps.stream().filter(a -> id.equals(a.getId())).findFirst().orElseThrow();
    }
}
