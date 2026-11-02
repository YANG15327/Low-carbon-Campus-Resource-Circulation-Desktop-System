package com.campus.recycle.service;

import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.dao.UserDao;
import com.campus.recycle.exception.NotFoundException;
import com.campus.recycle.exception.PermissionDeniedException;
import com.campus.recycle.exception.ValidationException;
import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;
import com.campus.recycle.util.Validator;

import java.util.List;
import java.util.Optional;

/**
 * 闲置物品业务逻辑：发布、修改、下架、检索。
 *
 * <p>负责物品字段校验、卖家 / 分类存在性校验，以及操作权限（仅发布者本人）
 * 校验。</p>
 */
public class ItemService {

    private final ItemDao itemDao;
    private final CategoryDao categoryDao;
    private final UserDao userDao;

    public ItemService(ItemDao itemDao, CategoryDao categoryDao, UserDao userDao) {
        this.itemDao = itemDao;
        this.categoryDao = categoryDao;
        this.userDao = userDao;
    }

    /** 发布闲置物品。 */
    public Item publish(int sellerId, int categoryId, String name, String description,
                        double price, List<String> tags) {
        validateItemFields(name, description, price);
        requireUser(sellerId);
        requireCategory(categoryId);

        Item item = new Item(null, sellerId, categoryId, name, description, price,
                ItemStatus.AVAILABLE, tags, null, null);
        int id = itemDao.insert(item);
        return itemDao.findById(id).orElseThrow(() -> new NotFoundException("物品创建失败"));
    }

    /** 修改物品（仅发布者本人，全量更新）。 */
    public Item update(int itemId, int operatorId, int categoryId, String name,
                       String description, double price, List<String> tags) {
        Item existing = requireItem(itemId);
        requireOwner(existing, operatorId);
        validateItemFields(name, description, price);
        requireCategory(categoryId);

        Item updated = new Item(existing.getId(), existing.getSellerId(), categoryId,
                name, description, price, existing.getStatus(), tags,
                existing.getCreatedAt(), null);
        itemDao.update(updated);
        return itemDao.findById(itemId).orElseThrow();
    }

    /** 下架物品（软删除：状态置为 REMOVED，仅发布者本人）。 */
    public void remove(int itemId, int operatorId) {
        Item existing = requireItem(itemId);
        requireOwner(existing, operatorId);
        itemDao.updateStatus(itemId, ItemStatus.REMOVED);
    }

    /** 按条件检索物品。 */
    public List<Item> search(ItemSearchCriteria criteria) {
        if (criteria == null) {
            return itemDao.findAll();
        }
        return itemDao.search(criteria);
    }

    public Optional<Item> findById(int itemId) {
        return itemDao.findById(itemId);
    }

    private void validateItemFields(String name, String description, double price) {
        Validator.requireText(name, "物品名称", 50);
        if (description != null && description.length() > 500) {
            throw new ValidationException("物品描述长度不能超过 500 个字符");
        }
        Validator.requirePrice(price);
    }

    private Item requireItem(int itemId) {
        return itemDao.findById(itemId)
                .orElseThrow(() -> new NotFoundException("物品不存在: id=" + itemId));
    }

    private void requireUser(int userId) {
        if (userDao.findById(userId).isEmpty()) {
            throw new NotFoundException("用户不存在: id=" + userId);
        }
    }

    private void requireCategory(int categoryId) {
        if (categoryDao.findById(categoryId).isEmpty()) {
            throw new NotFoundException("分类不存在: id=" + categoryId);
        }
    }

    private void requireOwner(Item item, int operatorId) {
        if (item.getSellerId() != operatorId) {
            throw new PermissionDeniedException("无权操作他人发布的物品");
        }
    }
}
