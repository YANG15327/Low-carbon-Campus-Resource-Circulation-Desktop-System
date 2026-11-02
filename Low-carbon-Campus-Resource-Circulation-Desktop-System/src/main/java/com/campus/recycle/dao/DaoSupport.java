package com.campus.recycle.dao;

import com.campus.recycle.exception.DataAccessException;
import com.campus.recycle.util.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO 基类：封装 JDBC 样板代码（连接获取、参数绑定、结果映射、异常转换）。
 *
 * <p>各具体 DAO 只需提供 SQL 与行映射逻辑，避免重复的 try-catch 与资源关闭
 * 代码（消除「重复代码」坏味道）。</p>
 */
public abstract class DaoSupport {

    @FunctionalInterface
    public interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected Connection getConnection() {
        return DBManager.getInstance().getConnection();
    }

    protected <T> List<T> queryList(String sql, ParamSetter setter, RowMapper<T> mapper) {
        List<T> result = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("数据库查询失败: " + sql, e);
        }
        return result;
    }

    protected <T> Optional<T> queryOptional(String sql, ParamSetter setter, RowMapper<T> mapper) {
        List<T> list = queryList(sql, setter, mapper);
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
    }

    /** 执行插入并返回自增主键，若驱动不支持返回 -1。 */
    protected int insert(String sql, ParamSetter setter) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setter.set(ps);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("插入失败，未影响任何行", null);
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new DataAccessException("数据库插入失败: " + sql, e);
        }
    }

    /** 执行更新/删除，返回受影响行数。 */
    protected int update(String sql, ParamSetter setter) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setter.set(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("数据库更新失败: " + sql, e);
        }
    }

    protected static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    /** 将 SQLite 的 "yyyy-MM-dd HH:mm:ss" 文本解析为 {@link LocalDateTime}。 */
    protected static LocalDateTime parseDateTime(String value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value.replace(' ', 'T'));
    }
}
