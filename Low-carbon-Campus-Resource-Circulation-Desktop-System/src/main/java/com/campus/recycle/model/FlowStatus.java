package com.campus.recycle.model;

/**
 * 流转记录状态：意向沟通进行中 / 已完成 / 已取消。
 */
public enum FlowStatus {
    PENDING("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String displayName;

    FlowStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
