package com.ta.service.student;

import com.ta.constant.ErrorCodes;
import com.ta.dto.student.AiAdvisorResponse;
import com.ta.dto.student.StudentApplicationCreateRequest;
import com.ta.dto.student.StudentAssignedJobItemResponse;
import com.ta.dto.student.StudentProfileResponse;
import com.ta.dto.student.StudentProfileUpdateRequest;
import com.ta.model.ApplicationRecord;
import com.ta.model.Attachment;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.model.User;
import com.ta.service.student.ai.AiAdvisorClient;
import com.ta.testsupport.MoTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class StudentServiceTest extends MoTestSupport {

    private static final String STUDENT_ID = "student_demo";
    private static final String TEACHER_ID = "teacher_demo";
    private StudentService service;

    @BeforeEach
    void seedData() throws Exception {
        service = new StudentService();
        when(servletContext.getRealPath("/WEB-INF/uploads/students"))
                .thenReturn(tempDataDir.resolve("uploads").toString());

        User student = user(STUDENT_ID, "Demo Student", "student@demo.test", "student", "S1001");
        User teacher = user(TEACHER_ID, "Demo Teacher", "teacher@demo.test", "teacher", "");
        writeUsers(List.of(student, teacher));

        StudentProfile profile = new StudentProfile();
        profile.setUserId(STUDENT_ID);
        profile.setStudentId("S1001");
        profile.setName("Demo Student");
        profile.setEmail("student@demo.test");
        profile.setProgramme("MSc CS");
        profile.setSkills("Java, Python");
        profile.setExperience("Lab tutoring");
        profile.setAttachments(new ArrayList<>(List.of(attachment("att_resume.pdf"))));
        writeStudents(List.of(profile));

        writeJobs(List.of(openJob("job_open"), openJob("job_hired")));
        writeApplications(new ArrayList<>());
        writeNotifications(new ArrayList<>());
    }

    @Test
    void updateProfile_persistsStudentFields() throws Exception {
        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setName("Updated Student");
        request.setPhone("07700900001");
        request.setSkills("Java, SQL");
        request.setExperience("Two lab sessions");

        StudentProfileResponse response = service.updateMyProfile(servletContext, STUDENT_ID, request);

        assertEquals("Updated Student", response.getName());
        assertEquals("07700900001", response.getPhone());
        assertEquals("Java, SQL", response.getSkills());
        assertEquals("Two lab sessions", response.getExperience());
        assertEquals("Updated Student", readUsers().get(0).getName());
        assertEquals("Java, SQL", readStudents().get(0).getSkills());
    }

    @Test
    void updateProfile_invalidPhone_throws400() {
        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setName("Demo Student");
        request.setPhone("12345");

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.updateMyProfile(servletContext, STUDENT_ID, request)
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void getProfile_missingProfileUsesUserFallback() throws Exception {
        writeStudents(new ArrayList<>());

        StudentProfileResponse response = service.getMyProfile(servletContext, STUDENT_ID);

        assertEquals(STUDENT_ID, response.getUserId());
        assertEquals("Demo Student", response.getName());
        assertEquals("student@demo.test", response.getEmail());
        assertEquals("S1001", response.getStudentId());
    }

    @Test
    void updateProfile_missingProfileCreatesProfile() throws Exception {
        writeStudents(new ArrayList<>());
        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setName("Created Profile");
        request.setPhone("07700900002");
        request.setSkills("Java");
        request.setExperience("Created by update");

        service.updateMyProfile(servletContext, STUDENT_ID, request);

        assertEquals(1, readStudents().size());
        assertEquals(STUDENT_ID, readStudents().get(0).getUserId());
        assertEquals("Created Profile", readStudents().get(0).getName());
    }

    @Test
    void uploadAndDeleteAttachment_persistsMetadataAndFile() throws Exception {
        Attachment uploaded = service.uploadAttachment(
                servletContext,
                STUDENT_ID,
                new ByteArrayInputStream("%PDF-1.4".getBytes()),
                8,
                "resume.pdf",
                "Resume"
        );

        assertEquals("resume.pdf", uploaded.getFileName());
        assertTrue(Files.exists(tempDataDir.resolve("uploads")
                .resolve("S1001")
                .resolve("profile-attachments")
                .resolve(uploaded.getId())));
        assertTrue(readStudents().get(0).getAttachments().stream()
                .anyMatch(a -> uploaded.getId().equals(a.getId())));

        service.deleteAttachment(servletContext, STUDENT_ID, uploaded.getId());

        assertFalse(Files.exists(tempDataDir.resolve("uploads")
                .resolve("S1001")
                .resolve("profile-attachments")
                .resolve(uploaded.getId())));
        assertTrue(readStudents().get(0).getAttachments().stream()
                .noneMatch(a -> uploaded.getId().equals(a.getId())));
    }

    @Test
    void uploadAttachment_invalidExtension_throws400() {
        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.uploadAttachment(
                        servletContext,
                        STUDENT_ID,
                        new ByteArrayInputStream("bad".getBytes()),
                        3,
                        "script.exe",
                        "Executable")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void deleteAttachment_missingId_throws404() {
        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.deleteAttachment(servletContext, STUDENT_ID, "missing")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void listJobs_returnsOnlyRecommendableOpenJobsWithMatchContext() throws Exception {
        JobPosting strong = openJob("job_strong");
        strong.setTitle("A Strong Match");
        strong.setRequirements("Java, Python");
        JobPosting weak = openJob("job_weak");
        weak.setTitle("B Weak Match");
        weak.setRequirements("Research");
        JobPosting draft = openJob("job_draft");
        draft.setStatus("draft");
        JobPosting rejected = openJob("job_rejected");
        rejected.setApprovalStatus("rejected");
        writeJobs(List.of(weak, rejected, draft, strong));

        var response = service.listJobs(servletContext, STUDENT_ID);

        assertEquals(2, response.getItems().size());
        assertEquals("job_strong", response.getItems().get(0).getId());
        assertEquals(1.0, response.getItems().get(0).getMatchScore());
        assertEquals(List.of("Java", "Python"), response.getItems().get(0).getMatchedSkills());
        assertEquals("job_weak", response.getItems().get(1).getId());
    }

    @Test
    void listMyApplications_mapsStatusesAndSkipsInactiveRows() throws Exception {
        ApplicationRecord viewed = application("app_viewed_student", "job_open", "viewed", true);
        viewed.setStudentId(STUDENT_ID);
        viewed.setAppliedAt("2026-05-11T10:00:00Z");
        ApplicationRecord hired = application("app_hired_student", "missing_job", "hired", true);
        hired.setStudentId(STUDENT_ID);
        hired.setAppliedAt("2026-05-12T10:00:00Z");
        ApplicationRecord inactive = application("app_inactive_student", "job_open", "pending", false);
        inactive.setStudentId(STUDENT_ID);
        writeApplications(List.of(viewed, hired, inactive));

        var response = service.listMyApplications(servletContext, STUDENT_ID);

        assertEquals(2, response.getItems().size());
        assertEquals("app_hired_student", response.getItems().get(0).getId());
        assertEquals("Unknown Job", response.getItems().get(0).getJobTitle());
        assertEquals("hired", response.getItems().get(0).getStatus());
        assertEquals("pending", response.getItems().get(1).getStatus());
    }

    @Test
    void applyAndWithdraw_createsAndRemovesApplicationWithNotifications() throws Exception {
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_open");
        request.setSelectedAttachmentIds(List.of("att_resume.pdf"));

        String applicationId = service.applyForJob(servletContext, STUDENT_ID, request).getId();

        assertEquals(1, readApplications().size());
        assertEquals("pending", readApplications().get(0).getStatus());
        assertEquals(List.of("att_resume.pdf"), readApplications().get(0).getSelectedAttachmentIds());
        assertTrue(readNotifications().stream().anyMatch(n -> applicationId.equals(n.getApplicationId())));

        service.withdrawApplication(servletContext, STUDENT_ID, applicationId);

        assertTrue(readApplications().isEmpty());
        assertTrue(readNotifications().stream().anyMatch(n -> ("noti_withdraw_" + applicationId).equals(n.getId())));
    }

    @Test
    void apply_duplicateActiveApplication_throws400() {
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_open");
        request.setSelectedAttachmentIds(List.of("att_resume.pdf"));
        service.applyForJob(servletContext, STUDENT_ID, request);

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.applyForJob(servletContext, STUDENT_ID, request)
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void apply_missingAttachmentSelection_throws400() {
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_open");

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.applyForJob(servletContext, STUDENT_ID, request)
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void apply_unknownSelectedAttachment_throws400() {
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_open");
        request.setSelectedAttachmentIds(List.of("missing"));

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.applyForJob(servletContext, STUDENT_ID, request)
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void apply_closedJob_throws400() throws Exception {
        JobPosting closed = openJob("job_closed");
        closed.setStatus("closed");
        writeJobs(List.of(openJob("job_open"), closed));
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_closed");
        request.setSelectedAttachmentIds(List.of("att_resume.pdf"));

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.applyForJob(servletContext, STUDENT_ID, request)
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void apply_missingStudent_throws401() {
        StudentApplicationCreateRequest request = new StudentApplicationCreateRequest();
        request.setJobId("job_open");
        request.setSelectedAttachmentIds(List.of("att_resume.pdf"));

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.applyForJob(servletContext, "missing_student", request)
        );
        assertEquals(ErrorCodes.UNAUTHORIZED, ex.getCode());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void assignedJobs_returnsOnlyActiveHiredJobsWithScheduleDetails() throws Exception {
        ApplicationRecord hired = application("app_hired_student", "job_hired", "hired", true);
        hired.setStudentId(STUDENT_ID);
        ApplicationRecord pending = application("app_pending_student", "job_open", "pending", true);
        pending.setStudentId(STUDENT_ID);
        writeApplications(List.of(hired, pending));

        List<StudentAssignedJobItemResponse> items = service.listMyAssignedJobs(servletContext, STUDENT_ID).getItems();

        assertEquals(1, items.size());
        StudentAssignedJobItemResponse item = items.get(0);
        assertEquals("job_hired", item.getJobId());
        assertEquals(10, item.getWeeklyHours());
        assertEquals("Mon 10:00-12:00", item.getSchedule());
        assertEquals("Room 101", item.getLocation());
        assertEquals("2026-05-24", item.getDeadline());
    }

    @Test
    void withdraw_hiredApplication_throws400() throws Exception {
        ApplicationRecord hired = application("app_hired_student", "job_hired", "hired", true);
        hired.setStudentId(STUDENT_ID);
        writeApplications(List.of(hired));

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.withdrawApplication(servletContext, STUDENT_ID, "app_hired_student")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void withdraw_otherStudentApplication_throws404() throws Exception {
        ApplicationRecord pending = application("app_other_student", "job_open", "pending", true);
        pending.setStudentId("other_student");
        writeApplications(List.of(pending));

        StudentBusinessException ex = assertThrows(
                StudentBusinessException.class,
                () -> service.withdrawApplication(servletContext, STUDENT_ID, "app_other_student")
        );
        assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void aiAdvisor_usesFallbackWhenExternalClientUnavailable() {
        AiAdvisorService advisor = new AiAdvisorService(
                new JobMatchingService(),
                new SkillExtractionService(),
                new AiAdvisorClient()
        );

        AiAdvisorResponse response = advisor.advise(servletContext, STUDENT_ID, "Which job fits me?");

        assertTrue(response.isFallback());
        assertTrue(response.getAnswer().contains("Based on the current matching results"));
    }

    private User user(String id, String name, String email, String role, String studentNumber) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("oldPass1");
        user.setRole(role);
        user.setStudentId(studentNumber);
        user.setProgramme("MSc CS");
        return user;
    }

    private JobPosting openJob(String id) {
        JobPosting job = new JobPosting();
        job.setId(id);
        job.setTeacherId(TEACHER_ID);
        job.setTeacherName("Demo Teacher");
        job.setModuleCode("EBU6304");
        job.setTitle("Software Engineering TA");
        job.setStatus("open");
        job.setPublished(true);
        job.setWithdrawn(false);
        job.setHours(10);
        job.setPositions(2);
        job.setSchedule("Mon 10:00-12:00");
        job.setLocation("Room 101");
        job.setDeadline("2026-05-24");
        job.setRequirements("Java, Python");
        return job;
    }

    private Attachment attachment(String id) {
        Attachment attachment = new Attachment();
        attachment.setId(id);
        attachment.setFileName("resume.pdf");
        attachment.setFileType("pdf");
        attachment.setLabel("Resume");
        attachment.setFileSize(10);
        attachment.setUploadedAt("2026-05-21T00:00:00Z");
        return attachment;
    }
}
