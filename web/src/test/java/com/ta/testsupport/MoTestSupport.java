package com.ta.testsupport;

import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.model.StudentProfile;
import com.ta.model.SystemSettings;
import com.ta.model.User;
import com.ta.service.admin.AdminBusinessException;
import com.ta.service.mo.MoBusinessException;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Shared test harness: isolated JSON data dir via {@code ta.data.dir}.
 */
public abstract class MoTestSupport {

    public static final String MO_ID = "teacher_demo";
    public static final String OTHER_MO_ID = "other_teacher";
    public static final String JOB_ID = "job_test_1";
    public static final String OTHER_JOB_ID = "job_other_1";
    public static final String STUDENT_USER_ID = "student_test_1";

    @TempDir
    protected Path tempDataDir;

    protected ServletContext servletContext;

    @BeforeEach
    void setUpDataDir() {
        System.setProperty("ta.data.dir", tempDataDir.toString());
        servletContext = mock(ServletContext.class);
    }

    @AfterEach
    void tearDownDataDir() {
        System.clearProperty("ta.data.dir");
    }

    protected void writeApplications(List<ApplicationRecord> applications) throws IOException {
        JsonUtility.saveApplications(servletContext, applications);
    }

    protected void writeJobs(List<JobPosting> jobs) throws IOException {
        JsonUtility.saveJobs(servletContext, jobs);
    }

    protected void writeNotifications(List<NotificationRecord> notifications) throws IOException {
        JsonUtility.saveNotifications(servletContext, notifications);
    }

    protected void writeHiringHistory(List<HiringHistoryRecord> records) throws IOException {
        JsonUtility.saveHiringHistory(servletContext, records);
    }

    protected void writeSystemSettings(SystemSettings settings) throws IOException {
        JsonUtility.saveSystemSettings(servletContext, settings);
    }

    protected void writeStudents(List<StudentProfile> students) throws IOException {
        JsonUtility.saveStudents(servletContext, students);
    }

    protected void writeUsers(List<User> users) throws IOException {
        JsonUtility.saveUsers(servletContext, users);
    }

    protected List<ApplicationRecord> readApplications() throws IOException {
        return JsonUtility.loadApplications(servletContext);
    }

    protected List<JobPosting> readJobs() throws IOException {
        return JsonUtility.loadJobs(servletContext);
    }

    protected List<NotificationRecord> readNotifications() throws IOException {
        return JsonUtility.loadNotifications(servletContext);
    }

    protected List<HiringHistoryRecord> readHiringHistory() throws IOException {
        return JsonUtility.loadHiringHistory(servletContext);
    }

    protected List<StudentProfile> readStudents() throws IOException {
        return JsonUtility.loadStudents(servletContext);
    }

    protected List<User> readUsers() throws IOException {
        return JsonUtility.loadUsers(servletContext);
    }

    protected static void assertMoBusinessException(Runnable action, String expectedCode, int expectedHttpStatus) {
        MoBusinessException ex = assertThrows(MoBusinessException.class, action::run);
        assertEquals(expectedCode, ex.getCode());
        assertEquals(expectedHttpStatus, ex.getHttpStatus());
    }

    protected static void assertAdminBusinessException(Runnable action, String expectedCode, int expectedHttpStatus) {
        AdminBusinessException ex = assertThrows(AdminBusinessException.class, action::run);
        assertEquals(expectedCode, ex.getCode());
        assertEquals(expectedHttpStatus, ex.getHttpStatus());
    }

    protected static JobPosting ownedJob(String jobId, boolean recruitmentClosed) {
        JobPosting job = new JobPosting();
        job.setId(jobId);
        job.setTeacherId(MO_ID);
        job.setTitle("Test Module");
        job.setModuleCode("CS9999");
        job.setRecruitmentClosed(recruitmentClosed);
        job.setPublished(true);
        return job;
    }

    protected static JobPosting otherMoJob() {
        JobPosting job = new JobPosting();
        job.setId(OTHER_JOB_ID);
        job.setTeacherId(OTHER_MO_ID);
        job.setTitle("Other Module");
        job.setPublished(true);
        return job;
    }

    protected static ApplicationRecord application(String id, String jobId, String status, boolean active) {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(id);
        app.setJobId(jobId);
        app.setStudentId(STUDENT_USER_ID);
        app.setStudentName("Test Student");
        app.setStudentNo("S001");
        app.setAppliedAt("2025-01-10T10:00:00Z");
        app.setStatus(status);
        app.setActive(active);
        return app;
    }

    protected static StudentProfile studentProfile() {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(STUDENT_USER_ID);
        profile.setName("Test Student");
        profile.setStudentId("S001");
        profile.setProgramme("CS");
        profile.setSkills("Java, Python");
        return profile;
    }

    protected static List<JobPosting> defaultJobs() {
        List<JobPosting> jobs = new ArrayList<>();
        jobs.add(ownedJob(JOB_ID, false));
        jobs.add(otherMoJob());
        return jobs;
    }

    protected static List<ApplicationRecord> defaultApplications() {
        List<ApplicationRecord> apps = new ArrayList<>();
        apps.add(application("app_pending", JOB_ID, "pending", true));
        apps.add(application("app_viewed", JOB_ID, "viewed", true));
        apps.add(application("app_shortlisted", JOB_ID, "shortlisted", true));
        apps.add(application("app_hired", JOB_ID, "hired", true));
        apps.add(application("app_rejected", JOB_ID, "rejected", true));
        apps.add(application("app_other_job", OTHER_JOB_ID, "pending", true));
        apps.add(application("app_inactive", JOB_ID, "pending", false));
        return apps;
    }
}
