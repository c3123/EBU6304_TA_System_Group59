package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * Cross-table recruitment summary for the admin Recruitment Results leadership view.
 * KPIs plus department-level hired and vacancy breakdown.
 */
public class AdminRecruitmentOutcomeResponse {
    /** Sum of {@code positions} for jobs that are not withdrawn. */
    private int totalPositionSlots;
    /** Jobs not withdrawn and in closed recruitment state. */
    private int closedJobs;
    /** Jobs not withdrawn, not closed, with status open (actively recruiting). */
    private int recruitingJobs;
    /** Active application records across all jobs. */
    private int totalApplications;
    /** Active applications with status hired. */
    private int totalHired;
    /** Sum over non-withdrawn jobs of max(0, positions - hiredCount). */
    private int totalVacancies;
    /** Per department: hired applications and unfilled slots (blank department label is Unspecified). */
    private List<AdminRecruitmentOutcomeDepartmentRow> departments = new ArrayList<>();
    /** Jobs with the largest unfilled slot counts (non-withdrawn, vacancy greater than zero), capped by request. */
    private List<AdminRecruitmentOutcomeVacancyRow> topVacancyJobs = new ArrayList<>();
    /** Effective cap used for {@link #topVacancyJobs} (echo of {@code vacancyTop} query). */
    private int vacancyTopLimit;
    /** When this snapshot was built (UTC, ISO-8601 seconds). */
    private String generatedAt;
    /** Echo of optional filter: first day (yyyy-MM-dd), or blank. */
    private String jobSince;
    /** Echo of optional filter: last day (yyyy-MM-dd), or blank. */
    private String jobUntil;

    public int getTotalPositionSlots() {
        return totalPositionSlots;
    }

    public void setTotalPositionSlots(int totalPositionSlots) {
        this.totalPositionSlots = totalPositionSlots;
    }

    public int getClosedJobs() {
        return closedJobs;
    }

    public void setClosedJobs(int closedJobs) {
        this.closedJobs = closedJobs;
    }

    public int getRecruitingJobs() {
        return recruitingJobs;
    }

    public void setRecruitingJobs(int recruitingJobs) {
        this.recruitingJobs = recruitingJobs;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getTotalHired() {
        return totalHired;
    }

    public void setTotalHired(int totalHired) {
        this.totalHired = totalHired;
    }

    public int getTotalVacancies() {
        return totalVacancies;
    }

    public void setTotalVacancies(int totalVacancies) {
        this.totalVacancies = totalVacancies;
    }

    public List<AdminRecruitmentOutcomeDepartmentRow> getDepartments() {
        return departments;
    }

    public void setDepartments(List<AdminRecruitmentOutcomeDepartmentRow> departments) {
        this.departments = departments != null ? departments : new ArrayList<>();
    }

    public List<AdminRecruitmentOutcomeVacancyRow> getTopVacancyJobs() {
        return topVacancyJobs;
    }

    public void setTopVacancyJobs(List<AdminRecruitmentOutcomeVacancyRow> topVacancyJobs) {
        this.topVacancyJobs = topVacancyJobs != null ? topVacancyJobs : new ArrayList<>();
    }

    public int getVacancyTopLimit() {
        return vacancyTopLimit;
    }

    public void setVacancyTopLimit(int vacancyTopLimit) {
        this.vacancyTopLimit = vacancyTopLimit;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getJobSince() {
        return jobSince;
    }

    public void setJobSince(String jobSince) {
        this.jobSince = jobSince;
    }

    public String getJobUntil() {
        return jobUntil;
    }

    public void setJobUntil(String jobUntil) {
        this.jobUntil = jobUntil;
    }
}
