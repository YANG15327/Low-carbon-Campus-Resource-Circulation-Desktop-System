package com.campus.recycle.dao;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;
import com.campus.recycle.model.User;
import com.campus.recycle.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDaoTest extends TestDatabaseSupport {

    private final UserDao userDao = new UserDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ItemDao itemDao = new ItemDao();
    private int sellerId;

    @BeforeEach
    void setUpData() {
        sellerId = userDao.insert(new User(null, "seller", "h", "s", UserRole.STUDENT, 100, null, null, null));
        categoryDao.insert(new Category(null, "教材书籍", ""));
        categoryDao.insert(new Category(null, "数码配件", ""));
        categoryDao.insert(new Category(null, "文体用品", ""));
    }

    private int insertItem(String name, double price, int categoryId, ItemStatus status, List<String> tags) {
        return itemDao.insert(new Item(null, sellerId, categoryId, name, "描述-" + name, price, status, tags, null, null));
    }

    @Test
    void insertAndFindById() {
        int id = insertItem("高等数学教材", 15.5, 1, ItemStatus.AVAILABLE, List.of("教材", "九成新"));
        Item found = itemDao.findById(id).orElseThrow();
        assertEquals("高等数学教材", found.getName());
        assertEquals(15.5, found.getPrice());
        assertEquals(2, found.getTags().size());
    }

    @Test
    void searchByKeywordMatchesNameAndTags() {
        insertItem("线性代数", 10, 1, ItemStatus.AVAILABLE, List.of("数学"));
        insertItem("篮球", 30, 3, ItemStatus.AVAILABLE, List.of("体育"));

        List<Item> byName = itemDao.search(ItemSearchCriteria.builder().keyword("代数").build());
        assertEquals(1, byName.size());
        assertEquals("线性代数", byName.get(0).getName());

        List<Item> byTag = itemDao.search(ItemSearchCriteria.builder().keyword("体育").build());
        assertEquals(1, byTag.size());
        assertEquals("篮球", byTag.get(0).getName());
    }

    @Test
    void searchByCategoryAndPriceRange() {
        insertItem("教材A", 10, 1, ItemStatus.AVAILABLE, List.of());
        insertItem("教材B", 50, 1, ItemStatus.AVAILABLE, List.of());
        insertItem("耳机", 80, 2, ItemStatus.AVAILABLE, List.of());

        List<Item> result = itemDao.search(ItemSearchCriteria.builder()
                .categoryId(1)
                .priceBetween(20.0, 100.0)
                .build());
        assertEquals(1, result.size());
        assertEquals("教材B", result.get(0).getName());
    }

    @Test
    void searchByStatus() {
        insertItem("在售物品", 10, 1, ItemStatus.AVAILABLE, List.of());
        insertItem("已售物品", 20, 1, ItemStatus.SOLD, List.of());

        List<Item> available = itemDao.search(ItemSearchCriteria.builder().status(ItemStatus.AVAILABLE).build());
        assertEquals(1, available.size());
    }

    @Test
    void findBySeller() {
        insertItem("物品1", 10, 1, ItemStatus.AVAILABLE, List.of());
        insertItem("物品2", 20, 1, ItemStatus.AVAILABLE, List.of());
        assertEquals(2, itemDao.findBySeller(sellerId).size());
    }

    @Test
    void updateStatusChangesItemState() {
        int id = insertItem("待下架", 10, 1, ItemStatus.AVAILABLE, List.of());
        assertTrue(itemDao.updateStatus(id, ItemStatus.REMOVED));
        assertEquals(ItemStatus.REMOVED, itemDao.findById(id).orElseThrow().getStatus());
    }
}
