package com.ta.service.admin;

import com.ta.constant.ErrorCodes;
import com.ta.dto.admin.AdminRecruitmentOutcomeResponse;
import com.ta.testsupport.AdminServiceTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminRecruitmentOutcomeServiceTest extends AdminServiceTestSupport {

    private AdminRecruitmentOutcomeService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminRecruitmentOutcomeService();
        var cs = job("job_cs", MO_ID, "open", "approved", true, false, false, 3, 10);
        cs.setDepartment("Computer Science");
        cs.setPublishedAt("2026-05-10T00:00:00Z");
        var math = job("job_math", MO_ID, "closed", "approved", true, false, true, 1, 6);
        math.setDepartment("Mathematics");
        math.setPublishedAt("2026-04-10T00:00:00Z");
        writeJobs(List.of(cs, math));
        writeApplications(List.of(
                app("app_hired", "job_cs", STUDENT_USER_ID, "hired", true),
                app("app_pending", "job_cs", STUDENT_2_ID, "pending", true),
                app("app_old", "job_math", STUDENT_2_ID, "hired", true)
        ));
    }

    @Test
    void load_aggregatesKpisDepartmentsAndVacancies() throws Exception {
        AdminRecruitmentOutcomeResponse response = service.load(servletContext, 5, null, null);

        assertEquals(4, response.getTotalPositionSlots());
        assertEquals(1, response.getClosedJobs());
        assertEquals(1, response.getRecruitingJobs());
        assertEquals(3, response.getTotalApplications());
        assertEquals(2, response.getTotalHired());
        assertEquals(2, response.getTotalVacancies());
        assertTrue(response.getDepartments().stream().anyMatch(r ->
                "Computer Science".equals(r.getDepartment()) && r.getHiredCount() == 1 && r.getVacancyCount() == 2));
        assertEquals("job_cs", response.getTopVacancyJobs().get(0).getJobId());
    }

    @Test
    void load_dateWindowFiltersJobsAndApplications() throws Exception {
        AdminRecruitmentOutcomeResponse response = service.load(servletContext, 5, "2026-05-01", "2026-05-31");

        assertEquals(3, response.getTotalPositionSlots());
        assertEquals(2, response.getTotalApplications());
        assertEquals(1, response.getTotalHired());
        assertEquals(2, response.getTotalVacancies());
    }

    @Test
    void load_invalidDateRange_throws400() {
        assertAdminBusinessException(
                () -> {
                    try {
                        service.load(servletContext, 5, "2026-06-01", "2026-05-01");
                    } catch (AdminBusinessException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                },
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void buildRecruitmentOutcomeCsv_containsKpisAndRows() throws Exception {
        String csv = service.buildRecruitmentOutcomeCsv(service.load(servletContext, 5, null, null));

        assertTrue(csv.contains("kpi,totalHired,2"));
        assertTrue(csv.contains("dept,Computer Science,1,2"));
        assertTrue(csv.contains("vac,1,job_cs"));
    }
}
