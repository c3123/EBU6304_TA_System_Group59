package com.ta.dto.mo;

import java.util.List;

/**
 * GET /api/mo/applications response body.
 */
public class MoApplicationListResponse {
    private List<MoApplicationListItemResponse> items;

    public List<MoApplicationListItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoApplicationListItemResponse> items) {
        this.items = items;
    }
}
