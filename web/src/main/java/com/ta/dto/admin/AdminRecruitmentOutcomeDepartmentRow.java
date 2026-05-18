package com.ta.dto.admin;

/**
 * One row in the recruitment outcome department breakdown (hired vs unfilled slots).
 */
public class AdminRecruitmentOutcomeDepartmentRow {
    private String department;
    private int hiredCount;
    private int vacancyCount;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getHiredCount() {
        return hiredCount;
    }

    public void setHiredCount(int hiredCount) {
        this.hiredCount = hiredCount;
    }

    public int getVacancyCount() {
        return vacancyCount;
    }

    public void setVacancyCount(int vacancyCount) {
        this.vacancyCount = vacancyCount;
    }
}
