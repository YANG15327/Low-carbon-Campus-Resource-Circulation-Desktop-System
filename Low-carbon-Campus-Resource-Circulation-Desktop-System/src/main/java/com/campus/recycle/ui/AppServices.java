package com.campus.recycle.ui;

import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.dao.UserDao;
import com.campus.recycle.db.DatabaseInitializer;
import com.campus.recycle.service.CategoryService;
import com.campus.recycle.service.ItemService;
import com.campus.recycle.service.UserService;

/**
 * Application composition root: initializes persistence and assembles services.
 */
public final class AppServices {

    private final UserService userService;
    private final ItemService itemService;
    private final CategoryService categoryService;

    private AppServices() {
        UserDao userDao = new UserDao();
        CategoryDao categoryDao = new CategoryDao();
        ItemDao itemDao = new ItemDao();
        userService = new UserService(userDao);
        itemService = new ItemService(itemDao, categoryDao, userDao);
        categoryService = new CategoryService(categoryDao, itemDao);
    }

    public static AppServices initialize() {
        DatabaseInitializer.initSchema();
        DatabaseInitializer.seed();
        return new AppServices();
    }

    public UserService getUserService() {
        return userService;
    }

    public ItemService getItemService() {
        return itemService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }
}
