package com.campus.recycle.dao;

import com.campus.recycle.model.Category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 物品分类数据访问对象（DAO 模式）。
 */
public class CategoryDao extends DaoSupport {

    public Optional<Category> findById(int id) {
        return queryOptional(
                "SELECT * FROM category WHERE id = ?",
                ps -> ps.setInt(1, id),
                this::mapRow);
    }

    public Optional<Category> findByName(String name) {
        return queryOptional(
                "SELECT * FROM category WHERE name = ?",
                ps -> ps.setString(1, name),
                this::mapRow);
    }

    public List<Category> findAll() {
        return queryList("SELECT * FROM category ORDER BY id", ps -> {
        }, this::mapRow);
    }

    public int insert(Category category) {
        return insert(
                "INSERT INTO category (name, description) VALUES (?, ?)",
                ps -> {
                    ps.setString(1, category.getName());
                    setNullableString(ps, 2, category.getDescription());
                });
    }

    public boolean update(Category category) {
        return update(
                "UPDATE category SET name = ?, description = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, category.getName());
                    setNullableString(ps, 2, category.getDescription());
                    ps.setInt(3, category.getId());
                }) > 0;
    }

    public boolean delete(int id) {
        return update("DELETE FROM category WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"));
    }
}
