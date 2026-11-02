package com.campus.recycle.dao;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.model.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryDaoTest extends TestDatabaseSupport {

    private final CategoryDao categoryDao = new CategoryDao();

    @Test
    void insertAndFindByName() {
        int id = categoryDao.insert(new Category(null, "教材", "各类教材"));
        assertTrue(id > 0);
        assertTrue(categoryDao.findByName("教材").isPresent());
    }

    @Test
    void findAllReturnsAllCategories() {
        categoryDao.insert(new Category(null, "A", ""));
        categoryDao.insert(new Category(null, "B", ""));
        List<Category> categories = categoryDao.findAll();
        assertEquals(2, categories.size());
    }

    @Test
    void updateChangesName() {
        int id = categoryDao.insert(new Category(null, "旧名", ""));
        categoryDao.update(new Category(id, "新名", "描述"));
        assertEquals("新名", categoryDao.findById(id).orElseThrow().getName());
    }

    @Test
    void deleteRemovesCategory() {
        int id = categoryDao.insert(new Category(null, "待删", ""));
        assertTrue(categoryDao.delete(id));
        assertTrue(categoryDao.findById(id).isEmpty());
    }
}
