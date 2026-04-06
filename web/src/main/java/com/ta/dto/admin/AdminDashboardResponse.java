package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardResponse {
    private int totalJobs;
    private int totalUsers;
    private int totalApplications;
    private List<AdminDashboardUserItemResponse> users = new ArrayList<>();
    private List<AdminDashboardJobItemResponse> jobs = new ArrayList<>();

    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public List<AdminDashboardUserItemResponse> getUsers() {
        return users;
    }

    public void setUsers(List<AdminDashboardUserItemResponse> users) {
        this.users = users;
    }

    public List<AdminDashboardJobItemResponse> getJobs() {
        return jobs;
    }

    public void setJobs(List<AdminDashboardJobItemResponse> jobs) {
        this.jobs = jobs;
    }
}
