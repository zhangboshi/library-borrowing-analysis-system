# 统计接口示例响应（示例数据基于 `sql/init.sql`）

> 以下示例仅供前端接入参考，字段形态与实际接口一致，数值会随时间窗口变化。

## 1. 概览 GET /api/stats/overview
```json
{
  "success": true,
  "data": {
    "totalBorrow": 80,
    "totalReaders": 10,
    "totalBooks": 20,
    "unreturned": 40,
    "recent7Days": 0
  },
  "message": "success"
}
```

## 2. 借阅趋势 GET /api/stats/borrow-trend?granularity=day&start=2024-01-01&end=2024-03-31
```json
{
  "success": true,
  "data": [
    {"date": "2024-01-05", "count": 1},
    {"date": "2024-01-09", "count": 1},
    {"date": "2024-01-13", "count": 1}
  ],
  "message": "success"
}
```

## 3. 分类占比 GET /api/stats/category-share?start=2024-01-01&end=2024-03-31
```json
{
  "success": true,
  "data": [
    {"category": "Technology", "count": 24},
    {"category": "Literature", "count": 16}
  ],
  "message": "success"
}
```

## 4. Top 图书 GET /api/stats/top-books?start=2024-01-01&end=2024-03-31&limit=5
```json
{
  "success": true,
  "data": [
    {"bookId": 8, "title": "1984", "count": 6},
    {"bookId": 10, "title": "The Great Gatsby", "count": 5}
  ],
  "message": "success"
}
```

## 5. 读者频次分布 GET /api/stats/reader-frequency?start=2024-01-01&end=2024-03-31
```json
{
  "success": true,
  "data": [
    {"bucket": "1次", "count": 0},
    {"bucket": "2-3次", "count": 0},
    {"bucket": "4-6次", "count": 10},
    {"bucket": "7次及以上", "count": 0}
  ],
  "message": "success"
}
```

## 6. 借阅状态分布 GET /api/stats/status-distribution?start=2024-01-01&end=2024-03-31
```json
{
  "success": true,
  "data": [
    {"status": "RETURNED", "count": 40},
    {"status": "UNRETURNED", "count": 40}
  ],
  "message": "success"
}
```
