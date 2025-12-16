# 数据库字典（建议）

演示服务使用 JSON/内存数据。以下为生产化推荐的表结构，可映射到 PostgreSQL/ClickHouse（分析）与 PostgreSQL/MySQL（事务）。

## 表：users
| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| id | uuid | 用户 ID | PK |
| card_no | varchar(32) | 借阅卡号 | 唯一索引 |
| name | text | 姓名 |  |
| segment | varchar(32) | 用户分群（成人/青少年/儿童） | 索引 |
| created_at | timestamptz | 创建时间 | 默认 now |

## 表：books
| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| id | varchar(32) | 图书 ID/ISBN | PK |
| title | text | 书名 | 全文索引（可选） |
| author | text | 作者 |  |
| genre | varchar(64) | 分类 | 索引 |
| branch_id | varchar(32) | 所属分馆 | FK -> branches.id |
| language | varchar(16) | 语言 |  |

## 表：branches
| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| id | varchar(32) | 分馆 ID | PK |
| name | text | 分馆名称 | 唯一 |
| city | varchar(64) | 城市 |  |
| timezone | varchar(32) | 时区 |  |

## 表：borrowings（核心宽表）
| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| id | uuid | 借阅记录 | PK |
| user_id | uuid | 用户 | FK -> users.id, 索引 |
| book_id | varchar(32) | 图书 | FK -> books.id, 索引 |
| branch_id | varchar(32) | 分馆 | FK -> branches.id, 复合索引(user_id, branch_id) |
| borrowed_at | timestamptz | 借出时间 | 索引（时间） |
| due_at | timestamptz | 应还时间 |  |
| returned_at | timestamptz | 归还时间 | 允许 NULL |
| duration_days | numeric(6,2) | 借阅时长（天） | 计算列/ETL |
| late_fee | numeric(8,2) | 逾期费用 | 默认 0 |
| rating | smallint | 评分 | 允许 NULL |
| source | varchar(32) | 数据来源（柜台/自助/线上） |  |

## 表：events（可选，流式摄取）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| event_id | uuid | 事件 ID |
| event_time | timestamptz | 发生时间 |
| event_type | varchar(32) | borrow/return/fee_paid |
| payload | jsonb | 事件内容 |

## 指标与物化视图
- `mv_daily_branch_load`: 按分馆/日期聚合借出、归还、逾期次数，支持快速趋势图。
- `mv_top_titles`: 热门图书排行物化视图，按 genre/branch 维度预聚合。
- `mv_user_stats`: 用户维度宽表，包含当前活跃借阅、累计逾期、评分均值。

## 索引建议
- B-tree：`borrowings(user_id)`, `borrowings(branch_id, borrowed_at desc)`, `books(genre)`。
- 时间分区：按月/季度分区 `borrowings`，加速时间范围查询。
- 物化视图刷新：每日全量 + 按小时增量；结合 `REFRESH MATERIALIZED VIEW CONCURRENTLY` 降低锁等待。
