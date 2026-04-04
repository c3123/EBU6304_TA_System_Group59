package com.ta.dto.mo;

import java.util.List;

/**
 * GET /api/mo/demands response body.
 */
public class MoDemandListResponse {
    private List<MoDemandItemResponse> items;

    public List<MoDemandItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoDemandItemResponse> items) {
        this.items = items;
    }
}
