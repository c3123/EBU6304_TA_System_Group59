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
