package com.ta.testsupport;

import com.ta.model.ApplicationRecord;
import com.ta.model.Attachment;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.NotificationRecord;
import com.ta.model.StudentProfile;
import com.ta.model.SystemSettings;
import com.ta.model.User;

import java.util.ArrayList;
import java.util.List;

public abstract class AdminServiceTestSupport extends MoTestSupport {

    protected static final String ADMIN_ID = "admin_demo";
    protected static final String STUDENT_2_ID = "student_demo_2";

    protected static User user(String id, String name, String email, String role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("demo123");
        user.setRole(role);
        if ("student".equals(role)) {
            user.setStudentId("SN-" + id);
            user.setProgramme("MSc CS");
        }
        return user;
    }

    protected static StudentProfile profile(String userId, String name) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setStudentId("SN-" + userId);
        profile.setName(name);
        profile.setEmail(userId + "@demo.test");
        profile.setProgramme("MSc CS");
        profile.setSkills("Java, Python");
        profile.setExperience("Lab support");
        Attachment attachment = new Attachment();
        attachment.setId("att_" + userId);
        attachment.setFileName("resume.pdf");
        attachment.setLabel("Resume");
        attachment.setFileType("pdf");
        profile.setAttachments(new ArrayList<>(List.of(attachment)));
        return profile;
    }

    protected static JobPosting job(String id,
                                    String teacherId,
                                    String status,
                                    String approvalStatus,
                                    boolean published,
                                    boolean withdrawn,
                                    boolean closed,
                                    int positions,
                                    int hours) {
        JobPosting job = new JobPosting();
        job.setId(id);
        job.setTeacherId(teacherId);
        job.setTeacherName("Teacher " + teacherId);
        job.setModuleCode("EBU" + id.replaceAll("\\D", ""));
        job.setTitle("Job " + id);
        job.setDepartment("Computer Science");
        job.setStatus(status);
        job.setApprovalStatus(approvalStatus);
        job.setPublished(published);
        job.setWithdrawn(withdrawn);
        job.setRecruitmentClosed(closed);
        job.setPositions(positions);
        job.setHours(hours);
        job.setDeadline("2099-05-24");
        job.setSchedule("Mon 10:00-12:00");
        job.setLocation("Room 101");
        job.setRequirements("Java, Python");
        job.setCreatedAt("2026-05-01T00:00:00Z");
        job.setUpdatedAt("2026-05-02T00:00:00Z");
        if (published) {
            job.setPublishedAt("2026-05-03T00:00:00Z");
        }
        if (closed) {
            job.setClosedAt("2026-05-20T00:00:00Z");
        }
        return job;
    }

    protected static JobPosting demand(String id, String teacherId, String approvalStatus) {
        JobPosting job = job(id, teacherId, "draft", approvalStatus, false, false, false, 2, 8);
        job.setPublished(false);
        job.setWithdrawn(false);
        return job;
    }

    protected static ApplicationRecord app(String id, String jobId, String studentId, String status, boolean active) {
        ApplicationRecord app = new ApplicationRecord();
        app.setId(id);
        app.setJobId(jobId);
        app.setStudentId(studentId);
        app.setStudentName("Student " + studentId);
        app.setStudentNo("SN-" + studentId);
        app.setAppliedAt("2026-05-10T10:00:00Z");
        app.setStatus(status);
        app.setActive(active);
        app.setSelectedAttachmentIds(List.of("att_" + studentId));
        return app;
    }

    protected static NotificationRecord notification(String id, String recipientId, String role, boolean read) {
        NotificationRecord record = new NotificationRecord();
        record.setId(id);
        record.setRecipientId(recipientId);
        record.setRecipientRole(role);
        record.setRead(read);
        record.setMessage("Message " + id);
        record.setCreatedAt("2026-05-10T10:00:00Z");
        return record;
    }

    protected static HiringHistoryRecord hiringHistory(String id, String jobId, List<String> applicationIds) {
        HiringHistoryRecord record = new HiringHistoryRecord();
        record.setId(id);
        record.setJobId(jobId);
        record.setMoId(MO_ID);
        record.setAction("finalize");
        record.setSubmittedAt("2026-05-15T12:00:00Z");
        record.setHiredApplicationIds(applicationIds);
        return record;
    }

    protected static SystemSettings settings(int threshold) {
        SystemSettings settings = new SystemSettings();
        settings.setWorkloadThresholdHours(threshold);
        settings.setUpdatedAt("2026-05-01T00:00:00Z");
        return settings;
    }
}
