package com.campus.recycle.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体（不可变 ADT）。
 *
 * <p>表示系统账号，含身份角色与信用分数。密码仅以「盐 + 哈希」形式存在，
 * 构造器对非法输入进行防御性校验。</p>
 */
public final class User {

    private final Integer id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final UserRole role;
    private final int creditScore;
    private final String nickname;
    private final String email;
    private final LocalDateTime createdAt;

    public User(Integer id, String username, String passwordHash, String salt,
                UserRole role, int creditScore, String nickname, String email,
                LocalDateTime createdAt) {
        this.id = id;
        this.username = requireNotBlank(username, "用户名");
        this.passwordHash = requireNotBlank(passwordHash, "密码哈希");
        this.salt = requireNotBlank(salt, "盐");
        this.role = role == null ? UserRole.STUDENT : role;
        if (creditScore < 0) {
            throw new IllegalArgumentException("信用分数不能为负");
        }
        this.creditScore = creditScore;
        this.nickname = nickname;
        this.email = email;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "不能为空");
        }
        return value;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public UserRole getRole() {
        return role;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role
                + ", creditScore=" + creditScore + '}';
    }
}
