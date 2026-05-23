package com.ta.service.mo;

import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoDemandCreateRequest;
import com.ta.dto.mo.MoDemandListResponse;
import com.ta.model.JobPosting;
import com.ta.model.User;
import com.ta.testsupport.MoTestSupport;
import com.ta.util.JsonUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoDemandServiceTest extends MoTestSupport {

    private MoDemandService service;

    @BeforeEach
    void setUpService() throws Exception {
        service = new MoDemandService();
        writeJobs(new ArrayList<>());
        JsonUtility.saveUsers(servletContext, List.of(moUser(MO_ID, "Demo Teacher"), moUser(OTHER_MO_ID, "Other Teacher")));
    }

    @Test
    void createDemand_createsPendingDraftForTeacher() throws Exception {
        String jobId = service.createDemand(servletContext, MO_ID, createRequest("Software Engineering", "Computer Science"));

        List<JobPosting> jobs = JsonUtility.loadJobs(servletContext);
        assertEquals(1, jobs.size());
        JobPosting job = jobs.get(0);
        assertEquals(jobId, job.getId());
        assertEquals(MO_ID, job.getTeacherId());
        assertEquals("Demo Teacher", job.getTeacherName());
        assertEquals("Software Engineering", job.getTitle());
        assertEquals("Software Engineering", job.getModuleCode());
        assertEquals("Computer Science", job.getDepartment());
        assertEquals(2, job.getPositions());
        assertEquals(4, job.getHourMin());
        assertEquals(8, job.getHourMax());
        assertEquals("Java, SQL", job.getRequirements());
        assertEquals("pending", job.getApprovalStatus());
        assertEquals("draft", job.getStatus());
        assertFalse(Boolean.TRUE.equals(job.getPublished()));
        assertFalse(Boolean.TRUE.equals(job.getWithdrawn()));
        assertNotNull(job.getCreatedAt());
        assertNotNull(job.getUpdatedAt());
    }

    @Test
    void createDemand_rejectsDuplicatePendingCourseForSameTeacher() throws Exception {
        service.createDemand(servletContext, MO_ID, createRequest("Software Engineering", "Computer Science"));

        assertMoBusinessException(
                () -> service.createDemand(servletContext, MO_ID, createRequest("software engineering", "Computer Science")),
                ErrorCodes.HAS_PENDING_SAME_COURSE,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void createDemand_allowsSameCourseForDifferentTeacher() throws Exception {
        service.createDemand(servletContext, OTHER_MO_ID, createRequest("Software Engineering", "Computer Science"));

        String jobId = service.createDemand(servletContext, MO_ID, createRequest("Software Engineering", "Computer Science"));

        assertNotNull(jobId);
        assertEquals(2, JsonUtility.loadJobs(servletContext).size());
    }

    @Test
    void createDemand_invalidHourRangeThrows400() {
        MoDemandCreateRequest request = createRequest("Software Engineering", "Computer Science");
        request.setHourMin(10);
        request.setHourMax(5);

        assertMoBusinessException(
                () -> service.createDemand(servletContext, MO_ID, request),
                ErrorCodes.VALIDATION_ERROR,
                HttpServletResponse.SC_BAD_REQUEST
        );
    }

    @Test
    void listMyDemands_returnsOnlyCurrentTeachersJobs() throws Exception {
        JobPosting own = job("job_own", MO_ID, "Own Module");
        JobPosting other = job("job_other", OTHER_MO_ID, "Other Module");
        writeJobs(List.of(own, other));

        MoDemandListResponse response = service.listMyDemands(servletContext, MO_ID);

        assertEquals(1, response.getItems().size());
        assertEquals("job_own", response.getItems().get(0).getJobId());
        assertEquals("Own Module", response.getItems().get(0).getCourseName());
    }

    private static MoDemandCreateRequest createRequest(String courseName, String department) {
        MoDemandCreateRequest request = new MoDemandCreateRequest();
        request.setCourseName(courseName);
        request.setDepartment(department);
        request.setPlannedCount(2);
        request.setHourMin(4);
        request.setHourMax(8);
        request.setRequirements(" Java, SQL ");
        return request;
    }

    private static User moUser(String id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setRole("mo");
        return user;
    }

    private static JobPosting job(String id, String teacherId, String title) {
        JobPosting job = new JobPosting();
        job.setId(id);
        job.setTeacherId(teacherId);
        job.setTitle(title);
        job.setDepartment("Computer Science");
        job.setPositions(1);
        job.setHourMin(2);
        job.setHourMax(4);
        job.setApprovalStatus("pending");
        job.setPublished(false);
        job.setWithdrawn(false);
        job.setStatus("draft");
        return job;
    }
}
