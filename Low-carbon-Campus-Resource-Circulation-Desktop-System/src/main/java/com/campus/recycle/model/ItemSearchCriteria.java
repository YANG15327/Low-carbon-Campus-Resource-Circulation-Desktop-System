package com.campus.recycle.model;

/**
 * 物品检索条件（不可变 ADT）。
 *
 * <p>封装「分类 / 关键词 / 价格区间 / 状态」等可选筛选条件，使用建造者模式
 * 构建，便于扩展新的筛选维度而不改动检索接口。</p>
 */
public final class ItemSearchCriteria {

    private final Integer categoryId;
    private final String keyword;
    private final Double minPrice;
    private final Double maxPrice;
    private final ItemStatus status;
    private final Integer sellerId;

    private ItemSearchCriteria(Builder builder) {
        this.categoryId = builder.categoryId;
        this.keyword = builder.keyword;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.status = builder.status;
        this.sellerId = builder.sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getKeyword() {
        return keyword;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** {@link ItemSearchCriteria} 建造器。 */
    public static final class Builder {
        private Integer categoryId;
        private String keyword;
        private Double minPrice;
        private Double maxPrice;
        private ItemStatus status;
        private Integer sellerId;

        private Builder() {
        }

        public Builder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder priceBetween(Double minPrice, Double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder status(ItemStatus status) {
            this.status = status;
            return this;
        }

        public Builder sellerId(Integer sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public ItemSearchCriteria build() {
            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                throw new IllegalArgumentException("最低价不能高于最高价");
            }
            return new ItemSearchCriteria(this);
        }
    }
}
