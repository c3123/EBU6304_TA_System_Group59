package com.ta.service.student;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ta.dto.student.AiAdvisorResponse;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.service.student.ai.AiAdvisorClient;
import com.ta.service.student.ai.AiAdvisorClient.AiAdvisorResult;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiAdvisorService {
    private static final String SYSTEM_PROMPT =
            "You are an AI advisor for a university TA recruitment platform.\n"
                    + "Your job is to help students understand TA job recommendations and skill gaps.\n"
                    + "Use only the provided structured matching results.\n"
                    + "Do not invent skills, jobs, scores, or experiences.\n"
                    + "Keep responses concise, practical, and professional.\n"
                    + "Use plain natural language.\n"
                    + "Do not use markdown.\n"
                    + "Do not use bullet points.\n"
                    + "Do not use asterisks, hashtags, or special formatting symbols.\n"
                    + "Do not generate long explanations.\n"
                    + "Keep the response within 2-4 short sentences.\n"
                    + "Write like a clean in-platform assistant message.\n"
                    + "If information is insufficient, clearly say so.";

    private final JobMatchingService jobMatchingService;
    private final SkillExtractionService skillExtractionService;
    private final AiAdvisorClient aiAdvisorClient;
    private final Gson gson = new Gson();

    public AiAdvisorService() {
        this(new JobMatchingService(), new SkillExtractionService(), new AiAdvisorClient());
    }

    public AiAdvisorService(JobMatchingService jobMatchingService,
                            SkillExtractionService skillExtractionService,
                            AiAdvisorClient aiAdvisorClient) {
        this.jobMatchingService = jobMatchingService != null ? jobMatchingService : new JobMatchingService();
        this.skillExtractionService = skillExtractionService != null ? skillExtractionService : new SkillExtractionService();
        this.aiAdvisorClient = aiAdvisorClient != null ? aiAdvisorClient : new AiAdvisorClient();
    }

    public AiAdvisorResponse advise(ServletContext context, String studentUserId, String question) {
        try {
            StudentProfile profile = loadStudentProfile(context, studentUserId);
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            List<JobMatchResult> recommendations = topRecommendations(
                    jobMatchingService.getRecommendedJobs(profile, jobs),
                    3
            );
            List<String> studentSkills = skillExtractionService.extractSkillsFromStudent(profile);
            String userPayload = buildUserPayload(question, studentSkills, recommendations);

            AiAdvisorResult result = aiAdvisorClient.ask(context, SYSTEM_PROMPT, userPayload);
            if (result.isSuccess()) {
                return response(result.getAnswer(), false);
            }
            return response(fallbackAnswer(recommendations), true);
        } catch (IOException ex) {
            return response(fallbackAnswer(new ArrayList<>()), true);
        }
    }

    private StudentProfile loadStudentProfile(ServletContext context, String studentUserId) throws IOException {
        if (isBlank(studentUserId)) {
            return null;
        }
        List<StudentProfile> profiles = JsonUtility.loadStudents(context);
        return profiles.stream()
                .filter(profile -> studentUserId.equals(profile.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private List<JobMatchResult> topRecommendations(List<JobMatchResult> recommendations, int limit) {
        List<JobMatchResult> top = new ArrayList<>();
        if (recommendations == null || recommendations.isEmpty()) {
            return top;
        }
        for (JobMatchResult result : recommendations) {
            if (top.size() >= limit) {
                break;
            }
            top.add(result);
        }
        return top;
    }

    private String buildUserPayload(String question, List<String> studentSkills, List<JobMatchResult> recommendations) {
        JsonObject payload = new JsonObject();
        payload.addProperty("question", question);
        payload.add("studentSkills", toJsonArray(studentSkills));

        JsonArray jobs = new JsonArray();
        for (JobMatchResult recommendation : recommendations) {
            JobPosting job = recommendation.getJob();
            JsonObject jobJson = new JsonObject();
            jobJson.addProperty("id", job == null ? "" : safe(job.getId()));
            jobJson.addProperty("title", job == null ? "" : safe(job.getTitle()));
            jobJson.addProperty("moduleCode", job == null ? "" : safe(job.getModuleCode()));
            jobJson.addProperty("teacherName", job == null ? "" : safe(job.getTeacherName()));
            jobJson.addProperty("department", job == null ? "" : safe(job.getDepartment()));
            jobJson.addProperty("hours", job == null ? 0 : job.getHours());
            jobJson.addProperty("hourMin", job == null || job.getHourMin() == null ? 0 : job.getHourMin());
            jobJson.addProperty("hourMax", job == null || job.getHourMax() == null ? 0 : job.getHourMax());
            jobJson.addProperty("positions", job == null ? 0 : job.getPositions());
            jobJson.addProperty("deadline", job == null ? "" : safe(job.getDeadline()));
            jobJson.addProperty("schedule", job == null ? "" : safe(job.getSchedule()));
            jobJson.addProperty("location", job == null ? "" : safe(job.getLocation()));
            jobJson.addProperty("status", job == null ? "" : safe(job.getStatus()));
            jobJson.addProperty("matchScore", roundToTwoDecimals(recommendation.getMatchScore()));
            jobJson.add("requiredSkills", toJsonArray(recommendation.getRequiredSkills()));
            jobJson.add("matchedSkills", toJsonArray(recommendation.getMatchedSkills()));
            jobJson.add("missingSkills", toJsonArray(recommendation.getMissingSkills()));
            jobs.add(jobJson);
        }
        payload.add("topRecommendedJobs", jobs);
        return gson.toJson(payload);
    }

    private JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private String fallbackAnswer(List<JobMatchResult> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "I do not have enough matching data yet. Add skills to your profile and check available jobs again.";
        }

        JobMatchResult best = recommendations.get(0);
        JobPosting job = best.getJob();
        String title = job != null && !isBlank(job.getTitle()) ? job.getTitle() : "the top recommended role";
        int percent = (int) Math.round(best.getMatchScore() * 100);
        return "Based on the current matching results, your strongest role appears to be "
                + title
                + " with a match rate of "
                + percent
                + "%. Matched skills: "
                + displaySkills(best.getMatchedSkills(), "none detected")
                + ". Skills to improve: "
                + displaySkills(best.getMissingSkills(), "no major missing skills")
                + ".";
    }

    private AiAdvisorResponse response(String answer, boolean fallback) {
        AiAdvisorResponse response = new AiAdvisorResponse();
        response.setAnswer(answer);
        response.setFallback(fallback);
        return response;
    }

    private String displaySkills(List<String> skills, String emptyText) {
        if (skills == null || skills.isEmpty()) {
            return emptyText;
        }
        return String.join(", ", skills);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
