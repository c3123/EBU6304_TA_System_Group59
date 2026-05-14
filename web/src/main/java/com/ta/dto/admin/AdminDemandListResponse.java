package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminDemandListResponse {
    private List<AdminDemandItemResponse> items = new ArrayList<>();

    public List<AdminDemandItemResponse> getItems() {
        return items;
    }

    public void setItems(List<AdminDemandItemResponse> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
