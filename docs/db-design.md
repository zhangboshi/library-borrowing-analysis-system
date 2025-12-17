# 数据库设计说明

本系统的数据库基于 MySQL 8 设计，包含三个核心业务实体：读者（reader）、图书（book）、借阅记录（borrow_record）。

## 表结构概览

| 表名 | 用途 |
| --- | --- |
| reader | 存储读者（学生、教师、馆员等）基本信息 |
| book | 存储图书及分类、作者、ISBN 等信息，支持分类统计与 Top 图书分析 |
| borrow_record | 存储借阅行为，关联读者与图书，用于趋势、分类占比、Top 图书、读者频次、状态分布等统计 |

## 字段与约束

### reader
- `id` BIGINT PK 自增
- `name` VARCHAR(100) 非空：读者姓名
- `type` ENUM('STUDENT','TEACHER','STAFF') 非空，默认 STUDENT：读者类型
- `department` VARCHAR(100) 可空：院系或部门
- `email` VARCHAR(150) 可空：邮箱
- `phone` VARCHAR(30) 可空：电话
- `created_at` TIMESTAMP：创建时间，默认当前时间

### book
- `id` BIGINT PK 自增
- `title` VARCHAR(200) 非空：书名
- `author` VARCHAR(100) 非空：作者
- `category` VARCHAR(100) 非空：分类（用于分类占比统计）
- `isbn` VARCHAR(30) 非空唯一：ISBN 编号
- `publish_year` INT 可空：出版年份
- `total_copies` INT 非空，默认 10：馆藏册数
- `created_at` TIMESTAMP：创建时间，默认当前时间
- 索引：`idx_category` 覆盖 category，便于按分类检索与统计

### borrow_record
- `id` BIGINT PK 自增
- `reader_id` BIGINT FK -> reader.id：借阅人
- `book_id` BIGINT FK -> book.id：借阅书
- `borrow_time` DATETIME 非空：借出时间（建索引支持趋势统计）
- `due_time` DATETIME 非空：应还时间
- `return_time` DATETIME 可空：归还时间（未还则为空）
- `status` ENUM('BORROWED','RETURNED','OVERDUE','LOST') 非空，默认 BORROWED：状态分布统计字段
- `created_at` TIMESTAMP：创建时间，默认当前时间
- 外键：`fk_borrow_reader`、`fk_borrow_book`
- 索引：`idx_borrow_time`（借阅时间趋势）、`idx_reader_id`（读者频次统计）、`idx_book_id`（Top 图书统计）、`idx_status`（状态分布）

## 演示数据设计说明
- 读者 10 条，覆盖学生、教师、馆员等类型。
- 图书 20 条，包含 Technology、History、Literature、Science、Art、Business、Design 等分类，支持分类占比和 Top 图书分析。
- 借阅记录 80 条，跨度 2024 年 1 月至 4 月，涵盖多分类、多读者、多书籍：
  - 状态含 RETURNED、BORROWED、OVERDUE，便于状态分布统计。
  - 借阅时间、应还时间和部分归还时间均已填充，未归还记录的 return_time 为 NULL。
  - 索引覆盖 borrow_time、reader_id、book_id、status，以支撑按日/月趋势、读者频次、Top 图书、状态分布等统计。

## 初始化脚本
完整的建表及数据脚本见 `sql/init.sql`，可直接在 MySQL 8 环境执行：
```sql
SOURCE sql/init.sql;
```
