# Library Borrowing Analysis System

A lightweight analytics API and documentation set for exploring library borrowing behavior. The repository includes a FastAPI demo service backed by sample data plus architecture notes for expanding into a production-grade platform.

## 快速启动（完整步骤）
1. 安装 Python 3.10+ 并切换到项目根目录。
2. 创建虚拟环境并安装依赖：
   ```bash
   python -m venv .venv
   source .venv/bin/activate
   pip install -r requirements.txt
   ```
3. 运行示例服务（默认端口 8000）：
   ```bash
   python -m uvicorn app.main:app --reload --port 8000
   ```
4. 打开 Swagger 文档或直接调用接口：
   - 交互式文档：http://localhost:8000/docs
   - 健康检查：http://localhost:8000/health
   - 示例查询：`curl -s "http://localhost:8000/stats/overview" | jq`

## API 列表（示例服务）
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/health` | 服务健康检查与记录总数 |
| GET | `/stats/overview` | 借阅概况（总量、活跃借阅、均值、热门分类/标题、分馆负载） |
| GET | `/borrowings` | 查询借阅（按用户、分馆、分类、时间范围、是否活跃） |
| POST | `/borrowings` | 新增借阅记录（写入内存数据，用于演示） |
| GET | `/users/{user_id}/history` | 用户借阅历史与费用统计 |
| GET | `/books/popular` | 热门图书排行（可限制数量） |
| GET | `/branches/{branch}/activity` | 指定分馆的借阅分布与费用情况 |
| GET | `/demo/insights` | 预置洞察文案与示例 cURL 查询 |

## 截图
![Demo dashboard](docs/assets/dashboard-sample.svg)

## Demo 脚本
详见 [`docs/demo-script.md`](docs/demo-script.md)，包含 cURL 脚本与前端操作步骤，可直接复制运行。

## 文档导航
- 架构设计：[`docs/architecture.md`](docs/architecture.md)
- 用例与时序图：[`docs/use-cases.md`](docs/use-cases.md)
- 数据库字典：[`docs/database.md`](docs/database.md)
- 性能与安全设计：[`docs/non-functional.md`](docs/non-functional.md)
- 扩展点说明：[`docs/extension.md`](docs/extension.md)

## 常见问题（FAQ）
- **Swagger 打不开？** 确认服务已启动且浏览器地址为 `http://localhost:8000/docs`，或使用 `http://localhost:8000/redoc`。
- **cURL 访问被拒绝？** 检查端口是否被占用，或将命令中的端口改为运行时指定的端口（例如 `--port 9000`）。
- **示例数据在哪？** 默认加载 `data/sample_borrowings.json`，可替换为你自己的导出数据后重启服务。
- **需要持久化吗？** 演示服务只在内存中维护新增数据；生产环境应接入数据库或数据仓库，详见数据库字典与扩展点章节。
- **如何扩展指标？** 参考 `docs/extension.md` 的“自定义指标管线”，添加新指标后在 `/stats/overview` 暴露。
