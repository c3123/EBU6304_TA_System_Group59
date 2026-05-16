package com.ta.dto.admin;

import java.util.ArrayList;
import java.util.List;

public class AdminAnnouncementListResponse {
    private List<AdminAnnouncementSummaryItem> items = new ArrayList<>();

    public List<AdminAnnouncementSummaryItem> getItems() {
        return items;
    }

    public void setItems(List<AdminAnnouncementSummaryItem> items) {
        this.items = items;
    }
}
