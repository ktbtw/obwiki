# obwiki-frontend

## 项目简介
本项目为 obwiki 的前端部分，基于 Vue3 + TypeScript + Ant Design Vue 实现社区、文章、评论等功能。

## 技术栈
- Vue 3
- TypeScript
- Ant Design Vue
- Axios
- WebSocket

## 启动方式
1. 安装依赖
   ```bash
   npm install
   # 或
   yarn install
   ```
2. 启动开发服务器
   ```bash
   npm run serve
   # 或
   yarn serve
   ```
3. 默认后端接口地址为 `http://127.0.0.1:8880/obwiki`

## 常用命令
```bash
# 启动开发环境
npm run serve

# 打包
npm run build
```

## 目录结构
- `src/views`      主要页面
- `src/api`        接口请求
- `src/store`      状态管理
