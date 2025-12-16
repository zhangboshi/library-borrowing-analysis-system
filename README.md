# library-borrowing-analysis-system

## 本地运行（占位说明）

1. 准备数据库  
   - 启动本地 MySQL 8，创建并授权账号（默认 `root/root`，可在 `server/src/main/resources/application.yml` 调整）。  
   - 执行初始化脚本：`mysql -u root -p < sql/init.sql`。
2. 启动后端  
   - 进入 `server` 目录：`cd server`  
   - 确保可以访问 Maven Central（若受限需配置镜像）。  
   - 运行：`mvn spring-boot:run`  
   - 健康检查：`http://localhost:8080/api/health`  
   - API 文档：`http://localhost:8080/swagger-ui`
3. 前端（后续补充）  
   - 待完成。

> 以上为占位说明，后续步骤与截图将在功能完善后更新。
