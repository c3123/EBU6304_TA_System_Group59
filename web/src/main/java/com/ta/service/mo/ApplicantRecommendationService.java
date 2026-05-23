package com.ta.service.mo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicantAiRecommendationRequest;
import com.ta.dto.mo.MoApplicantAiRecommendationResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.HiringHistoryRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.service.student.SkillExtractionService;
import com.ta.service.student.ai.AiAdvisorClient;
import com.ta.service.student.ai.AiAdvisorClient.AiAdvisorResult;
import com.ta.util.JobHoursUtil;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicantRecommendationService {
    private static final String SYSTEM_PROMPT =
            "You are an AI assistant for a university TA recruitment platform.\n"
                    + "Explain the provided rule-based applicant recommendation in 1-2 English sentences.\n"
                    + "Base the explanation only on skill match and workload balance.\n"
                    + "Do not use markdown or bullet points.\n"
                    + "Do not invent skills, jobs, experience, or hiring decisions.\n"
                    + "Do not override the recommendation level.";

    private final SkillExtractionService skillExtractionService;
    private final AiAdvisorClient aiAdvisorClient;
    private final Gson gson = new Gson();

    public ApplicantRecommendationService() {
        this(new SkillExtractionService(), new AiAdvisorClient());
    }

    public ApplicantRecommendationService(SkillExtractionService skillExtractionService,
                                          AiAdvisorClient aiAdvisorClient) {
        this.skillExtractionService = skillExtractionService != null ? skillExtractionService : new SkillExtractionService();
        this.aiAdvisorClient = aiAdvisorClient != null ? aiAdvisorClient : new AiAdvisorClient();
    }

    public MoApplicantAiRecommendationResponse recommend(ServletContext context,
                                                          String moId,
                                                          MoApplicantAiRecommendationRequest request) {
        if (request == null || isBlank(request.getJobId())) {
            throw new MoBusinessException(
                    ErrorCodes.VALIDATION_ERROR,
                    "jobId is required.",
                    HttpServletResponse.SC_BAD_REQUEST
            );
        }

        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> request.getJobId().equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Job not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));
            if (!Objects.equals(moId, job.getTeacherId())) {
                throw new MoBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only view recommendations for your own jobs.",
                        HttpServletResponse.SC_FORBIDDEN
                );
            }

            List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
            ApplicationRecord application = findApplication(applications, request);
            if (application == null || !request.getJobId().equals(application.getJobId()) || !application.isActive()) {
                throw new MoBusinessException(
                        ErrorCodes.APPLICATION_NOT_FOUND,
                        "Application not found.",
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            List<StudentProfile> profiles = JsonUtility.loadStudents(context);
            StudentProfile profile = profiles.stream()
                    .filter(p -> application.getStudentId() != null && application.getStudentId().equals(p.getUserId()))
                    .findFirst()
                    .orElse(null);

            MoApplicantAiRecommendationResponse response = buildRuleBasedResponse(
                    job,
                    application,
                    profile,
                    jobs,
                    applications,
                    JsonUtility.loadHiringHistory(context)
            );

            AiAdvisorResult ai = aiAdvisorClient.ask(context, SYSTEM_PROMPT, buildAiPayload(job, profile, response));
            if (ai.isSuccess() && !isBlank(ai.getAnswer())) {
                response.setAiExplanation(ai.getAnswer().trim());
                response.setFallback(false);
            } else {
                response.setAiExplanation(fallbackExplanation(response));
                response.setFallback(true);
            }
            return response;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to build applicant recommendation.", ex);
        }
    }

    private ApplicationRecord findApplication(List<ApplicationRecord> applications,
                                              MoApplicantAiRecommendationRequest request) {
        String applicationId = firstNonBlank(request.getApplicationId(), request.getApplicantId());
        if (!isBlank(applicationId)) {
            return applications.stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElse(null);
        }
        String studentId = request.getStudentId();
        if (isBlank(studentId)) {
            return null;
        }
        return applications.stream()
                .filter(a -> request.getJobId().equals(a.getJobId()) && studentId.equals(a.getStudentId()))
                .findFirst()
                .orElse(null);
    }

    private MoApplicantAiRecommendationResponse buildRuleBasedResponse(JobPosting job,
                                                                        ApplicationRecord application,
                                                                        StudentProfile profile,
                                                                        List<JobPosting> jobs,
                                                                        List<ApplicationRecord> applications,
                                                                        List<HiringHistoryRecord> history) {
        List<String> requiredSkills = skillExtractionService.extractSkillsFromJob(job);
        List<String> applicantSkills = skillExtractionService.extractSkillsFromStudent(profile);
        Set<String> applicantSkillSet = new HashSet<>(applicantSkills);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String required : requiredSkills) {
            if (applicantSkillSet.contains(required)) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        double skillMatchScore = requiredSkills.isEmpty()
                ? 0.0
                : roundToTwoDecimals((double) matched.size() / requiredSkills.size());
        int currentWorkload = calculateCurrentWorkload(
                application.getStudentId(),
                application.getId(),
                jobs,
                applications,
                history
        );
        int jobHours = JobHoursUtil.resolveWeeklyHours(job);
        int projected = currentWorkload + jobHours;
        String workloadStatus = workloadStatus(projected);
        String recommendationLevel = recommendationLevel(skillMatchScore, workloadStatus);

        MoApplicantAiRecommendationResponse response = new MoApplicantAiRecommendationResponse();
        response.setStudentId(application.getStudentId());
        response.setJobId(job.getId());
        response.setSkillMatchScore(skillMatchScore);
        response.setMatchedSkills(matched);
        response.setMissingSkills(missing);
        response.setCurrentWorkloadHours(currentWorkload);
        response.setJobEstimatedHours(jobHours);
        response.setProjectedWorkloadHours(projected);
        response.setWorkloadStatus(workloadStatus);
        response.setRecommendationLevel(recommendationLevel);
        response.setAiExplanation(fallbackExplanation(response));
        response.setFallback(true);
        return response;
    }

    private int calculateCurrentWorkload(String studentId,
                                         String currentApplicationId,
                                         List<JobPosting> jobs,
                                         List<ApplicationRecord> applications,
                                         List<HiringHistoryRecord> history) {
        if (isBlank(studentId)) {
            return 0;
        }
        Map<String, JobPosting> jobById = jobs.stream()
                .filter(j -> j.getId() != null)
                .collect(Collectors.toMap(JobPosting::getId, Function.identity(), (a, b) -> a));
        Set<String> historyHiredIds = new HashSet<>();
        if (history != null) {
            for (HiringHistoryRecord record : history) {
                if (record != null && record.getHiredApplicationIds() != null) {
                    historyHiredIds.addAll(record.getHiredApplicationIds());
                }
            }
        }

        int total = 0;
        Set<String> countedApplicationIds = new HashSet<>();
        for (ApplicationRecord app : applications) {
            if (app == null || !studentId.equals(app.getStudentId())) {
                continue;
            }
            if (!isBlank(currentApplicationId) && currentApplicationId.equals(app.getId())) {
                continue;
            }
            boolean hired = "hired".equalsIgnoreCase(app.getStatus()) || historyHiredIds.contains(app.getId());
            if (!hired || !countedApplicationIds.add(app.getId())) {
                continue;
            }
            total += JobHoursUtil.resolveWeeklyHours(jobById.get(app.getJobId()));
        }
        return total;
    }

    private String buildAiPayload(JobPosting job, StudentProfile profile, MoApplicantAiRecommendationResponse response) {
        JsonObject payload = new JsonObject();
        payload.addProperty("jobTitle", job == null ? "" : safe(job.getTitle()));
        payload.addProperty("jobId", response.getJobId());
        payload.add("requiredSkills", toJsonArray(skillExtractionService.extractSkillsFromJob(job)));
        payload.add("applicantSkills", toJsonArray(skillExtractionService.extractSkillsFromStudent(profile)));
        payload.add("matchedSkills", toJsonArray(response.getMatchedSkills()));
        payload.add("missingSkills", toJsonArray(response.getMissingSkills()));
        payload.addProperty("skillMatchScore", response.getSkillMatchScore());
        payload.addProperty("currentWorkloadHours", response.getCurrentWorkloadHours());
        payload.addProperty("jobEstimatedHours", response.getJobEstimatedHours());
        payload.addProperty("projectedWorkloadHours", response.getProjectedWorkloadHours());
        payload.addProperty("workloadStatus", response.getWorkloadStatus());
        payload.addProperty("recommendationLevel", response.getRecommendationLevel());
        return gson.toJson(payload);
    }

    private JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (String value : values) {
                array.add(value);
            }
        }
        return array;
    }

    private String fallbackExplanation(MoApplicantAiRecommendationResponse response) {
        int pct = (int) Math.round(response.getSkillMatchScore() * 100);
        return "The applicant matches "
                + pct
                + "% of the required skills. Their projected workload is "
                + response.getProjectedWorkloadHours()
                + " hours per week, which is considered "
                + response.getWorkloadStatus()
                + ".";
    }

    private String workloadStatus(int projectedHours) {
        if (projectedHours <= 8) {
            return "Balanced";
        }
        if (projectedHours <= 12) {
            return "Near Limit";
        }
        return "Overloaded";
    }

    private String recommendationLevel(double skillMatchScore, String workloadStatus) {
        if (skillMatchScore >= 0.75 && "Balanced".equals(workloadStatus)) {
            return "Highly Recommended";
        }
        if (skillMatchScore >= 0.6 && !"Overloaded".equals(workloadStatus)) {
            return "Recommended";
        }
        if (skillMatchScore >= 0.5 && "Overloaded".equals(workloadStatus)) {
            return "Use with Caution";
        }
        return "Not Recommended";
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
