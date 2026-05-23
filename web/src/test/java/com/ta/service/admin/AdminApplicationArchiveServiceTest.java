package com.ta.service.admin;

import com.ta.dto.admin.AdminApplicationArchiveResponse;
import com.ta.model.ApplicationRecord;
import com.ta.testsupport.AdminServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminApplicationArchiveServiceTest extends AdminServiceTestSupport {

    private AdminApplicationArchiveService service;

    @BeforeEach
    void seed() throws Exception {
        service = new AdminApplicationArchiveService();
        writeStudents(List.of(profile(STUDENT_USER_ID, "Archive Student")));
        writeJobs(List.of(
                job("job_1", MO_ID, "open", "approved", true, false, false, 2, 8),
                job("job_2", OTHER_MO_ID, "open", "approved", true, false, false, 1, 6)
        ));
        ApplicationRecord first = app("app_1", "job_1", STUDENT_USER_ID, "hired", true);
        first.setStudentName("");
        first.setEvaluationNotes("MO note");
        first.setDecisionFeedback("Hire reason");
        ApplicationRecord second = app("app_2", "job_2", STUDENT_2_ID, "pending", true);
        second.setStudentName("Other Student");
        writeApplications(List.of(first, second));
    }

    @Test
    void listArchive_joinsJobStudentAndPrivateFields() {
        AdminApplicationArchiveResponse response = service.listArchive(
                servletContext,
                "hired",
                "job_1",
                "all",
                "Archive"
        );

        assertEquals(1, response.getItems().size());
        var item = response.getItems().get(0);
        assertEquals("app_1", item.getApplicationId());
        assertEquals("Archive Student", item.getStudentName());
        assertEquals("Job job_1", item.getTitle());
        assertEquals("MO note", item.getEvaluationNotes());
        assertEquals("Hire reason", item.getDecisionFeedback());
    }

    @Test
    void listArchive_filtersByTeacherIdOrName() {
        AdminApplicationArchiveResponse byId = service.listArchive(servletContext, "all", "all", MO_ID, "all");
        AdminApplicationArchiveResponse byName = service.listArchive(servletContext, "all", "all", "Teacher " + OTHER_MO_ID, "all");

        assertEquals(1, byId.getItems().size());
        assertEquals("app_1", byId.getItems().get(0).getApplicationId());
        assertEquals(1, byName.getItems().size());
        assertEquals("app_2", byName.getItems().get(0).getApplicationId());
    }
}
