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

### 账号与鉴权（RBAC）
- 初始化账号：
  - 管理员：`admin` / `admin123`
  - 馆员：`librarian` / `lib123`
  - 访客：`viewer` / `viewer123`
- 所有业务接口（除 `/api/health`、`/api/auth/login`、`/api/auth/logout`）均需携带 `Authorization: Bearer <token>`。
- 角色能力：
  - ADMIN：读写所有资源、用户管理、统计访问、借阅写操作。
  - STAFF：借阅写操作、统计访问、资源读取。
  - VIEWER：只读访问（读者/图书/借阅列表、统计）。
- 退出：`POST /api/auth/logout`（会同时删除 Redis 中的 token）。

### 待完成功能
详见 `docs/remaining-work.md`，涵盖权限/审计收尾、借阅流程联调、导入导出、统计大屏、工程化与体验优化等里程碑。
