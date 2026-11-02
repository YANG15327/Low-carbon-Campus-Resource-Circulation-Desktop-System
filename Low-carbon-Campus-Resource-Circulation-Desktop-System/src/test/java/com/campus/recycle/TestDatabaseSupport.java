package com.campus.recycle;

import com.campus.recycle.db.DatabaseInitializer;
import com.campus.recycle.util.DBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * 测试基类：每个测试方法前重建内存数据库，保证用例相互隔离。
 */
public abstract class TestDatabaseSupport {

    @BeforeEach
    void setUpDatabase() {
        DBManager.getInstance().configure("jdbc:sqlite::memory:");
        DatabaseInitializer.initSchema();
    }

    @AfterEach
    void tearDownDatabase() {
        DBManager.getInstance().close();
    }
}
