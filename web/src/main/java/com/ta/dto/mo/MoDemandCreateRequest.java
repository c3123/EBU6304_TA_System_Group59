package com.ta.dto.mo;

/**
 * POST /api/mo/demands request body.
 */
public class MoDemandCreateRequest {
    private String courseName;
    private Integer plannedCount;
    private Integer hourMin;
    private Integer hourMax;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(Integer plannedCount) {
        this.plannedCount = plannedCount;
    }

    public Integer getHourMin() {
        return hourMin;
    }

    public void setHourMin(Integer hourMin) {
        this.hourMin = hourMin;
    }

    public Integer getHourMax() {
        return hourMax;
    }

    public void setHourMax(Integer hourMax) {
        this.hourMax = hourMax;
    }
}
