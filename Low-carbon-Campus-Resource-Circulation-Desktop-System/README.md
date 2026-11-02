# 低碳校园资源循环流转桌面系统

高校校园闲置物品（教材、数码配件、文体用品、生活物资）二手流转的本地桌面管理系统。
Java 17 + Swing + SQLite（JDBC），Maven 构建，最终可打包为可独立运行的 Jar。

## 项目结构

```
├── pom.xml                        # Maven 工程配置
├── sql/schema.sql                 # SQLite 数据库初始化脚本（交付物）
├── docs/                          # 迭代 1 设计文档
│   ├── 01-需求规格说明书.md
│   ├── 02-系统设计说明书.md       # 含 UML 类图 / 时序图
│   ├── 03-数据库设计.md
│   └── 04-小组分工.md
└── src/
    ├── main/java/com/campus/recycle/
    │   ├── model/                 # 实体 ADT + 枚举 + 检索条件
    │   ├── dao/                   # JDBC 数据访问层（DAO 模式）
    │   ├── service/               # 业务逻辑层
    │   ├── util/                  # DBManager(单例)/PasswordUtil/Validator
    │   ├── exception/             # 统一业务异常体系
    │   └── db/                    # DatabaseInitializer（建表 + 种子数据）
    └── test/java/com/campus/recycle/   # JUnit 5 单元测试
```

## 分层架构

```
UI（迭代 3）→ Service（业务规则/权限/校验）→ DAO（JDBC）→ SQLite
```

已落地设计模式：**单例（DBManager）**、**DAO 模式**、**建造者（ItemSearchCriteria）**。

## 构建与测试

```bash
# 运行全部单元测试
mvn test

# 打包
mvn package
```

默认数据库文件：`campus_recycle.db`（首次启动由 `DatabaseInitializer` 自动建表，
并写入默认分类与默认管理员 `admin / admin123`）。

## 当前进度

- [x] 迭代 1：需求规格、系统设计（UML）、数据库设计、小组分工
- [x] 迭代 2：核心业务模块（用户/物品/分类）、JDBC 数据访问层、37 个 JUnit 用例
- [ ] 迭代 3：Swing GUI、流转/评价/统计模块、集成测试
- [ ] 迭代 4：重构优化、打包可执行 Jar、报告/PPT/视频
