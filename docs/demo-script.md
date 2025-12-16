# Demo 脚本

以下脚本可直接在本地终端运行（默认端口 8000）。如修改端口，请同步更新命令中的 `8000`。

## 1. 启动服务
```bash
python -m uvicorn app.main:app --reload --port 8000
```

## 2. cURL 调用示例
```bash
# 健康检查
curl -s http://localhost:8000/health | jq

# 借阅概况
curl -s http://localhost:8000/stats/overview | jq

# 仅查看活跃借阅
curl -s "http://localhost:8000/borrowings?active_only=true" | jq

# 查询特定用户的历史（示例 user_id=u1033）
curl -s http://localhost:8000/users/u1033/history | jq

# 查看热门图书 Top3
curl -s "http://localhost:8000/books/popular?limit=3" | jq

# 新增借阅记录（演示写入内存）
curl -s -X POST http://localhost:8000/borrowings \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "u2001",
    "book_id": "9780140449266",
    "title": "The Iliad",
    "genre": "Classics",
    "branch": "Central"
  }' | jq
```

## 3. 前端/可视化步骤
1. 浏览器打开 `http://localhost:8000/docs`，在 Swagger UI 中选择任意接口（如 `/stats/overview`）点击「Try it out」执行。
2. 切换到 `http://localhost:8000/redoc` 查看文档式 API，适合演示给业务方。
3. 将 `curl` 输出与工具如 `jq` 或 Postman、Hoppscotch 结合，绘制快速图表（如导入到 VS Code REST Client 或直接粘贴到 BI 工具的 REST 连接器）。

## 4. 数据替换
- 使用你自己的借阅导出文件替换 `data/sample_borrowings.json` 后重启服务。
- 如果数据量较大，建议先接入 PostgreSQL/ClickHouse，并调整 `/stats/overview` 聚合逻辑指向数据库视图。
