package com.campus.recycle.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 闲置物品实体（不可变 ADT）。
 *
 * <p>标签列表在构造时做防御性拷贝，避免外部修改破坏内部状态。</p>
 */
public final class Item {

    private final Integer id;
    private final Integer sellerId;
    private final Integer categoryId;
    private final String name;
    private final String description;
    private final double price;
    private final ItemStatus status;
    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Item(Integer id, Integer sellerId, Integer categoryId, String name,
                String description, double price, ItemStatus status,
                List<String> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = Objects.requireNonNull(sellerId, "卖家ID不能为空");
        this.categoryId = Objects.requireNonNull(categoryId, "分类ID不能为空");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("物品名称不能为空");
        }
        if (Double.isNaN(price) || price < 0) {
            throw new IllegalArgumentException("价格不能为负");
        }
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status == null ? ItemStatus.AVAILABLE : status;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public ItemStatus getStatus() {
        return status;
    }

    /** 返回不可修改的标签列表。 */
    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name + "', price=" + price
                + ", status=" + status + ", sellerId=" + sellerId + '}';
    }
}
