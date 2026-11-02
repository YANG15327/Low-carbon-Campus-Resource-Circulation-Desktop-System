package com.campus.recycle.service;

import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.exception.DuplicateResourceException;
import com.campus.recycle.exception.NotFoundException;
import com.campus.recycle.exception.ValidationException;
import com.campus.recycle.model.Category;
import com.campus.recycle.util.Validator;

import java.util.List;
import java.util.Optional;

/**
 * 物品分类业务逻辑：增删改查，维护分类名唯一性及删除约束。
 */
public class CategoryService {

    private final CategoryDao categoryDao;
    private final ItemDao itemDao;

    public CategoryService(CategoryDao categoryDao, ItemDao itemDao) {
        this.categoryDao = categoryDao;
        this.itemDao = itemDao;
    }

    public Category create(String name, String description) {
        Validator.requireText(name, "分类名称", 30);
        if (categoryDao.findByName(name).isPresent()) {
            throw new DuplicateResourceException("分类已存在: " + name);
        }
        Category category = new Category(null, name, description);
        int id = categoryDao.insert(category);
        return categoryDao.findById(id).orElseThrow(() -> new NotFoundException("分类创建失败"));
    }

    public Category update(int id, String name, String description) {
        Validator.requireText(name, "分类名称", 30);
        requireExist(id);
        if (categoryDao.findByName(name).filter(c -> c.getId() != id).isPresent()) {
            throw new DuplicateResourceException("分类名已存在: " + name);
        }
        Category category = new Category(id, name, description);
        categoryDao.update(category);
        return categoryDao.findById(id).orElseThrow();
    }

    /** 删除分类：若该分类下仍有物品则拒绝删除，避免悬挂引用。 */
    public void delete(int id) {
        requireExist(id);
        if (itemDao.existsByCategory(id)) {
            throw new ValidationException("该分类下仍有物品，无法删除");
        }
        categoryDao.delete(id);
    }

    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    public Optional<Category> findById(int id) {
        return categoryDao.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryDao.findByName(name);
    }

    private Category requireExist(int id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new NotFoundException("分类不存在: id=" + id));
    }
}
