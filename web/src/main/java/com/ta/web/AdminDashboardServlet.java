package com.ta.web;

import com.google.gson.Gson;
import com.ta.dto.admin.AdminDashboardJobItemResponse;
import com.ta.dto.admin.AdminDashboardResponse;
import com.ta.dto.admin.AdminDashboardUserItemResponse;
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
import java.util.List;

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
