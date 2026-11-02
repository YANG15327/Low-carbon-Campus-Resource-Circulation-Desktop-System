package com.campus.recycle.util;

import com.campus.recycle.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接管理器（单例模式）。
 *
 * <p>维护应用唯一的 SQLite 连接，统一管理连接生命周期，并在建立连接时
 * 开启外键约束（{@code PRAGMA foreign_keys = ON}），保证数据完整性。</p>
 */
public final class DBManager {

    private static final Logger log = LoggerFactory.getLogger(DBManager.class);
    private static final DBManager INSTANCE = new DBManager();

    private static final String DEFAULT_URL = "jdbc:sqlite:campus_recycle.db";
    private static final String DRIVER_CLASS = "org.sqlite.JDBC";

    private String url = DEFAULT_URL;
    private Connection connection;

    private DBManager() {
        // 私有构造，防止外部实例化
    }

    /** 获取全局唯一实例。 */
    public static DBManager getInstance() {
        return INSTANCE;
    }

    /**
     * 重新配置数据库地址（用于测试时切换到内存数据库）。
     * 关闭旧连接，使下次 {@link #getConnection()} 建立到新地址的连接。
     */
    public synchronized void configure(String url) {
        close();
        this.url = url;
    }

    /** 获取当前连接，若未建立或已关闭则新建。 */
    public synchronized Connection getConnection() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("未找到 SQLite JDBC 驱动", e);
        }
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
                log.info("数据库连接已建立: {}", url);
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException("数据库连接失败", e);
        }
    }

    /** 关闭当前连接。 */
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("关闭数据库连接失败", e);
            } finally {
                connection = null;
            }
        }
    }
}
