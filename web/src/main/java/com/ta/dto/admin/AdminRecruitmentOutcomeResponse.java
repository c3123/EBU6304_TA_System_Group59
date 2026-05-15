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
    /** Per department: hired applications and unfilled slots (blank department label is 未填). */
    private List<AdminRecruitmentOutcomeDepartmentRow> departments = new ArrayList<>();

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
}
