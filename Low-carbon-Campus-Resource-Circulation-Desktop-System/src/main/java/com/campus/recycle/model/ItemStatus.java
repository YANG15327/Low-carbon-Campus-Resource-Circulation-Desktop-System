package com.campus.recycle.model;

/**
 * 闲置物品状态。
 */
public enum ItemStatus {
    AVAILABLE("在售"),
    RESERVED("已预订"),
    SOLD("已售出"),
    REMOVED("已下架");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
