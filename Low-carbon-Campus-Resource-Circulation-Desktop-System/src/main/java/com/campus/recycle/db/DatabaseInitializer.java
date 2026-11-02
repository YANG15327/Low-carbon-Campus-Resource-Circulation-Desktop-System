package com.campus.recycle.db;

import com.campus.recycle.dao.CategoryDao;
import com.campus.recycle.dao.ItemDao;
import com.campus.recycle.dao.UserDao;
import com.campus.recycle.exception.DataAccessException;
import com.campus.recycle.service.CategoryService;
import com.campus.recycle.service.UserService;
import com.campus.recycle.util.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化器：负责建表与初始种子数据（默认分类、默认管理员）。
 *
 * <p>与 {@code sql/schema.sql} 保持一致；脚本供人工初始化使用，本类供程序
 * 首次启动时自动初始化。</p>
 */
public final class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    /** 默认管理员账号。 */
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    private static final String[][] DEFAULT_CATEGORIES = {
            {"教材书籍", "教材、教辅、课外读物等"},
            {"数码配件", "耳机、充电器、U盘等数码周边"},
            {"文体用品", "体育器材、文具等"},
            {"生活物资", "生活日用品等"},
            {"其他", "其他闲置物品"}
    };

    private static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS user (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT    NOT NULL UNIQUE,
                password_hash TEXT    NOT NULL,
                salt          TEXT    NOT NULL,
                role          TEXT    NOT NULL DEFAULT 'STUDENT' CHECK (role IN ('STUDENT', 'ADMIN')),
                credit_score  INTEGER NOT NULL DEFAULT 100,
                nickname      TEXT,
                email         TEXT,
                created_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
            );
            CREATE TABLE IF NOT EXISTS category (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                name        TEXT NOT NULL UNIQUE,
                description TEXT
            );
            CREATE TABLE IF NOT EXISTS item (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                seller_id   INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                name        TEXT    NOT NULL,
                description TEXT,
                price       REAL    NOT NULL DEFAULT 0,
                status      TEXT    NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD', 'REMOVED')),
                tags        TEXT,
                created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                updated_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (seller_id)   REFERENCES user(id),
                FOREIGN KEY (category_id) REFERENCES category(id)
            );
            CREATE TABLE IF NOT EXISTS favorite (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id    INTEGER NOT NULL,
                item_id    INTEGER NOT NULL,
                created_at TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                UNIQUE (user_id, item_id),
                FOREIGN KEY (user_id) REFERENCES user(id),
                FOREIGN KEY (item_id) REFERENCES item(id)
            );
            CREATE TABLE IF NOT EXISTS flow_record (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id      INTEGER NOT NULL,
                buyer_id     INTEGER NOT NULL,
                seller_id    INTEGER NOT NULL,
                status       TEXT    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
                created_at   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                completed_at TEXT,
                FOREIGN KEY (item_id)  REFERENCES item(id),
                FOREIGN KEY (buyer_id) REFERENCES user(id),
                FOREIGN KEY (seller_id) REFERENCES user(id)
            );
            CREATE TABLE IF NOT EXISTS review (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                flow_record_id INTEGER NOT NULL,
                reviewer_id    INTEGER NOT NULL,
                reviewee_id    INTEGER NOT NULL,
                rating         INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                comment        TEXT,
                created_at     TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (flow_record_id) REFERENCES flow_record(id),
                FOREIGN KEY (reviewer_id)    REFERENCES user(id),
                FOREIGN KEY (reviewee_id)    REFERENCES user(id)
            );
            CREATE INDEX IF NOT EXISTS idx_item_category ON item(category_id);
            CREATE INDEX IF NOT EXISTS idx_item_seller   ON item(seller_id);
            CREATE INDEX IF NOT EXISTS idx_item_status   ON item(status);
            """;

    private DatabaseInitializer() {
        // 工具类，禁止实例化
    }

    /** 创建全部业务表结构（幂等）。 */
    public static void initSchema() {
        // 注意：连接由 DBManager 单例统一管理，不能放入 try-with-resources，
        // 否则会关闭共享连接（对内存数据库而言等同于销毁整个库）。
        try (Statement st = DBManager.getInstance().getConnection().createStatement()) {
            for (String sql : SCHEMA.split(";")) {
                String statement = sql.trim();
                if (!statement.isEmpty()) {
                    st.execute(statement);
                }
            }
            log.info("数据库表结构初始化完成");
        } catch (SQLException e) {
            throw new DataAccessException("初始化数据库结构失败", e);
        }
    }

    /** 写入初始种子数据（默认分类 + 默认管理员），幂等。 */
    public static void seed() {
        seedCategories();
        seedAdmin();
    }

    private static void seedCategories() {
        CategoryService service = new CategoryService(new CategoryDao(), new ItemDao());
        for (String[] category : DEFAULT_CATEGORIES) {
            if (service.findByName(category[0]).isEmpty()) {
                service.create(category[0], category[1]);
            }
        }
        log.info("默认分类已初始化");
    }

    private static void seedAdmin() {
        UserService service = new UserService(new UserDao());
        if (service.findByUsername(ADMIN_USERNAME).isEmpty()) {
            service.registerAdmin(ADMIN_USERNAME, ADMIN_PASSWORD);
            log.info("默认管理员账号已创建: {}", ADMIN_USERNAME);
        }
    }
}
