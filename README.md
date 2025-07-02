# obwiki-backend

## 项目简介
本项目为 obwiki 的后端服务，基于 Spring Boot 开发，提供社区、文章、评论等 API。

## 技术栈
- Java 17+
- Spring Boot
- MyBatis Plus
- MySQL
- WebSocket

## 启动方式
1. 配置数据库连接（application.yml）。
2. 执行 `obwiki.sql` 初始化数据库。
3. 使用 IDEA 或命令行运行 `ObwikiApplication.java`。

## 常用命令
```bash
# 启动后端
mvn spring-boot:run

# 打包
mvn clean package
```

## 目录结构
- `src/main/java/com/example/obwiki`  主要代码
- `src/main/resources/mapper`         MyBatis 映射文件
- `obwiki.sql`                        数据库脚本 