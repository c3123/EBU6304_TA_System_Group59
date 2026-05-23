package com.ta.service.mo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationExportRow;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.testsupport.MoTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoApplicationExportServiceTest extends MoTestSupport {

    private static final Type ROW_LIST_TYPE = new TypeToken<List<MoApplicationExportRow>>() { }.getType();
    private static final Gson GSON = new Gson();

    private final MoApplicationExportService service = new MoApplicationExportService();

    @BeforeEach
    void seedExportFixtures() throws Exception {
        writeJobs(defaultJobs());
        StudentProfile profile = studentProfile();
        profile.setSkills("Java, \"advanced\"");
        writeStudents(List.of(profile));

        List<ApplicationRecord> apps = new ArrayList<>();
        apps.add(application("app_exp_pending", JOB_ID, "pending", true));
        apps.add(application("app_exp_short", JOB_ID, "shortlisted", true));
        apps.add(application("app_exp_other", OTHER_JOB_ID, "shortlisted", true));
        writeApplications(apps);
    }

    @Test
    void buildExport_csvHasBomHeaderAndRows() {
        MoApplicationExportService.ExportFile file =
                service.buildExport(servletContext, MO_ID, JOB_ID, "all", "csv");
        String text = new String(file.getContent(), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("\uFEFF"));
        assertTrue(text.contains("name,applicant_id,major,application_time,status,skills"));
        assertEquals(3, text.split("\r\n").length);
        assertTrue(file.getFileName().endsWith("_all_applicants.csv"));
    }

    @Test
    void buildExport_jsonDeserializesRows() {
        MoApplicationExportService.ExportFile file =
                service.buildExport(servletContext, MO_ID, JOB_ID, "all", "json");
        List<MoApplicationExportRow> rows = GSON.fromJson(
                new String(file.getContent(), StandardCharsets.UTF_8), ROW_LIST_TYPE);
        assertEquals(2, rows.size());
        assertEquals("application/json;charset=UTF-8", file.getContentType());
    }

    @Test
    void buildExport_shortlistedScope_filtersRows() {
        MoApplicationExportService.ExportFile file =
                service.buildExport(servletContext, MO_ID, JOB_ID, "shortlisted", "csv");
        String text = new String(file.getContent(), StandardCharsets.UTF_8);
        long dataLines = text.lines().filter(line -> line.contains("shortlisted")).count();
        assertEquals(1, dataLines);
    }

    @Test
    void buildExport_csvEscapesCommasAndQuotes() {
        MoApplicationExportService.ExportFile file =
                service.buildExport(servletContext, MO_ID, JOB_ID, "all", "csv");
        String text = new String(file.getContent(), StandardCharsets.UTF_8);
        assertTrue(text.contains("\"Java, \"\"advanced\"\"\""));
    }

    @Test
    void buildExport_missingJobId_throws400() {
        assertMoBusinessException(
                () -> service.buildExport(servletContext, MO_ID, "", "all", "csv"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void buildExport_invalidScope_throws400() {
        assertMoBusinessException(
                () -> service.buildExport(servletContext, MO_ID, JOB_ID, "invalid", "csv"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void buildExport_invalidFormat_throws400() {
        assertMoBusinessException(
                () -> service.buildExport(servletContext, MO_ID, JOB_ID, "all", "xml"),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void buildExport_otherMoJob_throws403() {
        assertMoBusinessException(
                () -> service.buildExport(servletContext, MO_ID, OTHER_JOB_ID, "all", "csv"),
                ErrorCodes.FORBIDDEN_NOT_OWNER,
                HttpServletResponse.SC_FORBIDDEN
        );
    }
}
