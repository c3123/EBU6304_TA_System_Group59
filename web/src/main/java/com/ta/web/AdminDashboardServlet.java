package com.ta.web;

import com.google.gson.Gson;
import com.ta.dto.admin.AdminDashboardJobItemResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardUserItemResponse;
import com.ta.dto.admin.AdminDashboardWorkloadItemResponse;
import com.ta.dto.mo.ApiResponse;
import com.ta.model.ApplicationRecord;
import com.ta.model.JobPosting;
import com.ta.model.User;
import com.ta.util.JsonUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/api/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<User> users = JsonUtility.loadUsers(getServletContext());
            List<JobPosting> jobs = JsonUtility.loadJobs(getServletContext());
            List<ApplicationRecord> applications = JsonUtility.loadApplications(getServletContext());

            AdminDashboardResponse data = new AdminDashboardResponse();
            data.setTotalUsers(users.size());
            data.setTotalJobs(jobs.size());
            data.setTotalApplications((int) applications.stream().filter(ApplicationRecord::isActive).count());
            data.setUsers(toUsers(users));
            data.setJobs(toJobs(jobs));
            data.setWorkload(toWorkload(applications, jobs));

            writeApi(resp, HttpServletResponse.SC_OK, true, "OK", "success", data);
        } catch (Exception ex) {
            writeApi(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "INTERNAL_ERROR", ex.getMessage(), null);
        }
    }

    private List<AdminDashboardUserItemResponse> toUsers(List<User> users) {
        List<AdminDashboardUserItemResponse> items = new ArrayList<>();
        for (User user : users) {
            AdminDashboardUserItemResponse item = new AdminDashboardUserItemResponse();
            item.setId(user.getId());
            item.setName(user.getName());
            item.setEmail(user.getEmail());
            item.setRole(user.getRole());
            items.add(item);
        }
        return items;
    }

    private List<AdminDashboardJobItemResponse> toJobs(List<JobPosting> jobs) {
        List<AdminDashboardJobItemResponse> items = new ArrayList<>();
        for (JobPosting job : jobs) {
            AdminDashboardJobItemResponse item = new AdminDashboardJobItemResponse();
            item.setId(job.getId());
            item.setModuleCode(job.getModuleCode());
            item.setTitle(job.getTitle());
            item.setTeacherName(job.getTeacherName());
            item.setStatus(job.getStatus());
            item.setPositions(job.getPositions());
            item.setRecruitmentClosed(Boolean.TRUE.equals(job.getRecruitmentClosed()));
            item.setClosedAt(job.getClosedAt());
            items.add(item);
        }
        return items;
    }

    private List<AdminDashboardWorkloadItemResponse> toWorkload(List<ApplicationRecord> applications, List<JobPosting> jobs) {
        Map<String, Integer> jobHoursById = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            if (job.getId() != null) {
                jobHoursById.put(job.getId(), estimateWeeklyHours(job));
            }
        }

        Map<String, AdminDashboardWorkloadItemResponse> workloadByStudent = new LinkedHashMap<>();
        for (ApplicationRecord app : applications) {
            if (!isCountedApplication(app)) {
                continue;
            }
            if (app.getStudentId() == null || app.getStudentId().isBlank()) {
                continue;
            }

            int jobHours = jobHoursById.getOrDefault(app.getJobId(), 0);
            AdminDashboardWorkloadItemResponse item = workloadByStudent.computeIfAbsent(app.getStudentId(), key -> {
                AdminDashboardWorkloadItemResponse created = new AdminDashboardWorkloadItemResponse();
                created.setStudentId(key);
                created.setStudentName(app.getStudentName() == null ? key : app.getStudentName());
                created.setHiredCount(0);
                created.setWeeklyHours(0);
                return created;
            });

            item.setHiredCount(item.getHiredCount() + 1);
            item.setWeeklyHours(item.getWeeklyHours() + jobHours);
        }

        List<AdminDashboardWorkloadItemResponse> items = new ArrayList<>(workloadByStudent.values());
        items.sort(Comparator.comparingInt(AdminDashboardWorkloadItemResponse::getWeeklyHours).reversed());
        return items;
    }

    /**
     * Count active applications that still represent future workload.
     * Rejected applications are excluded.
     */
    private boolean isCountedApplication(ApplicationRecord app) {
        if (app == null || !app.isActive()) {
            return false;
        }
        return !"rejected".equalsIgnoreCase(app.getStatus());
    }

    /**
     * For ranged hours, use the midpoint value.
     */
    private int estimateWeeklyHours(JobPosting job) {
        if (job == null) {
            return 0;
        }

        Integer hourMin = job.getHourMin();
        Integer hourMax = job.getHourMax();
        if (hourMin != null && hourMax != null) {
            return Math.max(Math.round((hourMin + hourMax) / 2.0f), 0);
        }
        if (hourMin != null) {
            return Math.max(hourMin, 0);
        }
        if (hourMax != null) {
            return Math.max(hourMax, 0);
        }
        return Math.max(job.getHours(), 0);
    }

    private void writeApi(HttpServletResponse resp,
                          int httpStatus,
                          boolean success,
                          String code,
                          String message,
                          Object data) throws IOException {
        ApiResponse<Object> api = new ApiResponse<>();
        api.setSuccess(success);
        api.setCode(code);
        api.setMessage(message);
        api.setData(data);

        resp.setStatus(httpStatus);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(api));
    }
}
