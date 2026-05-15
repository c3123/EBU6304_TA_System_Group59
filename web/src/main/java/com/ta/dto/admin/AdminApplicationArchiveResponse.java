package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminApplicationArchiveResponse {
    private List<AdminApplicationArchiveItemResponse> items = new ArrayList<>();

    public List<AdminApplicationArchiveItemResponse> getItems() {
        return items;
    }

    public void setItems(List<AdminApplicationArchiveItemResponse> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
