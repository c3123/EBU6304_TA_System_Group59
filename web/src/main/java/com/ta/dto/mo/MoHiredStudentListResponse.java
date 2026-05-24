package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoHiredStudentListResponse {
    private List<MoHiredStudentItemResponse> items = new ArrayList<>();

    public List<MoHiredStudentItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoHiredStudentItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
