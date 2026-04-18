package com.ta.dto.student;

import java.util.List;

public class StudentApplicationListResponse {
    private List<StudentApplicationItemResponse> items;

    public List<StudentApplicationItemResponse> getItems() {
        return items;
    }

    public void setItems(List<StudentApplicationItemResponse> items) {
        this.items = items;
    }
}
