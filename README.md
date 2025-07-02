# obwiki 项目总览

## 项目简介
obwiki 是一个基于 Spring Boot + Vue3 的社区知识库系统，支持文章发布、评论、点赞、实时通知等功能，前后端分离，适合学习和二次开发。

---

## 技术栈
- **后端**：Java 17+、Spring Boot、MyBatis Plus、MySQL、WebSocket
- **前端**：Vue 3、TypeScript、Ant Design Vue、Axios、WebSocket

---

## 启动方式

### 1. 后端（obwiki-backend）
1. 配置数据库连接（`application.yml`）。
2. 执行 `obwiki.sql` 初始化数据库。
3. 使用 IDEA 或命令行运行 `ObwikiApplication.java`。

### 2. 前端（obwiki-frontend）
1. 进入 `obwiki-frontend` 目录，安装依赖：
   ```bash
   npm install
   # 或
   yarn install
   ```
2. 启动开发服务器：
   ```bash
   npm run serve
   # 或
   yarn serve
   ```
3. 默认后端接口地址为 `http://127.0.0.1:8880/obwiki`

---

## 常用命令

### 后端
```bash
# 启动后端
mvn spring-boot:run
# 打包
mvn clean package
```

### 前端
```bash
# 启动开发环境
npm run serve
# 打包
npm run build
```

---

## 目录结构

- `obwiki-backend/`      后端 Spring Boot 项目
  - `src/main/java/com/example/obwiki`  主要代码
  - `src/main/resources/mapper`         MyBatis 映射文件
  - `obwiki.sql`                        数据库脚本
- `obwiki-frontend/`     前端 Vue3 项目
  - `src/views`      主要页面
  - `src/api`        接口请求
  - `src/store`      状态管理

---

## 其他说明
- 前后端均支持热更新，适合本地开发和调试。
- 如需部署到服务器，请根据实际环境调整配置。

---
