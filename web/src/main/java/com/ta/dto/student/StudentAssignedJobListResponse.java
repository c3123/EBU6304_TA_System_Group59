package com.ta.dto.student;

import java.util.ArrayList;
import java.util.List;

public class StudentAssignedJobListResponse {
    private List<StudentAssignedJobItemResponse> items = new ArrayList<>();

    public List<StudentAssignedJobItemResponse> getItems() {
        return items;
    }

    public void setItems(List<StudentAssignedJobItemResponse> items) {
        this.items = items;
    }
}
