package com.campus.recycle.service;

import com.campus.recycle.TestDatabaseSupport;
import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.exception.DuplicateResourceException;
import com.campus.recycle.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryServiceTest extends TestDatabaseSupport {

    private final CategoryService categoryService =
            new CategoryService(new CategoryDao(), new ItemDao());

    @Test
    void createCategory() {
        Category category = categoryService.create("教材书籍", "各类教材");
        assertEquals("教材书籍", category.getName());
    }

    @Test
    void createRejectsDuplicateName() {
        categoryService.create("教材书籍", "");
        assertThrows(DuplicateResourceException.class,
                () -> categoryService.create("教材书籍", "重复"));
    }

    @Test
    void updateCategory() {
        Category created = categoryService.create("旧分类", "");
        Category updated = categoryService.update(created.getId(), "新分类", "描述");
        assertEquals("新分类", updated.getName());
    }
}
