package com.campus.recycle.model;

/**
 * 用户角色：普通学生 / 系统管理员。
 */
public enum UserRole {
    STUDENT("普通学生"),
    ADMIN("系统管理员");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
