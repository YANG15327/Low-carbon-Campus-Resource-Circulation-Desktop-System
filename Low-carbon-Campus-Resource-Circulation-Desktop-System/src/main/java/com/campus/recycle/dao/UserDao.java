package com.campus.recycle.dao;

import com.campus.recycle.model.User;
import com.campus.recycle.model.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问对象（DAO 模式）。
 */
public class UserDao extends DaoSupport {

    public Optional<User> findById(int id) {
        return queryOptional(
                "SELECT * FROM user WHERE id = ?",
                ps -> ps.setInt(1, id),
                this::mapRow);
    }

    public Optional<User> findByUsername(String username) {
        return queryOptional(
                "SELECT * FROM user WHERE username = ?",
                ps -> ps.setString(1, username),
                this::mapRow);
    }

    public List<User> findAll() {
        return queryList("SELECT * FROM user ORDER BY id", ps -> {
        }, this::mapRow);
    }

    public boolean existsByUsername(String username) {
        return queryOptional(
                "SELECT 1 FROM user WHERE username = ?",
                ps -> ps.setString(1, username),
                rs -> 1).isPresent();
    }

    /** 插入用户，返回自增主键。 */
    public int insert(User user) {
        return insert(
                "INSERT INTO user (username, password_hash, salt, role, credit_score, nickname, email) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getSalt());
                    ps.setString(4, user.getRole().name());
                    ps.setInt(5, user.getCreditScore());
                    setNullableString(ps, 6, user.getNickname());
                    setNullableString(ps, 7, user.getEmail());
                });
    }

    /** 更新用户昵称与邮箱（不可变实体，仅更新可编辑字段）。 */
    public boolean updateProfile(int id, String nickname, String email) {
        return update(
                "UPDATE user SET nickname = ?, email = ? WHERE id = ?",
                ps -> {
                    setNullableString(ps, 1, nickname);
                    setNullableString(ps, 2, email);
                    ps.setInt(3, id);
                }) > 0;
    }

    /** 更新密码（盐 + 哈希）。 */
    public boolean updatePassword(int id, String passwordHash, String salt) {
        return update(
                "UPDATE user SET password_hash = ?, salt = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, passwordHash);
                    ps.setString(2, salt);
                    ps.setInt(3, id);
                }) > 0;
    }

    /** 更新信用分数。 */
    public boolean updateCreditScore(int id, int creditScore) {
        return update(
                "UPDATE user SET credit_score = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, creditScore);
                    ps.setInt(2, id);
                }) > 0;
    }

    public boolean delete(int id) {
        return update("DELETE FROM user WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                UserRole.valueOf(rs.getString("role")),
                rs.getInt("credit_score"),
                rs.getString("nickname"),
                rs.getString("email"),
                parseDateTime(rs.getString("created_at")));
    }
}
