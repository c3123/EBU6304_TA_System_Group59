package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardResponse {
    private int totalJobs;
    private int totalUsers;
    private int totalApplications;
    private int totalStudents;
    private int totalTeachers;
    private int totalAdmins;
    private int totalActiveJobs;
    private int totalClosedJobs;
    private int totalDraftJobs;
    private int totalWithdrawnJobs;
    private int totalHiredRecords;
    private int totalUnfilledPositions;
    private int totalOpenApplications;
    private int totalWarningStudents;
    private int totalOverloadedStudents;
    private List<AdminDashboardUserItemResponse> users = new ArrayList<>();
    private List<AdminDashboardJobItemResponse> jobs = new ArrayList<>();
    private List<AdminDashboardWorkloadItemResponse> workload = new ArrayList<>();
    private List<AdminDashboardAlertResponse> alerts = new ArrayList<>();
    private List<AdminDashboardDailyCountItem> dailyJobPublications = new ArrayList<>();
    private List<AdminDashboardDailyCountItem> dailyApplications = new ArrayList<>();
    private List<AdminDashboardCountSlice> applicationsByDepartment = new ArrayList<>();
    private List<AdminDashboardCountSlice> applicationsByStatus = new ArrayList<>();

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

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(int totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getTotalActiveJobs() {
        return totalActiveJobs;
    }

    public void setTotalActiveJobs(int totalActiveJobs) {
        this.totalActiveJobs = totalActiveJobs;
    }

    public int getTotalClosedJobs() {
        return totalClosedJobs;
    }

    public void setTotalClosedJobs(int totalClosedJobs) {
        this.totalClosedJobs = totalClosedJobs;
    }

    public int getTotalDraftJobs() {
        return totalDraftJobs;
    }

    public void setTotalDraftJobs(int totalDraftJobs) {
        this.totalDraftJobs = totalDraftJobs;
    }

    public int getTotalWithdrawnJobs() {
        return totalWithdrawnJobs;
    }

    public void setTotalWithdrawnJobs(int totalWithdrawnJobs) {
        this.totalWithdrawnJobs = totalWithdrawnJobs;
    }

    public int getTotalHiredRecords() {
        return totalHiredRecords;
    }

    public void setTotalHiredRecords(int totalHiredRecords) {
        this.totalHiredRecords = totalHiredRecords;
    }

    public int getTotalUnfilledPositions() {
        return totalUnfilledPositions;
    }

    public void setTotalUnfilledPositions(int totalUnfilledPositions) {
        this.totalUnfilledPositions = totalUnfilledPositions;
    }

    public int getTotalOpenApplications() {
        return totalOpenApplications;
    }

    public void setTotalOpenApplications(int totalOpenApplications) {
        this.totalOpenApplications = totalOpenApplications;
    }

    public int getTotalWarningStudents() {
        return totalWarningStudents;
    }

    public void setTotalWarningStudents(int totalWarningStudents) {
        this.totalWarningStudents = totalWarningStudents;
    }

    public int getTotalOverloadedStudents() {
        return totalOverloadedStudents;
    }

    public void setTotalOverloadedStudents(int totalOverloadedStudents) {
        this.totalOverloadedStudents = totalOverloadedStudents;
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

    public List<AdminDashboardWorkloadItemResponse> getWorkload() {
        return workload;
    }

    public void setWorkload(List<AdminDashboardWorkloadItemResponse> workload) {
        this.workload = workload;
    }

    public List<AdminDashboardAlertResponse> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AdminDashboardAlertResponse> alerts) {
        this.alerts = alerts == null ? new ArrayList<>() : alerts;
    }

    public List<AdminDashboardDailyCountItem> getDailyJobPublications() {
        return dailyJobPublications;
    }

    public void setDailyJobPublications(List<AdminDashboardDailyCountItem> dailyJobPublications) {
        this.dailyJobPublications = dailyJobPublications == null ? new ArrayList<>() : dailyJobPublications;
    }

    public List<AdminDashboardDailyCountItem> getDailyApplications() {
        return dailyApplications;
    }

    public void setDailyApplications(List<AdminDashboardDailyCountItem> dailyApplications) {
        this.dailyApplications = dailyApplications == null ? new ArrayList<>() : dailyApplications;
    }

    public List<AdminDashboardCountSlice> getApplicationsByDepartment() {
        return applicationsByDepartment;
    }

    public void setApplicationsByDepartment(List<AdminDashboardCountSlice> applicationsByDepartment) {
        this.applicationsByDepartment = applicationsByDepartment == null ? new ArrayList<>() : applicationsByDepartment;
    }

    public List<AdminDashboardCountSlice> getApplicationsByStatus() {
        return applicationsByStatus;
    }

    public void setApplicationsByStatus(List<AdminDashboardCountSlice> applicationsByStatus) {
        this.applicationsByStatus = applicationsByStatus == null ? new ArrayList<>() : applicationsByStatus;
    }
}
