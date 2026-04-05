package com.ta.dto.student;

import java.util.List;

public class StudentJobListResponse {
    private List<StudentJobItemResponse> items;

    public List<StudentJobItemResponse> getItems() {
        return items;
    }

    public void setItems(List<StudentJobItemResponse> items) {
        this.items = items;
    }
}
