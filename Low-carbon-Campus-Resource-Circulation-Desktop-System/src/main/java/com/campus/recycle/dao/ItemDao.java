package com.campus.recycle.dao;

import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 闲置物品数据访问对象（DAO 模式），支持动态条件检索。
 */
public class ItemDao extends DaoSupport {

    public Optional<Item> findById(int id) {
        return queryOptional(
                "SELECT * FROM item WHERE id = ?",
                ps -> ps.setInt(1, id),
                this::mapRow);
    }

    public List<Item> findAll() {
        return queryList("SELECT * FROM item ORDER BY created_at DESC", ps -> {
        }, this::mapRow);
    }

    public List<Item> findBySeller(int sellerId) {
        return queryList(
                "SELECT * FROM item WHERE seller_id = ? ORDER BY created_at DESC",
                ps -> ps.setInt(1, sellerId),
                this::mapRow);
    }

    /**
     * 动态组合检索条件：分类 / 关键词（名称、描述、标签模糊匹配）/ 价格区间 / 状态 / 卖家。
     */
    public List<Item> search(ItemSearchCriteria c) {
        StringBuilder sql = new StringBuilder("SELECT * FROM item WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (c.getCategoryId() != null) {
            sql.append(" AND category_id = ?");
            params.add(c.getCategoryId());
        }
        if (c.getSellerId() != null) {
            sql.append(" AND seller_id = ?");
            params.add(c.getSellerId());
        }
        if (c.getKeyword() != null && !c.getKeyword().isBlank()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR tags LIKE ?)");
            String like = "%" + c.getKeyword().trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (c.getMinPrice() != null) {
            sql.append(" AND price >= ?");
            params.add(c.getMinPrice());
        }
        if (c.getMaxPrice() != null) {
            sql.append(" AND price <= ?");
            params.add(c.getMaxPrice());
        }
        if (c.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(c.getStatus().name());
        }
        sql.append(" ORDER BY created_at DESC");

        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, this::mapRow);
    }

    /** 插入物品，返回自增主键。 */
    public int insert(Item item) {
        return insert(
                "INSERT INTO item (seller_id, category_id, name, description, price, status, tags) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, item.getSellerId());
                    ps.setInt(2, item.getCategoryId());
                    ps.setString(3, item.getName());
                    setNullableString(ps, 4, item.getDescription());
                    ps.setDouble(5, item.getPrice());
                    ps.setString(6, item.getStatus().name());
                    setNullableString(ps, 7, joinTags(item.getTags()));
                });
    }

    /** 全量更新物品信息，并刷新更新时间。 */
    public boolean update(Item item) {
        return update(
                "UPDATE item SET category_id = ?, name = ?, description = ?, price = ?, status = ?, tags = ?, "
                        + "updated_at = datetime('now', 'localtime') WHERE id = ?",
                ps -> {
                    ps.setInt(1, item.getCategoryId());
                    ps.setString(2, item.getName());
                    setNullableString(ps, 3, item.getDescription());
                    ps.setDouble(4, item.getPrice());
                    ps.setString(5, item.getStatus().name());
                    setNullableString(ps, 6, joinTags(item.getTags()));
                    ps.setInt(7, item.getId());
                }) > 0;
    }

    /** 仅更新状态（用于下架、售出等状态流转）。 */
    public boolean updateStatus(int id, ItemStatus status) {
        return update(
                "UPDATE item SET status = ?, updated_at = datetime('now', 'localtime') WHERE id = ?",
                ps -> {
                    ps.setString(1, status.name());
                    ps.setInt(2, id);
                }) > 0;
    }

    public boolean delete(int id) {
        return update("DELETE FROM item WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }

    public boolean existsByCategory(int categoryId) {
        return queryOptional(
                "SELECT 1 FROM item WHERE category_id = ? LIMIT 1",
                ps -> ps.setInt(1, categoryId),
                rs -> 1).isPresent();
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        return new Item(
                rs.getInt("id"),
                rs.getInt("seller_id"),
                rs.getInt("category_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                ItemStatus.valueOf(rs.getString("status")),
                splitTags(rs.getString("tags")),
                parseDateTime(rs.getString("created_at")),
                parseDateTime(rs.getString("updated_at")));
    }

    private static String joinTags(List<String> tags) {
        return tags == null || tags.isEmpty() ? null : String.join(",", tags);
    }

    private static List<String> splitTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
