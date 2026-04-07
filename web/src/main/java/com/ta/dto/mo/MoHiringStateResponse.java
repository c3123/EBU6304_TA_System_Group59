package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoHiringStateResponse {
    private List<MoHiringStateItemResponse> items = new ArrayList<>();

    public List<MoHiringStateItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoHiringStateItemResponse> items) {
        this.items = items;
    }
}
