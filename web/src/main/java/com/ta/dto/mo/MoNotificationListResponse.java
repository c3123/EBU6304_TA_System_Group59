package com.ta.dto.mo;

import java.util.ArrayList;
import java.util.List;

public class MoNotificationListResponse {
    private Integer unreadCount;
    private List<MoNotificationItemResponse> items = new ArrayList<>();

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<MoNotificationItemResponse> getItems() {
        return items;
    }

    public void setItems(List<MoNotificationItemResponse> items) {
        this.items = items;
    }
}
