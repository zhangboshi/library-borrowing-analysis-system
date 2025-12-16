# library-borrowing-analysis-system

## 本地运行（占位说明）

1. 准备数据库  
   - 启动本地 MySQL 8，创建并授权账号（默认 `root/root`，可在 `server/src/main/resources/application.yml` 调整）。  
   - 执行初始化脚本：`mysql -u root -p < sql/init.sql`。
   - 如需 Redis：启动本地 Redis（默认 `localhost:6379`，可在 `application.yml` 调整）。
2. 启动后端  
   - 进入 `server` 目录：`cd server`  
   - 确保可以访问 Maven Central（若受限需配置镜像）。  
   - 运行：`mvn spring-boot:run`  
   - 健康检查：`http://localhost:8080/api/health`  
   - API 文档：`http://localhost:8080/swagger-ui`
3. 前端（后续补充）  
   - 待完成。

> 以上为占位说明，后续步骤与截图将在功能完善后更新。

### 账号与鉴权
- 初始化脚本会创建默认账号：`admin` / `admin123`（MD5 存储，仅用于示例，生产请改用强密码及加密方案）。
- 登录：`POST /api/auth/login`，携带 `Authorization: Bearer <token>` 访问 `GET /api/auth/me`、借阅记录写接口（新增/归还）等需要鉴权的接口。
- 列表查询接口（如读者、图书、借阅记录的 GET）默认不强制登录，方便演示。
