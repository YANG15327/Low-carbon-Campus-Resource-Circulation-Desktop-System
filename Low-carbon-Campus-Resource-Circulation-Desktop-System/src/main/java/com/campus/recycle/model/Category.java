package com.campus.recycle.model;

import java.util.Objects;

/**
 * 物品分类实体（不可变 ADT）。
 */
public final class Category {

    private final Integer id;
    private final String name;
    private final String description;

    public Category(Integer id, String name, String description) {
        this.id = id;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category other)) {
            return false;
        }
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
