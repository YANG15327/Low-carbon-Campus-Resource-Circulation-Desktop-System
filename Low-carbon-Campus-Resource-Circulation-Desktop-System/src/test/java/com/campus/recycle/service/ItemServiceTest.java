package com.campus.recycle.service;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.dao.UserDao;
import com.campus.recycle.exception.NotFoundException;
import com.campus.recycle.exception.PermissionDeniedException;
import com.campus.recycle.exception.ValidationException;
import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;
import com.campus.recycle.model.UserRole;
import com.campus.recycle.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemServiceTest extends TestDatabaseSupport {

    private final UserDao userDao = new UserDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ItemDao itemDao = new ItemDao();
    private final ItemService itemService = new ItemService(itemDao, categoryDao, userDao);

    private int sellerId;
    private int otherId;
    private int categoryId;

    @BeforeEach
    void setUpData() {
        sellerId = userDao.insert(new User(null, "seller", "h", "s", UserRole.STUDENT, 100, null, null, null));
        otherId = userDao.insert(new User(null, "other", "h", "s", UserRole.STUDENT, 100, null, null, null));
        categoryId = categoryDao.insert(new Category(null, "教材书籍", ""));
    }

    @Test
    void publishItem() {
        Item item = itemService.publish(sellerId, categoryId, "高等数学", "九成新", 20.0, List.of("教材"));
        assertEquals("高等数学", item.getName());
        assertEquals(ItemStatus.AVAILABLE, item.getStatus());
    }

    @Test
    void publishRejectsNegativePrice() {
        assertThrows(ValidationException.class,
                () -> itemService.publish(sellerId, categoryId, "物品", "", -1.0, List.of()));
    }

    @Test
    void publishRejectsMissingCategory() {
        assertThrows(NotFoundException.class,
                () -> itemService.publish(sellerId, 9999, "物品", "", 10.0, List.of()));
    }

    @Test
    void updateByOwnerSucceeds() {
        Item item = itemService.publish(sellerId, categoryId, "旧名称", "", 10.0, List.of());
        Item updated = itemService.update(item.getId(), sellerId, categoryId, "新名称", "新描述", 15.0, List.of("更新"));
        assertEquals("新名称", updated.getName());
        assertEquals(15.0, updated.getPrice());
    }

    @Test
    void updateByNonOwnerIsDenied() {
        Item item = itemService.publish(sellerId, categoryId, "物品", "", 10.0, List.of());
        assertThrows(PermissionDeniedException.class,
                () -> itemService.update(item.getId(), otherId, categoryId, "改", "", 1.0, List.of()));
    }

    @Test
    void removeSetsStatusRemoved() {
        Item item = itemService.publish(sellerId, categoryId, "待下架", "", 10.0, List.of());
        itemService.remove(item.getId(), sellerId);
        assertEquals(ItemStatus.REMOVED, itemService.findById(item.getId()).orElseThrow().getStatus());
    }

    @Test
    void searchReturnsFilteredResults() {
        itemService.publish(sellerId, categoryId, "高等数学", "", 20.0, List.of("教材"));
        itemService.publish(sellerId, categoryId, "大学英语", "", 15.0, List.of("教材"));
        List<Item> result = itemService.search(ItemSearchCriteria.builder().keyword("数学").build());
        assertEquals(1, result.size());
        assertEquals("高等数学", result.get(0).getName());
    }
}
