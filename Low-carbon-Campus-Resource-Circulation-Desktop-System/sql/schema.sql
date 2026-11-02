-- =====================================================================
-- 低碳校园资源循环流转桌面系统 —— SQLite 数据库初始化脚本
-- 说明：本脚本创建全部业务表结构，可直接在 SQLite 中执行。
--       初始分类数据与默认管理员账号由程序首次运行时自动生成
--       （见 com.campus.recycle.db.DatabaseInitializer）。
-- =====================================================================

PRAGMA foreign_keys = ON;

-- 用户表（普通学生 / 管理员）
CREATE TABLE IF NOT EXISTS user (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    salt          TEXT    NOT NULL,
    role          TEXT    NOT NULL DEFAULT 'STUDENT'
                          CHECK (role IN ('STUDENT', 'ADMIN')),
    credit_score  INTEGER NOT NULL DEFAULT 100,
    nickname      TEXT,
    email         TEXT,
    created_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 物品分类表
CREATE TABLE IF NOT EXISTS category (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    description TEXT
);

-- 闲置物品表
CREATE TABLE IF NOT EXISTS item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    seller_id   INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    description TEXT,
    price       REAL    NOT NULL DEFAULT 0,
    status      TEXT    NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD', 'REMOVED')),
    tags        TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (seller_id)   REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorite (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL,
    item_id    INTEGER NOT NULL,
    created_at TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (item_id) REFERENCES item(id)
);

-- 流转记录表（供需沟通 + 成交 / 交换状态）
CREATE TABLE IF NOT EXISTS flow_record (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id      INTEGER NOT NULL,
    buyer_id     INTEGER NOT NULL,
    seller_id    INTEGER NOT NULL,
    status       TEXT    NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    created_at   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    completed_at TEXT,
    FOREIGN KEY (item_id)  REFERENCES item(id),
    FOREIGN KEY (buyer_id) REFERENCES user(id),
    FOREIGN KEY (seller_id) REFERENCES user(id)
);

-- 评价表（信用评价）
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

-- 常用查询索引
CREATE INDEX IF NOT EXISTS idx_item_category ON item(category_id);
CREATE INDEX IF NOT EXISTS idx_item_seller   ON item(seller_id);
CREATE INDEX IF NOT EXISTS idx_item_status   ON item(status);
