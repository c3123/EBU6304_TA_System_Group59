package com.ta.service.mo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ta.constant.ErrorCodes;
import com.ta.dto.mo.MoApplicationExportRow;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoApplicationExportService {
    private static final Gson EXPORT_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CSV_HEADER = "name,applicant_id,major,application_time,status,skills";

    public ExportFile buildExport(ServletContext context, String moId, String jobId, String scope, String format) {
        validateRequest(jobId, scope, format);
        try {
            List<JobPosting> jobs = JsonUtility.loadJobs(context);
            JobPosting job = jobs.stream()
                    .filter(j -> jobId.equals(j.getId()))
                    .findFirst()
                    .orElseThrow(() -> new MoBusinessException(
                            ErrorCodes.JOB_NOT_FOUND,
                            "Job not found.",
                            HttpServletResponse.SC_NOT_FOUND
                    ));
            if (!moId.equals(job.getTeacherId())) {
                throw new MoBusinessException(
                        ErrorCodes.FORBIDDEN_NOT_OWNER,
                        "You can only export applications for your own jobs.",
                        HttpServletResponse.SC_FORBIDDEN
                );
            }

            List<MoApplicationExportRow> rows = buildRows(context, jobId, scope);
            if ("json".equalsIgnoreCase(format)) {
                String json = EXPORT_GSON.toJson(rows);
                return new ExportFile(fileName(job, scope, "json"), "application/json;charset=UTF-8", json.getBytes(StandardCharsets.UTF_8));
            }

            String csv = toCsv(rows);
            byte[] csvBytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
            return new ExportFile(fileName(job, scope, "csv"), "text/csv;charset=UTF-8", csvBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export applications.", e);
        }
    }

    private void validateRequest(String jobId, String scope, String format) {
        if (jobId == null || jobId.isBlank()) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "jobId is required.", HttpServletResponse.SC_BAD_REQUEST);
        }
        if (!"all".equalsIgnoreCase(scope) && !"shortlisted".equalsIgnoreCase(scope)) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "scope must be all or shortlisted.", HttpServletResponse.SC_BAD_REQUEST);
        }
        if (!"csv".equalsIgnoreCase(format) && !"json".equalsIgnoreCase(format)) {
            throw new MoBusinessException(ErrorCodes.VALIDATION_ERROR, "format must be csv or json.", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private List<MoApplicationExportRow> buildRows(ServletContext context, String jobId, String scope) throws IOException {
        List<ApplicationRecord> applications = JsonUtility.loadApplications(context);
        List<StudentProfile> profiles = JsonUtility.loadStudents(context);
        Map<String, StudentProfile> profileByUserId = profiles.stream()
                .filter(profile -> profile.getUserId() != null)
                .collect(Collectors.toMap(StudentProfile::getUserId, Function.identity(), (a, b) -> a));

        List<MoApplicationExportRow> rows = new ArrayList<>();
        for (ApplicationRecord application : applications) {
            if (!application.isActive() || !jobId.equals(application.getJobId())) {
                continue;
            }
            if ("shortlisted".equalsIgnoreCase(scope) && !"shortlisted".equalsIgnoreCase(application.getStatus())) {
                continue;
            }
            StudentProfile profile = profileByUserId.get(application.getStudentId());
            rows.add(toRow(application, profile));
        }
        rows.sort(Comparator.comparing(MoApplicationExportRow::getApplication_time, Comparator.nullsLast(String::compareTo)).reversed());
        return rows;
    }

    private MoApplicationExportRow toRow(ApplicationRecord application, StudentProfile profile) {
        MoApplicationExportRow row = new MoApplicationExportRow();
        row.setName(firstNonBlank(application.getStudentName(), profile == null ? null : profile.getName(), application.getStudentId()));
        row.setApplicant_id(firstNonBlank(application.getStudentNo(), profile == null ? null : profile.getStudentId(), application.getStudentId()));
        row.setMajor(firstNonBlank(profile == null ? null : profile.getProgramme(), application.getCourseGrade(), ""));
        row.setApplication_time(safe(application.getAppliedAt()));
        row.setStatus(safe(application.getStatus()));
        row.setSkills(safe(profile == null ? null : profile.getSkills()));
        return row;
    }

    private String toCsv(List<MoApplicationExportRow> rows) {
        StringBuilder sb = new StringBuilder(CSV_HEADER).append("\r\n");
        for (MoApplicationExportRow row : rows) {
            sb.append(csvCell(row.getName())).append(',')
                    .append(csvCell(row.getApplicant_id())).append(',')
                    .append(csvCell(row.getMajor())).append(',')
                    .append(csvCell(row.getApplication_time())).append(',')
                    .append(csvCell(row.getStatus())).append(',')
                    .append(csvCell(row.getSkills())).append("\r\n");
        }
        return sb.toString();
    }

    private String csvCell(String value) {
        String text = safe(value);
        boolean needsQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        String escaped = text.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }

    private String fileName(JobPosting job, String scope, String extension) {
        String base = firstNonBlank(job.getModuleCode(), job.getTitle(), job.getId());
        String safeBase = base.replaceAll("[^A-Za-z0-9_-]+", "_");
        return safeBase + "_" + scope.toLowerCase() + "_applicants." + extension.toLowerCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class ExportFile {
        private final String fileName;
        private final String contentType;
        private final byte[] content;

        public ExportFile(String fileName, String contentType, byte[] content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
