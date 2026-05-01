package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoJobHistoryResponse {
    private List<MoJobHistoryItemResponse> items = new ArrayList<>();

    public List<MoJobHistoryItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoJobHistoryItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
